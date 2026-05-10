package server;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class PrototypeServer extends AbstractServer{
	private ServerGUI gui;
	public PrototypeServer(int port, ServerGUI gui) {
	    super(port);
	    this.gui = gui;
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
		String clientIp = client.getInetAddress().getHostAddress();
		String clientHost = client.getInetAddress().getHostName();
		System.out.println("Client connected");
	    System.out.println("Client IP: " + clientIp);
	    System.out.println("Client Host: " + clientHost);
	    System.out.println("Connection Status: Connected");

	    if (gui != null) {
	        gui.updateClientInfo(clientIp, clientHost);
	    }
	}
}
