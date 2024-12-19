package org.ftp;

import org.ftp.command.builder.CommandDirector;
import org.ftp.command.CommandResponse;
import org.ftp.command.builder.LoggedContainerBuilder;
import org.ftp.command.container.CommandContainer;

public class LoggedState implements SessionState {

  private final CommandContainer commandContainer;

  public LoggedState() {
    this.commandContainer = CommandDirector.makeCommandContainer(new LoggedContainerBuilder());
  }

  @Override
  public CommandResponse handleCommand(UserSession session, String command) {
    return commandContainer
        .retrieveCommand(command.trim().split(" ")[0])
        .execute(session, command.trim().split(" "));
  }
}
