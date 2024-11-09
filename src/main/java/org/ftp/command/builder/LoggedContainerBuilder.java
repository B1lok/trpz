package org.ftp.command.builder;

import org.ftp.command.container.CommandContainer;

public class LoggedContainerBuilder implements CommandContainerBuilder {

  @Override
  public CommandContainer build() {
    return new CommandContainer.Builder()
//        .addCommand()
//        .addCommand()
//        .setUnknownCommand()
        .build();
  }
}
