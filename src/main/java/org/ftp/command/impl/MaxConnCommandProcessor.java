package org.ftp.command.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.ConnectionLimits;
import org.ftp.domain.GlobalLimits;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.ConnectionLimitsRepository;
import org.ftp.repository.impl.GlobalLimitsRepository;
import org.ftp.repository.impl.UserRepository;

public class MaxConnCommandProcessor extends CommandProcessor {

  private final UserRepository userRepository;
  private final GlobalLimitsRepository globalLimitsRepository;
  private final ConnectionLimitsRepository connectionLimitsRepository;

  public MaxConnCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
    this.globalLimitsRepository = RepositoryFactory.getGlobalLimitsRepository();
    this.connectionLimitsRepository = RepositoryFactory.getConnectionLimitsRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 2 || arguments.length == 3;
  }

  @Override
  protected boolean allowAccess(UserSession session, String[] arguments) {
    return userRepository.isAdmin(session.getCredentials().getUsername());
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    if (arguments.length == 2) {
      return handleMaxConnGlobally(session, arguments);
    } else {
      return handleMaxConnPerUser(session, arguments);
    }
  }

  private CommandResponse handleMaxConnGlobally(UserSession session, String[] arguments) {
    Integer maxConn = getValidMaxConn(arguments[1]);
    if (maxConn == -1) {
      return new CommandResponse.Builder()
          .withCode(501)
          .withMessage("Invalid number of connections.")
          .build();
    }
    GlobalLimits updatedGlobalLimits = GlobalLimits.builder()
        .maxConnections(maxConn)
        .maxSpeed(globalLimitsRepository.getLastMaxSpeed().get())
        .updatedById(
            userRepository.findByUsername(session.getCredentials().getUsername()).get().getId())
        .updatedAt(LocalDateTime.now())
        .build();
    globalLimitsRepository.create(updatedGlobalLimits);
    session.updateServerConnections(maxConn);
    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage("Max connections updated successfully.")
        .build();
  }

  private CommandResponse handleMaxConnPerUser(UserSession session, String[] arguments) {
    int maxConn = getValidMaxConn(arguments[1]);
    if (maxConn == -1) {
      return new CommandResponse.Builder()
          .withCode(501)
          .withMessage("Invalid number of connections.")
          .build();
    }
    String username = arguments[2];
    Optional<User> userOptional = userRepository.findByUsername(username);
    if (userOptional.isEmpty()) {
      return new CommandResponse.Builder()
          .withCode(501)
          .withMessage("User does not exist.")
          .build();
    }
    Optional<ConnectionLimits> connectionLimitsOptional = connectionLimitsRepository
        .findByUsername(username);
    if (connectionLimitsOptional.isEmpty()) {
      ConnectionLimits connectionLimits = ConnectionLimits.builder()
          .userId(userOptional.get().getId())
          .maxConnections(maxConn)
          .maxSpeed(globalLimitsRepository.getLastMaxSpeed().get())
          .build();
      connectionLimitsRepository.create(connectionLimits);
    } else {
      ConnectionLimits connectionLimits = connectionLimitsOptional.get();
      connectionLimits.setMaxConnections(maxConn);
      connectionLimitsRepository.update(connectionLimits);
    }
    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage("Max connections updated successfully.")
        .build();
  }

  private Integer getValidMaxConn(String maxConn) {
    try {
      int maxConnInt = Integer.parseInt(maxConn);
      if (maxConnInt < 0) {
        return -1;
      }
      return maxConnInt;
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
