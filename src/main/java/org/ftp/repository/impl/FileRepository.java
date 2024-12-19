package org.ftp.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.ftp.domain.Directory;
import org.ftp.domain.File;
import org.ftp.domain.Permission;
import org.ftp.exception.RepositoryException;
import org.ftp.repository.Repository;

public class FileRepository implements Repository<File, Integer> {

  private static FileRepository instance;
  private final DataSource dataSource;

  private FileRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static synchronized FileRepository getInstance(DataSource dataSource) {
    if (instance == null) {
      instance = new FileRepository(dataSource);
    }
    return instance;
  }

  @Override
  public File readById(Integer id) {
    String sql = "SELECT * FROM files WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToFile(rs);
        } else {
          throw new RepositoryException("No file found with ID: " + id);
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read file by ID: " + id);
    }
  }

  @Override
  public File create(File entity) {
    String sql = "INSERT INTO files (name, directory_id, owner_id, group_id, permission) " +
        "VALUES (?, ?, ?, ?, ?) RETURNING id";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, entity.getName());
      stmt.setInt(2, entity.getDirectory().getId());
      stmt.setObject(3, entity.getOwnerId());
      stmt.setObject(4, entity.getGroupId());
      stmt.setInt(5, entity.getPermission().toOctal());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          entity.setId(rs.getInt("id"));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to create file: " + entity.getName());
    }
    return entity;
  }

  @Override
  public File update(File entity) {
    String sql = "UPDATE files SET name = ?, directory_id = ?, owner_id = ?, group_id = ?, permission = ? WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, entity.getName());
      stmt.setInt(2, entity.getDirectory().getId());
      stmt.setObject(3, entity.getOwnerId());
      stmt.setObject(4, entity.getGroupId());
      stmt.setInt(5, entity.getPermission().toOctal());
      stmt.setInt(6, entity.getId());

      int rowsUpdated = stmt.executeUpdate();
      if (rowsUpdated == 0) {
        throw new RepositoryException("No file found with ID: " + entity.getId());
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to update file with ID: " + entity.getId());
    }
    return entity;
  }

  @Override
  public void delete(Integer id) {
    String sql = "DELETE FROM files WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);

      int rowsDeleted = stmt.executeUpdate();
      if (rowsDeleted == 0) {
        throw new RepositoryException("No file found with ID: " + id);
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to delete file with ID: " + id);
    }
  }
  public List<File> findAllByDirectoryId(int directoryId) {
    String sql = "SELECT * FROM files WHERE directory_id = ?";
    List<File> files = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, directoryId);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          files.add(mapResultSetToFile(rs));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to find files in directory with ID: " + directoryId);
    }
    return files;
  }

  public Optional<File> findByNameAndDirectory(String name, Directory directory) {
    String sql = "SELECT * FROM files WHERE name = ? AND directory_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, name);
      stmt.setInt(2, directory.getId());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapResultSetToFile(rs));
        }
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to find file by name: " + name);
    }
    return Optional.empty();
  }

  public List<File> readAll() {
    String sql = "SELECT * FROM files";
    List<File> files = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        files.add(mapResultSetToFile(rs));
      }
    } catch (SQLException e) {
      throw new RepositoryException("Failed to read all files");
    }
    return files;
  }

  private File mapResultSetToFile(ResultSet rs) throws SQLException {
    return File.builder()
        .id(rs.getInt("id"))
        .name(rs.getString("name"))
        .directory(Directory.builder().id(rs.getInt("directory_id")).build())
        .ownerId((Integer) rs.getObject("owner_id"))
        .groupId((Integer) rs.getObject("group_id"))
        .permission(new Permission(rs.getInt("permission")))
        .build();
  }
}
