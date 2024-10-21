package socket;

import java.io.*;
import utils.EventHandler;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * This example demonstrates how to create a basic TCP connection to a server,
 * with automatic reconnection logic when the connection is lost.
 */
public class OutboundTcpSocketClient {

    private EventHandler dataHandler;
    private Socket socket;
    private InputStream input;
    private PrintWriter output;
    private Thread thread;
    private URI serverUri;
    private volatile boolean isRunning = true; // Flag to control the connection attempts

    public OutboundTcpSocketClient(URI serverUri, EventHandler dataHandler) {
        this.serverUri = serverUri;
        this.dataHandler = dataHandler;
        connect();
    }

    private void connect() {
        String host = serverUri.getHost();
        int port = serverUri.getPort();

        while (isRunning) {
            try {
                this.socket = new Socket(host, port);
                this.input = socket.getInputStream();
                this.output = new PrintWriter(socket.getOutputStream(), true);
                System.out.println("Connected to server");

                this.thread = new Thread(this::listenForMessages);
                this.thread.start();
                break; // Exit loop once connected

            } catch (IOException e) {
                System.out.println("Connection failed, retrying in 5 seconds...");
                try {
                    Thread.sleep(5000); // Wait for 5 seconds before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.out.println("Reconnection thread interrupted");
                    return;
                }
            }
        }
    }

    public void send(String message) {
        if (output != null) {
            output.println(message);
        } else {
            System.out.println("Cannot send message, no active connection");
        }
    }

    public void close() {
        isRunning = false; // Stop reconnection attempts
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        byte[] buffer = new byte[1024];
        int bytesRead;
        try {
            while ((bytesRead = input.read(buffer)) != -1) {
                String message = new String(buffer, 0, bytesRead);
                dataHandler.onDataReceived(message);
                System.out.println("Received: " + message);
            }
        } catch (IOException e) {
            System.out.println("Connection lost, attempting to reconnect...");
            reconnect(); // Call reconnect when connection is lost
        } finally {
            close();
            reconnect(); // Call reconnect when connection is lost
        }
    }

    private void reconnect() {
        close(); // Ensure the previous connection is closed
        connect(); // Attempt to reconnect
    }
}
