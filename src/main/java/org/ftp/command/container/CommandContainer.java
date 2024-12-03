package org.ftp.command.container;

import com.google.common.collect.ImmutableMap;
import org.ftp.command.CommandProcessor;

public class CommandContainer {

  private final ImmutableMap<String, CommandProcessor> commandMap;
  private final CommandProcessor unknownCommand;

  public CommandContainer(ImmutableMap<String, CommandProcessor> commandMap, CommandProcessor unknownCommand) {
    this.commandMap = commandMap;
    this.unknownCommand = unknownCommand;
  }

  public CommandProcessor retrieveCommand(String commandName){
    return commandMap.getOrDefault(commandName, unknownCommand);
  }
  public static class Builder {
    private final ImmutableMap.Builder<String, CommandProcessor> commandMapBuilder = ImmutableMap.builder();
    private CommandProcessor unknownCommand;

    public Builder addCommand(String commandName, CommandProcessor command) {
      commandMapBuilder.put(commandName, command);
      return this;
    }

    public Builder setUnknownCommand(CommandProcessor unknownCommand) {
      this.unknownCommand = unknownCommand;
      return this;
    }

    public CommandContainer build() {
      return new CommandContainer(commandMapBuilder.build(), unknownCommand);
    }
  }
}
