package org.ftp.command.impl;

import java.util.List;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.ConnectionStatistics;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.ConnectionStatisticsRepository;
import org.ftp.repository.impl.UserRepository;

public class MyStatCommandProcessor extends CommandProcessor {

  private final UserRepository userRepository;
  private final ConnectionStatisticsRepository connectionStatisticsRepository;

  public MyStatCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
    this.connectionStatisticsRepository = RepositoryFactory.getConnectionStatisticsRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 1;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    User me = userRepository.findByUsername(session.getCredentials().getUsername()).get();
    List<ConnectionStatistics> connectionStatistics = connectionStatisticsRepository.findLastConnectionsByUserId(
        me.getId());
    StringBuilder responseMessage = new StringBuilder("My last connections ").append(
        me.getUsername()).append(":\n");
    responseMessage.append(String.format("%-30s %-30s\n", "Connection Time", "Disconnection Time"));
    responseMessage.append("------------------------------------------------------------\n");

    for (ConnectionStatistics stats : connectionStatistics) {
      String connectionTime =
          stats.getConnectionTime() != null ? stats.getConnectionTime().toString() : "N/A";
      String disconnectionTime =
          stats.getDisconnectionTime() != null ? stats.getDisconnectionTime().toString() : "N/A";
      responseMessage.append(String.format("%-30s %-30s\n",
          connectionTime, disconnectionTime));
    }

    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage(responseMessage.toString())
        .build();

  }
}
