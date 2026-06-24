package server.dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import common.Booking;
/**
 * Handles all booking-related database operations in the GoNature system.
 *
 * This class is responsible for creating, updating, retrieving,
 * cancelling and managing bookings. It also manages booking confirmations,
 * check-in/check-out operations, visitor counting and booking notifications.
 *
 * The class communicates directly with the database using JDBC.
 *
 * @author Group 4
 * @version 1.0
 */
public class BookingDAO {
	/**
	 * Active database connection.
	 */
	private final Connection connection;
	/**
	 * DAO used for park-related operations.
	 */
	private final ParkDAO parkDAO;
	/**
	 * DAO used for visitor-related operations.
	 */
	private final VisitorDAO visitorDAO;
	/**
	 * DAO used for notification-related operations.
	 */
	private final NotificationDAO notificationDAO;
	/**
	 * Creates a new BookingDAO instance.
	 *
	 * @param connection active database connection
	 * @param parkDAO park data access object
	 * @param visitorDAO visitor data access object
	 * @param notificationDAO notification data access object
	 */
	public BookingDAO(Connection connection, ParkDAO parkDAO, VisitorDAO visitorDAO,
			NotificationDAO notificationDAO) {
		this.connection = connection;
		this.parkDAO = parkDAO;
		this.visitorDAO = visitorDAO;
		this.notificationDAO = notificationDAO;
	}
	/**
	 * Generates a unique booking confirmation code.
	 *
	 * @return generated confirmation code
	 */
	public String generateConfirmationCode() {
		for (int i = 0; i < 20; i++) {
			String code = String.valueOf(100000 + (int) (Math.random() * 900000));

			String q = "SELECT booking_id FROM bookings WHERE confirmation_code = ? LIMIT 1";

			try (PreparedStatement ps = connection.prepareStatement(q)) {
				ps.setString(1, code);

				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next()) {
						return code;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return String.valueOf(System.currentTimeMillis()).substring(7);
	}
	/**
	 * Saves a booking in the database.
	 *
	 * @param b booking object
	 * @return true if the booking was saved successfully, otherwise false
	 */
	public boolean saveBooking(Booking b) {
		return saveBookingAndReturnCode(b) != null;
	}
	/**
	 * Saves a booking and returns its generated confirmation code.
	 *
	 * @param b booking object
	 * @return confirmation code if successful, otherwise null
	 */
	public String saveBookingAndReturnCode(Booking b) {
		String code = generateConfirmationCode();

		boolean casualVisitor = "CASUAL".equals(b.getVisitorId());
		boolean guideGroup = b.isGuideGroup() && (casualVisitor || visitorDAO.isVisitorGuide(b.getVisitorId()));
		boolean subscriber = !casualVisitor && visitorDAO.isVisitorSubscriber(b.getVisitorId());
		String query = "INSERT INTO bookings "
				+ "(visitor_id, park_name, visit_date, visit_time, visitors_count, email, telephone, status, total_price, booking_type, confirmation_code, is_guide_group, is_subscriber) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, b.getVisitorId());
			pstmt.setString(2, b.getParkName());
			pstmt.setDate(3, Date.valueOf(b.getVisitDate()));
			pstmt.setTime(4, Time.valueOf(b.getVisitTime()));
			pstmt.setInt(5, b.getVisitorsCount());
			pstmt.setString(6, b.getEmail());
			pstmt.setString(7, b.getTelephone() == null ? "" : b.getTelephone());
			pstmt.setString(8, b.getStatus());
			pstmt.setInt(9, b.getPrice());
			pstmt.setString(10, guideGroup ? "Guide" : "Regular Visitor");
			pstmt.setString(11, code);
			pstmt.setInt(12, guideGroup ? 1 : 0);
			pstmt.setInt(13, subscriber ? 1 : 0);

			if (pstmt.executeUpdate() > 0) {
				int bookingId = -1;

				try (ResultSet keys = pstmt.getGeneratedKeys()) {
					if (keys.next()) {
						bookingId = keys.getInt(1);
					}
				}

				if (!"CASUAL".equals(b.getVisitorId())) {
					String message = "Booking approved.\n"
							+ "Park: " + b.getParkName() + "\n"
							+ "Date: " + b.getVisitDate() + "\n"
							+ "Time: " + b.getVisitTime() + "\n"
							+ "Visitors: " + b.getVisitorsCount() + "\n"
							+ "Confirmation Code: " + code + "\n"
							+ "Total Price: " + b.getPrice() + " ILS";

					String phoneToSend = (b.getTelephone() != null && !b.getTelephone().isEmpty()) ? b.getTelephone() : visitorDAO.getVisitorPhone(b.getVisitorId());
					
					notificationDAO.createNotification(
							b.getVisitorId(),
							bookingId == -1 ? null : bookingId,
							"BOOKING_CONFIRMATION",
							message,
							b.getEmail(),
							phoneToSend
					);
				}

				return code;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * Retrieves a booking from the database by its booking ID.
	 *
	 * @param bookingId booking identifier
	 * @return booking object if found, otherwise null
	 */
	public Booking getBookingById(int bookingId) {
		String q = "SELECT * FROM bookings WHERE booking_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setInt(1, bookingId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Booking b = new Booking(rs.getInt("booking_id"), rs.getString("visitor_id"),
							rs.getString("park_name"), rs.getDate("visit_date").toLocalDate(),
							rs.getTime("visit_time").toLocalTime(), rs.getInt("visitors_count"),
							rs.getString("status"));
					b.setPrice(rs.getInt("total_price"));
					b.setVisitorType(rs.getString("booking_type"));
					b.setEmail(rs.getString("email"));
					b.setTelephone(rs.getString("telephone"));
					b.setGuideGroup(rs.getInt("is_guide_group") == 1);
					b.setSubscriber(rs.getInt("is_subscriber") == 1);
					return b;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Retrieves a booking from the database by its confirmation code.
	 *
	 * @param code booking confirmation code
	 * @return booking object if found, otherwise null
	 */
	public Booking getBookingByConfirmationCode(String code) {
		String q = "SELECT * FROM bookings WHERE confirmation_code = ?";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, code);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Booking b = new Booking(
							rs.getInt("booking_id"),
							rs.getString("visitor_id"),
							rs.getString("park_name"),
							rs.getDate("visit_date").toLocalDate(),
							rs.getTime("visit_time").toLocalTime(),
							rs.getInt("visitors_count"),
							rs.getString("status"));

					b.setPrice(rs.getInt("total_price"));
					b.setVisitorType(rs.getString("booking_type"));
					b.setEmail(rs.getString("email"));
					b.setTelephone(rs.getString("telephone"));
					b.setGuideGroup(rs.getInt("is_guide_group") == 1);
					b.setSubscriber(rs.getInt("is_subscriber") == 1);
					return b;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * Updates an existing booking in the database.
	 *
	 * @param b booking object with updated details
	 * @return true if the booking was updated successfully, otherwise false
	 */
	public boolean updateBooking(Booking b) {
		String query = "UPDATE bookings SET park_name=?, visit_date=?, visit_time=?, visitors_count=?, email=?, telephone=?, total_price=?, booking_type=?, is_guide_group=?, is_subscriber=? WHERE booking_id=? AND visitor_id=? AND status != 'Cancelled'";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			boolean guideGroup = b.isGuideGroup() && visitorDAO.isVisitorGuide(b.getVisitorId());
			boolean subscriber = visitorDAO.isVisitorSubscriber(b.getVisitorId());

			pstmt.setString(1, b.getParkName());
			pstmt.setDate(2, Date.valueOf(b.getVisitDate()));
			pstmt.setTime(3, Time.valueOf(b.getVisitTime()));
			pstmt.setInt(4, b.getVisitorsCount());
			pstmt.setString(5, b.getEmail());
			pstmt.setString(6, b.getTelephone() == null ? "" : b.getTelephone());
			pstmt.setInt(7, b.getPrice());
			pstmt.setString(8, guideGroup ? "Guide" : "Regular Visitor");
			pstmt.setInt(9, guideGroup ? 1 : 0);
			pstmt.setInt(10, subscriber ? 1 : 0);
			pstmt.setInt(11, b.getBookingId());
			pstmt.setString(12, b.getVisitorId());
			return pstmt.executeUpdate() > 0;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * Registers visitor exit for a booking.
	 *
	 * @param bookingId booking identifier
	 * @return true if the exit was registered successfully, otherwise false
	 */
	public boolean exitBooking(int bookingId) {
		String q = "UPDATE bookings SET status = 'Exited', checkout_time = CURRENT_TIMESTAMP "
				+ "WHERE booking_id = ? AND status = 'Entered'";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setInt(1, bookingId);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Confirms a visitor's arrival before the confirmation deadline.
	 *
	 * @param bookingId booking identifier
	 * @return true if the booking was confirmed successfully, otherwise false
	 */
	public boolean confirmArrival(int bookingId) {
		String q = "UPDATE bookings "
				+ "SET status = 'Confirmed' "
				+ "WHERE booking_id = ? "
				+ "AND status = 'Pending' "
				+ "AND reminder_sent_at IS NOT NULL "
				+ "AND confirmation_deadline IS NOT NULL "
				+ "AND CURRENT_TIMESTAMP <= confirmation_deadline";

		try (PreparedStatement pstmt = connection.prepareStatement(q)) {
			pstmt.setInt(1, bookingId);
			return pstmt.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Registers visitor entry into a park.
	 *
	 * @param bookingId booking identifier
	 * @return true if the check-in operation succeeded, otherwise false
	 */
	public boolean checkInBooking(int bookingId) {
		String q = "UPDATE bookings "
				+ "SET status = 'Entered', checkin_time = CURRENT_TIMESTAMP "
				+ "WHERE booking_id = ? "
				+ "AND status IN ('Pending', 'Confirmed')";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setInt(1, bookingId);
			return ps.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Updates the status of a booking.
	 *
	 * @param bookingId booking identifier
	 * @param status new booking status
	 * @return true if the update succeeded, otherwise false
	 */
	public boolean setBookingStatus(int bookingId, String status) {
		String q = "UPDATE bookings SET status = ? WHERE booking_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, status);
			ps.setInt(2, bookingId);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Processes pending booking confirmations.
	 *
	 * Sends reminder notifications for upcoming visits and automatically
	 * cancels bookings that were not confirmed before the deadline.
	 */
	public void processBookingConfirmations() {
		try {
			String remindersQuery = "SELECT booking_id, visitor_id, park_name, visit_date, visit_time, visitors_count, email, telephone "
					+ "FROM bookings "
					+ "WHERE status = 'Pending' "
					+ "AND visit_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY) "
					+ "AND reminder_sent_at IS NULL";

			try (PreparedStatement selectPs = connection.prepareStatement(remindersQuery);
					ResultSet rs = selectPs.executeQuery()) {

				while (rs.next()) {
					int bookingId = rs.getInt("booking_id");
					String visitorId = rs.getString("visitor_id");
					String parkName = rs.getString("park_name");
					String visitDate = String.valueOf(rs.getDate("visit_date"));
					String visitTime = String.valueOf(rs.getTime("visit_time"));
					int visitorsCount = rs.getInt("visitors_count");
					String email = rs.getString("email");
					String telephone = rs.getString("telephone");

					String updateReminder = "UPDATE bookings "
							+ "SET reminder_sent_at = CURRENT_TIMESTAMP, "
							+ "confirmation_deadline = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 HOUR) "
							+ "WHERE booking_id = ? "
							+ "AND status = 'Pending' "
							+ "AND reminder_sent_at IS NULL";

					try (PreparedStatement updatePs = connection.prepareStatement(updateReminder)) {
						updatePs.setInt(1, bookingId);

						if (updatePs.executeUpdate() > 0) {
							String message = "Reminder: your visit is tomorrow.\n"
									+ "Park: " + parkName + "\n"
									+ "Date: " + visitDate + "\n"
									+ "Time: " + visitTime + "\n"
									+ "Visitors: " + visitorsCount + "\n"
									+ "Please confirm or cancel within 2 hours.";

							String phoneToSend = (telephone != null && !telephone.isEmpty()) ? telephone : visitorDAO.getVisitorPhone(visitorId);
							notificationDAO.createNotification(visitorId, bookingId, "VISIT_REMINDER",
									message, email, phoneToSend);
						}
					}
				}
			}

			String expiredQuery = "SELECT booking_id, visitor_id, park_name, visit_date, visit_time, visitors_count, email, telephone "
					+ "FROM bookings "
					+ "WHERE status = 'Pending' "
					+ "AND reminder_sent_at IS NOT NULL "
					+ "AND confirmation_deadline IS NOT NULL "
					+ "AND confirmation_deadline <= CURRENT_TIMESTAMP";

			try (PreparedStatement selectPs = connection.prepareStatement(expiredQuery);
					ResultSet rs = selectPs.executeQuery()) {

				while (rs.next()) {
					int bookingId = rs.getInt("booking_id");
					String visitorId = rs.getString("visitor_id");
					String parkName = rs.getString("park_name");
					String visitDate = String.valueOf(rs.getDate("visit_date"));
					String visitTime = String.valueOf(rs.getTime("visit_time"));
					int visitorsCount = rs.getInt("visitors_count");
					String email = rs.getString("email");
					String telephone = rs.getString("telephone");

					String updateCancel = "UPDATE bookings "
							+ "SET status = 'Cancelled', cancelled_at = CURRENT_TIMESTAMP "
							+ "WHERE booking_id = ? "
							+ "AND status = 'Pending' "
							+ "AND confirmation_deadline <= CURRENT_TIMESTAMP";

					try (PreparedStatement updatePs = connection.prepareStatement(updateCancel)) {
						updatePs.setInt(1, bookingId);

						if (updatePs.executeUpdate() > 0) {
							String message = "Your booking was automatically cancelled because you did not confirm in time.\n"
									+ "Park: " + parkName + "\n"
									+ "Date: " + visitDate + "\n"
									+ "Time: " + visitTime + "\n"
									+ "Visitors: " + visitorsCount;

							String phoneToSend = (telephone != null && !telephone.isEmpty()) ? telephone : visitorDAO.getVisitorPhone(visitorId);
							notificationDAO.createNotification(visitorId, bookingId, "AUTO_CANCEL",
									message, email, phoneToSend);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Counts the number of visitors expected in a park at a specific date and time.
	 *
	 * The calculation includes active bookings and valid waiting-list entries.
	 *
	 * @param parkName park name
	 * @param date visit date
	 * @param time visit time
	 * @return total number of visitors
	 */
	public int countVisitorsAt(String parkName, LocalDate date, LocalTime time) {
		int total = 0;
		String q1 = "SELECT COALESCE(SUM(visitors_count), 0) " + "FROM bookings " + "WHERE park_name = ? "
				+ "AND visit_date = ? " + "AND status IN ('Pending', 'Confirmed', 'Entered') "
				+ "AND visit_time < ADDTIME(?, SEC_TO_TIME(? * 3600)) "
				+ "AND ADDTIME(visit_time, SEC_TO_TIME(? * 3600)) > ?";
		try (PreparedStatement ps = connection.prepareStatement(q1)) {
			int duration = parkDAO.getParkVisitDurationHours(parkName);

			ps.setString(1, parkName);
			ps.setDate(2, Date.valueOf(date));
			ps.setTime(3, Time.valueOf(time));
			ps.setInt(4, duration);
			ps.setInt(5, duration);
			ps.setTime(6, Time.valueOf(time));
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				total += rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String q2 = "SELECT COALESCE(SUM(visitors_count), 0) "
				+ "FROM waitinglist "
				+ "WHERE park_name = ? "
				+ "AND visit_date = ? "
				+ "AND notified_time IS NOT NULL "
				+ "AND TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) < 120 "
				+ "AND TIMESTAMP(visit_date, visit_time) > CURRENT_TIMESTAMP "
				+ "AND visit_time < ADDTIME(?, SEC_TO_TIME(? * 3600)) "
				+ "AND ADDTIME(visit_time, SEC_TO_TIME(? * 3600)) > ?";

		try (PreparedStatement ps = connection.prepareStatement(q2)) {
			int duration = parkDAO.getParkVisitDurationHours(parkName);

			ps.setString(1, parkName);
			ps.setDate(2, Date.valueOf(date));
			ps.setTime(3, Time.valueOf(time));
			ps.setInt(4, duration);
			ps.setInt(5, duration);
			ps.setTime(6, Time.valueOf(time));

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				total += rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return total;
	}
	/**
	 * Retrieves the current number of visitors inside a specific park.
	 *
	 * @param parkName park name
	 * @return current number of visitors in the park
	 */
	public int getCurrentVisitorsInPark(String parkName) {
		String q = "SELECT COALESCE(SUM(visitors_count),0) FROM bookings WHERE park_name = ? AND status = 'Entered'";
		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, parkName);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	/**
	 * Retrieves all bookings of a specific visitor.
	 *
	 * @param visitorId visitor identifier
	 * @return list of visitor bookings
	 */
	public ArrayList<Booking> getActiveBookings(String visitorId) {
		ArrayList<Booking> list = new ArrayList<>();

		String query = "SELECT * FROM bookings "
				+ "WHERE visitor_id = ? "
				+ "ORDER BY visit_date, visit_time";

		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, visitorId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Booking b = new Booking(
							rs.getInt("booking_id"),
							rs.getString("visitor_id"),
							rs.getString("park_name"),
							rs.getDate("visit_date").toLocalDate(),
							rs.getTime("visit_time").toLocalTime(),
							rs.getInt("visitors_count"),
							rs.getString("status")
					);

					b.setPrice(rs.getInt("total_price"));
					b.setVisitorType(rs.getString("booking_type"));
					b.setEmail(rs.getString("email"));
					b.setTelephone(rs.getString("telephone"));
					b.setGuideGroup(rs.getInt("is_guide_group") == 1);
					b.setSubscriber(rs.getInt("is_subscriber") == 1);

					list.add(b);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}
	
	public int cancelBookingAndReturnRefund(int bookingId, String visitorId) {
		String priceQuery = "SELECT total_price FROM bookings "
				+ "WHERE booking_id = ? "
				+ "AND visitor_id = ? "
				+ "AND status != 'Cancelled'";

		try (PreparedStatement pstmt = connection.prepareStatement(priceQuery)) {
			pstmt.setInt(1, bookingId);
			pstmt.setString(2, visitorId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					int refund = rs.getInt("total_price");

					String updateQuery = "UPDATE bookings "
							+ "SET status = 'Cancelled', cancelled_at = CURRENT_TIMESTAMP "
							+ "WHERE booking_id = ?";

					try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
						ps.setInt(1, bookingId);
						ps.executeUpdate();
					}

					return refund;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}
}