package org.ftp.command;

import org.ftp.UserSession;

public interface Command {
  CommandResponse execute(UserSession session, String[] args);
}
