package org.ftp.command;

public enum CommandName {;

  private final String commandName;

  CommandName(String commandName) {
    this.commandName = commandName;
  }

  public String getCommandName() {
    return commandName;
  }
}
