package server;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class PrototypeServer extends AbstractServer{
	public PrototypeServer(int port) {
	    super(port);
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void serverStarted() {
	    System.out.println("Server started and listening on port " + getPort());
	}
	@Override
	protected void clientConnected(ConnectionToClient client) {
	    System.out.println("Client connected");
	    System.out.println("Client IP: " + client.getInetAddress().getHostAddress());
	    System.out.println("Client Host: " + client.getInetAddress().getHostName());
	    System.out.println("Connection Status: Connected");
	}
}
