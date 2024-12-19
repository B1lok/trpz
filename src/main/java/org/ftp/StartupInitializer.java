package org.ftp;

import static org.ftp.util.PasswordUtils.hashPassword;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import org.ftp.domain.Directory;
import org.ftp.domain.GlobalLimits;
import org.ftp.domain.Group;
import org.ftp.domain.Permission;
import org.ftp.domain.Role;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.DirectoryRepository;
import org.ftp.repository.impl.GlobalLimitsRepository;
import org.ftp.repository.impl.GroupRepository;
import org.ftp.repository.impl.UserRepository;

public class StartupInitializer {

  public static void initializeServer() {
    // Initialize repositories
    UserRepository userRepository = RepositoryFactory.getUserRepository();
    GlobalLimitsRepository globalLimitsRepository = RepositoryFactory.getGlobalLimitsRepository();
    GroupRepository groupRepository = RepositoryFactory.getGroupRepository();
    DirectoryRepository directoryRepository = RepositoryFactory.getDirectoryRepository();

    // Create admin user
    User admin = createAdminUser(userRepository);

    // Create "admins" group and add the admin to it
    Group admins = createAdminsGroup(groupRepository, admin);

    // Set global limits
    setGlobalLimits(globalLimitsRepository, admin);

    // Create directories in the database
    createDirectoriesInDatabase(directoryRepository, admin, admins);

    // Create directories on the file system
    initializeFileSystemDirectories();
  }

  public static Optional<Integer> getMaxConnections() {
    GlobalLimitsRepository globalLimitsRepository = RepositoryFactory.getGlobalLimitsRepository();
    return globalLimitsRepository.getLastMaxConnections();
  }

  private static User createAdminUser(UserRepository userRepository) {
    return userRepository.create(User.builder()
        .username("admin")
        .password(hashPassword("admin"))
        .role(Role.ADMIN)
        .build());
  }

  private static Group createAdminsGroup(GroupRepository groupRepository, User admin) {
    Group admins = groupRepository.create(Group.builder()
        .name("admins")
        .build());
    groupRepository.addUserToGroup(admin.getId(), admins.getId());
    return admins;
  }

  private static void setGlobalLimits(GlobalLimitsRepository globalLimitsRepository, User admin) {
    globalLimitsRepository.create(GlobalLimits.builder()
        .maxConnections(10)
        .maxSpeed(1)
        .updatedAt(LocalDateTime.now())
        .updatedById(admin.getId())
        .build());
  }

  private static void createDirectoriesInDatabase(
      DirectoryRepository directoryRepository, User admin, Group admins) {
    Directory serverDirectory = directoryRepository.create(Directory.builder()
        .path("/server")
        .parentId(null)
        .groupId(admins.getId())
        .permission(new Permission(775))
        .ownerId(admin.getId())
        .build());

    Directory rootDirectory = directoryRepository.create(Directory.builder()
        .path("/server/root")
        .parentId(serverDirectory.getId())
        .groupId(admins.getId())
        .permission(new Permission(775))
        .ownerId(admin.getId())
        .build());

    Directory homeDirectory = directoryRepository.create(Directory.builder()
        .path("/server/home")
        .parentId(serverDirectory.getId())
        .groupId(admins.getId())
        .permission(new Permission(775))
        .ownerId(admin.getId())
        .build());
    Directory adminHomeDirectory = directoryRepository.create(Directory.builder()
        .path("/server/home/admin")
        .parentId(homeDirectory.getId())
        .groupId(admins.getId())
        .permission(new Permission(711))
        .ownerId(admin.getId())
        .build());
  }

  public static void initializeFileSystemDirectories() {
    Path serverPath = Paths.get(System.getProperty("user.dir"), "server");
    Path rootPath = serverPath.resolve("root");
    Path homePath = serverPath.resolve("home");
    Path adminHomePath = homePath.resolve("admin");
    try {
      Files.createDirectories(rootPath);
      Files.createDirectories(homePath);
      Files.createDirectories(adminHomePath);
      System.out.println("Directories successfully initialized.");
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize directories on the file system", e);
    }
  }
}