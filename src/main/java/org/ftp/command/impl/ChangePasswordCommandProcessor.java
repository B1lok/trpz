package org.ftp.command.impl;

import static org.ftp.util.PasswordUtils.hashPassword;
import static org.ftp.util.PasswordUtils.verifyPassword;

import org.ftp.UserSession;
import org.ftp.UserStatus;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.UserRepository;

public class ChangePasswordCommandProcessor extends CommandProcessor {

  private final UserRepository userRepository;

  public ChangePasswordCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
  }

  @Override
  protected boolean allowAccess(UserSession session, String[] arguments) {
    return session.getCredentials().getStatus().equals(UserStatus.LOGGEDIN);
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 3;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String username = session.getCredentials().getUsername();
    String oldPassword = arguments[1];
    String newPassword = arguments[2];
    var user = userRepository.findByUsername(username).get();
    if (!verifyPassword(oldPassword, user.getPassword())) {
      return new CommandResponse.Builder()
          .withCode(530)
          .withMessage("Old password is incorrect.")
          .build();
    }
    String hashedNewPassword = hashPassword(newPassword);
    user.setPassword(hashedNewPassword);
    userRepository.update(user);
    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage("Password changed successfully.")
        .build();
  }
}
