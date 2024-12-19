package org.ftp.command.impl;

import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;

public class PortCommandProcessor extends CommandProcessor {

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    if (arguments.length != 2) {
      return false;
    }

    String[] stringSplit = arguments[1].split(",");
    if (stringSplit.length != 6) {
      return false;
    }

    try {
      for (int i = 0; i < 4; i++) {
        int part = Integer.parseInt(stringSplit[i]);
        if (part < 0 || part > 255) {
          return false;
        }
      }
      int p1 = Integer.parseInt(stringSplit[4]);
      int p2 = Integer.parseInt(stringSplit[5]);
      if (p1 < 0 || p1 > 255 || p2 < 0 || p2 > 255) {
        return false;
      }

      int port = (p1 * 256) + p2;
      if (port < 0 || port > 65535) {
        return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String[] stringSplit = arguments[1].split(",");
    String hostName =
        stringSplit[0] + "." + stringSplit[1] + "." + stringSplit[2] + "." + stringSplit[3];
    int port = Integer.parseInt(stringSplit[4]) * 256 + Integer.parseInt(stringSplit[5]);
    session.openDataConnectionActive(hostName, port);
    return new CommandResponse.Builder().withCode(200).withMessage("PORT command successful.")
        .build();
  }
}
