package org.ftp.command.impl;

import static org.ftp.util.FileSystemUtils.getPermissionString;

import java.util.List;
import org.ftp.UserSession;
import org.ftp.UserStatus;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.Directory;
import org.ftp.domain.File;
import org.ftp.domain.Permission;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.DirectoryRepository;
import org.ftp.repository.impl.FileRepository;
import org.ftp.repository.impl.UserRepository;
import org.ftp.util.FileSystemUtils;
import org.ftp.visitor.PermissionCheckVisitor;

public class ListCommandProcessor extends CommandProcessor {

  private final PermissionCheckVisitor permissionCheckVisitor;
  private final DirectoryRepository directoryRepository;
  private final UserRepository userRepository;
  private final FileRepository fileRepository;

  public ListCommandProcessor() {
    this.permissionCheckVisitor = new PermissionCheckVisitor(Permission.Access.READ);
    this.directoryRepository = RepositoryFactory.getDirectoryRepository();
    this.userRepository = RepositoryFactory.getUserRepository();
    this.fileRepository = RepositoryFactory.getFileRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 1;
  }

  @Override
  protected boolean allowAccess(UserSession session, String[] arguments) {
    return session.getCredentials().getStatus().equals(UserStatus.LOGGEDIN);
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {

    Directory currentDirectory = directoryRepository.readById(session.getCurrDirectoryId());
    User user = userRepository.getUserWithGroupsByUsername(session.getCredentials().getUsername())
        .get();
    if (!permissionCheckVisitor.visitDirectory(currentDirectory, user)) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Permission denied.")
          .build();
    }

    List<File> files = fileRepository.findAllByDirectoryId(currentDirectory.getId());
    List<Directory> subdirectories = directoryRepository.findAllSubdirectoriesByParentId(
        currentDirectory.getId());
    StringBuilder responseMessage = new StringBuilder("Contents of ").append(
        currentDirectory.getPath()).append(":\n");
    for (Directory subdirectory : subdirectories) {
      responseMessage.append("[DIR] ")
          .append(subdirectory.getPath().lastIndexOf("/") == -1 ? subdirectory.getPath()
              : subdirectory.getPath().substring(subdirectory.getPath().lastIndexOf("/") + 1))
          .append(" ")
          .append(getPermissionString(subdirectory.getPermission()))
          .append("\n");
    }
    for (File file : files) {
      responseMessage.append("[FILE] ")
          .append(file.getName())
          .append(" ")
          .append(getPermissionString(file.getPermission()))
          .append("\n");
    }
    responseMessage.append("End of directory listing.");
    return new CommandResponse.Builder()
        .withCode(220)
        .withMessage(responseMessage.toString())
        .build();
  }


}
