package client;

import common.ClientRequest;
import common.ServerResponse;
import ocsf.client.AbstractClient;

public class PrototypeClient extends AbstractClient {
	
	private ClientGUI gui;
	public PrototypeClient(String host, int port, ClientGUI gui) {
	    super(host, port);
	    this.gui = gui;
	}
	
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
