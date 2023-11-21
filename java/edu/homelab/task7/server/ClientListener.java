package edu.homelab.task7.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientListener extends Thread {

  private final UUID clientId;
  private final Socket clientSocket;
  private final List<ClientListener> clients;

  private BufferedReader inputFromClient;
  private PrintWriter outputToClient;
  private String username;

  public ClientListener(Socket clientSocket, List<ClientListener> clients) {
    this.clientId = UUID.randomUUID();
    this.clientSocket = clientSocket;
    this.clients = clients;
  }

  @Override
  public void run() {
    try {
      inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      outputToClient = new PrintWriter(clientSocket.getOutputStream(), true);

      String message;
      while ((message = inputFromClient.readLine()) != null) {
        handleClientMessage(message);
      }
    } catch (IOException e) {
      System.out.println("Error handling client " + clientId + ": " + e.getMessage());
    } finally {
      disconnect();
    }
  }

  private void handleClientMessage(String message) {
    if (username == null && message.startsWith("@username: ")) {
      setUsername(message);
      return;
    }

    if (username != null && message.startsWith("@")) {
      sendPrivateMessage(message);
      return;
    }

    broadcastMessage(message);
  }

  private void setUsername(String message) {
    username = message.substring(11);
    System.out.printf("Client %s connected! Username: %s%n", clientId, username);
    for (ClientListener clientListener : clients) {
      if (clientListener.username.equals(username)) {
        clientListener.sendMessage(username, "welcome back!");
      } else {
        clientListener.sendMessage(username, "joined the chat!");
      }
    }
  }

  private void sendPrivateMessage(String message) {
    String targetUsername = message.substring(1, message.indexOf(" "));
    String personalMessage = message.substring(message.indexOf(" ") + 1);

    for (ClientListener clientListener : clients) {
      if (clientListener.username.equals(targetUsername) || clientListener.username.equals(username)) {
        clientListener.sendMessage(this.username + " [" + targetUsername + "]", personalMessage);
      }
    }
  }

  private void broadcastMessage(String message) {
    clients.forEach(client -> client.sendMessage(username, message));
  }

  public void sendMessage(String senderUsername, String message) {
    outputToClient.println("@" + senderUsername + ": " + message);
  }

  public void disconnect() {
    try {
      inputFromClient.close();
      outputToClient.close();
      clientSocket.close();
      clients.remove(this);
      clients.forEach(client -> client.sendMessage(username, "has disconnected!"));
      System.out.println("Client " + clientId + " disconnected!");
    } catch (IOException e) {
      System.out.println("Error disconnecting client " + clientId + ": " + e.getMessage());
    }
  }
}
