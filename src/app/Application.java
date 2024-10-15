package app;

import config.Config;
import dialog.MainDialog;
import model.J48Classifier;
import socket.InboundSocketServer;
import socket.OutboundSocketClient;
import socket.OutboundTcpSocketClient;
import utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public class Application {

    public static Application instance;

    public J48Classifier classifier;
    public InboundSocketServer server;
//    public OutboundSocketClient client;
    public OutboundTcpSocketClient client;
    public MainDialog mainDialog = new MainDialog();
    public boolean isReady = false;


    public Application() {
        instance = this;
        mainDialog.setVisible(true);
        classifier = new J48Classifier();
    }

    public void setIsReady(boolean ready){
        isReady = ready;
        MainDialog.instance.startButton.setEnabled(ready);
    }

    public void start() throws IOException, InterruptedException {
        Logger.log("Press the button to start");
        setIsReady(true);
    }

    public void run() throws IOException, InterruptedException {

//        int port = Config.PORT; // 843 flash policy port
//        server = new InboundSocketServer(classifier, port);
//        server.start();
//
//        Logger.log("Server started on port: " + server.getPort());

//        client = new OutboundSocketClient(URI.create(Config.ENDPOINT), classifier);
//        Logger.log("Try to connect to endpoint at: " + Config.ENDPOINT);

        client = new OutboundTcpSocketClient(URI.create(Config.ENDPOINT), classifier);
        Logger.log("Try to connect to endpoint at: " + Config.ENDPOINT);

//        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
//        while (true) {
//            String in = sysin.readLine();
//            System.out.println(in);
////            server.broadcast(in);
//            if (in.equals("exit")) {
////                server.stop(1000);
//                break;
//            }
//        }
    }
}
