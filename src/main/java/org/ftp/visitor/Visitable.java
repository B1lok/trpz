package org.ftp.visitor;

import org.ftp.domain.User;

public interface Visitable {
  boolean accept(Visitor visitor, User user);
}
