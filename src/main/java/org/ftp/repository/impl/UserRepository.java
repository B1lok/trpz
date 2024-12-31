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
import org.ftp.domain.Role;
import org.ftp.domain.User;
import org.ftp.exception.RepositoryException;
import org.ftp.repository.Repository;

public class UserRepository implements Repository<User, Integer> {

  private static UserRepository instance;
  private final DataSource dataSource;

  private UserRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static synchronized UserRepository getInstance(DataSource dataSource) {
    if (instance == null) {
      instance = new UserRepository(dataSource);
    }
    return instance;
  }

  public List<User> readAll() {
    String sql = "SELECT * FROM users";
    List<User> users = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        User user = User.builder()
            .id(rs.getInt("id"))
            .username(rs.getString("username"))
            .password(rs.getString("password"))
            .role(Role.valueOf(rs.getString("role")))
            .build();
        users.add(user);
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read all users");
    }

    return users;
  }
  public Optional<User> findByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, username);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(User.builder()
              .id(rs.getInt("id"))
              .username(rs.getString("username"))
              .password(rs.getString("password"))
              .role(Role.valueOf(rs.getString("role")))
              .build());
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to find user by username: " + username);
    }
    return Optional.empty();
  }
  public Optional<User> getUserWithGroupsByUsername(String username) {
    String sql = "SELECT u.id AS user_id, u.username, u.password, u.role, g.id AS group_id, g.name AS group_name " +
        "FROM users u " +
        "LEFT JOIN user_group ug ON u.id = ug.user_id " +
        "LEFT JOIN groups g ON ug.group_id = g.id " +
        "WHERE u.username = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, username);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          User user = mapResultSetToUser(rs);
          List<Group> groups = new ArrayList<>();
          do {
            if (rs.getInt("group_id") != 0) {
              Group group = Group.builder()
                  .id(rs.getInt("group_id")).name(rs.getString("group_name")).build();
              groups.add(group);
            }
          } while (rs.next());

          user.setGroups(groups);
          return Optional.of(user);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to get user with groups by username: " + username);
    }
    return Optional.empty();
  }
  public boolean isAdmin(String username) {
    String sql = "SELECT role FROM users WHERE username = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, username);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          String role = rs.getString("role");
          return Role.ADMIN.name().equalsIgnoreCase(role);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to check if user is admin: " + username);
    }
    return false;
  }
  @Override
  public User readById(Integer id) {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return User.builder()
              .id(rs.getInt("id"))
              .username(rs.getString("username"))
              .password(rs.getString("password"))
              .role(Role.valueOf(rs.getString("role")))
              .build();
        } else {
          throw new RepositoryException("No user found with ID: " + id);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read user by ID: " + id);
    }
  }
  public List<Group> findGroupsByUserId(int userId) {
    String sql = "SELECT g.id, g.name " +
        "FROM groups g " +
        "JOIN user_group ug ON g.id = ug.group_id " +
        "WHERE ug.user_id = ?";
    List<Group> groups = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          Group group = Group.builder()
              .id(rs.getInt("id"))
              .name(rs.getString("name"))
              .build();
          groups.add(group);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to retrieve groups for user with ID: " + userId);
    }
    return groups;
  }

  public List<String> findUsernamesByGroupId(int groupId) {
    String sql = "SELECT u.username " +
        "FROM users u " +
        "JOIN user_group ug ON u.id = ug.user_id " +
        "WHERE ug.group_id = ?";
    List<String> usernames = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, groupId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          usernames.add(rs.getString("username"));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to find usernames for group ID: " + groupId);
    }
    return usernames;
  }

  @Override
  public User create(User entity) {
    String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?) RETURNING id";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, entity.getUsername());
      stmt.setString(2, entity.getPassword());
      stmt.setString(3, entity.getRole().name());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          entity.setId(rs.getInt("id"));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to create user: " + entity.getUsername());
    }

    return entity;
  }

  @Override
  public User update(User entity) {
    String sql = "UPDATE users SET username = ?, password = ?, role = ? WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, entity.getUsername());
      stmt.setString(2, entity.getPassword());
      stmt.setString(3, entity.getRole().name());
      stmt.setInt(4, entity.getId());

      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated > 0) {
        System.out.println("User with ID " + entity.getId() + " updated successfully.");
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to update user with ID: " + entity.getId());
    }
    return entity;
  }

  @Override
  public void delete(Integer id) {
    String sql = "DELETE FROM users WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);

      int rowsDeleted = stmt.executeUpdate();
      if (rowsDeleted == 0) {
        throw new RepositoryException("No user found with ID: " + id);
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to delete user with ID: " + id);
    }
  }
  public boolean existsByUsername(String username) {
    String sql = "SELECT 1 FROM users WHERE username = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, username);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to check existence of user: " + username);
    }
  }
  private User mapResultSetToUser(ResultSet rs) throws SQLException {
    return User.builder()
        .id(rs.getInt("user_id"))
        .username(rs.getString("username"))
        .password(rs.getString("password"))
        .role(Role.valueOf(rs.getString("role")))
        .build();
  }
}
