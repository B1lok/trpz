package org.ftp;

import org.ftp.command.CommandResponse;
import org.ftp.command.container.CommandContainer;
import org.ftp.command.container.LoggedCommandContainer;

public class LoggedState implements SessionState{

  private final CommandContainer commandContainer = new LoggedCommandContainer();
  @Override
  public CommandResponse handleCommand(UserSession session, String command) {
    return commandContainer.retrieveCommand(command).execute(session, null);
  }
}
