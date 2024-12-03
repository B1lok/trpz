package org.ftp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FtpServer {

  private final int controlPort = 21;
  private ServerSocket welcomeSocket;
  private boolean serverRunning = true;
  private int noOfThreads = 0;

  public static void main(String[] args) {
    FtpServer ftpServer = new FtpServer();
    ftpServer.start();
  }

  public void start() {
    try {
      welcomeSocket = new ServerSocket(controlPort);
      System.out.println("FTP Server started. Listening on port " + controlPort);
      handleClientConnections();
    } catch (IOException e) {
      System.out.println("Could not create server socket");
      stop();
    }
  }

  private void handleClientConnections() {
    while (serverRunning) {
      try {
        Socket clientSocket = welcomeSocket.accept();
        int dataPort = controlPort + noOfThreads + 1;
        UserSession userSession = new UserSession(clientSocket, dataPort);
        System.out.println("New connection received. Starting new session.");
        noOfThreads++;
        userSession.start();
      } catch (IOException e) {
        System.out.println("Exception encountered while accepting client connection");
      }
    }
    stop();
  }

  public void stop() {
    try {
      if (welcomeSocket != null && !welcomeSocket.isClosed()) {
        welcomeSocket.close();
        System.out.println("Server stopped.");
      }
    } catch (IOException e) {
      System.out.println("Problem stopping the server");
    }
  }
}