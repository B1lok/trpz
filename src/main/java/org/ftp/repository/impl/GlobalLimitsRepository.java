package org.ftp.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.ftp.domain.GlobalLimits;
import org.ftp.exception.RepositoryException;
import org.ftp.repository.Repository;

public class GlobalLimitsRepository implements Repository<GlobalLimits, Integer>{

  private static GlobalLimitsRepository instance;
  private final DataSource dataSource;

  private GlobalLimitsRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static synchronized GlobalLimitsRepository getInstance(DataSource dataSource) {
    if (instance == null) {
      instance = new GlobalLimitsRepository(dataSource);
    }
    return instance;
  }

  @Override
  public GlobalLimits readById(Integer id) {
    String sql = "SELECT * FROM global_limits WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToGlobalLimits(rs);
        } else {
          throw new RepositoryException("No GlobalLimits found with ID: " + id);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read GlobalLimits by ID: " + id);
    }
  }
  public Optional<Integer> getLastMaxConnections() {
    String sql = "SELECT max_connections FROM global_limits ORDER BY id DESC LIMIT 1";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      if (rs.next()) {
        return Optional.of(rs.getInt("max_connections"));
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to retrieve the last max_connections value");
    }
  }
  public Optional<Integer> getLastMaxSpeed() {
    String sql = "SELECT max_speed FROM global_limits ORDER BY id DESC LIMIT 1";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      if (rs.next()) {
        return Optional.of(rs.getInt("max_speed"));
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to retrieve the last max_speed value");
    }
  }

  @Override
  public GlobalLimits create(GlobalLimits entity) {
    String sql = "INSERT INTO global_limits (max_connections, max_speed, updated_at, updated_by) " +
        "VALUES (?, ?, ?, ?) RETURNING id";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, entity.getMaxConnections());
      stmt.setInt(2, entity.getMaxSpeed());
      stmt.setObject(3, entity.getUpdatedAt());
      stmt.setInt(4, entity.getUpdatedById());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          entity.setId(rs.getInt("id"));
          return entity;
        }else {
          throw new RepositoryException("Failed to create GlobalLimits entry");
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to create GlobalLimits entry");
    }
  }

  @Override
  public GlobalLimits update(GlobalLimits entity) {
    String sql = "UPDATE global_limits SET max_connections = ?, max_speed = ?, updated_at = ?, updated_by = ? " +
        "WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, entity.getMaxConnections());
      stmt.setInt(2, entity.getMaxSpeed());
      stmt.setObject(3, entity.getUpdatedAt());
      stmt.setInt(4, entity.getUpdatedById());
      stmt.setInt(5, entity.getId());

      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated > 0) {
        return entity;
      } else {
        throw new RepositoryException("No GlobalLimits found with ID: " + entity.getId());
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to update GlobalLimits with ID: " + entity.getId());
    }
  }

  @Override
  public void delete(Integer id) {
    String sql = "DELETE FROM global_limits WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);

      int rowsDeleted = stmt.executeUpdate();
      if (rowsDeleted == 0) {
        throw new RepositoryException("No GlobalLimits found with ID: " + id);
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to delete GlobalLimits with ID: " + id);
    }
  }

  private GlobalLimits mapResultSetToGlobalLimits(ResultSet rs) throws SQLException {
    return GlobalLimits.builder()
        .id(rs.getInt("id"))
        .maxConnections(rs.getInt("max_connections"))
        .maxSpeed(rs.getInt("max_speed"))
        .updatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class))
        .updatedById(rs.getInt("updated_by"))
        .build();
  }
}
