package org.ftp.command.impl;

import java.util.List;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.Directory;
import org.ftp.domain.File;
import org.ftp.domain.Permission;
import org.ftp.visitor.PermissionCheckVisitor;
import org.ftp.visitor.Visitor;

public class ListCommandProcessor extends CommandProcessor {

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 0;
  }

  @Override
  protected boolean allowAccess(UserSession session, String[] arguments) {
    return super.allowAccess(session, arguments);
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {


    Directory directory = getDirectoryByPath(session.getCurrDirectory());
    if (directory == null) {
      return new CommandResponse.Builder()
          .withCode(404)
          .withMessage("Directory not found.")
          .build();
    }
    Visitor permissionVisitor = new PermissionCheckVisitor(Permission.Access.READ);
    if (!directory.accept(permissionVisitor, null)) {
      return new CommandResponse.Builder()
          .withCode(403)
          .withMessage("Permission denied.")
          .build();
    }
    List<File> files = getFilesInDirectory(directory);
    List<Directory> subdirectories = getSubdirectoriesInDirectory(directory);
    StringBuilder responseMessage = new StringBuilder("Contents of ").append(directory.getPath()).append(":\n");
    for (Directory subdirectory : subdirectories) {
      responseMessage.append("[DIR] ").append(subdirectory.getPath()).append("\n");
    }
    for (File file : files) {
      responseMessage.append("[FILE] ").append(file.getName()).append("\n");
    }

    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage(responseMessage.toString())
        .build();
  }

  private Directory getDirectoryByPath(String path) {
    // Implement logic to fetch the directory based on the given path and user session
    // Return null if the directory is not found
    return null;
  }

  private List<File> getFilesInDirectory(Directory directory) {
    // Implement logic to fetch files in the directory
    return List.of(); // Example: Return an empty list for now
  }
  private List<Directory> getSubdirectoriesInDirectory(Directory directory) {
    // Implement logic to fetch subdirectories in the directory
    return List.of(); // Example: Return an empty list for now
  }

}
