package socket;

import utils.EventHandler;

import java.io.*;
import java.net.Socket;
import java.net.URI;

/**
 * This example demonstrates how to create a basic TCP connection to a server.
 */

public class OutboundTcpSocketClient {

    private EventHandler dataHandler;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public OutboundTcpSocketClient(URI serverUri, EventHandler dataHandler) throws IOException {
        this.dataHandler = dataHandler;
        String host = serverUri.getHost();
        int port = serverUri.getPort();
        this.socket = new Socket(host, port);
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    public void connect() {
        try {
            send("Hello :)");
            System.out.println("Opened connection to " + socket.getRemoteSocketAddress());

            // Start a thread to listen for incoming messages
            new Thread(this::listenForMessages).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        output.println(message);
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
            System.out.println("Connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForMessages() {
        String message;
        try {
            while ((message = input.readLine()) != null) {
                dataHandler.onDataReceived(message);
                System.out.println("Received: " + message);
            }
        } catch (IOException e) {
            System.out.println("Connection closed unexpectedly");
            e.printStackTrace();
        } finally {
            close();
        }
    }
}
