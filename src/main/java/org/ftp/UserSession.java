package org.ftp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.ftp.command.CommandResponse;

/**
 * Represents a user session for the FTP server. Handles control and data connections, command
 * processing, and session state management.
 */
public class UserSession extends Thread {

  private static final Logger LOGGER = Logger.getLogger(UserSession.class.getName());
  private final AtomicInteger activeConnections;
  private final AtomicInteger serverConnections;
  @Getter
  private final ConnectionManager connectionManager;
  @Getter
  private final UserCredentials credentials = new UserCredentials();
  private final Socket controlSocket;
  @Getter
  private final int dataPort;
  @Getter
  @Setter
  private TransferType transferType = TransferType.BINARY;
  @Getter
  @Setter
  private int currDirectoryId;
  private BufferedReader controlIn;
  private ServerSocket dataSocket;
  @Getter
  private Socket dataConnection;
  private PrintWriter controlOutWriter;
  @Setter
  private SessionState state;
  @Setter
  private boolean isRunning = false;

  /**
   * Constructs a UserSession object.
   *
   * @param controlSocket the control socket for this session
   * @param dataPort      the data port assigned to this session
   */
  public UserSession(Socket controlSocket, int dataPort, AtomicInteger activeConnections,
      ConnectionManager connectionManager, AtomicInteger maxConnections) {
    this.controlSocket = controlSocket;
    this.dataPort = dataPort;
    this.state = new NotLoggedState();
    this.activeConnections = activeConnections;
    this.connectionManager = connectionManager;
    this.serverConnections = maxConnections;
  }


  @Override
  public void run() {
    try {
      initializeSession();
      sendResponseToClient(new CommandResponse.Builder().
          withCode(220).withMessage("FTP Server ready").build());
      System.out.println("ACTIVE CONNECTIONS: " + activeConnections.get());
      while (!isRunning) {
        sendResponseToClient(state.handleCommand(this, controlIn.readLine()));
      }
    } catch (SocketException e){
      LOGGER.log(Level.INFO, "Client closed data connection");
    }
    catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error in user session");
    } finally {
      cleanupResources();
      activeConnections.decrementAndGet();
      if (credentials.getStatus() == UserStatus.LOGGEDIN) {
        connectionManager.removeConnection(credentials.getUsername());
      }
    }
  }

  /**
   * Initializes the session by setting up input/output streams.
   *
   * @throws IOException if an error occurs while setting up streams
   */
  private void initializeSession() throws IOException {
    controlIn = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
    controlOutWriter = new PrintWriter(controlSocket.getOutputStream(), true);
    LOGGER.info("Session initialized for client: " + controlSocket.getRemoteSocketAddress());
  }

  /**
   * Cleans up resources used by the session.
   */
  private void cleanupResources() {
    try {
      if (controlIn != null) {
        controlIn.close();
      }
      if (controlOutWriter != null) {
        controlOutWriter.close();
      }
      if (controlSocket != null && !controlSocket.isClosed()) {
        controlSocket.close();
      }
      if (dataConnection != null && !dataConnection.isClosed()) {
        dataConnection.close();
      }
      if (dataSocket != null && !dataSocket.isClosed()) {
        dataSocket.close();
      }
      LOGGER.info(
          "Session resources cleaned up");
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Error during resource cleanup", e);
    }
  }

  public void openDataConnectionPassive() {
    try {
      dataSocket = new ServerSocket(dataPort);
      dataConnection = dataSocket.accept();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error opening data connection", e);
    }
  }
  /**
   * Sends an intermediate message to the client during command execution.
   *
   * @param message The message to send to the client.
   */
  public void sendIntermediateMessageToClient(CommandResponse message) {
    if (controlOutWriter != null) {
      controlOutWriter.println(message);
    } else {
      LOGGER.warning("Control output writer is not initialized.");
    }
  }
  public void updateServerConnections(int connections) {
    serverConnections.set(connections);
  }
  public void openDataConnectionActive(String ipAddress, int port) {
    try {
      System.out.println("OPENING DATA CONNECTION " + ipAddress + " on port " + port);
      dataConnection = new Socket(ipAddress, port);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error opening data connection", e);
    }
  }

  public void closeDataConnection() {
    try {
      dataConnection.close();
      if (dataSocket != null) {
        dataSocket.close();
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error closing data connection", e);
    }
    dataConnection = null;
    dataSocket = null;
  }
  /**
   * Checks if the data connection is currently open.
   *
   * @return true if the data connection is open, false otherwise.
   */
  public boolean isDataConnectionOpen() {
    return dataConnection != null && dataConnection.isConnected() && !dataConnection.isClosed();
  }

  private void sendResponseToClient(CommandResponse response) {
    controlOutWriter.println(response);
  }
}
