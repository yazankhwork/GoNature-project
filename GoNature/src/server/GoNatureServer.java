package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import common.Message;
import common.Booking;

public class GoNatureServer {
    private static final int PORT = 5555;
    private static DatabaseController dbController = new DatabaseController();
    private static ServerSocket serverSocket;
    private static boolean isRunning = false;

    public static void startServer() {
        isRunning = true;
        dbController.connectToDatabase();
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running on port " + PORT);
            while (isRunning) {
                Socket socket = serverSocket.accept();
                handleClient(socket);
            }
        } catch (SocketException se) { System.out.println("Server stopped."); } 
          catch (IOException e) { e.printStackTrace(); }
    }

    public static void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) { serverSocket.close(); }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void handleClient(Socket socket) {
        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            Message clientMsg = (Message) input.readObject();
            String command = clientMsg.getCommand();

            switch (command) {
                case "CONNECT": output.writeObject(new Message("CONNECTED", null)); break;
                    
                case "LOGIN":
                    @SuppressWarnings("unchecked")
                    ArrayList<String> loginData = (ArrayList<String>) clientMsg.getData();
                    output.writeObject(new Message(dbController.loginVisitor(loginData.get(0), loginData.get(1)), null));
                    break;
                    
                case "REGISTER":
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> regData = (ArrayList<Object>) clientMsg.getData();
                    output.writeObject(new Message(dbController.registerVisitor((String)regData.get(0), (String)regData.get(1), (boolean)regData.get(2)), null));
                    break;

                case "ADD_DATA":
                    Booking newB = (Booking) clientMsg.getData();
                    if (newB.getVisitorsCount() > 15) {
                        output.writeObject(new Message("LIMIT_REACHED", "Error: A single booking cannot exceed 15 visitors."));
                    } else {
                        boolean saved = dbController.saveBooking(newB);
                        int price = newB.getVisitorsCount() * 30;
                        output.writeObject(new Message(saved ? "SUCCESS_PAID" : "FAILED", "Order approved. Total Paid: " + price + " ILS"));
                    }
                    break;
                    
                case "LOAD_DATA":
                    output.writeObject(new Message("SUCCESS", dbController.getUserBookings((String) clientMsg.getData())));
                    break;
                    
                case "UPDATE_DATA":
                    output.writeObject(new Message(dbController.updateBooking((Booking) clientMsg.getData()) ? "SUCCESS" : "FAILED", null));
                    break;
                    
                case "CANCEL_DATA":
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> deleteData = (ArrayList<Object>) clientMsg.getData();
                    int refund = dbController.cancelBooking((int)deleteData.get(0), (String)deleteData.get(1));
                    if (refund > 0) output.writeObject(new Message("CANCELLED_REFUND", "Booking Cancelled. Refund: " + refund + " ILS."));
                    else if (refund == 0) output.writeObject(new Message("CANCELLED_NO_REFUND", "Booking Cancelled."));
                    else output.writeObject(new Message("FAILED", "Could not cancel booking."));
                    break;
            }
            output.flush(); socket.close();
        } catch (Exception e) { System.err.println("Client handling error."); }
    }
}