package org.ftp.memento;

import org.ftp.SessionState;

public interface Memento {
  String getCurrDirectory();
  SessionState getState();
}
