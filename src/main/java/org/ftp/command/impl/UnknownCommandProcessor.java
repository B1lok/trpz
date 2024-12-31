package org.ftp.command.impl;

import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;

public class UnknownCommandProcessor extends CommandProcessor {

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    return new CommandResponse.Builder()
        .withCode(500)
        .withMessage("Unknown command. Please check the command and try again.")
        .build();
  }
}
