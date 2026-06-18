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
	private static String lastError = "";

	/** Reason the last startServer attempt failed (shown by the Server GUI). */
	public static String getLastError() {
		return lastError;
	}

	private final DatabaseController dbController = new DatabaseController();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public GoNatureServer(int port) {
		super(port);
	}

	public static synchronized boolean startServer(String host, String user, String pass) {
		return startServer(host, user, pass, DEFAULT_PORT);
	}

	/**
	 * Starts the GoNature OCSF server using a selected port. The server decides
	 * which port to listen on, and clients connect to that port.
	 */
	public static synchronized boolean startServer(String host, String user, String pass, int port) {
		if (runningServer != null) {
			lastError = "Server is already running.";
			return false;
		}

		GoNatureServer server = new GoNatureServer(port);

		// Do not start if the database connection fails.
		if (!server.connectDB(host, user, pass)) {
			lastError = "Database connection failed. Check host / user / password.";
			server.shutdownScheduler();
			return false;
		}

		try {
			// OCSF listen() binds the port and starts its own accept thread (it does
			// not block). It throws IOException if the port is already in use.
			server.listen();
			runningServer = server;
			lastError = "";
			System.out.println("GoNature OCSF server started on port " + port);
			return true;
		} catch (java.io.IOException e) {
			lastError = "Port " + port + " is already in use (or cannot be opened).";
			server.shutdownScheduler();
			return false;
		}
	}

	/**
	 * Stops the GoNature OCSF server. This method is used by the Server GUI.
	 */
	public static synchronized boolean stopServer() {
		if (runningServer == null) {
			System.out.println("Server is not running.");
			return false;
		}

		try {
			GoNatureServer serverToStop = runningServer;

			runningServer = null;

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
		scheduler.scheduleAtFixedRate(dbController::processBookingConfirmations, 0, 1, TimeUnit.MINUTES);
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

				int subId = dbController.buySubscription(
						(String) subData.get(0), // visitor id
						(String) subData.get(1), // first name
						(String) subData.get(2), // last name
						(String) subData.get(3), // phone
						(String) subData.get(4), // email
						(int) subData.get(5),    // family members
						(String) subData.get(6), // payment method
						(String) subData.get(7)  // credit card
				);

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

				int emptyTickets = dbController.getBookableCapacity(bSpots.getParkName()) - dbController
						.countVisitorsAt(bSpots.getParkName(), bSpots.getVisitDate(), bSpots.getVisitTime());

				response = new Message("AVAILABLE_SPOTS_RESPONSE", Math.max(0, emptyTickets));
				break;
			}

			case "CHECK_AVAILABILITY": {
				Booking checkB = (Booking) data;

				int current = dbController.countVisitorsAt(checkB.getParkName(), checkB.getVisitDate(),
						checkB.getVisitTime());

				int cap = dbController.getBookableCapacity(checkB.getParkName());
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
					int current = dbController.countVisitorsAt(newB.getParkName(), newB.getVisitDate(), newB.getVisitTime());
					int bookableCap = dbController.getBookableCapacity(newB.getParkName());

					if (current + newB.getVisitorsCount() > bookableCap) {
						response = new Message("FAILED", "Could not save booking. Not enough bookable spots.");
						break;
					}
					String code = dbController.saveBookingAndReturnCode(newB);

					response = new Message(code != null ? "SUCCESS_PAID" : "FAILED",
							code != null
									? "Order approved. Total Paid: " + newB.getPrice() + " ILS\nConfirmation Code: " + code
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
				dbController.processBookingConfirmations();
				response = new Message("SUCCESS", dbController.getUserBookings((String) data));
				break;
			}
			case "GET_VISITOR_NOTIFICATIONS": {
				response = new Message("VISITOR_NOTIFICATIONS",
						dbController.getVisitorNotifications((String) data));
				break;
			}

			case "UPDATE_DATA": {
				boolean ok = dbController.updateBooking((Booking) data);
				response = new Message(ok ? "SUCCESS" : "FAILED", ok ? null : "Could not update booking.");
				break;
			}

			case "CONFIRM_ARRIVAL": {
				dbController.processBookingConfirmations();

				int confirmBookingId = Integer.parseInt(data.toString());
				boolean ok = dbController.confirmArrival(confirmBookingId);

				response = new Message(ok ? "ARRIVAL_CONFIRMED" : "FAILED",
						ok ? "Arrival confirmed." : "Could not confirm arrival. The 2-hour confirmation deadline may have expired.");

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
				dbController.manageWaitingListQueue();
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

				String code = dbController.payAndClaimWaitingListAndReturnCode((int) payData.get(0), (int) payData.get(1));

				if (code != null) {
					dbController.manageWaitingListQueue();
				}

				response = new Message(code != null ? "SUCCESS_PAID" : "FAILED",
						code != null
								? "Spot paid and claimed successfully!\nConfirmation Code: " + code
								: "Could not claim waiting-list spot.");

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
			case "CREATE_PARK_CHANGE_REQUEST": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> p = (ArrayList<Object>) data;

				boolean ok = dbController.createParkChangeRequest(
						(String) p.get(0),
						(int) p.get(1),
						(int) p.get(2),
						(int) p.get(3),
						(String) p.get(4));

				response = new Message(ok ? "REQUEST_CREATED" : "FAILED",
						ok ? "Request sent to department manager." : "Could not create request.");
				break;
			}

			case "GET_PENDING_PARK_CHANGE_REQUESTS": {
				response = new Message("PENDING_PARK_CHANGE_REQUESTS",
						dbController.getPendingParkChangeRequests());
				break;
			}
			case "GET_PENDING_DISCOUNT_REQUESTS": {
				response = new Message("PENDING_DISCOUNT_REQUESTS",
						dbController.getPendingDiscountRequests());
				break;
			}

			case "APPROVE_DISCOUNT_REQUEST": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> p = (ArrayList<Object>) data;

				boolean ok = dbController.approveDiscountRequest((int) p.get(0), (String) p.get(1));

				response = new Message(ok ? "DISCOUNT_REQUEST_APPROVED" : "FAILED",
						ok ? "Discount request approved." : "Could not approve discount request.");
				break;
			}

			case "REJECT_DISCOUNT_REQUEST": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> p = (ArrayList<Object>) data;

				boolean ok = dbController.rejectDiscountRequest((int) p.get(0), (String) p.get(1));

				response = new Message(ok ? "DISCOUNT_REQUEST_REJECTED" : "FAILED",
						ok ? "Discount request rejected." : "Could not reject discount request.");
				break;
			}
			case "CREATE_DISCOUNT_REQUEST": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> p = (ArrayList<Object>) data;

				boolean ok = dbController.createDiscountRequest(
						(String) p.get(0), // park name
						(String) p.get(1), // discount name
						(int) p.get(2),    // discount percent
						(String) p.get(3)  // requested by
				);

				response = new Message(ok ? "DISCOUNT_REQUEST_CREATED" : "FAILED",
						ok ? "Discount request sent to department manager."
								: "Could not create discount request.");

				break;
			}
			case "GET_APPROVED_DISCOUNT": {
				int discountPercent = dbController.getApprovedDiscountPercent((String) data);

				response = new Message("APPROVED_DISCOUNT", discountPercent);
				break;
			}

			case "APPROVE_PARK_CHANGE_REQUEST": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> p = (ArrayList<Object>) data;

				boolean ok = dbController.approveParkChangeRequest((int) p.get(0), (String) p.get(1));

				response = new Message(ok ? "REQUEST_APPROVED" : "FAILED",
						ok ? "Request approved. Park parameters updated." : "Could not approve request.");
				break;
			}

			case "REJECT_PARK_CHANGE_REQUEST": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> p = (ArrayList<Object>) data;

				boolean ok = dbController.rejectParkChangeRequest((int) p.get(0), (String) p.get(1));

				response = new Message(ok ? "REQUEST_REJECTED" : "FAILED",
						ok ? "Request rejected." : "Could not reject request.");
				break;
			}
			case "GET_PARK_PARAMS": {
				response = new Message("PARK_PARAMS", dbController.getParkParams((String) data));

				break;
			}

			case "UPDATE_PARK_CAPACITY": {
				response = new Message("APPROVAL_REQUIRED",
						"Park changes must be sent as a request to the department manager.");
				break;
			}

			case "CHECKIN": {
				String code = data.toString().trim();
				Booking bk = dbController.getBookingByConfirmationCode(code);

				if (bk == null) {
					response = new Message("CHECKIN_FAILED", "No booking found with confirmation code " + code);
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
					dbController.checkInBooking(bk.getBookingId());

					response = new Message("CHECKIN_OK",
							"Checked in " + bk.getVisitorsCount() + " visitor(s). Bill: " + bk.getPrice() + " ILS.");
				}

				break;
			}

			case "CHECKOUT": {
				String code = data.toString().trim();
				Booking bk = dbController.getBookingByConfirmationCode(code);

				if (bk == null) {
					response = new Message("CHECKOUT_FAILED", "No booking found with confirmation code " + code);
					break;
				}

				boolean ok = dbController.exitBooking(bk.getBookingId());

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
					boolean guideGroup = cb.isGuideGroup() || "Guide".equals(cb.getVisitorType());

					int bill;
					if (guideGroup) {
						if (cb.getVisitorsCount() < 2 || cb.getVisitorsCount() > 15) {
							response = new Message("CASUAL_FAILED",
									"Casual group with guide must include 2 to 15 people including the guide.");
							break;
						}

						bill = (int) Math.round(cb.getVisitorsCount() * 30 * 0.90);

						cb.setVisitorType("Guide");
						cb.setGuideGroup(true);
						cb.setSubscriber(false);
					} else {
						bill = cb.getVisitorsCount() * 30;

						cb.setVisitorType("Regular Visitor");
						cb.setGuideGroup(false);
						cb.setSubscriber(false);
					}

					cb.setStatus("Entered");
					cb.setPrice(bill);

					boolean saved = dbController.saveBooking(cb);

					response = new Message(saved ? "CASUAL_OK" : "CASUAL_FAILED",
							saved ? "Walk-in admitted. Bill: " + bill + " ILS." : "Could not register walk-in.");;
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
			case "REPORT_DETAILED_VISITS": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> f = (ArrayList<Object>) data;

				response = new Message("REPORT_DETAILED_VISITS_RESULT",
						dbController.reportDetailedVisits((String) f.get(0), (int) f.get(1), (int) f.get(2)));

				break;
			}
			case "REPORT_NOT_FULL": {
				@SuppressWarnings("unchecked")
				ArrayList<Object> f = (ArrayList<Object>) data;

				response = new Message("REPORT_NOT_FULL_RESULT",
						dbController.reportParkNotFull((String) f.get(0), (int) f.get(1), (int) f.get(2)));

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