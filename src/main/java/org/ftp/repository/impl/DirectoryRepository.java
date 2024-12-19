package org.ftp.repository.impl;

import static org.ftp.util.FileSystemUtils.getDefaultDirectoryPermission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.ftp.domain.Directory;
import org.ftp.domain.Permission;
import org.ftp.exception.RepositoryException;
import org.ftp.repository.Repository;

public class DirectoryRepository implements Repository<Directory, Integer> {

  private static DirectoryRepository instance;
  private final DataSource dataSource;

  private DirectoryRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static synchronized DirectoryRepository getInstance(DataSource dataSource) {
    if (instance == null) {
      instance = new DirectoryRepository(dataSource);
    }
    return instance;
  }

  @Override
  public Directory readById(Integer id) {
    String sql = "SELECT * FROM directories WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToDirectory(rs);
        } else {
          throw new RepositoryException("No directory found with ID: " + id);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read directory by ID: " + id);
    }
  }

  @Override
  public Directory create(Directory entity) {
    String sql = "INSERT INTO directories (path, parent_id, owner_id, group_id, permission) " +
        "VALUES (?, ?, ?, ?, ?) RETURNING id";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, entity.getPath());
      stmt.setObject(2, entity.getParentId());
      stmt.setInt(3, entity.getOwnerId());
      stmt.setObject(4, entity.getGroupId());
      stmt.setInt(5, entity.getPermission() == null ? getDefaultDirectoryPermission()
          : entity.getPermission().toOctal());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          entity.setId(rs.getInt("id"));
        }
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      throw new RepositoryException("Failed to create directory: " + entity.getPath());
    }
    return entity;
  }
  public boolean existByPath(String path) {
    String sql = "SELECT 1 FROM directories WHERE path = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, path);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to check existence of directory by path: " + path);
    }
  }
  public boolean isEmpty(Integer directoryId) {
    String sql = """
        SELECT 1 FROM directories WHERE parent_id = ?
        UNION
        SELECT 1 FROM files WHERE directory_id = ?
    """;
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, directoryId);
      stmt.setInt(2, directoryId);

      try (ResultSet rs = stmt.executeQuery()) {
        return !rs.next();
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to check if directory is empty with ID: " + directoryId);
    }
  }
  @Override
  public Directory update(Directory entity) {
    String sql = "UPDATE directories SET path = ?, parent_id = ?, owner_id = ?, group_id = ?, permission = ? WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, entity.getPath());
      stmt.setObject(2, entity.getParentId());
      stmt.setInt(3, entity.getOwnerId());
      stmt.setObject(4, entity.getGroupId());
      stmt.setInt(5, entity.getPermission() == null ? getDefaultDirectoryPermission()
          : entity.getPermission().toOctal());
      stmt.setInt(6, entity.getId());

      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated == 0) {
        throw new RepositoryException("No directory found with ID: " + entity.getId());
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to update directory with ID: " + entity.getId());
    }
    return entity;
  }

  @Override
  public void delete(Integer id) {
    String sql = "DELETE FROM directories WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);

      int rowsDeleted = stmt.executeUpdate();
      if (rowsDeleted == 0) {
        throw new RepositoryException("No directory found with ID: " + id);
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to delete directory with ID: " + id);
    }
  }

  private Directory mapResultSetToDirectory(ResultSet rs) throws SQLException {
    return Directory.builder()
        .id(rs.getInt("id"))
        .path(rs.getString("path"))
        .parentId((Integer) rs.getObject("parent_id"))
        .ownerId(rs.getInt("owner_id"))
        .groupId((Integer) rs.getObject("group_id"))
        .permission(new Permission(rs.getInt("permission")))
        .build();
  }
  public Optional<String> findDirectoryNameById(Integer id) {
    String sql = "SELECT path FROM directories WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(rs.getString("path"));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to find directory by ID: " + id);
    }
    return Optional.empty();
  }
  public Optional<Directory> findByPath(String path) {
    String sql = "SELECT * FROM directories WHERE path = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, path);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapResultSetToDirectory(rs));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to find directory by path: " + path);
    }
    return Optional.empty();
  }
  public List<Directory> findAllSubdirectoriesByParentId(int parentId) {
    String sql = "SELECT * FROM directories WHERE parent_id = ?";
    List<Directory> subdirectories = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, parentId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          subdirectories.add(mapResultSetToDirectory(rs));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to find subdirectories in directory with ID: " + parentId);
    }

    return subdirectories;
  }

  public List<Directory> readAll() {
    String sql = "SELECT * FROM directories";
    List<Directory> directories = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        directories.add(mapResultSetToDirectory(rs));
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read all directories");
    }
    return directories;
  }
}
