package edu.homelab.task7.server;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.lang.System.*;

public class Server extends Thread {

  private final UUID id = UUID.randomUUID();
  private final ServerSocket server;
  private final List<ClientListener> clients = new ArrayList<>();

  public Server(int port) throws IOException {
    this.server = new ServerSocket(port);
  }

  public void run() {
    try {
      out.println("Server " + id + " started on port " + server.getLocalPort());
      while (!server.isClosed()) {
        var client = server.accept();
        var clientListener = new ClientListener(client, clients);
        clientListener.connect();
        clients.add(clientListener);
      }
    } catch (IOException ignored) {
      // Ignore exception
    }
  }

  public void disconnect() {
    try {
      server.close();
      out.println("Server " + id + " stopped");
    } catch (IOException ignored) {
      // Ignore exception
    }
  }

  public boolean isActive() {
    return !clients.isEmpty();
  }

  public static void main(String[] args) throws InterruptedException, IOException {
    var server = new Server(8080);
    server.start();

    // If no active clients, disconnect server
    boolean disconnect = false;
    while (!disconnect) {
      Thread.sleep(30000);
      disconnect = !server.isActive();
    }

    server.disconnect();
  }
}
