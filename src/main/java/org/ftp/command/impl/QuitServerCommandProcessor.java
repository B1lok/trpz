package org.ftp.command.impl;

import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;

public class QuitServerCommandProcessor extends CommandProcessor {

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 0;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    session.setQuitCommandLoop(true);
    return new CommandResponse.Builder()
        .withCode(221)
        .withMessage("Goodbye")
        .build();
  }
}
