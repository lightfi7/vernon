package socket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import utils.EventHandler;

/**
 * This example demonstrates how to create a websocket connection to a server. Only the most
 * important callbacks are overloaded.
 */

public class OutboundSocketClient extends WebSocketClient {

    EventHandler dataHandler;

    public OutboundSocketClient(URI serverUri, EventHandler dataHandler, Draft draft) {
        super(serverUri, draft);
        this.dataHandler = dataHandler;
    }

    public OutboundSocketClient(URI serverURI, EventHandler dataHandler) {
        super(serverURI);
        this.dataHandler = dataHandler;
    }

    public OutboundSocketClient(URI serverUri, Map<String, String> httpHeaders, EventHandler dataHandler) {
        super(serverUri, httpHeaders);
        this.dataHandler = dataHandler;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send("Hello, it is me. Mario :)");
        System.out.println("opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage(String message) {
        dataHandler.onDataReceived(message);
        System.out.println("received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The close codes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                        + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }


}