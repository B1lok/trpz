package org.ftp.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.ftp.domain.ConnectionStatistics;
import org.ftp.exception.RepositoryException;
import org.ftp.repository.Repository;

public class ConnectionStatisticsRepository implements Repository<ConnectionStatistics, Integer> {

  private static ConnectionStatisticsRepository instance;
  private final DataSource dataSource;

  private ConnectionStatisticsRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static synchronized ConnectionStatisticsRepository getInstance(DataSource dataSource) {
    if (instance == null) {
      instance = new ConnectionStatisticsRepository(dataSource);
    }
    return instance;
  }

  @Override
  public ConnectionStatistics readById(Integer id) {
    String sql = "SELECT * FROM connection_statistics WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToConnectionStatistics(rs);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read connection statistics by ID: " + id);
    }
    return null;
  }

  @Override
  public ConnectionStatistics create(ConnectionStatistics entity) {
    String sql =
        "INSERT INTO connection_statistics (connection_time, disconnection_time, user_id) " +
            "VALUES (?, ?, ?) RETURNING id";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setObject(1, entity.getConnectionTime());
      stmt.setObject(2, entity.getDisconnectionTime());
      stmt.setInt(3, entity.getUserId());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          entity.setId(rs.getInt("id"));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to create connection statistics");
    }
    return entity;
  }

  @Override
  public ConnectionStatistics update(ConnectionStatistics entity) {
    String sql = "UPDATE connection_statistics SET connection_time = ?, disconnection_time = ?, user_id = ? WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setObject(1, entity.getConnectionTime());
      stmt.setObject(2, entity.getDisconnectionTime());
      stmt.setInt(3, entity.getUserId());
      stmt.setInt(4, entity.getId());

      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated == 0) {
        throw new RepositoryException("No connection statistics found with ID: " + entity.getId());
      }
    } catch (SQLException e) {
      throw new RepositoryException(
          "Failed to update connection statistics with ID: " + entity.getId());
    }
    return entity;
  }

  @Override
  public void delete(Integer id) {
    String sql = "DELETE FROM connection_statistics WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);

      int rowsDeleted = stmt.executeUpdate();
      if (rowsDeleted == 0) {
        throw new RepositoryException("No connection statistics found with ID: " + id);
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to delete connection statistics with ID: " + id);
    }
  }
  public List<ConnectionStatistics> findLastConnectionsByUserId(Integer userId) {
    String sql = "SELECT * FROM connection_statistics " +
        "WHERE user_id = ? " +
        "ORDER BY connection_time DESC " +
        "LIMIT 20";
    List<ConnectionStatistics> statisticsList = new ArrayList<>();
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          statisticsList.add(mapResultSetToConnectionStatistics(rs));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException(
          "Failed to retrieve connection statistics for user ID: " + userId);
    }
    return statisticsList;
  }

  private ConnectionStatistics mapResultSetToConnectionStatistics(ResultSet rs)
      throws SQLException {
    return ConnectionStatistics.builder()
        .id(rs.getInt("id"))
        .connectionTime(rs.getObject("connection_time", java.time.LocalDateTime.class))
        .disconnectionTime(rs.getObject("disconnection_time", java.time.LocalDateTime.class))
        .userId(rs.getInt("user_id"))
        .build();
  }
}
