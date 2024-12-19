package org.ftp.command.builder;

import static org.ftp.command.CommandName.PASSWORD;
import static org.ftp.command.CommandName.USER;

import org.ftp.command.container.CommandContainer;
import org.ftp.command.impl.PassCommandProcessor;
import org.ftp.command.impl.UnknownCommandProcessor;
import org.ftp.command.impl.UserCommandProcessor;

public class NonLoggedContainerBuilder implements CommandContainerBuilder {

  @Override
  public CommandContainer build() {
    return new CommandContainer.Builder()
        .addCommand(PASSWORD.getCommandName(), new PassCommandProcessor())
        .addCommand(USER.getCommandName(), new UserCommandProcessor())
        .setUnknownCommand(new UnknownCommandProcessor())
        .build();
  }
}
