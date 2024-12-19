package org.ftp.command.impl;

import static org.ftp.util.FileSystemUtils.HOME_DIRECTORY_PATH;

import java.time.LocalDateTime;
import java.util.Optional;
import org.ftp.LoggedState;
import org.ftp.UserSession;
import org.ftp.UserStatus;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.ConnectionStatistics;
import org.ftp.domain.Directory;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.ConnectionStatisticsRepository;
import org.ftp.repository.impl.DirectoryRepository;
import org.ftp.repository.impl.UserRepository;
import org.ftp.util.PasswordUtils;

public class PassCommandProcessor extends CommandProcessor {

  private final UserRepository userRepository;
  private final DirectoryRepository directoryRepository;
  private final ConnectionStatisticsRepository connectionStatisticsRepository;

  public PassCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
    this.connectionStatisticsRepository = RepositoryFactory.getConnectionStatisticsRepository();
    this.directoryRepository = RepositoryFactory.getDirectoryRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 2;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    if (session.getCredentials().getStatus() != UserStatus.ENTEREDUSERNAME) {
      return new CommandResponse.Builder()
          .withCode(503)
          .withMessage("Please provide a valid username first.")
          .build();
    }
    String enteredPassword = arguments[1];
    String username = session.getCredentials().getUsername();
    User user = userRepository.findByUsername(username)
        .orElse(null);

    if (user == null || !PasswordUtils.verifyPassword(enteredPassword, user.getPassword())) {
      return new CommandResponse.Builder()
          .withCode(530)
          .withMessage("Invalid username or password.")
          .build();
    }
    if (!session.getConnectionManager().canConnect(username)) {
      return new CommandResponse.Builder()
          .withCode(421)
          .withMessage("Maximum number of connections reached for this user.")
          .build();
    }
    Optional<Directory> homeDirectory = directoryRepository.findByPath(
        HOME_DIRECTORY_PATH + "/" + username);
    if (homeDirectory.isEmpty()) {
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage("User home directory not found.")
          .build();
    }
    int homeDirectoryId = homeDirectory.get().getId();
    session.getConnectionManager().addConnection(username);
    session.getCredentials().setStatus(UserStatus.LOGGEDIN);
    ConnectionStatistics connectionStatistics = connectionStatisticsRepository.create(
        getNewConnectionStatistics(user.getId()));
    session.getCredentials().setConnectionId(connectionStatistics.getId());
    session.getCredentials().setUserDirectoryId(homeDirectoryId);
    session.setCurrDirectoryId(homeDirectoryId);
    session.setState(new LoggedState());
    return new CommandResponse.Builder()
        .withCode(230)
        .withMessage("User logged in successfully.")
        .build();
  }

  private ConnectionStatistics getNewConnectionStatistics(int userId) {
    return ConnectionStatistics.builder()
        .userId(userId)
        .connectionTime(LocalDateTime.now())
        .build();
  }
}
