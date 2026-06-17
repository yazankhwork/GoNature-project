package server.network;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import common.Message;
import server.database.DatabaseController;
import common.Booking;

public class GoNatureServer {

	private static final int PORT = 5555;
	private static DatabaseController dbController = new DatabaseController();
	private static ServerSocket serverSocket;
	private static boolean isRunning = false;
	private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public static void startServer(String dbHost, String dbUser, String dbPass) {
	    isRunning = true;
	    dbController.connectToDatabase(dbHost, dbUser, dbPass);
	    scheduler.scheduleAtFixedRate(dbController::manageWaitingListQueue, 0, 1, TimeUnit.MINUTES);
	    try {
	        serverSocket = new ServerSocket(PORT);
	        System.out.println("Server is running on port " + PORT);
	        while (isRunning) {
	            Socket clientSocket = serverSocket.accept();
	            new Thread(() -> handleClient(clientSocket)).start();
	        }
	    } catch (SocketException se) {
			System.out.println("Server stopped.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void stopServer() {
		isRunning = false;
		if (scheduler != null)
			scheduler.shutdown();
		try {
			if (serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleClient(Socket socket) {
		try {
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.flush();
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

			Message clientMsg = (Message) input.readObject();
			String command = clientMsg.getCommand();
			System.out.println("SERVER COMMAND RECEIVED: [" + command + "]");

			switch (command) {
			case "CONNECT":
				output.writeObject(new Message("CONNECTED", null));
				break;

			case "LOGIN":
				@SuppressWarnings("unchecked")
				ArrayList<String> loginData = (ArrayList<String>) clientMsg.getData();
				String[] loginRes = dbController.loginVisitor(loginData.get(0), loginData.get(1));
				output.writeObject(new Message(loginRes[0], loginRes));
				break;

			case "LOGIN_EMPLOYEE":
				@SuppressWarnings("unchecked")
				ArrayList<String> empLoginData = (ArrayList<String>) clientMsg.getData();
				String[] empRes = dbController.loginEmployee(empLoginData.get(0), empLoginData.get(1));
				output.writeObject(new Message(empRes[0], empRes));
				break;

			case "BUY_SUBSCRIPTION":
				@SuppressWarnings("unchecked")
				ArrayList<Object> subData = (ArrayList<Object>) clientMsg.getData();
				int subId = dbController.buySubscription((String) subData.get(0), (int) subData.get(1),
						(String) subData.get(2));
				if (subId > 0)
					output.writeObject(new Message("SUCCESS", String.valueOf(subId)));
				else
					output.writeObject(new Message("FAILED", null));
				break;

			case "REGISTER":
				@SuppressWarnings("unchecked")
				ArrayList<Object> regData = (ArrayList<Object>) clientMsg.getData();
				output.writeObject(new Message(dbController.registerVisitor((String) regData.get(0),
						(String) regData.get(1), (boolean) regData.get(2), (String) regData.get(3)), null));
				break;

			// NEW: Specific command for Service Rep registering/upgrading a guide
			case "REGISTER_GUIDE":
				@SuppressWarnings("unchecked")
				ArrayList<String> guideData = (ArrayList<String>) clientMsg.getData();
				String guideRes = dbController.registerOrUpdateGuide(guideData.get(0), guideData.get(1),
						guideData.get(2));
				output.writeObject(new Message(guideRes, null));
				break;

			case "GET_AVAILABLE_SPOTS":
				Booking bSpots = (Booking) clientMsg.getData();
				int emptyTickets = dbController.getParkCapacity(bSpots.getParkName()) - dbController
						.countVisitorsAt(bSpots.getParkName(), bSpots.getVisitDate(), bSpots.getVisitTime());

				output.writeObject(new Message("AVAILABLE_SPOTS_RESPONSE", Math.max(0, emptyTickets)));
				break;

			case "CHECK_AVAILABILITY":
				Booking checkB = (Booking) clientMsg.getData();
				int current = dbController.countVisitorsAt(checkB.getParkName(), checkB.getVisitDate(),
						checkB.getVisitTime());
				int cap = dbController.getParkCapacity(checkB.getParkName());
				int req = checkB.getVisitorsCount();
				int available = cap - current;

				if (available >= req)
					output.writeObject(new Message("OK", null));
				else if (available > 0)
					output.writeObject(new Message("PARTIAL_AVAILABILITY", available));
				else {
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
					String msg = "Park is full at " + rTime + ".";
					if (bBef && bAft)
						msg += "\nSuggestion: Try " + bef + " or " + aft;
					else if (bBef)
						msg += "\nSuggestion: Try " + bef;
					else if (bAft)
						msg += "\nSuggestion: Try " + aft;
					else
						msg = "FULL";
					output.writeObject(new Message(msg.equals("FULL") ? "FULL" : "SUGGESTION", msg));
				}
				break;

			case "ADD_DATA":
				Booking newB = (Booking) clientMsg.getData();
				if (newB.getVisitorsCount() > 15)
					output.writeObject(new Message("LIMIT_REACHED", "Error: Max 15 visitors."));
				else {
					if ("Waiting List".equals(newB.getStatus())) {
						dbController.enterWaitingList(newB);
						output.writeObject(new Message("LIMIT_REACHED", "Successfully joined the Waiting List."));
					} else {
						boolean saved = dbController.saveBooking(newB);
						output.writeObject(new Message(saved ? "SUCCESS_PAID" : "FAILED",
								"Order approved. Total Paid: " + newB.getPrice() + " ILS"));
					}
				}
				break;

			case "ADD_SPLIT_BOOKING":
				@SuppressWarnings("unchecked")
				ArrayList<Booking> splitData = (ArrayList<Booking>) clientMsg.getData();
				dbController.saveBooking(splitData.get(0));
				dbController.enterWaitingList(splitData.get(1));
				output.writeObject(new Message("SUCCESS_PAID",
						"Partial booking confirmed! Paid: " + splitData.get(0).getPrice() + " ILS.\nThe remaining "
								+ splitData.get(1).getVisitorsCount() + " visitors were added to the Waiting List."));
				break;

			case "LOAD_DATA":
				output.writeObject(new Message("SUCCESS", dbController.getUserBookings((String) clientMsg.getData())));
				break;

			case "UPDATE_DATA":
				output.writeObject(new Message(
						dbController.updateBooking((Booking) clientMsg.getData()) ? "SUCCESS" : "FAILED", null));
				break;

			case "CONFIRM_ARRIVAL":
				int confirmBookingId = Integer.parseInt(clientMsg.getData().toString());
				output.writeObject(
						new Message(dbController.confirmArrival(confirmBookingId) ? "ARRIVAL_CONFIRMED" : "FAILED",
								"Arrival confirmed."));
				break;

			case "CANCEL_DATA":
				@SuppressWarnings("unchecked")
				ArrayList<Object> deleteData = (ArrayList<Object>) clientMsg.getData();
				int refund = dbController.cancelBooking((int) deleteData.get(0), (String) deleteData.get(1));
				dbController.manageWaitingListQueue();
				if (refund > 0)
					output.writeObject(
							new Message("CANCELLED_REFUND", "Booking Cancelled. Refund: " + refund + " ILS."));
				else if (refund == 0)
					output.writeObject(new Message("CANCELLED_NO_REFUND", "Booking Cancelled."));
				else
					output.writeObject(new Message("FAILED", "Could not cancel booking."));
				break;

			case "CHECK_WAITINGLIST":
				ArrayList<Object> wlMsg = dbController.getWaitingListMessage((String) clientMsg.getData());
				if (wlMsg != null)
					output.writeObject(new Message("HAS_EMPTY_PLACE", wlMsg));
				else
					output.writeObject(new Message("NO_MESSAGES", null));
				break;

			case "PAY_WAITING_LIST":
				@SuppressWarnings("unchecked")
				ArrayList<Object> payData = (ArrayList<Object>) clientMsg.getData();
				boolean claimOk = dbController.payAndClaimWaitingList((int) payData.get(0), (int) payData.get(1));
				output.writeObject(
						new Message(claimOk ? "SUCCESS_PAID" : "FAILED", "Spot paid and claimed successfully!"));
				break;

			case "DECLINE_WAITING_LIST":
				dbController.declineWaitingList((int) clientMsg.getData());
				dbController.manageWaitingListQueue();
				output.writeObject(new Message("SUCCESS", "Spot declined. Passed to the next person."));
				break;
			case "GET_ACTIVE_VISITORS":
				output.writeObject(new Message("ACTIVE_VISITORS",
						dbController.getCurrentVisitorsInPark((String) clientMsg.getData())));
				break;

			case "GET_PARK_PARAMS":
				output.writeObject(
						new Message("PARK_PARAMS", dbController.getParkCapacity((String) clientMsg.getData())));
				break;

			case "UPDATE_PARK_CAPACITY": {
				ArrayList<Object> p = (ArrayList<Object>) clientMsg.getData();
				boolean ok = dbController.updateParkCapacity((String) p.get(0), (int) p.get(1));
				output.writeObject(new Message(ok ? "SUCCESS" : "FAILED", null));
				break;
			}

			case "CHECKIN": {
				int bid = (int) clientMsg.getData();
				Booking bk = dbController.getBookingById(bid);
				if (bk == null || "Cancelled".equals(bk.getStatus())) {
					output.writeObject(new Message("CHECKIN_FAILED", "No valid booking with ID " + bid));
				} else {
					int cap1 = dbController.getParkCapacity(bk.getParkName());
					int now = dbController.getCurrentVisitorsInPark(bk.getParkName());
					if (now + bk.getVisitorsCount() > cap1) {
						output.writeObject(new Message("CHECKIN_FAILED", "Park is full, cannot admit this booking."));
					} else {
						dbController.setBookingStatus(bid, "Entered");
						output.writeObject(new Message("CHECKIN_OK", "Checked in " + bk.getVisitorsCount()
								+ " visitor(s). Bill: " + bk.getPrice() + " ILS."));
					}
				}
				break;
			}

			case "CHECKOUT": {
				int bid = (int) clientMsg.getData();
				boolean ok = dbController.setBookingStatus(bid, "Exited");
				output.writeObject(new Message(ok ? "CHECKOUT_OK" : "CHECKOUT_FAILED", null));
				break;
			}

			case "CASUAL_VISIT": {
				Booking cb = (Booking) clientMsg.getData();
				int cap2 = dbController.getParkCapacity(cb.getParkName());
				int now = dbController.getCurrentVisitorsInPark(cb.getParkName());
				if (now + cb.getVisitorsCount() > cap2) {
					output.writeObject(new Message("CASUAL_FAILED", "Park is full. No room for a walk-in now."));
				} else {
					int bill = cb.getVisitorsCount() * 100; // casual = full price per person
					cb.setStatus("Entered");
					cb.setPrice(bill);
					cb.setVisitorType("Regular Visitor");
					boolean saved = dbController.saveBooking(cb);
					output.writeObject(new Message(saved ? "CASUAL_OK" : "CASUAL_FAILED",
							saved ? ("Walk-in admitted. Bill: " + bill + " ILS.") : "Could not register walk-in."));
				}
				break;
			}

			case "REPORT_VISITS": {
				ArrayList<Object> f = (ArrayList<Object>) clientMsg.getData();
				output.writeObject(new Message("REPORT_VISITS_RESULT",
						dbController.reportVisitorsByType((String) f.get(0), (int) f.get(1), (int) f.get(2))));
				break;
			}

			case "REPORT_CANCELLATIONS": {
				ArrayList<Object> f = (ArrayList<Object>) clientMsg.getData();
				output.writeObject(new Message("REPORT_CANCELLATIONS_RESULT",
						dbController.reportCancellations((String) f.get(0), (int) f.get(1), (int) f.get(2))));
				break;
			}
			default:
				output.writeObject(new Message("UNKNOWN_COMMAND", "Server does not recognize command."));
				break;
			}
			output.flush();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}