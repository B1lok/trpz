package org.ftp;

import lombok.Data;

@Data
public class UserCredentials {
  private String username;
  private int userDirectoryId;
  private UserStatus status;
  private int connectionId;
}
