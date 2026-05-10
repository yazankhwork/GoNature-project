package server;

import java.io.IOException;
import common.ClientRequest;
import common.Order;
import common.ServerResponse;
import server.db.OrderDBQueries;
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

	    if (msg instanceof ClientRequest) {
	        ClientRequest request = (ClientRequest) msg;

	        System.out.println("Request received: " + request.getRequestType());
	        System.out.println("Order number: " + request.getOrderNumber());

	        if (request.getRequestType().equals("LOAD_ORDER")) {
	            handleLoadOrder(request, client);
	        } else if (request.getRequestType().equals("UPDATE_ORDER")) {
	            handleUpdateOrder(request, client);
	        }
	    }
	}
	private void handleLoadOrder(ClientRequest request, ConnectionToClient client) {
	    Order order = OrderDBQueries.getOrderByNumber(request.getOrderNumber());

	    try {
	        if (order != null) {
	            ServerResponse response =
	                new ServerResponse(true, "Order loaded successfully", order);

	            client.sendToClient(response);
	        } else {
	            ServerResponse response =
	                new ServerResponse(false, "Order not found");

	            client.sendToClient(response);
	        }
	    } catch (IOException e) {
	        System.out.println("Failed to send response to client: " + e.getMessage());
	    }
	}
	private void handleUpdateOrder(ClientRequest request, ConnectionToClient client) {
	    boolean updated = OrderDBQueries.updateOrder(
	        request.getOrderNumber(),
	        request.getOrderDate(),
	        request.getNumberOfVisitors()
	    );

	    try {
	        if (updated) {
	            Order updatedOrder = OrderDBQueries.getOrderByNumber(request.getOrderNumber());

	            ServerResponse response =
	                new ServerResponse(true, "Order updated successfully", updatedOrder);

	            client.sendToClient(response);
	        } else {
	            ServerResponse response =
	                new ServerResponse(false, "Order not found or update failed");

	            client.sendToClient(response);
	        }
	    } catch (IOException e) {
	        System.out.println("Failed to send response to client: " + e.getMessage());
	    }
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
