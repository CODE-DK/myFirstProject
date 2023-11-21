package edu.homelab.task7.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientListener {

  private final UUID id = UUID.randomUUID();
  private final Socket client;
  private final List<ClientListener> clients;

  private BufferedReader in;
  private PrintWriter out;
  private String username;

  public ClientListener(Socket client, List<ClientListener> clients) {
    this.client = client;
    this.clients = clients;
  }

  public UUID getId() {
    return id;
  }

  public void connect() {
    try {
      in = new BufferedReader(new InputStreamReader(client.getInputStream()));
      out = new PrintWriter(client.getOutputStream(), true);

      while (!client.isClosed()) {
        var message = in.readLine();

        // Stop connection if client sends "quit" message
        if (message.equals("quit")) {
          disconnect();
          continue;
        }

        // Set username if client sends "@username: " message
        if (username == null && message.startsWith("@username: ")) {
          username = message.substring(11);
          out.println("Client " + id + " set username: " + username);
          sendMessage(username, "joined the chat");
          continue;
        }

        clients.stream()
          .filter(client -> !client.getId().equals(id))
          .forEach(client -> client.sendMessage(username, message));
      }
    } catch (IOException e) {
      out.println("Error during startConnection(): " + e.getMessage());
    }
  }

  public void sendMessage(String username, String message) {
    out.println("@" + username + ": " + message);
  }

  public void disconnect() {
    try {
      in.close();
      out.close();
      client.close();
    } catch (IOException e) {
      // Ignore exception
    }
  }
}
