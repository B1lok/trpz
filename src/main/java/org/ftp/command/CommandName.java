package org.ftp.command;

public enum CommandName {

  USER("USER"),
  PASSWORD("PASS"),
  LIST("LIST"),
  QUIT("QUIT"),
  ADDUSER("ADDUSER"),
  CHPASS("CHPASS"),
  ADDGROUP("ADDGROUP"),
  AUG("AUG"),
  RUG("RUG"),
  PWD("PWD"),
  CWD("CWD"),
  MKD("MKD"),
  RMD("RMD"),
  TYPE("TYPE"),
  PASV("PASV"),
  PORT("PORT"),
  RETR("RETR"),
  STOR("STOR"),
  CHMOD("CHMOD"),
  CHGRP("CHGRP"),
  RM("RM"),
  MAXCONN("MAXCONN"),
  MAXSPEED("MAXSPEED"),
  STAT("STAT"),
  MYSTAT("MYSTAT"),
  MYGROUPS("MYGROUPS"),
  GROUPS("GROUPS");

  private final String commandName;

  CommandName(String commandName) {
    this.commandName = commandName;
  }

  public String getCommandName() {
    return commandName;
  }
}
