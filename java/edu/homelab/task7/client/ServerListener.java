package edu.homelab.task7.client;

import java.io.*;
import java.net.*;

class ServerListener extends Thread {
  private final Socket clientSocket;

  public ServerListener(Socket socket) {
    this.clientSocket = socket;
  }

  @Override
  public void run() {
    try (BufferedReader serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
      String messageFromServer;
      while ((messageFromServer = serverInput.readLine()) != null) {
        System.out.println(messageFromServer);
      }
    } catch (IOException e) {
      System.out.println("Server listener error: " + e.getMessage());
    }
  }
}
