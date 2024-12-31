package org.ftp.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.ftp.domain.ConnectionLimits;
import org.ftp.exception.RepositoryException;
import org.ftp.repository.Repository;

public class ConnectionLimitsRepository implements Repository<ConnectionLimits, Integer> {

  private static ConnectionLimitsRepository instance;
  private final DataSource dataSource;

  private ConnectionLimitsRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static synchronized ConnectionLimitsRepository getInstance(DataSource dataSource) {
    if (instance == null) {
      instance = new ConnectionLimitsRepository(dataSource);
    }
    return instance;
  }

  @Override
  public ConnectionLimits readById(Integer id) {
    String sql = "SELECT * FROM connection_limits WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToConnectionLimits(rs);
        } else {
          throw new RepositoryException("No connection limits found with ID: " + id);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read connection limits by ID: " + id);
    }
  }

  @Override
  public ConnectionLimits create(ConnectionLimits entity) {
    String sql = "INSERT INTO connection_limits (max_connections, max_speed, user_id) " +
        "VALUES (?, ?, ?) RETURNING id";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, entity.getMaxConnections());
      stmt.setInt(2, entity.getMaxSpeed());
      stmt.setInt(3, entity.getUserId());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          entity.setId(rs.getInt("id"));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to create connection limits");
    }
    return entity;
  }

  @Override
  public ConnectionLimits update(ConnectionLimits entity) {
    String sql = "UPDATE connection_limits SET max_connections = ?, max_speed = ?, user_id = ? WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, entity.getMaxConnections());
      stmt.setInt(2, entity.getMaxSpeed());
      stmt.setInt(3, entity.getUserId());
      stmt.setInt(4, entity.getId());

      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated == 0) {
        throw new RepositoryException("No connection limits found with ID: " + entity.getId());
      }
    } catch (SQLException e) {
      throw new RepositoryException(
          "Failed to update connection limits with ID: " + entity.getId());
    }
    return entity;
  }

  @Override
  public void delete(Integer id) {
    String sql = "DELETE FROM connection_limits WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);

      int rowsDeleted = stmt.executeUpdate();
      if (rowsDeleted == 0) {
        throw new RepositoryException("No connection limits found with ID: " + id);
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to delete connection limits with ID: " + id);
    }
  }
  /**
   * Finds connection limits for a user by their username.
   *
   * @param username the username to search for
   * @return an Optional containing the ConnectionLimits if found, or an empty Optional otherwise
   */
  public Optional<ConnectionLimits> findByUsername(String username) {
    String sql = """
        SELECT cl.id, cl.max_connections, cl.max_speed, cl.user_id
        FROM connection_limits cl
        JOIN users u ON cl.user_id = u.id
        WHERE u.username = ?
        """;

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, username);

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapResultSetToConnectionLimits(rs));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to find connection limits for username: " + username);
    }

    return Optional.empty();
  }

  private ConnectionLimits mapResultSetToConnectionLimits(ResultSet rs) throws SQLException {
    return ConnectionLimits.builder()
        .id(rs.getInt("id"))
        .maxConnections(rs.getInt("max_connections"))
        .maxSpeed(rs.getInt("max_speed"))
        .userId(rs.getInt("user_id"))
        .build();
  }
}
