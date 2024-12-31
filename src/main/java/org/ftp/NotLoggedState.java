package org.ftp;

import org.ftp.command.CommandResponse;
import org.ftp.command.builder.CommandDirector;
import org.ftp.command.builder.NonLoggedContainerBuilder;
import org.ftp.command.container.CommandContainer;

public class NotLoggedState implements SessionState {

  private final CommandContainer commandContainer;

  public NotLoggedState() {
    this.commandContainer = CommandDirector.makeCommandContainer(new NonLoggedContainerBuilder());
  }

  @Override
  public CommandResponse handleCommand(UserSession session, String command) {
    return commandContainer
        .retrieveCommand(command.trim().split(" ")[0])
        .execute(session, command.trim().split(" "));
  }
}
