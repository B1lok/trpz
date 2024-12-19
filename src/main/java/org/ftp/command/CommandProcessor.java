package org.ftp.command;

import org.ftp.UserSession;

public abstract class CommandProcessor {
  public final CommandResponse execute(UserSession session, String[] args){
    if (!allowAccess(session, args)) {
      return new CommandResponse.Builder().withCode(403).withMessage("Access denied.").build();
    }

    if (!validArguments(session, args)) {
      return new CommandResponse.Builder().withCode(501).withMessage("Invalid arguments.").build();
    }

    try {
      return process(session, args);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return new CommandResponse.Builder().withCode(500).withMessage("An error occurred.").build();
    }
  }

  protected boolean allowAccess(UserSession session, String[] arguments) {
    return true;
  }


  protected boolean validArguments(UserSession session, String[] arguments) {
    return true;
  }

  protected abstract CommandResponse process(UserSession session, String[] arguments);
}
