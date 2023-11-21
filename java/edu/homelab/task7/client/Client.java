package edu.homelab.task7.client;

import java.io.*;
import java.net.*;

public class Client {

  private final Socket client;
  private PrintWriter out;
  private BufferedReader in;

  public Client() throws IOException {
    this("localhost", 8080);
  }

  public Client(String ip, int port) throws IOException {
    this.client = new Socket(ip, port);
  }

  public void connect() {
    try {
      in = new BufferedReader(new InputStreamReader(client.getInputStream()));
      out = new PrintWriter(client.getOutputStream(), true);

      while (!client.isClosed()) {
        var message = in.readLine();
        System.out.println(message);
      }
    } catch (IOException e) {
      // Ignore exception
    }
  }

  public void sendMessage(String message) {
    out.println(message);
  }

  public void disconnect() {
    try {
      in.close();
      out.close();
      client.close();
    } catch (IOException ignored) {
      // Ignore exception
    }
  }
}
