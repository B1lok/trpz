package org.ftp.command.impl;

import static org.ftp.util.FileSystemUtils.getServerBasePath;
import static org.ftp.util.PasswordUtils.hashPassword;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.Directory;
import org.ftp.domain.Permission;
import org.ftp.domain.Role;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.DirectoryRepository;
import org.ftp.repository.impl.UserRepository;
import org.ftp.util.FileSystemUtils;

public class CreateUserCommandProcessor extends CommandProcessor {

  private final UserRepository userRepository;
  private final DirectoryRepository directoryRepository;

  public CreateUserCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
    this.directoryRepository = RepositoryFactory.getDirectoryRepository();
  }

  @Override
  protected boolean allowAccess(UserSession session, String[] arguments) {
    return userRepository.isAdmin(session.getCredentials().getUsername());
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 4;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String username = arguments[1];
    String password = arguments[2];
    String role = arguments[3].toUpperCase();
    if (!isValidRole(role)) {
      return new CommandResponse.Builder()
          .withCode(501)
          .withMessage("Invalid role. Allowed roles: ADMIN, USER")
          .build();
    }
    if (userRepository.existsByUsername(username)) {
      return new CommandResponse.Builder()
          .withCode(409)
          .withMessage("User with username '" + username + "' already exists.")
          .build();
    }
    User newUser = User.builder()
        .username(username)
        .password(hashPassword(password))
        .role(Role.valueOf(role))
        .build();
    userRepository.create(newUser);
    Directory homeDirectory = directoryRepository.findByPath(FileSystemUtils.HOME_DIRECTORY_PATH)
        .get();
    Directory newUserDirectory = Directory.builder()
        .ownerId(newUser.getId())
        .permission(new Permission(711))
        .path(homeDirectory.getPath()+ "/" + username)
        .groupId(null)
        .parentId(homeDirectory.getId())
        .build();
    try {
      Files.createDirectories(Path.of(getServerBasePath(), newUserDirectory.getPath()));
    } catch (IOException e) {
      return new CommandResponse.Builder()
          .withCode(500)
          .withMessage("Failed to create directory for user '" + username + "'.")
          .build();
    }
    directoryRepository.create(newUserDirectory);
    return new CommandResponse.Builder()
        .withCode(201)
        .withMessage("User '" + username + "' created successfully.")
        .build();
  }

  private boolean isValidRole(String role) {
    try {
      Role.valueOf(role);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
