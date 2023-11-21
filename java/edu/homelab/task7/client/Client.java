package edu.homelab.task7.client;

import java.io.*;
import java.net.*;

public class Client extends Thread {

  private final Socket clientSocket;
  private PrintWriter outputToServer;
  private BufferedReader inputFromUser;
  private boolean running;

  public Client(String ipAddress, int port) throws IOException {
    this.clientSocket = new Socket(ipAddress, port);
    this.running = true;
  }

  @Override
  public void run() {
    try {
      // Establish input and output streams
      inputFromUser = new BufferedReader(new InputStreamReader(System.in));
      outputToServer = new PrintWriter(clientSocket.getOutputStream(), true);

      // Start listening to messages from the server
      Thread serverListener = new ServerListener(clientSocket);
      serverListener.start();

      System.out.println("Welcome to chat!");
      System.out.println("Type 'quit' to disconnect.");
      System.out.println("Type '@username <message>' to send a private message.");
      System.out.print("Enter username: ");

      String message = inputFromUser.readLine();
      outputToServer.println("@username: " + message);

      while (running && !clientSocket.isClosed()) {
        message = inputFromUser.readLine();

        if ("quit".equalsIgnoreCase(message)) {
          disconnect();
          continue;
        }

        if (!message.trim().isEmpty()) {
          sendMessage(message);
        }
      }
    } catch (IOException e) {
      System.out.println("Error: " + e.getMessage());
    } finally {
      cleanup();
    }
  }

  private void sendMessage(String message) {
    outputToServer.println(message);
  }

  private void disconnect() {
    running = false;
    cleanup();
  }

  private void cleanup() {
    try {
      if (inputFromUser != null) inputFromUser.close();
      if (outputToServer != null) outputToServer.close();
      if (clientSocket != null) clientSocket.close();
    } catch (IOException e) {
      System.out.println("Error closing resources: " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    try {
      Client client = new Client("localhost", 8080);
      client.start();
    } catch (IOException e) {
      System.out.println("Unable to start client: " + e.getMessage());
    }
  }
}
