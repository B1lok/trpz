package org.ftp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.Getter;
import org.ftp.command.CommandResponse;
import org.ftp.memento.Memento;
import org.ftp.memento.SnapshotHistory;

public class UserSession extends Thread {

  private Socket controlSocket;
  private SessionState state;
  private ServerSocket dataSocket;
  private BufferedReader controlIn;
  private PrintWriter controlOutWriter;
  private int dataPort;
  private String root;
  @Getter
  private String currDirectory;
  private boolean quitCommandLoop = false;

  private final SnapshotHistory snapshotHistory;
  public UserSession(Socket controlSocket, int dataPort) {
    this.controlSocket = controlSocket;
    this.dataPort = dataPort;
    this.currDirectory = System.getProperty("user.dir") + "/server";
    this.root = System.getProperty("user.dir");
    this.state = new NotLoggedState();
    this.snapshotHistory = new SnapshotHistory();
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

  public void setCurrDirectory(String currDirectory) {
    this.currDirectory = currDirectory;
  }

  public void setQuitCommandLoop(boolean quitCommandLoop) {
    this.quitCommandLoop = quitCommandLoop;
  }

  public void saveSnapshot() {
    snapshotHistory.saveSnapshot(new SessionSnapshot(currDirectory, state));
  }

  public void restoreState() {
    Memento snapshot = snapshotHistory.restoreState();
    if (snapshot != null) {
      currDirectory = snapshot.getCurrDirectory();
      state = snapshot.getState();
    }
  }

  private void sendResponseToClient(CommandResponse response) {
    controlOutWriter.println(response);
  }
  public static class SessionSnapshot implements Memento {
    private final String currDirectory;
    private final SessionState state;

    public SessionSnapshot(String currDirectory, SessionState state) {
      this.currDirectory = currDirectory;
      this.state = state;
    }

    @Override
    public String getCurrDirectory() {
      return currDirectory;
    }

    @Override
    public SessionState getState() {
      return state;
    }
  }

}
