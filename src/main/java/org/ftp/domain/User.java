package org.ftp.domain;

import lombok.Data;

@Data
public class User {
  private int id;
  private String username;
  private String password;
  private Role role;
}
