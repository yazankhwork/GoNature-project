package client;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalTime;

import common.Message;
import common.Booking;

/**
 * Test client for the GoNature system.
 * <p>
 * This class establishes a TCP connection with the GoNature server,
 * creates a sample booking request, sends it to the server, and
 * displays the response returned by the server.
 * </p>
 *
 * The client demonstrates:
 * <ul>
 *     <li>Socket communication.</li>
 *     <li>Object serialization using ObjectOutputStream.</li>
 *     <li>Object deserialization using ObjectInputStream.</li>
 *     <li>Sending Message objects between client and server.</li>
 *     <li>Receiving and processing server responses.</li>
 * </ul>
 *
 * @author Bolos Saad
 */
public class GoNatureClient {

    /**
     * Entry point of the test client application.
     * <p>
     * Connects to the server, creates a sample booking,
     * sends the booking request, waits for a response,
     * and prints the result to the console.
     * </p>
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        /**
         * Hostname or IP address of the server.
         */
        String hostname = "localhost";

        /**
         * Port number used for communication with the server.
         */
        int port = 5555;

        try (Socket socket = new Socket(hostname, port)) {

            System.out.println("Connected to the server!");

            /**
             * Stream used to send serialized objects to the server.
             */
            ObjectOutputStream output =
                    new ObjectOutputStream(socket.getOutputStream());

            output.flush();

            /**
             * Stream used to receive serialized objects from the server.
             */
            ObjectInputStream input =
                    new ObjectInputStream(socket.getInputStream());

            /**
             * Sample booking object used for testing server communication.
             */
            Booking myBooking =
                    new Booking(
                            0,
                            "212702559",
                            "Carmel Park",
                            LocalDate.now(),
                            LocalTime.of(10, 0),
                            5,
                            "Pending"
                    );

            /**
             * Message object containing the booking request.
             */
            Message requestMsg =
                    new Message(
                            "CREATE_BOOKING",
                            myBooking
                    );

            System.out.println("Sending booking request to server...");

            output.writeObject(requestMsg);
            output.flush();

            /**
             * Response message received from the server.
             */
            Message responseMsg =
                    (Message) input.readObject();

            System.out.println(
                    "Server replied with command: "
                            + responseMsg.getCommand()
            );

            if (responseMsg.getData() instanceof Booking) {

                /**
                 * Booking object returned by the server.
                 */
                Booking returnedBooking =
                        (Booking) responseMsg.getData();

                System.out.println(
                        "Success! The booking status is now: "
                                + returnedBooking.getStatus()
                );
            }

        } catch (Exception ex) {

            System.out.println(
                    "Client Error: "
                            + ex.getMessage()
            );

            ex.printStackTrace();
        }
    }
}