package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.time.LocalTime;

import common.Message;
import common.Booking;

/**
 * Main server class for the GoNature system.
 * <p>
 * This server listens for incoming client connections,
 * processes client requests, communicates with the
 * database controller, and returns responses to clients.
 * </p>
 *
 * Supported operations include:
 * <ul>
 *     <li>Client connection verification</li>
 *     <li>User login and registration</li>
 *     <li>Booking creation</li>
 *     <li>Booking updates</li>
 *     <li>Booking cancellation</li>
 *     <li>Availability checking</li>
 *     <li>Waiting list management</li>
 *     <li>Booking retrieval</li>
 * </ul>
 *
 * @author Bolos Saad
 */
public class GoNatureServer {

    /**
     * Port used by the server.
     */
    private static final int PORT = 5555;

    /**
     * Database controller used for all database operations.
     */
    private static DatabaseController dbController =
            new DatabaseController();

    /**
     * Server socket used to accept client connections.
     */
    private static ServerSocket serverSocket;

    /**
     * Indicates whether the server is currently running.
     */
    private static boolean isRunning = false;

    /**
     * Starts the GoNature server.
     * <p>
     * Initializes the database connection,
     * creates the server socket,
     * and continuously accepts client connections.
     * </p>
     */
    public static void startServer() {

        isRunning = true;

        dbController.connectToDatabase();

        try {

            serverSocket = new ServerSocket(PORT);

            System.out.println(
                    "Server is running on port " + PORT
            );

            while (isRunning) {

                Socket socket =
                        serverSocket.accept();

                handleClient(socket);
            }

        } catch (SocketException se) {

            System.out.println("Server stopped.");

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * Stops the server and closes the server socket.
     */
    public static void stopServer() {

        isRunning = false;

        try {

            if (serverSocket != null &&
                !serverSocket.isClosed()) {

                serverSocket.close();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    /**
     * Processes a client request.
     * <p>
     * Reads a message from the client,
     * determines the requested command,
     * executes the required business logic,
     * and sends a response back.
     * </p>
     *
     * @param socket connected client socket
     */
    private static void handleClient(Socket socket) {

        try {

            ObjectOutputStream output =
                    new ObjectOutputStream(
                            socket.getOutputStream()
                    );

            output.flush();

            ObjectInputStream input =
                    new ObjectInputStream(
                            socket.getInputStream()
                    );

            Message clientMsg =
                    (Message) input.readObject();

            String command =
                    clientMsg.getCommand();

            switch (command) {

                case "CONNECT":

                    output.writeObject(
                            new Message(
                                    "CONNECTED",
                                    null
                            )
                    );

                    break;

                case "LOGIN":

                    @SuppressWarnings("unchecked")
                    ArrayList<String> loginData =
                            (ArrayList<String>)
                                    clientMsg.getData();

                    String[] loginRes =
                            dbController.loginVisitor(
                                    loginData.get(0),
                                    loginData.get(1)
                            );

                    output.writeObject(
                            new Message(
                                    loginRes[0],
                                    loginRes[1]
                            )
                    );

                    break;

                case "REGISTER":

                    @SuppressWarnings("unchecked")
                    ArrayList<Object> regData =
                            (ArrayList<Object>)
                                    clientMsg.getData();

                    output.writeObject(
                            new Message(
                                    dbController.registerVisitor(
                                            (String) regData.get(0),
                                            (String) regData.get(1),
                                            (boolean) regData.get(2),
                                            (String) regData.get(3)
                                    ),
                                    null
                            )
                    );

                    break;

                case "GET_AVAILABLE_SPOTS":

                    Booking bSpots =
                            (Booking) clientMsg.getData();

                    int currentInPark =
                            dbController.countVisitorsAt(
                                    bSpots.getParkName(),
                                    bSpots.getVisitDate(),
                                    bSpots.getVisitTime()
                            );

                    int emptyTickets =
                            150 - currentInPark;

                    if (emptyTickets < 0) {
                        emptyTickets = 0;
                    }

                    output.writeObject(
                            new Message(
                                    "AVAILABLE_SPOTS_RESPONSE",
                                    emptyTickets
                            )
                    );

                    break;

                case "CHECK_AVAILABILITY":

                    Booking checkB =
                            (Booking) clientMsg.getData();

                    int current =
                            dbController.countVisitorsAt(
                                    checkB.getParkName(),
                                    checkB.getVisitDate(),
                                    checkB.getVisitTime()
                            );

                    if (current +
                            checkB.getVisitorsCount()
                            <= 150) {

                        output.writeObject(
                                new Message(
                                        "OK",
                                        null
                                )
                        );

                    } else {

                        LocalTime reqTime =
                                checkB.getVisitTime();

                        LocalTime before =
                                reqTime.minusHours(1);

                        LocalTime after =
                                reqTime.plusHours(1);

                        boolean validBefore =
                                !before.isBefore(
                                        LocalTime.of(8, 0)
                                );

                        boolean validAfter =
                                !after.isAfter(
                                        LocalTime.of(18, 0)
                                );

                        int countBefore =
                                validBefore
                                        ? dbController.countVisitorsAt(
                                        checkB.getParkName(),
                                        checkB.getVisitDate(),
                                        before
                                )
                                        : 999;

                        int countAfter =
                                validAfter
                                        ? dbController.countVisitorsAt(
                                        checkB.getParkName(),
                                        checkB.getVisitDate(),
                                        after
                                )
                                        : 999;

                        boolean canBookBefore =
                                validBefore &&
                                        (countBefore +
                                                checkB.getVisitorsCount()
                                                <= 150);

                        boolean canBookAfter =
                                validAfter &&
                                        (countAfter +
                                                checkB.getVisitorsCount()
                                                <= 150);

                        String msg =
                                "Park is full at "
                                        + reqTime + ".";

                        if (canBookBefore &&
                                canBookAfter) {

                            msg += "\nTry "
                                    + before
                                    + " or "
                                    + after;

                        } else if (canBookBefore) {

                            msg += "\nTry "
                                    + before;

                        } else if (canBookAfter) {

                            msg += "\nTry "
                                    + after;

                        } else {

                            msg = "FULL";
                        }

                        output.writeObject(
                                new Message(
                                        msg.equals("FULL")
                                                ? "FULL"
                                                : "SUGGESTION",
                                        msg
                                )
                        );
                    }

                    break;

                case "CLAIM_WAITING_SPOTS":

                    @SuppressWarnings("unchecked")
                    ArrayList<Object> claimData =
                            (ArrayList<Object>)
                                    clientMsg.getData();

                    int bId =
                            (int) claimData.get(0);

                    int spotsToTake =
                            (int) claimData.get(1);

                    boolean successClaim =
                            dbController.claimWaitingSpots(
                                    bId,
                                    spotsToTake
                            );

                    output.writeObject(
                            new Message(
                                    successClaim
                                            ? "SUCCESS_PAID"
                                            : "FAILED",
                                    "Waiting list order updated and processed successfully!"
                            )
                    );

                    break;

                case "ADD_DATA":

                    Booking newB =
                            (Booking) clientMsg.getData();

                    if (newB.getVisitorsCount() > 15) {

                        output.writeObject(
                                new Message(
                                        "LIMIT_REACHED",
                                        "Error: A single booking cannot exceed 15 visitors."
                                )
                        );

                    } else {

                        boolean saved =
                                dbController.saveBooking(
                                        newB
                                );

                        int price =
                                newB.getVisitorsCount()
                                        * 30;

                        if ("Waiting List".equals(
                                newB.getStatus())) {

                            output.writeObject(
                                    new Message(
                                            "LIMIT_REACHED",
                                            "Successfully joined the Waiting List."
                                    )
                            );

                        } else {

                            output.writeObject(
                                    new Message(
                                            saved
                                                    ? "SUCCESS_PAID"
                                                    : "FAILED",
                                            "Order approved. Total Paid: "
                                                    + price
                                                    + " ILS"
                                    )
                            );
                        }
                    }

                    break;

                case "LOAD_DATA":

                    output.writeObject(
                            new Message(
                                    "SUCCESS",
                                    dbController.getUserBookings(
                                            (String) clientMsg.getData()
                                    )
                            )
                    );

                    break;

                case "UPDATE_DATA":

                    output.writeObject(
                            new Message(
                                    dbController.updateBooking(
                                            (Booking) clientMsg.getData()
                                    )
                                            ? "SUCCESS"
                                            : "FAILED",
                                    null
                            )
                    );

                    break;

                case "CANCEL_DATA":

                    @SuppressWarnings("unchecked")
                    ArrayList<Object> deleteData =
                            (ArrayList<Object>)
                                    clientMsg.getData();

                    int refund =
                            dbController.cancelBooking(
                                    (int) deleteData.get(0),
                                    (String) deleteData.get(1)
                            );

                    if (refund > 0) {

                        output.writeObject(
                                new Message(
                                        "CANCELLED_REFUND",
                                        "Booking Cancelled. Refund: "
                                                + refund
                                                + " ILS."
                                )
                        );

                    } else if (refund == 0) {

                        output.writeObject(
                                new Message(
                                        "CANCELLED_NO_REFUND",
                                        "Booking Cancelled."
                                )
                        );

                    } else {

                        output.writeObject(
                                new Message(
                                        "FAILED",
                                        "Could not cancel booking."
                                )
                        );
                    }

                    break;
            }

            output.flush();
            socket.close();

        } catch (Exception e) {

            System.err.println(
                    "Client handling error."
            );
        }
    }
}