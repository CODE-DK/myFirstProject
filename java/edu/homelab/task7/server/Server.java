package edu.homelab.task7.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server extends Thread {

  private final ServerSocket serverSocket;
  private final List<ClientListener> clientListeners;
  private volatile boolean running;

  public Server(int port) throws IOException {
    this.serverSocket = new ServerSocket(port);
    this.clientListeners = new CopyOnWriteArrayList<>();
    this.running = true;
  }

  @Override
  public void run() {
    System.out.println("Server is ready...");

    while (running && !serverSocket.isClosed()) {
      try {
        Socket clientSocket = serverSocket.accept();
        ClientListener clientListener = new ClientListener(clientSocket, clientListeners);
        clientListeners.add(clientListener);
        clientListener.start();
      } catch (IOException e) {
        if (running) {
          System.out.println("Error accepting client connection: " + e.getMessage());
        } else {
          System.out.println("Server stopped accepting new connections.");
        }
      }
    }
  }

  public void stopServer() {
    running = false;
    clientListeners.forEach(ClientListener::disconnect);
    clientListeners.clear();

    try {
      serverSocket.close();
    } catch (IOException e) {
      System.out.println("Error closing server: " + e.getMessage());
    }
  }

  public boolean hasActiveConnections() {
    return !clientListeners.isEmpty();
  }

  public static void main(String[] args) {
    try {
      Server server = new Server(8080);
      server.start();

      // Monitor and disconnect server when no active clients
      while (true) {
        Thread.sleep(30000);
        if (!server.hasActiveConnections()) {
          server.stopServer();
          break;
        }
      }
    } catch (IOException | InterruptedException e) {
      System.out.println("Server error: " + e.getMessage());
    }
  }
}
