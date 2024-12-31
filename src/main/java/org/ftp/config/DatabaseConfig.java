package org.ftp.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DatabaseConfig {
  private static HikariDataSource dataSource;
  static {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:postgresql://db:5432/ftp-server");
    config.setUsername("postgres");
    config.setPassword("postgres");
    config.setMaximumPoolSize(10);
    config.setMinimumIdle(2);
    config.setIdleTimeout(30000);
    config.setConnectionTimeout(30000);
    config.setDriverClassName("org.postgresql.Driver");

    dataSource = new HikariDataSource(config);
  }

//  private static HikariDataSource dataSource;
//  static {
//    HikariConfig config = new HikariConfig();
//    config.setJdbcUrl("jdbc:postgresql://localhost:5432/ftp-server");
//    config.setUsername("postgres");
//    config.setPassword("postgres");
//    config.setMaximumPoolSize(10);
//    config.setMinimumIdle(2);
//    config.setIdleTimeout(30000);
//    config.setConnectionTimeout(30000);
//    config.setDriverClassName("org.postgresql.Driver");
//
//    dataSource = new HikariDataSource(config);
//  }


  // comment for LOCAL DEVELOPMENT
  public static DataSource getDataSource() {
    return dataSource;
  }
}
