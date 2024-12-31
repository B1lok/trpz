package org.ftp.util;


import java.util.List;
import org.ftp.domain.Permission;

public class FileSystemUtils {

  public static final List<String> DIRECTORY_NAVIGATION_SYMBOLS = List.of("..", "/.");
  public static final String HOME_DIRECTORY_PATH = "/server/home";
  public static final String ROOT_PATH = "/server";
  public static final String ROOT_DIRECTORY = "~";

  public static String getServerBasePath() {
//    return System.getProperty("user.dir");
    return "/ftp-data";
  }

  public static int getServerRootId() {
    return 1;
  }

  public static boolean isAbsolutePath(String path) {
    return path.startsWith(FileSystemUtils.ROOT_PATH);
  }

  public static int getDefaultFilePermission() {
    return 644;
  }

  public static int getDefaultDirectoryPermission() {
    return 755;
  }

  public static String getPermissionString(Permission permission) {
    StringBuilder permissionString = new StringBuilder();

    int octalValue = permission.toOctal();
    String octalStr = String.format("%03d", octalValue);

    for (char digit : octalStr.toCharArray()) {
      int value = Character.getNumericValue(digit);

      permissionString.append((value & 4) != 0 ? 'r' : '-');
      permissionString.append((value & 2) != 0 ? 'w' : '-');
      permissionString.append((value & 1) != 0 ? 'x' : '-');
    }

    return permissionString.toString();
  }

}
