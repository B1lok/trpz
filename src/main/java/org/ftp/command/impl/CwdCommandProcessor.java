package org.ftp.command.impl;

import static org.ftp.util.FileSystemUtils.isAbsolutePath;

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

public class CwdCommandProcessor extends CommandProcessor {

  private final DirectoryRepository directoryRepository;
  private final UserRepository userRepository;
  private final PermissionCheckVisitor permissionCheckVisitor;

  public CwdCommandProcessor() {
    this.directoryRepository = RepositoryFactory.getDirectoryRepository();
    this.userRepository = RepositoryFactory.getUserRepository();
    this.permissionCheckVisitor = new PermissionCheckVisitor(Access.EXECUTE);
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 1 || arguments.length == 2;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    if (arguments.length == 1 || (arguments.length == 2 && arguments[1].equals(
        FileSystemUtils.ROOT_DIRECTORY))) {
      if (session.getCurrDirectoryId() == session.getCredentials().getUserDirectoryId()) {
        return getAlreadyInHomeDirectoryResponse();
      }
      session.setCurrDirectoryId(session.getCredentials().getUserDirectoryId());
      return getRootCwdResponse();
    }
    String changeDirectory = arguments[1];
    Directory currentWorkingDirectory = directoryRepository.readById(session.getCurrDirectoryId());
    User user = userRepository.getUserWithGroupsByUsername(session.getCredentials().getUsername())
        .get();
    if (FileSystemUtils.DIRECTORY_NAVIGATION_SYMBOLS.contains(changeDirectory)) {
      if (currentWorkingDirectory.getParentId() == null) {
        return new CommandResponse.Builder()
            .withCode(550)
            .withMessage("Cannot change to parent directory. Already in root directory.")
            .build();
      }
      Directory parentDirectory = directoryRepository.readById(
          currentWorkingDirectory.getParentId());
      if (!permissionCheckVisitor.visitDirectory(parentDirectory, user)) {
        return new CommandResponse.Builder()
            .withCode(550)
            .withMessage("Permission denied.")
            .build();
      }
      session.setCurrDirectoryId(parentDirectory.getId());
      return new CommandResponse.Builder()
          .withCode(250)
          .withMessage("The current directory has been changed to the parent directory: "
              + parentDirectory.getPath())
          .build();
    }
    String newDirectoryPath = isAbsolutePath(changeDirectory) ? changeDirectory
        : currentWorkingDirectory.getPath() + "/" + changeDirectory;
    Optional<Directory> newDirectory = directoryRepository.findByPath(newDirectoryPath);
    if (newDirectory.isEmpty()) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Directory does not exist.")
          .build();
    }
    if (!permissionCheckVisitor.visitDirectory(newDirectory.get(), user)) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Permission denied.")
          .build();
    }
    session.setCurrDirectoryId(newDirectory.get().getId());
    return new CommandResponse.Builder()
        .withCode(250)
        .withMessage("The current directory has been changed to: " + newDirectory.get().getPath())
        .build();
  }

  private CommandResponse getRootCwdResponse() {
    return new CommandResponse.Builder()
        .withCode(250)
        .withMessage("The current directory has been changed to the home directory.")
        .build();
  }

  private CommandResponse getAlreadyInHomeDirectoryResponse() {
    return new CommandResponse.Builder()
        .withCode(250)
        .withMessage("Already in home directory.")
        .build();
  }

}
