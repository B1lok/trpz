package org.ftp.domain;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class User {
  private int id;
  private String username;
  private String password;
  private Role role;
  private List<Group> groups;
}
