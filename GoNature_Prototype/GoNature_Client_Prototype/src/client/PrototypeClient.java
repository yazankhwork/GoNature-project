package client;

import common.ClientRequest;
import common.ServerResponse;
import ocsf.client.AbstractClient;

/**
 * PrototypeClient handles the communication between the client GUI and the server.
 * It sends requests to the server and receives responses from it.
 */
public class PrototypeClient extends AbstractClient {
	
    private ClientGUI gui;

    /**
     * Creates a client connection object.
     *
     * @param host the server IP address
     * @param port the server port number
     * @param gui the client GUI that will be updated after server responses
     */
    public PrototypeClient(String host, int port, ClientGUI gui) {
        super(host, port);
        this.gui = gui;
    }
	
    /**
     * Handles messages received from the server.
     * If the message is a ServerResponse, it updates the GUI status
     * and displays the order details if the request was successful.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof ServerResponse) {
            ServerResponse response = (ServerResponse) msg;

            System.out.println("Response from server: " + response.getMessage());

            if (gui != null) {
                gui.showStatus("Status: " + response.getMessage());

                if (response.isSuccess() && response.getOrder() != null) {
                    gui.displayOrder(response.getOrder());
                }
            }
        } else {
            System.out.println("Message from server: " + msg);
        }
    }

    /**
     * Sends a client request to the server.
     * If sending fails, it shows an error message in the GUI.
     */
    public void sendRequest(ClientRequest request) {
        try {
            sendToServer(request);
        } catch (Exception e) {
            System.out.println("Failed to send request: " + e.getMessage());

            if (gui != null) {
                gui.showStatus("Status: Failed to send request");
            }
        }
    }
}