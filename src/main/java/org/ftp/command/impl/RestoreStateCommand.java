package org.ftp.command.impl;

import org.ftp.UserSession;
import org.ftp.command.Command;
import org.ftp.command.CommandResponse;

public class RestoreStateCommand implements Command {
  @Override
  public CommandResponse execute(UserSession session, String[] args) {
    session.restoreState();
    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage("State restored successfully")
        .build();
  }
}
