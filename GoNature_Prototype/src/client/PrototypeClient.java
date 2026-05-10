package client;

import java.io.IOException;

import ocsf.client.AbstractClient;

public class PrototypeClient extends AbstractClient {
	
	public PrototypeClient(String host, int port) {
	    super(host, port);
	}
	@Override
	protected void handleMessageFromServer(Object msg) {
	    System.out.println("Message from server: " + msg);
	}
}
