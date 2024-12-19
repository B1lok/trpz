package org.ftp.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
  public static String hashPassword(String plainTextPassword) {
    return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
  }

  public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
    return BCrypt.checkpw(plainTextPassword, hashedPassword);
  }

  public static void main(String[] args) {
    System.out.println(hashPassword("admin"));
  }
}
