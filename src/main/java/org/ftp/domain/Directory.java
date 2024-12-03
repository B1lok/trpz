package org.ftp.domain;

import lombok.Data;
import org.ftp.visitor.Visitable;
import org.ftp.visitor.Visitor;


@Data
public class Directory implements Visitable {
  private int id;
  private String path;
  private int parentId;
  private int ownerId;
  private int groupId;
  private Permission permission;

  @Override
  public boolean accept(Visitor visitor, User user) {
    return visitor.visitDirectory(this, user);
  }
}
