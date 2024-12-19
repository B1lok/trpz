package org.ftp.command.impl;

import static org.ftp.util.FileSystemUtils.getDefaultDirectoryPermission;
import static org.ftp.util.FileSystemUtils.getServerBasePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.Directory;
import org.ftp.domain.Permission;
import org.ftp.domain.Permission.Access;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.DirectoryRepository;
import org.ftp.repository.impl.UserRepository;
import org.ftp.util.FileSystemUtils;
import org.ftp.visitor.PermissionCheckVisitor;

public class MkdCommandProcessor extends CommandProcessor {

  private final DirectoryRepository directoryRepository;
  private final PermissionCheckVisitor permissionCheckVisitor;
  private final UserRepository userRepository;

  public MkdCommandProcessor() {
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
    String newDirectoryName = arguments[1];
    if (!isValidDirectoryName(newDirectoryName)) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Invalid directory name.")
          .build();
    }
    Directory currentDirectory = directoryRepository.readById(session.getCurrDirectoryId());
    User user = userRepository.getUserWithGroupsByUsername(session.getCredentials().getUsername())
        .get();
    if (!permissionCheckVisitor.visitDirectory(currentDirectory, user)) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Permission denied.")
          .build();
    }
    String newName = currentDirectory.getPath() + "/" + newDirectoryName;
    if (directoryRepository.existByPath(newName)){
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Directory already exists.")
          .build();
    }
    Directory newDirectory = Directory.builder()
        .path(newName)
        .ownerId(user.getId())
        .groupId(null)
        .permission(new Permission(getDefaultDirectoryPermission()))
        .parentId(currentDirectory.getId())
        .build();
    try {
      Files.createDirectories(Path.of(getServerBasePath(), newDirectory.getPath()));
    } catch (IOException e) {
      return new CommandResponse.Builder()
          .withCode(500)
          .withMessage("Failed to create directory")
          .build();
    }
    directoryRepository.create(newDirectory);
    return new CommandResponse.Builder()
        .withCode(257)
        .withMessage("Directory created.")
        .build();
  }

  private boolean isValidDirectoryName(String directoryName) {
    return directoryName.matches("[a-zA-Z0-9-]+");
  }
}
