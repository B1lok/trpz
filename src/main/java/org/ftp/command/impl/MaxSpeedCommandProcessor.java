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

public class MaxSpeedCommandProcessor extends CommandProcessor {

  private final UserRepository userRepository;
  private final GlobalLimitsRepository globalLimitsRepository;
  private final ConnectionLimitsRepository connectionLimitsRepository;

  public MaxSpeedCommandProcessor() {
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
      return handleMaxSpeedGlobally(session, arguments);
    } else {
      return handleMaxSpeedPerUser(session, arguments);
    }
  }
  private CommandResponse handleMaxSpeedGlobally(UserSession session, String[] arguments) {
    Integer maxSpeed = getValidMaxConn(arguments[1]);
    if (maxSpeed == -1) {
      return new CommandResponse.Builder()
          .withCode(501)
          .withMessage("Invalid speed transfer value.")
          .build();
    }
    GlobalLimits updatedGlobalLimits = GlobalLimits.builder()
        .maxConnections(globalLimitsRepository.getLastMaxConnections().get())
        .maxSpeed(maxSpeed)
        .updatedById(
            userRepository.findByUsername(session.getCredentials().getUsername()).get().getId())
        .updatedAt(LocalDateTime.now())
        .build();
    globalLimitsRepository.create(updatedGlobalLimits);
    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage("Max speed transfer updated successfully.")
        .build();
  }

  private CommandResponse handleMaxSpeedPerUser(UserSession session, String[] arguments) {
    Integer maxSpeed = getValidMaxConn(arguments[1]);
    if (maxSpeed == -1) {
      return new CommandResponse.Builder()
          .withCode(501)
          .withMessage("Invalid speed transfer value.")
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
          .maxConnections(globalLimitsRepository.getLastMaxConnections().get())
          .maxSpeed(maxSpeed)
          .build();
      connectionLimitsRepository.create(connectionLimits);
    } else {
      ConnectionLimits connectionLimits = connectionLimitsOptional.get();
      connectionLimits.setMaxSpeed(maxSpeed);
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
