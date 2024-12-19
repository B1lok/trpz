package org.ftp.command.impl;

import org.ftp.UserSession;
import org.ftp.UserStatus;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.UserRepository;

public class UserCommandProcessor extends CommandProcessor {
  private final UserRepository userRepository;

  public UserCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
  }
  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 2;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String username = arguments[1];
    if (!userRepository.existsByUsername(username)) {
      return new CommandResponse.Builder()
          .withCode(530)
          .withMessage("Invalid username. This User does not exist.")
          .build();
    }
    session.getCredentials().setUsername(username);
    session.getCredentials().setStatus(UserStatus.ENTEREDUSERNAME);
    return new CommandResponse.Builder()
        .withCode(331)
        .withMessage("User name okay, need password.")
        .build();
  }
}
