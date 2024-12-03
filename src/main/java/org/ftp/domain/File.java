package org.ftp.domain;

import lombok.Data;
import org.ftp.visitor.Visitable;
import org.ftp.visitor.Visitor;


@Data
public abstract class File implements Visitable {
  private int id;
  private String name;
  private Directory directory;
  private int ownerId;
  private int groupId;
  private Permission permission;

  @Override
  public boolean accept(Visitor visitor, User user) {
    return visitor.visitFile(this, user);
  }
}
