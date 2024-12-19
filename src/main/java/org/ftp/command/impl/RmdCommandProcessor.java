package org.ftp.command.impl;

import static org.ftp.util.FileSystemUtils.isAbsolutePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.Directory;
import org.ftp.domain.Permission.Access;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.DirectoryRepository;
import org.ftp.repository.impl.UserRepository;
import org.ftp.util.FileSystemUtils;
import org.ftp.visitor.PermissionCheckVisitor;

public class RmdCommandProcessor extends CommandProcessor {

  private final DirectoryRepository directoryRepository;
  private final PermissionCheckVisitor permissionCheckVisitor;
  private final UserRepository userRepository;

  public RmdCommandProcessor() {
    this.directoryRepository = RepositoryFactory.getDirectoryRepository();
    permissionCheckVisitor = new PermissionCheckVisitor(Access.WRITE);
    this.userRepository = RepositoryFactory.getUserRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 2;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String directoryName = arguments[1];
    Directory currentWorkingDirectory = directoryRepository.readById(session.getCurrDirectoryId());
    if (!isAbsolutePath(directoryName)) {
      directoryName = currentWorkingDirectory.getPath() + "/" + directoryName;
    }
    Optional<Directory> directoryToRemove = directoryRepository.findByPath(directoryName);
    User user = userRepository.getUserWithGroupsByUsername(session.getCredentials().getUsername())
        .get();
    if (directoryToRemove.isEmpty()) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Directory does not exist.")
          .build();
    }
    if (!directoryRepository.isEmpty(directoryToRemove.get().getId())) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Directory is not empty.")
          .build();
    }
    if (directoryToRemove.get().getId() == session.getCredentials().getUserDirectoryId()) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Cannot remove home directory.")
          .build();
    }
    if (!permissionCheckVisitor.visitDirectory(directoryToRemove.get(), user)) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Permission denied.")
          .build();
    }
    boolean isRemovingCurrentDirectory = directoryToRemove.get().getId()
        .equals(currentWorkingDirectory.getId());
    try {
      Files.delete(Path.of(FileSystemUtils.getServerBasePath(), directoryToRemove.get().getPath()));
    } catch (IOException e) {
      return new CommandResponse.Builder()
          .withCode(450)
          .withMessage("Failed to remove directory from the file system.")
          .build();
    }
    directoryRepository.delete(directoryToRemove.get().getId());
    if (isRemovingCurrentDirectory) {
      session.setCurrDirectoryId(session.getCredentials().getUserDirectoryId());
    }
    return new CommandResponse.Builder()
        .withCode(250)
        .withMessage("Directory '" + directoryName + "' removed successfully.")
        .build();
  }
}
