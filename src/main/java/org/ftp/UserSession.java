package org.ftp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.ftp.command.CommandResponse;

public class UserSession extends Thread {

  private Socket controlSocket;
  private SessionState state;
  private ServerSocket dataSocket;
  private BufferedReader controlIn;
  private PrintWriter controlOutWriter;
  private int dataPort;
  private String root;
  private String currDirectory;
  private boolean quitCommandLoop = false;


  public UserSession(Socket controlSocket, int dataPort) {
    this.controlSocket = controlSocket;
    this.dataPort = dataPort;
    this.currDirectory = System.getProperty("user.dir") + "/server";
    this.root = System.getProperty("user.dir");
  }


  @Override
  public void run() {
    try {
      controlIn = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

      controlOutWriter = new PrintWriter(controlSocket.getOutputStream(), true);

      sendResponseToClient(new CommandResponse.Builder().
          withCode(220).withMessage("FTP Server ready").build());

      while (!quitCommandLoop) {
        sendResponseToClient(state.handleCommand(this, controlIn.readLine()));
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        controlIn.close();
        controlOutWriter.close();
        controlSocket.close();

      } catch (IOException e) {
      }
    }

  }

  private void sendResponseToClient(CommandResponse response) {
    controlOutWriter.println(response);
  }

}
