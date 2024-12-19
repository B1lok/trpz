package org.ftp.command.impl;

import org.ftp.TransferType;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;

public class TypeCommandProcessor extends CommandProcessor {

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 2;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    if (arguments[1].equals("A")) {
      session.setTransferType(TransferType.ASCII);
      return new CommandResponse.Builder()
          .withCode(200)
          .withMessage("Switched to ASCII mode.")
          .build();
    } else if (arguments[1].equals("I")) {
      session.setTransferType(TransferType.BINARY);
      return new CommandResponse.Builder()
          .withCode(200)
          .withMessage("Switched to BINARY mode.")
          .build();
    } else {
      return new CommandResponse.Builder()
          .withCode(504)
          .withMessage("Invalid type code.")
          .build();
    }
  }
}
