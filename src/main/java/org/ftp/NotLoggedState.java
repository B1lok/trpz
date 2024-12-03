package org.ftp;

import org.ftp.command.CommandResponse;
import org.ftp.command.container.CommandContainer;
import org.ftp.command.container.NotLoggedCommandContainer;

public class NotLoggedState implements SessionState {

  private final CommandContainer commandContainer = new NotLoggedCommandContainer();


  @Override
  public CommandResponse handleCommand(UserSession session, String command) {
    return commandContainer.retrieveCommand(command).execute(session, null);
  }
}
