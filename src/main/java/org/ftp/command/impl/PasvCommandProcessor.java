package org.ftp.command.impl;

import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.util.SystemUtils;

public class PasvCommandProcessor extends CommandProcessor {

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 1;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String myIp = SystemUtils.SERVER_IP;
    String myIpSplit[] = myIp.split("\\.");
    int dataPort = session.getDataPort();
    int p1 = dataPort / 256;
    int p2 = dataPort % 256;
    session.sendIntermediateMessageToClient(new CommandResponse.Builder()
        .withCode(227)
        .withMessage(
            "Entering Passive Mode (" + myIpSplit[0] + "," + myIpSplit[1] + "," + myIpSplit[2] + ","
                + myIpSplit[3] + "," + p1 + "," + p2 + ")")
        .build());
    session.openDataConnectionPassive();
    return new CommandResponse.Builder()
        .withCode(227)
        .withMessage("Entered Passive Mode (" + myIpSplit[0] + "," + myIpSplit[1] + "," + myIpSplit[2] + ","
            + myIpSplit[3] + "," + p1 + "," + p2 + ")")
        .build();
  }
}
