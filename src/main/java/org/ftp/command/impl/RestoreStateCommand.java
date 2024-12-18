package org.ftp.command.impl;

import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;

public class RestoreStateCommand extends CommandProcessor {

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    session.restoreState();
    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage("State restored successfully")
        .build();
  }
}
