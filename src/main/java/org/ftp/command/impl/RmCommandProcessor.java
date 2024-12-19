package org.ftp.command.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.Directory;
import org.ftp.domain.File;
import org.ftp.domain.Permission.Access;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.DirectoryRepository;
import org.ftp.repository.impl.FileRepository;
import org.ftp.repository.impl.UserRepository;
import org.ftp.util.FileSystemUtils;
import org.ftp.visitor.PermissionCheckVisitor;

public class RmCommandProcessor extends CommandProcessor {

  private final DirectoryRepository directoryRepository;
  private final PermissionCheckVisitor permissionCheckVisitorWrite;
  private final PermissionCheckVisitor permissionCheckVisitorExecute;
  private final UserRepository userRepository;
  private final FileRepository fileRepository;

  public RmCommandProcessor() {
    this.directoryRepository = RepositoryFactory.getDirectoryRepository();
    this.permissionCheckVisitorWrite = new PermissionCheckVisitor(Access.WRITE);
    this.permissionCheckVisitorExecute = new PermissionCheckVisitor(Access.EXECUTE);
    this.userRepository = RepositoryFactory.getUserRepository();
    this.fileRepository = RepositoryFactory.getFileRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 2;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String fileName = arguments[1];
    Directory currentWorkingDirectory = directoryRepository.readById(session.getCurrDirectoryId());
    Optional<File> file = fileRepository.findByNameAndDirectory(fileName, currentWorkingDirectory);
    if (file.isEmpty()) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("File does not exist.")
          .build();
    }
    User user = userRepository.getUserWithGroupsByUsername(session.getCredentials().getUsername())
        .get();
    if (!permissionCheckVisitorWrite.visitDirectory(currentWorkingDirectory, user)
        || !permissionCheckVisitorExecute.visitDirectory(currentWorkingDirectory, user)) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Permission denied.")
          .build();
    }

    try {
      Files.delete(Path.of(FileSystemUtils.getServerBasePath(),
          currentWorkingDirectory.getPath() + "/" + file.get().getName()));
    } catch (IOException e) {
      return new CommandResponse.Builder()
          .withCode(450)
          .withMessage("Failed to remove directory from the file system.")
          .build();
    }
    fileRepository.delete(file.get().getId());
    return new CommandResponse.Builder()
        .withCode(250)
        .withMessage("File removed.")
        .build();
  }
}
