package org.ftp.domain;

import lombok.Data;


@Data
public class Directory {
  private int id;
  private String path;
  private int parentId;
  private int ownerId;
  private int groupId;
  private int permission;
}
