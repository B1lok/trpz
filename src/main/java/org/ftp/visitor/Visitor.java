package org.ftp.visitor;

import org.ftp.domain.Directory;
import org.ftp.domain.File;
import org.ftp.domain.User;

public interface Visitor {
  boolean visitFile(File file, User user);
  boolean visitDirectory(Directory directory, User user);
}
