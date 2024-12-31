package org.ftp.command.impl;

import java.util.List;
import java.util.Optional;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.ConnectionStatistics;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.ConnectionStatisticsRepository;
import org.ftp.repository.impl.UserRepository;

public class StatCommandProcessor extends CommandProcessor {

  private final UserRepository userRepository;
  private final ConnectionStatisticsRepository connectionStatisticsRepository;

  public StatCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
    this.connectionStatisticsRepository = RepositoryFactory.getConnectionStatisticsRepository();
  }

  @Override
  protected boolean allowAccess(UserSession session, String[] arguments) {
    return userRepository.isAdmin(session.getCredentials().getUsername());
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 2;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String username = arguments[1];
    Optional<User> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      return new CommandResponse.Builder()
          .withCode(501)
          .withMessage("User not found.")
          .build();
    }
    List<ConnectionStatistics> connectionStatistics = connectionStatisticsRepository.findLastConnectionsByUserId(
        user.get().getId());
    if (connectionStatistics.isEmpty()) {
      return new CommandResponse.Builder()
          .withCode(200)
          .withMessage("No connection statistics found for user " + username + ".")
          .build();
    }
    StringBuilder responseMessage = new StringBuilder("Last connections for user ").append(username)
        .append(":\n");
    responseMessage.append(String.format("%-30s %-30s\n", "Connection Time", "Disconnection Time"));
    responseMessage.append("------------------------------------------------------------\n");

    for (ConnectionStatistics stats : connectionStatistics) {
      String connectionTime =
          stats.getConnectionTime() != null ? stats.getConnectionTime().toString() : "N/A";
      String disconnectionTime =
          stats.getDisconnectionTime() != null ? stats.getDisconnectionTime().toString() : "N/A";
      responseMessage.append(String.format("%-20s %-20s\n",
          connectionTime, disconnectionTime));
    }

    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage(responseMessage.toString())
        .build();
  }
}
