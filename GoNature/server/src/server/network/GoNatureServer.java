package server.network;

import java.io.IOException;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import common.Message;
import common.Booking;
import server.database.DatabaseController;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

/**
 * GoNature server built on the OCSF AbstractServer framework. OCSF runs one
 * thread per connected client and keeps connections open, so multiple clients
 * are served at the same time. All business logic is in
 * handleMessageFromClient; the server is the only component touching the DB.
 */
public class GoNatureServer extends AbstractServer {
	
	private static final int DEFAULT_PORT = 5555;
	private static GoNatureServer runningServer;
	private static Thread serverThread;
	private final DatabaseController dbController = new DatabaseController();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public GoNatureServer(int port) {
		super(port);
	}
	public static synchronized boolean startServer(String host, String user, String pass) {
	    return startServer(host, user, pass, DEFAULT_PORT);
	}

	/**
	 * Starts the GoNature OCSF server using a selected port.
	 * The server decides which port to listen on, and clients connect to that port.
	 */
	public static synchronized boolean startServer(String host, String user, String pass, int port) {
	    if (runningServer != null) {
	        System.out.println("Server is already running.");
	        return false;
	    }

	    try {
	        GoNatureServer server = new GoNatureServer(port);

	        boolean dbConnected = server.connectDB(host, user, pass);

	        if (!dbConnected) {
	            System.err.println("Database connection failed. Server was not started.");
	            return false;
	        }

	        runningServer = server;

	        serverThread = new Thread(() -> {
	            try {
	                runningServer.listen();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        });

	        serverThread.setDaemon(true);
	        serverThread.start();

	        System.out.println("GoNature OCSF server started on port " + port);
	        return true;

	    } catch (Exception e) {
	        e.printStackTrace();
	        runningServer = null;
	        serverThread = null;
	        return false;
	    }
	}

	/**
	 * Stops the GoNature OCSF server.
	 * This method is used by the Server GUI.
	 */
	public static synchronized boolean stopServer() {
	    if (runningServer == null) {
	        System.out.println("Server is not running.");
	        return false;
	    }

	    try {
	        GoNatureServer serverToStop = runningServer;

	        runningServer = null;
	        serverThread = null;

	        serverToStop.close();
	        serverToStop.shutdownScheduler();

	        System.out.println("GoNature OCSF server stopped.");
	        return true;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	private void shutdownScheduler() {
	    if (!scheduler.isShutdown()) {
	        scheduler.shutdownNow();
	    }
	}
	/** Connects the database. Returns true on success. */
	public boolean connectDB(String host, String user, String pass) {
		return dbController.connectToDatabase(host, user, pass);
	}

	@Override
	protected void serverStarted() {
		System.out.println("Server listening on port " + getPort());
		scheduler.scheduleAtFixedRate(dbController::manageWaitingListQueue, 0, 1, TimeUnit.MINUTES);
	}

	@Override
	protected void serverStopped() {
	    System.out.println("Server stopped.");
	    shutdownScheduler();
	}

	@Override
	protected void clientConnected(ConnectionToClient client) {
		System.out.println("Client connected: " + client);
	}

	@Override
	protected synchronized void clientDisconnected(ConnectionToClient client) {
		System.out.println("Client disconnected: " + client);
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		Message response = new Message("ERROR", "No response was created.");

		if (!(msg instanceof Message)) {
			sendSafe(client, new Message("ERROR", "Invalid message type."));
			return;
		}

		Message clientMsg = (Message) msg;
		String command = clientMsg.getCommand();
		Object data = clientMsg.getData();

		try {
			switch (command) {

			case "CONNECT": {
				response = new Message("CONNECTED", null);
				break;
			}

			case "LOGIN": {
				@SuppressWarnings("unchecked")
				ArrayList<String> loginData = (ArrayList<String>) data;

				String[] loginRes = dbController.loginVisitor(loginData.get(0), loginData.get(1));
				response = new Message(loginRes[0], loginRes);
				break;
			}

			case "LOGIN_EMPLOYEE": {
				@SuppressWarnings("unchecked")
				ArrayList<String> empLoginData = (ArrayList<String>) data;

				String[] empRes = dbController.loginEmployee(empLoginData.get(0), empLoginData.get(1));
				response = new Message(empRes[0], empRes);
				break;
			}

			case "BUY_SUBSCRIPTION": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> subData = (ArrayList<Object>) data;

				int subId = dbController.buySubscription((String) subData.get(0), (int) subData.get(1),
						(String) subData.get(2));

				if (subId > 0) {
					response = new Message("SUCCESS", String.valueOf(subId));
				} else {
					response = new Message("FAILED", "Failed to register subscription.");
				}

				break;
			}

			case "REGISTER": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> regData = (ArrayList<Object>) data;

				String result = dbController.registerVisitor((String) regData.get(0), (String) regData.get(1),
						(boolean) regData.get(2), (String) regData.get(3));

				response = new Message(result, null);
				break;
			}

			case "REGISTER_GUIDE": {
				@SuppressWarnings("unchecked")
				ArrayList<String> guideData = (ArrayList<String>) data;

				String guideRes = dbController.registerOrUpdateGuide(guideData.get(0), guideData.get(1),
						guideData.get(2));

				response = new Message(guideRes, null);
				break;
			}

			case "GET_AVAILABLE_SPOTS": {
				Booking bSpots = (Booking) data;

				int emptyTickets = dbController.getParkCapacity(bSpots.getParkName()) - dbController
						.countVisitorsAt(bSpots.getParkName(), bSpots.getVisitDate(), bSpots.getVisitTime());

				response = new Message("AVAILABLE_SPOTS_RESPONSE", Math.max(0, emptyTickets));
				break;
			}

			case "CHECK_AVAILABILITY": {
				Booking checkB = (Booking) data;

				int current = dbController.countVisitorsAt(checkB.getParkName(), checkB.getVisitDate(),
						checkB.getVisitTime());

				int cap = dbController.getParkCapacity(checkB.getParkName());
				int req = checkB.getVisitorsCount();
				int available = cap - current;

				if (available >= req) {
					response = new Message("OK", null);

				} else if (available > 0) {
					response = new Message("PARTIAL_AVAILABILITY", available);

				} else {
					LocalTime rTime = checkB.getVisitTime();
					LocalTime bef = rTime.minusHours(1);
					LocalTime aft = rTime.plusHours(1);

					boolean vBef = !bef.isBefore(LocalTime.of(8, 0));
					boolean vAft = !aft.isAfter(LocalTime.of(18, 0));

					int cBef = vBef ? dbController.countVisitorsAt(checkB.getParkName(), checkB.getVisitDate(), bef)
							: 999;

					int cAft = vAft ? dbController.countVisitorsAt(checkB.getParkName(), checkB.getVisitDate(), aft)
							: 999;

					boolean bBef = vBef && (cBef + req <= cap);
					boolean bAft = vAft && (cAft + req <= cap);

					String msg1 = "Park is full at " + rTime + ".";

					if (bBef && bAft) {
						msg1 += "\nSuggestion: Try " + bef + " or " + aft;
						response = new Message("SUGGESTION", msg1);

					} else if (bBef) {
						msg1 += "\nSuggestion: Try " + bef;
						response = new Message("SUGGESTION", msg1);

					} else if (bAft) {
						msg1 += "\nSuggestion: Try " + aft;
						response = new Message("SUGGESTION", msg1);

					} else {
						response = new Message("FULL", "Park is full. You can join the waiting list.");
					}
				}

				break;
			}

			case "ADD_DATA": {
				Booking newB = (Booking) data;

				if (newB.getVisitorsCount() > 15) {
					response = new Message("LIMIT_REACHED", "Error: Max 15 visitors.");

				} else if ("Waiting List".equals(newB.getStatus())) {
					boolean ok = dbController.enterWaitingList(newB);

					response = new Message(ok ? "LIMIT_REACHED" : "FAILED",
							ok ? "Successfully joined the Waiting List." : "Could not join waiting list.");

				} else {
					boolean saved = dbController.saveBooking(newB);

					response = new Message(saved ? "SUCCESS_PAID" : "FAILED",
							saved ? "Order approved. Total Paid: " + newB.getPrice() + " ILS"
									: "Could not save booking.");
				}

				break;
			}

			case "ADD_SPLIT_BOOKING": {
				@SuppressWarnings("unchecked")
				ArrayList<Booking> splitData = (ArrayList<Booking>) data;

				boolean savedBooking = dbController.saveBooking(splitData.get(0));
				boolean savedWaiting = dbController.enterWaitingList(splitData.get(1));

				if (savedBooking && savedWaiting) {
					response = new Message("SUCCESS_PAID",
							"Partial booking confirmed! Paid: " + splitData.get(0).getPrice() + " ILS.\nThe remaining "
									+ splitData.get(1).getVisitorsCount()
									+ " visitors were added to the Waiting List.");
				} else {
					response = new Message("FAILED", "Could not complete split booking.");
				}

				break;
			}

			case "LOAD_DATA": {
				response = new Message("SUCCESS", dbController.getUserBookings((String) data));
				break;
			}

			case "UPDATE_DATA": {
				boolean ok = dbController.updateBooking((Booking) data);
				response = new Message(ok ? "SUCCESS" : "FAILED", ok ? null : "Could not update booking.");
				break;
			}

			case "CONFIRM_ARRIVAL": {
				int confirmBookingId = Integer.parseInt(data.toString());
				boolean ok = dbController.confirmArrival(confirmBookingId);

				response = new Message(ok ? "ARRIVAL_CONFIRMED" : "FAILED",
						ok ? "Arrival confirmed." : "Could not confirm arrival.");

				break;
			}

			case "CANCEL_DATA": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> deleteData = (ArrayList<Object>) data;

				int refund = dbController.cancelBooking((int) deleteData.get(0), (String) deleteData.get(1));

				dbController.manageWaitingListQueue();

				if (refund > 0) {
					response = new Message("CANCELLED_REFUND", "Booking Cancelled. Refund: " + refund + " ILS.");

				} else if (refund == 0) {
					response = new Message("CANCELLED_NO_REFUND", "Booking Cancelled.");

				} else {
					response = new Message("FAILED", "Could not cancel booking.");
				}

				break;
			}

			case "CHECK_WAITINGLIST": {
				ArrayList<Object> wlMsg = dbController.getWaitingListMessage((String) data);

				if (wlMsg != null) {
					response = new Message("HAS_EMPTY_PLACE", wlMsg);
				} else {
					response = new Message("NO_MESSAGES", null);
				}

				break;
			}

			case "PAY_WAITING_LIST": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> payData = (ArrayList<Object>) data;

				boolean claimOk = dbController.payAndClaimWaitingList((int) payData.get(0), (int) payData.get(1));

				response = new Message(claimOk ? "SUCCESS_PAID" : "FAILED",
						claimOk ? "Spot paid and claimed successfully!" : "Could not claim waiting-list spot.");

				break;
			}

			case "DECLINE_WAITING_LIST": {
				dbController.declineWaitingList((int) data);
				dbController.manageWaitingListQueue();

				response = new Message("SUCCESS", "Spot declined. Passed to the next person.");
				break;
			}

			case "GET_ACTIVE_VISITORS": {
				response = new Message("ACTIVE_VISITORS", dbController.getCurrentVisitorsInPark((String) data));

				break;
			}

			case "GET_PARK_PARAMS": {
				response = new Message("PARK_PARAMS", dbController.getParkCapacity((String) data));

				break;
			}

			case "UPDATE_PARK_CAPACITY": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> p = (ArrayList<Object>) data;

				boolean ok = dbController.updateParkCapacity((String) p.get(0), (int) p.get(1));

				response = new Message(ok ? "SUCCESS" : "FAILED", ok ? null : "Could not update capacity.");
				break;
			}

			case "CHECKIN": {
				int bid = (int) data;
				Booking bk = dbController.getBookingById(bid);

				if (bk == null) {
					response = new Message("CHECKIN_FAILED", "No booking found with ID " + bid);
					break;
				}

				if ("Cancelled".equals(bk.getStatus())) {
					response = new Message("CHECKIN_FAILED", "This booking is cancelled.");
					break;
				}

				if ("Entered".equals(bk.getStatus())) {
					response = new Message("CHECKIN_FAILED", "This booking is already checked in.");
					break;
				}

				if ("Exited".equals(bk.getStatus())) {
					response = new Message("CHECKIN_FAILED", "This booking already exited.");
					break;
				}

				if (bk.getVisitDate().isBefore(LocalDate.now())) {
					response = new Message("CHECKIN_FAILED", "This booking date already passed.");
					break;
				}

				if (bk.getVisitDate().isAfter(LocalDate.now())) {
					response = new Message("CHECKIN_FAILED", "This booking is not for today.");
					break;
				}

				int cap1 = dbController.getParkCapacity(bk.getParkName());
				int now = dbController.getCurrentVisitorsInPark(bk.getParkName());

				if (now + bk.getVisitorsCount() > cap1) {
					response = new Message("CHECKIN_FAILED", "Park is full, cannot admit this booking.");

				} else {
					dbController.setBookingStatus(bid, "Entered");

					response = new Message("CHECKIN_OK",
							"Checked in " + bk.getVisitorsCount() + " visitor(s). Bill: " + bk.getPrice() + " ILS.");
				}

				break;
			}

			case "CHECKOUT": {
				int bid = (int) data;
				boolean ok = dbController.exitBooking(bid);

				response = new Message(ok ? "CHECKOUT_OK" : "CHECKOUT_FAILED",
						ok ? "Check-out registered." : "Check-out failed. Booking must be currently Entered.");

				break;
			}

			case "CASUAL_VISIT": {
				Booking cb = (Booking) data;

				if (cb.getVisitorsCount() <= 0) {
					response = new Message("CASUAL_FAILED", "Visitor count must be at least 1.");
					break;
				}

				int cap2 = dbController.getParkCapacity(cb.getParkName());
				int now = dbController.getCurrentVisitorsInPark(cb.getParkName());

				if (now + cb.getVisitorsCount() > cap2) {
					response = new Message("CASUAL_FAILED", "Park is full. No room for a walk-in now.");

				} else {
					int bill = cb.getVisitorsCount() * 30;

					cb.setStatus("Entered");
					cb.setPrice(bill);
					cb.setVisitorType("Regular Visitor");

					boolean saved = dbController.saveBooking(cb);

					response = new Message(saved ? "CASUAL_OK" : "CASUAL_FAILED",
							saved ? "Walk-in admitted. Bill: " + bill + " ILS." : "Could not register walk-in.");
				}

				break;
			}

			case "REPORT_VISITS": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> f = (ArrayList<Object>) data;

				response = new Message("REPORT_VISITS_RESULT",
						dbController.reportVisitorsByType((String) f.get(0), (int) f.get(1), (int) f.get(2)));

				break;
			}

			case "REPORT_CANCELLATIONS": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> f = (ArrayList<Object>) data;

				response = new Message("REPORT_CANCELLATIONS_RESULT",
						dbController.reportCancellations((String) f.get(0), (int) f.get(1), (int) f.get(2)));

				break;
			}

			default: {
				response = new Message("UNKNOWN_COMMAND", "Server does not recognize command: " + command);
				break;
			}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response = new Message("ERROR", "Server error: " + e.getMessage());
		}

		sendSafe(client, response);
	}

	private void sendSafe(ConnectionToClient client, Message response) {
		try {
			client.sendToClient(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}