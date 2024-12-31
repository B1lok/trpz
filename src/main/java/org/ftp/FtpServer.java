package org.ftp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ftp.command.CommandResponse;

public class FtpServer {

  private static final Logger LOGGER = Logger.getLogger(FtpServer.class.getName());

  private static final int CONTROL_PORT = 21;
  private static final int START_DATA_PORT = 1024;
  private static final int THREAD_POOL_SIZE = 10;
  private static final AtomicInteger MAX_CONNECTIONS = new AtomicInteger(10);
  private final AtomicInteger activeConnections = new AtomicInteger(0);
  private final ConnectionManager connectionManager = new ConnectionManager();

  private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
  private ServerSocket welcomeSocket;
  private volatile boolean serverRunning = true;

  public static void main(String[] args) {
    StartupInitializer.initializeServer();
    MAX_CONNECTIONS.set(StartupInitializer.getMaxConnections().orElse(10));
    FtpServer ftpServer = new FtpServer();
    ftpServer.start();
  }

  /**
   * Starts the FTP server and begins accepting client connections.
   */
  public void start() {
    try {
      welcomeSocket = new ServerSocket(CONTROL_PORT);
      LOGGER.info("FTP Server started. Listening on port " + CONTROL_PORT);
      handleClientConnections();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not create server socket", e);
      stop();
    }
  }

  /**
   * Handles incoming client connections in a loop until the server is stopped.
   */
  private void handleClientConnections() {
    while (serverRunning) {
      try {
        Socket clientSocket = welcomeSocket.accept();
        if (activeConnections.get() < MAX_CONNECTIONS.get()) {
          LOGGER.info("New connection received. Starting new session.");
          UserSession session = new UserSession(clientSocket,
              START_DATA_PORT + activeConnections.get() + 1, activeConnections, connectionManager, MAX_CONNECTIONS);
          session.start();
          activeConnections.incrementAndGet();
        } else {
          LOGGER.warning("Connection rejected: Maximum connections reached.");
          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
          out.println(new CommandResponse.Builder()
              .withCode(421)
              .withMessage("Maximum number of connections reached.")
              .build());
          clientSocket.close();
        }
      } catch (IOException e) {
        if (serverRunning) {
          LOGGER.log(Level.WARNING, "Exception while accepting client connection", e);
        }
      }
    }
    stop();
  }

  /**
   * Stops the FTP server and releases resources.
   */
  public void stop() {
    serverRunning = false;
    threadPool.shutdown();
    try {
      if (welcomeSocket != null && !welcomeSocket.isClosed()) {
        welcomeSocket.close();
      }
      LOGGER.info("Server stopped.");
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Problem stopping the server", e);
    }
  }
}