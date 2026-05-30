package server;

import java.io.IOException;

import common.ClientRequest;
import common.Order;
import common.ServerResponse;

import server.db.OrderDBQueries;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

   // Main server class that handles client requests
   // and communicates with the database

public class PrototypeServer extends AbstractServer {

    private ServerGUI gui;
   //Creates a new server instance
    public PrototypeServer(int port, ServerGUI gui) {
        super(port);
        this.gui = gui;
    }
   //Handles incoming messages from clients
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
   //Loads an order from the database
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
   //Updates an existing order
    private void handleUpdateOrder(ClientRequest request, ConnectionToClient client) {

        boolean updated = OrderDBQueries.updateOrder(
                request.getOrderNumber(),
                request.getOrderDate(),
                request.getNumberOfVisitors()
        );

        try {
            if (updated) {

                Order updatedOrder =
                        OrderDBQueries.getOrderByNumber(request.getOrderNumber());

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
   //Called when the server starts
    @Override
    protected void serverStarted() {
        System.out.println("Server started and listening on port " + getPort());
    }
   //Called when the server stops
    @Override
    protected void serverStopped() {
        System.out.println("Server stopped");
    }
   //Called when a client connects
    @Override
    protected void clientConnected(ConnectionToClient client) {

        String clientIp = client.getInetAddress().getHostAddress();
        String clientHost = client.getInetAddress().getHostName();

        System.out.println("Client connected");
        System.out.println("Client IP: " + clientIp);
        System.out.println("Client Host: " + clientHost);
        System.out.println("Connection Status: Connected");

        if (gui != null) {
            gui.clientConnected(clientIp, clientHost);
        }
    }
   //Called when a client disconnects
    @Override
    synchronized protected void clientDisconnected(ConnectionToClient client) {

        System.out.println("Client disconnected");

        if (gui != null) {
            gui.clientDisconnected();
        }
    }
   //Handles client connection exceptions
    @Override
    synchronized protected void clientException(ConnectionToClient client, Throwable exception) {

        System.out.println("Client disconnected because of exception: " + exception.getMessage());

        if (gui != null) {
            gui.clientDisconnected();
        }
    }
}