package app;

import config.Config;
import model.J48Classifier;
import socket.InboundSocketServer;
import socket.OutboundSocketClient;
import utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public class Application {

    public static Application instance;

    public J48Classifier classifier;
    public InboundSocketServer server;
    public OutboundSocketClient client;
    public boolean isReady = false;

    public Application() {
        instance = this;
        classifier = new J48Classifier();
    }

    public void Start() throws IOException, InterruptedException {

        Logger.log("Starting application...");

        int port = 8887; // 843 flash policy port
        server = new InboundSocketServer(classifier, port);
        server.start();

        Logger.log("Server started on port: " + server.getPort());

        client = new OutboundSocketClient(URI.create(Config.ENDPOINT));
        Logger.log("Successfully connected to endpoint at: " + Config.ENDPOINT);

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
            server.broadcast(in);
            if (in.equals("exit")) {
                server.stop(1000);
                break;
            }
        }

    }

}
