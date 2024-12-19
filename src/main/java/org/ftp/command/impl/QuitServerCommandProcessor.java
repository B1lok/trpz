package org.ftp.command.impl;

import java.time.LocalDateTime;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.ConnectionStatistics;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.ConnectionStatisticsRepository;

public class QuitServerCommandProcessor extends CommandProcessor {

  private final ConnectionStatisticsRepository connectionStatisticsRepository;

  public QuitServerCommandProcessor() {
    this.connectionStatisticsRepository = RepositoryFactory.getConnectionStatisticsRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 1;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    ConnectionStatistics connectionStatistics = getConnectionStatistic(
        session.getCredentials().getConnectionId());
    connectionStatistics.setDisconnectionTime(LocalDateTime.now());
    connectionStatisticsRepository.update(connectionStatistics);
    session.setRunning(false);
    return new CommandResponse.Builder()
        .withCode(221)
        .withMessage("Goodbye")
        .build();
  }

  private ConnectionStatistics getConnectionStatistic(int connectionId) {
    return connectionStatisticsRepository.readById(connectionId);
  }
}
