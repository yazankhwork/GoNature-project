package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.time.LocalTime;
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
                    // התאמה לפונקציה החדשה שמחזירה מערך
                    String[] loginRes = dbController.loginVisitor(loginData.get(0), loginData.get(1));
                    output.writeObject(new Message(loginRes[0], loginRes[1]));
                    break;
                    
                case "REGISTER":
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> regData = (ArrayList<Object>) clientMsg.getData();
                    // העברת השם המלא מהלקוח למסד הנתונים
                    output.writeObject(new Message(dbController.registerVisitor((String)regData.get(0), (String)regData.get(1), (boolean)regData.get(2), (String)regData.get(3)), null));
                    break;

                case "GET_AVAILABLE_SPOTS":
                    Booking bSpots = (Booking) clientMsg.getData();
                    int currentInPark = dbController.countVisitorsAt(bSpots.getParkName(), bSpots.getVisitDate(), bSpots.getVisitTime());
                    int emptyTickets = 150 - currentInPark;
                    if (emptyTickets < 0) emptyTickets = 0;
                    output.writeObject(new Message("AVAILABLE_SPOTS_RESPONSE", emptyTickets));
                    break;

                case "CHECK_AVAILABILITY":
                    Booking checkB = (Booking) clientMsg.getData();
                    int current = dbController.countVisitorsAt(checkB.getParkName(), checkB.getVisitDate(), checkB.getVisitTime());

                    if (current + checkB.getVisitorsCount() <= 150) {
                        output.writeObject(new Message("OK", null));
                    } else {
                        LocalTime reqTime = checkB.getVisitTime();
                        LocalTime before = reqTime.minusHours(1);
                        LocalTime Thread = reqTime.plusHours(1);

                        boolean validBefore = !before.isBefore(LocalTime.of(8, 0));
                        boolean validAfter = !Thread.isAfter(LocalTime.of(18, 0));

                        int countBefore = validBefore ? dbController.countVisitorsAt(checkB.getParkName(), checkB.getVisitDate(), before) : 999;
                        int countAfter = validAfter ? dbController.countVisitorsAt(checkB.getParkName(), checkB.getVisitDate(), Thread) : 999;

                        boolean canBookBefore = validBefore && (countBefore + checkB.getVisitorsCount() <= 150);
                        boolean canBookAfter = validAfter && (countAfter + checkB.getVisitorsCount() <= 150);

                        String msg = "Park is full at " + reqTime + ".";
                        
                        if (canBookBefore && canBookAfter) {
                            msg += "\nTry " + before + " or " + Thread;
                        } else if (canBookBefore) {
                            msg += "\nTry " + before;
                        } else if (canBookAfter) {
                            msg += "\nTry " + Thread;
                        } else {
                            msg = "FULL";
                        }

                        output.writeObject(new Message(msg.equals("FULL") ? "FULL" : "SUGGESTION", msg));
                    }
                    break;

                case "CLAIM_WAITING_SPOTS":
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> claimData = (ArrayList<Object>) clientMsg.getData();
                    int bId = (int) claimData.get(0);
                    int spotsToTake = (int) claimData.get(1);
                    boolean successClaim = dbController.claimWaitingSpots(bId, spotsToTake);
                    output.writeObject(new Message(successClaim ? "SUCCESS_PAID" : "FAILED", "Waiting list order updated and processed successfully!"));
                    break;

                case "ADD_DATA":
                    Booking newB = (Booking) clientMsg.getData();
                    if (newB.getVisitorsCount() > 15) {
                        output.writeObject(new Message("LIMIT_REACHED", "Error: A single booking cannot exceed 15 visitors."));
                    } else {
                        boolean saved = dbController.saveBooking(newB);
                        int price = newB.getVisitorsCount() * 30;
                        if ("Waiting List".equals(newB.getStatus())) {
                            output.writeObject(new Message("LIMIT_REACHED", "Successfully joined the Waiting List."));
                        } else {
                            output.writeObject(new Message(saved ? "SUCCESS_PAID" : "FAILED", "Order approved. Total Paid: " + price + " ILS"));
                        }
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