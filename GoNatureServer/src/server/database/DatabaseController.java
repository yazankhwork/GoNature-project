package server.database;

import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;
import common.Booking;
import server.dao.NotificationDAO;
import server.dao.EmployeeDAO;
import server.dao.VisitorDAO;
import server.dao.ParkDAO;
import server.dao.DiscountDAO;
import server.dao.BookingDAO;
import server.dao.WaitingListDAO;
import server.dao.ReportDAO;

public class DatabaseController {
	private Connection connection;
	private NotificationDAO notificationDAO;
	private EmployeeDAO employeeDAO;
	private VisitorDAO visitorDAO;
	private ParkDAO parkDAO;
	private DiscountDAO discountDAO;
	private BookingDAO bookingDAO;
	private WaitingListDAO waitingListDAO;
	private ReportDAO reportDAO;
	
	public boolean connectToDatabase(String host, String user, String pass) {
		try {
			String url = "jdbc:mysql://" + host + "/gonature_db"
					+ "?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";
			connection = DriverManager.getConnection(url, user, pass);
			visitorDAO = new VisitorDAO(connection);
			employeeDAO = new EmployeeDAO(connection);
			notificationDAO = new NotificationDAO(connection);

			parkDAO = new ParkDAO(connection, employeeDAO);
			discountDAO = new DiscountDAO(connection, employeeDAO);

			bookingDAO = new BookingDAO(connection, parkDAO, visitorDAO, notificationDAO);
			waitingListDAO = new WaitingListDAO(connection, bookingDAO, parkDAO, visitorDAO, notificationDAO);
			reportDAO = new ReportDAO(connection, parkDAO, bookingDAO);
			System.out.println("Database connected successfully!");
			return true;
		} catch (SQLException e) {
			System.err.println("DB Connection Error: " + e.getMessage());
			return false;
		}
	}

	public int countVisitorsAt(String parkName, LocalDate date, LocalTime time) {
		return bookingDAO.countVisitorsAt(parkName, date, time);
	}

	public String[] loginVisitor(String username, String password) {
		return visitorDAO.loginVisitor(username, password);
	}

	public String[] loginEmployee(String empId, String password) {
		return employeeDAO.loginEmployee(empId, password);
	}

	public int buySubscription(String visitorId, String firstName, String lastName, String phone, String email,
			int familyMembers, String paymentMethod, String creditCard) {
		return visitorDAO.buySubscription(visitorId, firstName, lastName, phone, email,
				familyMembers, paymentMethod, creditCard);
	}

	public String registerVisitor(String visitorId, String username, String password, String email, String phone) {
		return visitorDAO.registerVisitor(visitorId, username, password, email, phone);
	}

	public String registerOrUpdateGuide(String visitorId, String password, String username) {
		return visitorDAO.registerOrUpdateGuide(visitorId, password, username);
	}

	public boolean saveBooking(Booking b) {
		return bookingDAO.saveBooking(b);
	}

	public String saveBookingAndReturnCode(Booking b) {
		return bookingDAO.saveBookingAndReturnCode(b);
	}
	public boolean isVisitorSubscriber(String visitorId) {
		return visitorDAO.isVisitorSubscriber(visitorId);
	}

	public boolean isVisitorGuide(String visitorId) {
		return visitorDAO.isVisitorGuide(visitorId);
	}
	public String getVisitorPhone(String visitorId) {
		return visitorDAO.getVisitorPhone(visitorId);
	}
	public void createNotification(String visitorId, Integer bookingId, String notificationType,
			String messageText, String email, String phone) {
		notificationDAO.createNotification(visitorId, bookingId, notificationType, messageText, email, phone);
	}
	public ArrayList<ArrayList<Object>> getVisitorNotifications(String visitorId) {
		return notificationDAO.getVisitorNotifications(visitorId);
	}
	public String generateConfirmationCode() {
		return bookingDAO.generateConfirmationCode();
	}
	public boolean enterWaitingList(Booking b) {
		return waitingListDAO.enterWaitingList(b);
	}

	public ArrayList<Booking> getUserBookings(String visitorId) {
		ArrayList<Booking> all = bookingDAO.getActiveBookings(visitorId);
		all.addAll(waitingListDAO.getWaitingEntriesAsBookings(visitorId));
		return all;
	}

	public boolean updateBooking(Booking b) {
		return bookingDAO.updateBooking(b);
	}

	public boolean exitBooking(int bookingId) {
		return bookingDAO.exitBooking(bookingId);
	}

	public boolean confirmArrival(int bookingId) {
		return bookingDAO.confirmArrival(bookingId);
	}
	public void processBookingConfirmations() {
		bookingDAO.processBookingConfirmations();
	}
	public int cancelBooking(int id, String visitorId) {
		int refund = bookingDAO.cancelBookingAndReturnRefund(id, visitorId);

		if (refund >= 0) {
			return refund;
		}

		boolean removedFromWaitingList = waitingListDAO.removeWaitingEntry(id, visitorId);

		if (removedFromWaitingList) {
			return 0;
		}

		return -1;
	}

	public void manageWaitingListQueue() {
		waitingListDAO.manageWaitingListQueue();
	}

	public ArrayList<Object> getWaitingListMessage(String visitorId) {
		return waitingListDAO.getWaitingListMessage(visitorId);
	}

	public boolean payAndClaimWaitingList(int waitingId, int price) {
		return waitingListDAO.payAndClaimWaitingList(waitingId, price);
	}

	public String payAndClaimWaitingListAndReturnCode(int waitingId, int price) {
		return waitingListDAO.payAndClaimWaitingListAndReturnCode(waitingId, price);
	}

	public void declineWaitingList(int waitingId) {
		waitingListDAO.declineWaitingList(waitingId);
	}

	public Booking getBookingById(int bookingId) {
		return bookingDAO.getBookingById(bookingId);
	}
	public Booking getBookingByConfirmationCode(String code) {
		return bookingDAO.getBookingByConfirmationCode(code);
	}

	public int getParkCapacity(String parkName) {
		return parkDAO.getParkCapacity(parkName);
	}

	public int getParkBookingPercent(String parkName) {
		return parkDAO.getParkBookingPercent(parkName);
	}

	public int getParkVisitDurationHours(String parkName) {
		return parkDAO.getParkVisitDurationHours(parkName);
	}

	public int getBookableCapacity(String parkName) {
		return parkDAO.getBookableCapacity(parkName);
	}

	public boolean updateParkCapacity(String parkName, int newCapacity) {
		return parkDAO.updateParkCapacity(parkName, newCapacity);
	}
	public String getEmployeeRole(String empId) {
		return employeeDAO.getEmployeeRole(empId);
	}

	public boolean isEmployeeRole(String empId, String role) {
		return employeeDAO.isEmployeeRole(empId, role);
	}

	public ArrayList<Object> getParkParams(String parkName) {
		return parkDAO.getParkParams(parkName);
	}

	public boolean createParkChangeRequest(String parkName, int newCapacity, int newBookingPercent,
			int newVisitDurationHours, String requestedBy) {
		return parkDAO.createParkChangeRequest(parkName, newCapacity, newBookingPercent,
				newVisitDurationHours, requestedBy);
	}
	
	public boolean createDiscountRequest(String parkName, String discountName, int discountPercent, String startDate, String endDate, String requestedBy) {
		return discountDAO.createDiscountRequest(parkName, discountName, discountPercent, startDate, endDate, requestedBy);
	}
	
	public ArrayList<ArrayList<Object>> getPendingDiscountRequests() {
		return discountDAO.getPendingDiscountRequests();
	}

	public boolean approveDiscountRequest(int requestId, String decisionBy) {
		return discountDAO.approveDiscountRequest(requestId, decisionBy);
	}

	public boolean rejectDiscountRequest(int requestId, String decisionBy) {
		return discountDAO.rejectDiscountRequest(requestId, decisionBy);
	}
	public int getApprovedDiscountPercent(String parkName) {
		return discountDAO.getApprovedDiscountPercent(parkName);
	}

	public ArrayList<ArrayList<Object>> getPendingParkChangeRequests() {
		return parkDAO.getPendingParkChangeRequests();
	}

	public boolean approveParkChangeRequest(int requestId, String decisionBy) {
		return parkDAO.approveParkChangeRequest(requestId, decisionBy);
	}

	public boolean rejectParkChangeRequest(int requestId, String decisionBy) {
		return parkDAO.rejectParkChangeRequest(requestId, decisionBy);
	}

	public int getCurrentVisitorsInPark(String parkName) {
		return bookingDAO.getCurrentVisitorsInPark(parkName);
	}

	public boolean setBookingStatus(int bookingId, String status) {
		return bookingDAO.setBookingStatus(bookingId, status);
	}
	public boolean checkInBooking(int bookingId) {
		return bookingDAO.checkInBooking(bookingId);
	}

	public java.util.HashMap<String, Integer> reportVisitorsByType(String park, int year, int month) {
		return reportDAO.reportVisitorsByType(park, year, month);
	}
	public ArrayList<ArrayList<Object>> reportDetailedVisits(String park, int year, int month) {
		return reportDAO.reportDetailedVisits(park, year, month);
	}
	public ArrayList<ArrayList<Object>> reportParkNotFull(String park, int year, int month) {
		return reportDAO.reportParkNotFull(park, year, month);
	}

	public ArrayList<Object> reportCancellations(String park, int year, int month) {
		return reportDAO.reportCancellations(park, year, month);
	}
}