package app;

import model.J48Classifier;
import socket.InboundSocketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Application {

    public void Start() throws IOException, InterruptedException {
        System.out.println("Starting...");

        J48Classifier classifier = new J48Classifier();

        int port = 8887; // 843 flash policy port
        InboundSocketServer s = new InboundSocketServer(classifier, port);

        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());

        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String in = sysin.readLine();
            s.broadcast(in);
            if (in.equals("exit")) {
                s.stop(1000);
                break;
            }
        }

    }

}
