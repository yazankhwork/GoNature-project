package client;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalTime;
import common.Message;
import common.Booking;

/**
 * The Client class that connects to the GoNature server.
 * It sends Message objects (like booking requests) over TCP/IP.
 */
public class GoNatureClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 5555;

        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Connected to the server!");

            // IMPORTANT: In Java, ALWAYS create ObjectOutputStream first and flush it!
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            // 1. Create a sample Booking object (using your ID from the Excel sheet)
            Booking myBooking = new Booking(0, "212702559", "Carmel Park", 
                                            LocalDate.now(), LocalTime.of(10, 0), 5, "Pending");

            // 2. Package it inside a Message
            Message requestMsg = new Message("CREATE_BOOKING", myBooking);

            // 3. Send the object to the server
            System.out.println("Sending booking request to server...");
            output.writeObject(requestMsg);
            output.flush();

            // 4. Wait for the server's response
            Message responseMsg = (Message) input.readObject();
            System.out.println("Server replied with command: " + responseMsg.getCommand());
            
            // 5. Check what happened to our booking
            if (responseMsg.getData() instanceof Booking) {
                Booking returnedBooking = (Booking) responseMsg.getData();
                System.out.println("Success! The booking status is now: " + returnedBooking.getStatus());
            }

        } catch (Exception ex) {
            System.out.println("Client Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}