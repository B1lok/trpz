package org.ftp.domain;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Permission {
  public enum Access { READ, WRITE, EXECUTE }

  private final Set<Access> ownerPermissions;
  private final Set<Access> groupPermissions;
  private final Set<Access> othersPermissions;

  public Permission(int octal) {
    ownerPermissions = parsePermissions(octal / 100);
    groupPermissions = parsePermissions((octal / 10) % 10);
    othersPermissions = parsePermissions(octal % 10);
  }

  private Set<Access> parsePermissions(int value) {
    Set<Access> permissions = EnumSet.noneOf(Access.class);
    if ((value & 4) != 0) permissions.add(Access.READ);
    if ((value & 2) != 0) permissions.add(Access.WRITE);
    if ((value & 1) != 0) permissions.add(Access.EXECUTE);
    return permissions;
  }

  public boolean hasAccess(User user, int ownerId, Integer groupId, List<Group> userGroups, Access access) {
    if (ownerId == user.getId()) {
      return ownerPermissions.contains(access);
    }

    if (groupId != null){
      for (Group group : userGroups) {
        if (group.getId() == groupId) {
          return groupPermissions.contains(access);
        }
      }
    }

    return othersPermissions.contains(access);
  }
  public int toOctal() {
    return calculatePermissions(ownerPermissions) * 100 +
        calculatePermissions(groupPermissions) * 10 +
        calculatePermissions(othersPermissions);
  }

  private int calculatePermissions(Set<Access> permissions) {
    int value = 0;
    if (permissions.contains(Access.READ)) value += 4;
    if (permissions.contains(Access.WRITE)) value += 2;
    if (permissions.contains(Access.EXECUTE)) value += 1;
    return value;
  }
}
