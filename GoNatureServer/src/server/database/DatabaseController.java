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
/**
 * Controls all database-related operations in the GoNature system.
 * This class connects the server to the MySQL database and coordinates
 * the different DAO classes that handle visitors, employees, bookings,
 * parks, discounts, waiting lists, notifications and reports.
 *
 * The server uses this class as a central access point to the data layer.
 *
 * @author Group 4
 * @version 1.0
 */
public class DatabaseController {
	/**
	 * Active connection to the MySQL database.
	 */
	private Connection connection;
	/**
	 * DAO used for notification-related database operations.
	 */
	private NotificationDAO notificationDAO;
	/**
	 * DAO used for employee-related database operations.
	 */
	private EmployeeDAO employeeDAO;
	/**
	 * DAO used for visitor-related database operations.
	 */
	private VisitorDAO visitorDAO;
	/**
	 * DAO used for park-related database operations.
	 */
	private ParkDAO parkDAO;
	/**
	 * DAO used for discount-related database operations.
	 */
	private DiscountDAO discountDAO;
	/**
	 * DAO used for booking-related database operations.
	 */
	private BookingDAO bookingDAO;
	/**
	 * DAO used for waiting-list-related database operations.
	 */
	private WaitingListDAO waitingListDAO;
	/**
	 * DAO used for report-related database operations.
	 */
	private ReportDAO reportDAO;
	/**
	 * Connects to the GoNature MySQL database and initializes all DAO objects.
	 *
	 * @param host database host address
	 * @param user database username
	 * @param pass database password
	 * @return true if the database connection succeeded, otherwise false
	 */
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
	/**
	 * Retrieves profile information for a user (visitor or employee).
	 *
	 * @param userId user identifier
	 * @return list of user info strings, or null if not found
	 */
	public ArrayList<String> getUserInfo(String userId) {
		ArrayList<String> info = visitorDAO.getVisitorInfo(userId);
		if (info != null) {
			return info;
		}
		return employeeDAO.getEmployeeInfo(userId);
	}
	/**
	 * Registers a guest visitor in the database.
	 *
	 * @param visitorId visitor identifier
	 * @return String status: ALREADY_EXISTS, REGISTERED, or FAILED
	 */
	public String registerGuest(String visitorId) {
		return visitorDAO.registerGuest(visitorId);
	}
	/**
	 * Updates a visitor's profile information.
	 *
	 * @param visitorId visitor identifier
	 * @param fullName new full name
	 * @param email new email
	 * @param phone new phone
	 * @return true if successful, false otherwise
	 */
	public boolean updateVisitorProfile(String visitorId, String fullName, String email, String phone) {
		return visitorDAO.updateProfile(visitorId, fullName, email, phone);
	}
	/**
	 * Verifies the password of a certified guide.
	 *
	 * @param visitorId guide identifier
	 * @param password guide password
	 * @return true if password matches, false otherwise
	 */
	public boolean verifyGuidePassword(String visitorId, String password) {
		return visitorDAO.verifyGuidePassword(visitorId, password);
	}
	/**
	 * Counts the number of visitors scheduled for a specific park, date and time.
	 *
	 * @param parkName park name
	 * @param date visit date
	 * @param time visit time
	 * @return number of visitors already booked
	 */
	public int countVisitorsAt(String parkName, LocalDate date, LocalTime time) {
		return bookingDAO.countVisitorsAt(parkName, date, time);
	}
	/**
	 * Authenticates a visitor using username and password.
	 *
	 * @param username visitor username
	 * @param password visitor password
	 * @return login result data
	 */
	public String[] loginVisitor(String username, String password) {
		return visitorDAO.loginVisitor(username, password);
	}
	/**
	 * Authenticates a visitor using ONLY their ID (No password required).
	 * * @param visitorId visitor identifier
	 * @return login result data
	 */
	public String[] loginVisitorById(String visitorId) {
		return visitorDAO.loginVisitorById(visitorId);
	}
	/**
	 * Authenticates an employee using employee ID and password.
	 *
	 * @param empId employee identifier
	 * @param password employee password
	 * @return login result data
	 */
	public String[] loginEmployee(String empId, String password) {
		return employeeDAO.loginEmployee(empId, password);
	}
	/**
	 * Registers a visitor subscription in the system.
	 *
	 * @param visitorId visitor identifier
	 * @param firstName subscriber first name
	 * @param lastName subscriber last name
	 * @param phone subscriber phone number
	 * @param email subscriber email address
	 * @param familyMembers number of family members included in the subscription
	 * @param paymentMethod payment method
	 * @param creditCard credit card details
	 * @return generated subscription ID, or a non-positive value if registration failed
	 */
	public int buySubscription(String visitorId, String firstName, String lastName, String phone, String email,
			int familyMembers, String paymentMethod, String creditCard) {
		return visitorDAO.buySubscription(visitorId, firstName, lastName, phone, email,
				familyMembers, paymentMethod, creditCard);
	}
	/**
	 * Registers a new visitor in the system.
	 *
	 * @param visitorId visitor identifier
	 * @param username visitor username
	 * @param password visitor password
	 * @param email visitor email address
	 * @param phone visitor phone number
	 * @return registration result message
	 */
	public String registerVisitor(String visitorId, String username, String password, String email, String phone) {
		return visitorDAO.registerVisitor(visitorId, username, password, email, phone);
	}
	/**
	 * Registers a visitor as a guide or updates an existing guide account.
	 *
	 * @param visitorId guide identifier
	 * @param password guide password
	 * @param username guide username
	 * @return operation result message
	 */
	public String registerOrUpdateGuide(String visitorId, String password, String username) {
		return visitorDAO.registerOrUpdateGuide(visitorId, password, username);
	}
	/**
	 * Saves a booking in the database.
	 *
	 * @param b booking object
	 * @return true if the booking was saved successfully, otherwise false
	 */
	public boolean saveBooking(Booking b) {
		return bookingDAO.saveBooking(b);
	}
	/**
	 * Saves a booking and generates a confirmation code.
	 *
	 * @param b booking object
	 * @return confirmation code if successful, otherwise null
	 */
	public String saveBookingAndReturnCode(Booking b) {
		return bookingDAO.saveBookingAndReturnCode(b);
	}
	/**
	 * Checks whether a visitor has an active subscription.
	 *
	 * @param visitorId visitor identifier
	 * @return true if the visitor is a subscriber, otherwise false
	 */
	public boolean isVisitorSubscriber(String visitorId) {
		return visitorDAO.isVisitorSubscriber(visitorId);
	}
	/**
	 * Checks whether a visitor is registered as a guide.
	 *
	 * @param visitorId visitor identifier
	 * @return true if the visitor is a guide, otherwise false
	 */
	public boolean isVisitorGuide(String visitorId) {
		return visitorDAO.isVisitorGuide(visitorId);
	}
	/**
	 * Retrieves the phone number of a visitor.
	 *
	 * @param visitorId visitor identifier
	 * @return visitor phone number
	 */
	public String getVisitorPhone(String visitorId) {
		return visitorDAO.getVisitorPhone(visitorId);
	}
	/**
	 * Creates a notification for a visitor.
	 *
	 * @param visitorId visitor identifier
	 * @param bookingId related booking identifier
	 * @param notificationType notification type
	 * @param messageText notification message
	 * @param email visitor email address
	 * @param phone visitor phone number
	 */
	public void createNotification(String visitorId, Integer bookingId, String notificationType,
			String messageText, String email, String phone) {
		notificationDAO.createNotification(visitorId, bookingId, notificationType, messageText, email, phone);
	}
	/**
	 * Retrieves all notifications associated with a visitor.
	 *
	 * @param visitorId visitor identifier
	 * @return list of visitor notifications
	 */
	public ArrayList<ArrayList<Object>> getVisitorNotifications(String visitorId) {
		return notificationDAO.getVisitorNotifications(visitorId);
	}
	/**
	 * Generates a unique booking confirmation code.
	 *
	 * @return generated confirmation code
	 */
	public String generateConfirmationCode() {
		return bookingDAO.generateConfirmationCode();
	}
	/**
	 * Adds a booking request to the waiting list.
	 *
	 * @param b booking object
	 * @return true if added successfully, otherwise false
	 */
	public boolean enterWaitingList(Booking b) {
		return waitingListDAO.enterWaitingList(b);
	}
	/**
	 * Retrieves all active bookings and waiting-list entries of a visitor.
	 *
	 * @param visitorId visitor identifier
	 * @return list of visitor bookings
	 */
	public ArrayList<Booking> getUserBookings(String visitorId) {
		ArrayList<Booking> all = bookingDAO.getActiveBookings(visitorId);
		all.addAll(waitingListDAO.getWaitingEntriesAsBookings(visitorId));
		return all;
	}
	/**
	 * Updates an existing booking.
	 *
	 * @param b booking object
	 * @return true if the update succeeded, otherwise false
	 */
	public boolean updateBooking(Booking b) {
		return bookingDAO.updateBooking(b);
	}
	/**
	 * Registers the exit of visitors from a park.
	 *
	 * @param bookingId booking identifier
	 * @return true if the operation succeeded, otherwise false
	 */
	public boolean exitBooking(int bookingId) {
		return bookingDAO.exitBooking(bookingId);
	}
	/**
	 * Confirms visitor arrival for a booking.
	 *
	 * @param bookingId booking identifier
	 * @return true if confirmation succeeded, otherwise false
	 */
	public boolean confirmArrival(int bookingId) {
		return bookingDAO.confirmArrival(bookingId);
	}
	/**
	 * Processes pending booking confirmations and updates their status.
	 */
	public void processBookingConfirmations() {
		bookingDAO.processBookingConfirmations();
	}
	/**
	 * Cancels a booking or removes a waiting-list entry.
	 *
	 * @param id booking or waiting-list identifier
	 * @param visitorId visitor identifier
	 * @return refund amount or operation result code
	 */
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
	/**
	 * Manages the waiting-list queue and promotes eligible visitors.
	 */
	public void manageWaitingListQueue() {
		waitingListDAO.manageWaitingListQueue();
	}
	/**
	 * Retrieves waiting-list messages for a visitor.
	 *
	 * @param visitorId visitor identifier
	 * @return waiting-list notification data
	 */
	public ArrayList<Object> getWaitingListMessage(String visitorId) {
		return waitingListDAO.getWaitingListMessage(visitorId);
	}
	/**
	 * Allows a visitor to pay for and claim a waiting-list offer.
	 *
	 * @param waitingId waiting-list identifier
	 * @param price booking price
	 * @return true if successful, otherwise false
	 */
	public boolean payAndClaimWaitingList(int waitingId, int price) {
		return waitingListDAO.payAndClaimWaitingList(waitingId, price);
	}
	/**
	 * Allows a visitor to pay for and claim a waiting-list offer.
	 * Generates a confirmation code for the booking.
	 *
	 * @param waitingId waiting-list identifier
	 * @param price booking price
	 * @return generated confirmation code if successful, otherwise null
	 */
	public String payAndClaimWaitingListAndReturnCode(int waitingId, int price) {
		return waitingListDAO.payAndClaimWaitingListAndReturnCode(waitingId, price);
	}
	/**
	 * Declines a waiting-list offer.
	 *
	 * @param waitingId waiting-list identifier
	 */
	public void declineWaitingList(int waitingId) {
		waitingListDAO.declineWaitingList(waitingId);
	}
	/**
	 * Retrieves a booking by its identifier.
	 *
	 * @param bookingId booking identifier
	 * @return booking object if found, otherwise null
	 */
	public Booking getBookingById(int bookingId) {
		return bookingDAO.getBookingById(bookingId);
	}
	/**
	 * Retrieves a booking by its confirmation code.
	 *
	 * @param code booking confirmation code
	 * @return booking object if found, otherwise null
	 */
	public Booking getBookingByConfirmationCode(String code) {
		return bookingDAO.getBookingByConfirmationCode(code);
	}
	/**
	 * Retrieves the maximum capacity of a park.
	 *
	 * @param parkName park name
	 * @return park capacity
	 */
	public int getParkCapacity(String parkName) {
		return parkDAO.getParkCapacity(parkName);
	}
	/**
	 * Retrieves the allowed booking percentage of a park.
	 *
	 * @param parkName park name
	 * @return booking percentage
	 */
	public int getParkBookingPercent(String parkName) {
		return parkDAO.getParkBookingPercent(parkName);
	}
	/**
	 * Retrieves the standard visit duration of a park.
	 *
	 * @param parkName park name
	 * @return visit duration in hours
	 */
	public int getParkVisitDurationHours(String parkName) {
		return parkDAO.getParkVisitDurationHours(parkName);
	}
	/**
	 * Calculates the number of places that can be booked in advance for a park.
	 *
	 * @param parkName park name
	 * @return bookable capacity
	 */
	public int getBookableCapacity(String parkName) {
		return parkDAO.getBookableCapacity(parkName);
	}
	/**
	 * Updates the maximum capacity of a park.
	 *
	 * @param parkName park name
	 * @param newCapacity new park capacity
	 * @return true if the update succeeded, otherwise false
	 */
	public boolean updateParkCapacity(String parkName, int newCapacity) {
		return parkDAO.updateParkCapacity(parkName, newCapacity);
	}
	/**
	 * Retrieves the role of an employee.
	 *
	 * @param empId employee identifier
	 * @return employee role
	 */
	public String getEmployeeRole(String empId) {
		return employeeDAO.getEmployeeRole(empId);
	}
	/**
	 * Checks whether an employee has a specific role.
	 *
	 * @param empId employee identifier
	 * @param role role to check
	 * @return true if the employee has the given role, otherwise false
	 */
	public boolean isEmployeeRole(String empId, String role) {
		return employeeDAO.isEmployeeRole(empId, role);
	}
	/**
	 * Retrieves all parameters of a park.
	 *
	 * @param parkName park name
	 * @return park parameters
	 */
	public ArrayList<Object> getParkParams(String parkName) {
		return parkDAO.getParkParams(parkName);
	}
	/**
	 * Creates a request to change park parameters.
	 *
	 * @param parkName park name
	 * @param newCapacity requested new capacity
	 * @param newBookingPercent requested new booking percentage
	 * @param newVisitDurationHours requested new visit duration
	 * @param requestedBy employee who created the request
	 * @return true if the request was created successfully, otherwise false
	 */
	public boolean createParkChangeRequest(String parkName, int newCapacity, int newBookingPercent,
			int newVisitDurationHours, String requestedBy) {
		return parkDAO.createParkChangeRequest(parkName, newCapacity, newBookingPercent,
				newVisitDurationHours, requestedBy);
	}
	/**
	 * Creates a discount request for a park.
	 *
	 * @param parkName park name
	 * @param discountName discount name
	 * @param discountPercent requested discount percentage
	 * @param startDate discount start date
	 * @param endDate discount end date
	 * @param requestedBy employee who created the request
	 * @return true if the request was created successfully, otherwise false
	 */	
	public boolean createDiscountRequest(String parkName, String discountName, int discountPercent, String startDate, String endDate, String requestedBy) {
		return discountDAO.createDiscountRequest(parkName, discountName, discountPercent, startDate, endDate, requestedBy);
	}
	/**
	 * Retrieves all pending discount requests.
	 *
	 * @return list of pending discount requests
	 */	
	public ArrayList<ArrayList<Object>> getPendingDiscountRequests() {
		return discountDAO.getPendingDiscountRequests();
	}
	/**
	 * Approves a discount request.
	 *
	 * @param requestId discount request identifier
	 * @param decisionBy employee who approved the request
	 * @return true if the request was approved successfully, otherwise false
	 */
	public boolean approveDiscountRequest(int requestId, String decisionBy) {
		return discountDAO.approveDiscountRequest(requestId, decisionBy);
	}
	/**
	 * Rejects a discount request.
	 *
	 * @param requestId discount request identifier
	 * @param decisionBy employee who rejected the request
	 * @return true if the request was rejected successfully, otherwise false
	 */
	public boolean rejectDiscountRequest(int requestId, String decisionBy) {
		return discountDAO.rejectDiscountRequest(requestId, decisionBy);
	}
	/**
	 * Retrieves the currently approved discount percentage for a park.
	 *
	 * @param parkName park name
	 * @return approved discount percentage
	 */
	public int getApprovedDiscountPercent(String parkName) {
		return discountDAO.getApprovedDiscountPercent(parkName);
	}
	/**
	 * Retrieves all pending park change requests.
	 *
	 * @return list of pending park change requests
	 */
	public ArrayList<ArrayList<Object>> getPendingParkChangeRequests() {
		return parkDAO.getPendingParkChangeRequests();
	}
	/**
	 * Approves a park change request.
	 *
	 * @param requestId request identifier
	 * @param decisionBy employee who approved the request
	 * @return true if the request was approved successfully, otherwise false
	 */
	public boolean approveParkChangeRequest(int requestId, String decisionBy) {
		return parkDAO.approveParkChangeRequest(requestId, decisionBy);
	}
	/**
	 * Rejects a park change request.
	 *
	 * @param requestId request identifier
	 * @param decisionBy employee who rejected the request
	 * @return true if the request was rejected successfully, otherwise false
	 */
	public boolean rejectParkChangeRequest(int requestId, String decisionBy) {
		return parkDAO.rejectParkChangeRequest(requestId, decisionBy);
	}
	/**
	 * Retrieves the current number of visitors inside a park.
	 *
	 * @param parkName park name
	 * @return current visitor count
	 */
	public int getCurrentVisitorsInPark(String parkName) {
		return bookingDAO.getCurrentVisitorsInPark(parkName);
	}
	/**
	 * Updates the status of a booking.
	 *
	 * @param bookingId booking identifier
	 * @param status new booking status
	 * @return true if the update succeeded, otherwise false
	 */
	public boolean setBookingStatus(int bookingId, String status) {
		return bookingDAO.setBookingStatus(bookingId, status);
	}
	/**
	 * Registers visitor entry into a park.
	 *
	 * @param bookingId booking identifier
	 * @return true if the check-in succeeded, otherwise false
	 */
	public boolean checkInBooking(int bookingId) {
		return bookingDAO.checkInBooking(bookingId);
	}
	/**
	 * Generates a visitor-type report for a specific park and month.
	 *
	 * @param park park name
	 * @param year report year
	 * @param month report month
	 * @return visitor statistics grouped by visitor type
	 */
	public java.util.HashMap<String, Integer> reportVisitorsByType(String park, int year, int month) {
		return reportDAO.reportVisitorsByType(park, year, month);
	}
	/**
	 * Generates a detailed visits report.
	 *
	 * @param park park name
	 * @param year report year
	 * @param month report month
	 * @return detailed visit report data
	 */
	public ArrayList<ArrayList<Object>> reportDetailedVisits(String park, int year, int month) {
		return reportDAO.reportDetailedVisits(park, year, month);
	}
	/**
	 * Generates a report showing park utilization below full capacity.
	 *
	 * @param park park name
	 * @param year report year
	 * @param month report month
	 * @return park utilization report
	 */
	public ArrayList<ArrayList<Object>> reportParkNotFull(String park, int year, int month) {
		return reportDAO.reportParkNotFull(park, year, month);
	}
	/**
	 * Generates a cancellation report for a specific park and month.
	 *
	 * @param park park name
	 * @param year report year
	 * @param month report month
	 * @return cancellation report data
	 */
	public ArrayList<Object> reportCancellations(String park, int year, int month) {
		return reportDAO.reportCancellations(park, year, month);
	}
}