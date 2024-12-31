package org.ftp.command.impl;

import static org.ftp.util.FileSystemUtils.getDefaultFilePermission;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Optional;
import org.ftp.TransferType;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.ConnectionLimits;
import org.ftp.domain.Directory;
import org.ftp.domain.Permission;
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

public class StorCommandProcessor extends CommandProcessor {

  private final DirectoryRepository directoryRepository;
  private final FileRepository fileRepository;
  private final UserRepository userRepository;
  private final PermissionCheckVisitor permissionCheckVisitor;
  private final GlobalLimitsRepository globalLimitsRepository;
  private final ConnectionLimitsRepository connectionLimitsRepository;

  public StorCommandProcessor() {
    this.directoryRepository = RepositoryFactory.getDirectoryRepository();
    this.fileRepository = RepositoryFactory.getFileRepository();
    this.userRepository = RepositoryFactory.getUserRepository();
    this.permissionCheckVisitor = new PermissionCheckVisitor(Access.WRITE);
    this.globalLimitsRepository = RepositoryFactory.getGlobalLimitsRepository();
    this.connectionLimitsRepository = RepositoryFactory.getConnectionLimitsRepository();
  }

  @Override
  protected boolean allowAccess(UserSession session, String[] arguments) {
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
    Optional<ConnectionLimits> connectionLimits = connectionLimitsRepository
        .findByUsername(user.getUsername());
    int bandwidthLimitMB = connectionLimits.map(ConnectionLimits::getMaxSpeed)
        .orElseGet(() -> globalLimitsRepository.getLastMaxSpeed().orElse(1));
    if (!permissionCheckVisitor.visitDirectory(currentWorkingDirectory, user)) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Permission denied.")
          .build();
    }
    if (fileRepository.findByNameAndDirectory(fileName, currentWorkingDirectory).isPresent()) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("File already exists.")
          .build();
    }
    if (!session.isDataConnectionOpen()) {
      return new CommandResponse.Builder()
          .withCode(425)
          .withMessage("Data connection not open.")
          .build();
    }

    String path = FileSystemUtils.getServerBasePath() + currentWorkingDirectory.getPath();
    if (session.getTransferType() == TransferType.BINARY) {
      storeBinaryFile(session, path, fileName, bandwidthLimitMB);
    } else {
      storeAsciiFile(session, path, fileName, bandwidthLimitMB);
    }
    org.ftp.domain.File file = org.ftp.domain.File.builder()
        .directory(currentWorkingDirectory)
        .groupId(null)
        .name(fileName)
        .ownerId(user.getId())
        .permission(new Permission(getDefaultFilePermission()))
        .build();
    fileRepository.create(file);
    return new CommandResponse.Builder()
        .withCode(226)
        .withMessage("File stored successfully.")
        .build();
  }

  private void storeBinaryFile(UserSession session, String path, String fileName, int maxSpeed) {
    File file = new File(path, fileName);

    try (BufferedInputStream inputStream = new BufferedInputStream(
        session.getDataConnection().getInputStream());
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {

      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
        throttleTransfer(bytesRead, maxSpeed);
      }

    } catch (IOException e) {
      throw new FileTransferException("Error during file transfer: " + e.getMessage());
    } finally {
      session.closeDataConnection();
    }
  }

  private void storeAsciiFile(UserSession session, String path, String fileName, int maxSpeed) {
    File file = new File(path, fileName);

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(session.getDataConnection().getInputStream()));
        PrintWriter writer = new PrintWriter(new FileOutputStream(file), true)) {

      String line;
      while ((line = reader.readLine()) != null) {
        writer.println(line);
        throttleTransfer(line.getBytes().length, maxSpeed);
      }

    } catch (IOException e) {
      throw new FileTransferException("Error during file transfer: " + e.getMessage());
    } finally {
      session.closeDataConnection();
    }
  }
  private void throttleTransfer(int bytesTransferred, double maxTransferSpeedMBps) {
    double maxSpeedBps = maxTransferSpeedMBps * 1024 * 1024;
    double sleepTimeMs = (bytesTransferred / maxSpeedBps) * 1000;

    try {
      if (sleepTimeMs > 0) {
        Thread.sleep((long) sleepTimeMs);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}

