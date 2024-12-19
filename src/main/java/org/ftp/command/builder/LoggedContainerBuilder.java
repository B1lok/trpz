package org.ftp.command.builder;

import org.ftp.command.CommandName;
import org.ftp.command.container.CommandContainer;
import org.ftp.command.impl.AddUserToGroupCommandProcessor;
import org.ftp.command.impl.ChangePasswordCommandProcessor;
import org.ftp.command.impl.ChgrpCommandProcessor;
import org.ftp.command.impl.ChmodCommandProcessor;
import org.ftp.command.impl.CreateGroupCommandProcessor;
import org.ftp.command.impl.CreateUserCommandProcessor;
import org.ftp.command.impl.CwdCommandProcessor;
import org.ftp.command.impl.GroupsCommandProcessor;
import org.ftp.command.impl.ListCommandProcessor;
import org.ftp.command.impl.MaxConnCommandProcessor;
import org.ftp.command.impl.MaxSpeedCommandProcessor;
import org.ftp.command.impl.MkdCommandProcessor;
import org.ftp.command.impl.MyGroupsCommandProcessor;
import org.ftp.command.impl.MyStatCommandProcessor;
import org.ftp.command.impl.PasvCommandProcessor;
import org.ftp.command.impl.PortCommandProcessor;
import org.ftp.command.impl.PwdCommandProcessor;
import org.ftp.command.impl.QuitServerCommandProcessor;
import org.ftp.command.impl.RemoveUserFromGroupCommandProcessor;
import org.ftp.command.impl.RetrCommandProcessor;
import org.ftp.command.impl.RmCommandProcessor;
import org.ftp.command.impl.RmdCommandProcessor;
import org.ftp.command.impl.StatCommandProcessor;
import org.ftp.command.impl.StorCommandProcessor;
import org.ftp.command.impl.TypeCommandProcessor;
import org.ftp.command.impl.UnknownCommandProcessor;

public class LoggedContainerBuilder implements CommandContainerBuilder {

  @Override
  public CommandContainer build() {
    return new CommandContainer.Builder()
        .addCommand(CommandName.LIST.getCommandName(), new ListCommandProcessor())
        .addCommand(CommandName.QUIT.getCommandName(), new QuitServerCommandProcessor())
        .addCommand(CommandName.ADDUSER.getCommandName(), new CreateUserCommandProcessor())
        .addCommand(CommandName.CHPASS.getCommandName(), new ChangePasswordCommandProcessor())
        .addCommand(CommandName.ADDGROUP.getCommandName(), new CreateGroupCommandProcessor())
        .addCommand(CommandName.AUG.getCommandName(), new AddUserToGroupCommandProcessor())
        .addCommand(CommandName.RUG.getCommandName(), new RemoveUserFromGroupCommandProcessor())
        .addCommand(CommandName.PWD.getCommandName(), new PwdCommandProcessor())
        .addCommand(CommandName.CWD.getCommandName(), new CwdCommandProcessor())
        .addCommand(CommandName.MKD.getCommandName(), new MkdCommandProcessor())
        .addCommand(CommandName.RMD.getCommandName(), new RmdCommandProcessor())
        .addCommand(CommandName.TYPE.getCommandName(), new TypeCommandProcessor())
        .addCommand(CommandName.PASV.getCommandName(), new PasvCommandProcessor())
        .addCommand(CommandName.PORT.getCommandName(), new PortCommandProcessor())
        .addCommand(CommandName.STOR.getCommandName(), new StorCommandProcessor())
        .addCommand(CommandName.RETR.getCommandName(), new RetrCommandProcessor())
        .addCommand(CommandName.CHMOD.getCommandName(), new ChmodCommandProcessor())
        .addCommand(CommandName.CHGRP.getCommandName(), new ChgrpCommandProcessor())
        .addCommand(CommandName.RM.getCommandName(), new RmCommandProcessor())
        .addCommand(CommandName.MAXCONN.getCommandName(), new MaxConnCommandProcessor())
        .addCommand(CommandName.MAXSPEED.getCommandName(), new MaxSpeedCommandProcessor())
        .addCommand(CommandName.MYSTAT.getCommandName(), new MyStatCommandProcessor())
        .addCommand(CommandName.STAT.getCommandName(), new StatCommandProcessor())
        .addCommand(CommandName.GROUPS.getCommandName(), new GroupsCommandProcessor())
        .addCommand(CommandName.MYGROUPS.getCommandName(), new MyGroupsCommandProcessor())
        .setUnknownCommand(new UnknownCommandProcessor())
        .build();
  }
}
