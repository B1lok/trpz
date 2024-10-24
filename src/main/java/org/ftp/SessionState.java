package org.ftp;

import org.ftp.command.CommandResponse;

public interface SessionState {

  CommandResponse handleCommand(UserSession session, String command);
}
