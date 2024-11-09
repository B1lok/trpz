package org.ftp.command.builder;

import org.ftp.command.builder.CommandContainerBuilder;
import org.ftp.command.container.CommandContainer;

public abstract class CommandDirector {


  public static CommandContainer makeCommandContainer(CommandContainerBuilder builder) {
    return builder.build();
  }
}
