package org.ftp.domain;

import lombok.Builder;
import lombok.Data;
import org.ftp.visitor.Visitable;
import org.ftp.visitor.Visitor;


@Data
@Builder(toBuilder = true)
public class Directory implements Visitable {
  private Integer id;
  private String path;
  private Integer parentId;
  private Integer ownerId;
  private Integer groupId;
  private Permission permission;

  @Override
  public boolean accept(Visitor visitor, User user) {
    return visitor.visitDirectory(this, user);
  }
}
