package socket;

import java.io.*;
import utils.EventHandler;
import java.net.Socket;
import java.net.URI;

/**
 * This example demonstrates how to create a basic TCP connection to a server.
 */
public class OutboundTcpSocketClient {

    private EventHandler dataHandler;
    private Socket socket;
    private InputStream input;
    private PrintWriter output;
    private Thread thread;

    public OutboundTcpSocketClient(URI serverUri, EventHandler dataHandler) throws IOException {
        this.dataHandler = dataHandler;
        String host = serverUri.getHost();
        int port = serverUri.getPort();
        this.socket = new Socket(host, port);
        this.input = socket.getInputStream();
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.thread = new Thread(this::listenForMessages);
        this.thread.start();
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
        byte[] buffer = new byte[1024];
        int bytesRead;
        try {
            while ((bytesRead = input.read(buffer)) != -1) {
                String message = new String(buffer, 0, bytesRead);
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