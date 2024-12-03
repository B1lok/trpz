package org.ftp.command.container;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.ftp.command.Command;

public class CommandContainer {

  private final ImmutableMap<String, Command> commandMap;
  private final Command unknownCommand;

  public CommandContainer(ImmutableMap<String, Command> commandMap, Command unknownCommand) {
    this.commandMap = commandMap;
    this.unknownCommand = unknownCommand;
  }

  public Command retrieveCommand(String commandName){
    return commandMap.getOrDefault(commandName, unknownCommand);
  }
  public static class Builder {
    private final ImmutableMap.Builder<String, Command> commandMapBuilder = ImmutableMap.builder();
    private Command unknownCommand;

    public Builder addCommand(String commandName, Command command) {
      commandMapBuilder.put(commandName, command);
      return this;
    }

    public Builder setUnknownCommand(Command unknownCommand) {
      this.unknownCommand = unknownCommand;
      return this;
    }

    public CommandContainer build() {
      return new CommandContainer(commandMapBuilder.build(), unknownCommand);
    }
  }
}
