package org.ftp.domain;

import lombok.Data;


@Data
public class File  {
  private int id;
  private String name;
  private Directory directory;
  private int ownerId;
  private int groupId;
  private int permission;
}
