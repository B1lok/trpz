package org.ftp.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.ftp.domain.Group;
import org.ftp.exception.RepositoryException;
import org.ftp.repository.Repository;

public class GroupRepository implements Repository<Group, Integer> {

  private static GroupRepository instance;
  private final DataSource dataSource;

  private GroupRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static synchronized GroupRepository getInstance(DataSource dataSource) {
    if (instance == null) {
      instance = new GroupRepository(dataSource);
    }
    return instance;
  }

  @Override
  public Group readById(Integer id) {
    String sql = "SELECT * FROM groups WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToGroup(rs);
        } else {
          throw new RepositoryException("No group found with ID: " + id);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read group by ID: " + id);
    }
  }

  @Override
  public Group create(Group entity) {
    String sql = "INSERT INTO groups (name) VALUES (?) RETURNING id";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, entity.getName());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          entity.setId(rs.getInt("id"));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to create group: " + entity.getName());
    }
    return entity;
  }

  @Override
  public Group update(Group entity) {
    String sql = "UPDATE groups SET name = ? WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, entity.getName());
      stmt.setInt(2, entity.getId());

      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated == 0) {
        throw new RepositoryException("No group found with ID: " + entity.getId());
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to update group with ID: " + entity.getId());
    }
    return entity;
  }
  public boolean existsByName(String groupName) {
    String sql = "SELECT 1 FROM groups WHERE name = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, groupName);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to check existence of group: " + groupName);
    }
  }

  @Override
  public void delete(Integer id) {
    String sql = "DELETE FROM groups WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);

      int rowsDeleted = stmt.executeUpdate();
      if (rowsDeleted == 0) {
        throw new RepositoryException("No group found with ID: " + id);
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to delete group with ID: " + id);
    }
  }

  private Group mapResultSetToGroup(ResultSet rs) throws SQLException {
    return Group.builder()
        .id(rs.getInt("id"))
        .name(rs.getString("name"))
        .build();
  }
  public void addUserToGroup(int userId, int groupId) {
    String sql = "INSERT INTO user_group (user_id, group_id) VALUES (?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, userId);
      stmt.setInt(2, groupId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new RepositoryException("Failed to add user to group");
    }
  }
  public Optional<Group> findByGroupName(String groupName) {
    String sql = "SELECT * FROM groups WHERE name = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, groupName);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          Group group = mapResultSetToGroup(rs);
          return Optional.of(group);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to find group by name: " + groupName);
    }
    return Optional.empty();
  }
  public void removeUserFromGroup(int userId, int groupId) {
    String sql = "DELETE FROM user_group WHERE user_id = ? AND group_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, userId);
      stmt.setInt(2, groupId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new RepositoryException("Failed to remove user from group");
    }
  }
  public boolean isUserMemberOfGroup(int userId, int groupId) {
    String sql = "SELECT 1 FROM user_group WHERE user_id = ? AND group_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);
      stmt.setInt(2, groupId);

      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to check if user is a member of group");
    }
  }
  public List<Group> readAll() {
    String sql = "SELECT * FROM groups";
    List<Group> groups = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        groups.add(mapResultSetToGroup(rs));
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read all groups");
    }

    return groups;
  }
}
