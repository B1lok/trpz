package org.ftp.visitor;

import org.ftp.domain.Directory;
import org.ftp.domain.File;
import org.ftp.domain.Permission;
import org.ftp.domain.User;

public class PermissionCheckVisitor implements Visitor {
  private final Permission.Access access;

  public PermissionCheckVisitor(Permission.Access access) {
    this.access = access;
  }

  @Override
  public boolean visitFile(File file, User user) {
    return file.getPermission().hasAccess(user, file.getOwnerId(), file.getGroupId(), user.getGroups(), access);
  }

  @Override
  public boolean visitDirectory(Directory directory, User user) {
    return directory.getPermission().hasAccess(user, directory.getOwnerId(), directory.getGroupId(), user.getGroups(), access);
  }
}
