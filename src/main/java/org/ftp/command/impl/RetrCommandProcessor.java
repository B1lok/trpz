package org.ftp.command.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Optional;
import org.ftp.TransferType;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.ConnectionLimits;
import org.ftp.domain.Directory;
import org.ftp.domain.Permission.Access;
import org.ftp.domain.User;
import org.ftp.exception.FileTransferException;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.ConnectionLimitsRepository;
import org.ftp.repository.impl.DirectoryRepository;
import org.ftp.repository.impl.FileRepository;
import org.ftp.repository.impl.GlobalLimitsRepository;
import org.ftp.repository.impl.UserRepository;
import org.ftp.util.FileSystemUtils;
import org.ftp.visitor.PermissionCheckVisitor;

public class RetrCommandProcessor extends CommandProcessor {

  private final DirectoryRepository directoryRepository;
  private final FileRepository fileRepository;
  private final UserRepository userRepository;
  private final PermissionCheckVisitor permissionCheckVisitor;
  private final GlobalLimitsRepository globalLimitsRepository;
  private final ConnectionLimitsRepository connectionLimitsRepository;

  public RetrCommandProcessor() {
    this.directoryRepository = RepositoryFactory.getDirectoryRepository();
    this.fileRepository = RepositoryFactory.getFileRepository();
    this.userRepository = RepositoryFactory.getUserRepository();
    this.permissionCheckVisitor = new PermissionCheckVisitor(Access.READ);
    this.globalLimitsRepository = RepositoryFactory.getGlobalLimitsRepository();
    this.connectionLimitsRepository = RepositoryFactory.getConnectionLimitsRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    if (arguments.length != 2) {
      return false;
    }

    String fileName = arguments[1];

    if (fileName.contains("/") || fileName.contains("\\")) {
      return false;
    }

    String fileNamePattern = "^[a-zA-Z0-9._-]+\\.[a-zA-Z0-9]{1,5}$";
    return fileName.matches(fileNamePattern);
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String fileName = arguments[1];
    Directory currentWorkingDirectory = directoryRepository.readById(session.getCurrDirectoryId());
    User user = userRepository.getUserWithGroupsByUsername(session.getCredentials().getUsername())
        .get();
    Optional<org.ftp.domain.File> file = fileRepository.findByNameAndDirectory(fileName,
        currentWorkingDirectory);
    Optional<ConnectionLimits> connectionLimits = connectionLimitsRepository
        .findByUsername(user.getUsername());
    int bandwidthLimitMB = connectionLimits.map(ConnectionLimits::getMaxSpeed)
        .orElseGet(() -> globalLimitsRepository.getLastMaxSpeed().orElse(1));
    if (file.isEmpty()) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("File not found.")
          .build();
    }
    if (!permissionCheckVisitor.visitFile(file.get(), user)) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Permission denied.")
          .build();
    }
    if (!session.isDataConnectionOpen()) {
      return new CommandResponse.Builder()
          .withCode(425)
          .withMessage("Data connection not open.")
          .build();
    }
    String path =
        FileSystemUtils.getServerBasePath() + currentWorkingDirectory.getPath() + "/" + fileName;
    File fileToRetrieve = new File(path);
    if (session.getTransferType().equals(TransferType.ASCII)) {
      retrieveAsciiFile(session, fileToRetrieve, bandwidthLimitMB);
    } else {
      retrieveBinaryFile(session, fileToRetrieve, bandwidthLimitMB);
    }
    return new CommandResponse.Builder()
        .withCode(226)
        .withMessage("File transfer successful.")
        .build();
  }

  private void retrieveBinaryFile(UserSession session, File file, int maxSpeed) {
    try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        BufferedOutputStream outputStream = new BufferedOutputStream(
            session.getDataConnection().getOutputStream())) {

      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }

      outputStream.flush();

    } catch (IOException e) {
      throw new FileTransferException("Error during file transfer: " + e.getMessage());
    } finally {
      session.closeDataConnection();
    }
  }

  private void retrieveAsciiFile(UserSession session, File file, int maxSpeed) {
    try (BufferedReader reader = new BufferedReader(new FileReader(file));
        PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(session.getDataConnection().getOutputStream()), true)) {

      String line;
      while ((line = reader.readLine()) != null) {
        writer.println(line);
      }

    } catch (IOException e) {
      throw new FileTransferException("Error during file transfer: " + e.getMessage());
    } finally {
      session.closeDataConnection();
    }
  }
}
