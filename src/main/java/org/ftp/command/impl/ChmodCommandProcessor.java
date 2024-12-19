package org.ftp.command.impl;

import java.util.Optional;
import org.ftp.UserSession;
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

public class ChmodCommandProcessor extends CommandProcessor {

  private final FileRepository fileRepository;
  private final DirectoryRepository directoryRepository;
  private final UserRepository userRepository;

  public ChmodCommandProcessor() {
    this.fileRepository = RepositoryFactory.getFileRepository();
    this.directoryRepository = RepositoryFactory.getDirectoryRepository();
    this.userRepository = RepositoryFactory.getUserRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 3 && arguments[1].matches("[0-7]{3}");
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String path = arguments[2];
    Directory currentWorkingDirectory = directoryRepository.readById(session.getCurrDirectoryId());
    User user = userRepository.getUserWithGroupsByUsername(session.getCredentials().getUsername())
        .get();
    Optional<File> optionalFile = fileRepository.findByNameAndDirectory(path, currentWorkingDirectory);
    Optional<Directory> optionalDirectory = directoryRepository.findByPath(
        currentWorkingDirectory.getPath() + "/" + path);
    if (optionalFile.isPresent()) {
      File file = optionalFile.get();
      if (!file.getOwnerId().equals(user.getId())) {
        return new CommandResponse.Builder()
            .withCode(550)
            .withMessage("Permission denied. You are not the owner of the file")
            .build();
      }
      file.setPermission(new Permission(Integer.parseInt(arguments[1])));
      fileRepository.update(file);
    } else if (optionalDirectory.isPresent()) {
      Directory directory = optionalDirectory.get();
      if (!directory.getOwnerId().equals(user.getId())) {
        return new CommandResponse.Builder()
            .withCode(550)
            .withMessage("Permission denied. You are not the owner of the directory")
            .build();
      }
      directory.setPermission(new Permission(Integer.parseInt(arguments[1])));
      directoryRepository.update(directory);
    } else {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("Target not found: " + path)
          .build();
    }
    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage("Permission changed successfully")
        .build();
  }
}
