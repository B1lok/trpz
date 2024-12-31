package org.ftp.repository;

import javax.sql.DataSource;
import org.ftp.config.DatabaseConfig;
import org.ftp.repository.impl.ConnectionLimitsRepository;
import org.ftp.repository.impl.ConnectionStatisticsRepository;
import org.ftp.repository.impl.DirectoryRepository;
import org.ftp.repository.impl.FileRepository;
import org.ftp.repository.impl.GlobalLimitsRepository;
import org.ftp.repository.impl.GroupRepository;
import org.ftp.repository.impl.UserRepository;

public class RepositoryFactory {

  private static final DataSource dataSource = DatabaseConfig.getDataSource();

  private static UserRepository userRepository;
  private static GlobalLimitsRepository globalLimitsRepository;
  private static ConnectionLimitsRepository connectionLimitsRepository;
  private static ConnectionStatisticsRepository connectionStatisticsRepository;
  private static GroupRepository groupRepository;
  private static FileRepository fileRepository;
  private static DirectoryRepository directoryRepository;

  public static synchronized UserRepository getUserRepository() {
    if (userRepository == null) {
      userRepository = UserRepository.getInstance(dataSource);
    }
    return userRepository;
  }

  public static synchronized GlobalLimitsRepository getGlobalLimitsRepository() {
    if (globalLimitsRepository == null) {
      globalLimitsRepository = GlobalLimitsRepository.getInstance(dataSource);
    }
    return globalLimitsRepository;
  }

  public static synchronized ConnectionLimitsRepository getConnectionLimitsRepository() {
    if (connectionLimitsRepository == null) {
      connectionLimitsRepository = ConnectionLimitsRepository.getInstance(dataSource);
    }
    return connectionLimitsRepository;
  }

  public static synchronized ConnectionStatisticsRepository getConnectionStatisticsRepository() {
    if (connectionStatisticsRepository == null) {
      connectionStatisticsRepository = ConnectionStatisticsRepository.getInstance(dataSource);
    }
    return connectionStatisticsRepository;
  }

  public static synchronized GroupRepository getGroupRepository() {
    if (groupRepository == null) {
      groupRepository = GroupRepository.getInstance(dataSource);
    }
    return groupRepository;
  }

  public static synchronized DirectoryRepository getDirectoryRepository() {
    if (directoryRepository == null) {
      directoryRepository = DirectoryRepository.getInstance(dataSource);
    }
    return directoryRepository;
  }

  public static synchronized FileRepository getFileRepository() {
    if (fileRepository == null) {
      fileRepository = FileRepository.getInstance(dataSource);
    }
    return fileRepository;
  }
}
