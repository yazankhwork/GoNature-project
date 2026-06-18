package server.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.time.LocalDate;
import java.time.LocalTime;
import common.Booking;

public class DatabaseController {
	private Connection connection;

	public boolean connectToDatabase(String host, String user, String pass) {
		try {
			String url = "jdbc:mysql://" + host + "/gonature_db"
					+ "?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";
			connection = DriverManager.getConnection(url, user, pass);
			System.out.println("Database connected successfully!");
			return true;
		} catch (SQLException e) {
			System.err.println("DB Connection Error: " + e.getMessage());
			return false;
		}
	}

	public int countVisitorsAt(String parkName, LocalDate date, LocalTime time) {
		int total = 0;
		String q1 = "SELECT COALESCE(SUM(visitors_count), 0) " + "FROM bookings " + "WHERE park_name = ? "
				+ "AND visit_date = ? " + "AND status IN ('Pending', 'Confirmed', 'Entered') "
				+ "AND visit_time < ADDTIME(?, SEC_TO_TIME(? * 3600)) "
				+ "AND ADDTIME(visit_time, SEC_TO_TIME(? * 3600)) > ?";
		try (PreparedStatement ps = connection.prepareStatement(q1)) {
			int duration = getParkVisitDurationHours(parkName);

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
				+ "AND TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) < 60 "
				+ "AND TIMESTAMP(visit_date, visit_time) > CURRENT_TIMESTAMP "
				+ "AND visit_time < ADDTIME(?, SEC_TO_TIME(? * 3600)) "
				+ "AND ADDTIME(visit_time, SEC_TO_TIME(? * 3600)) > ?";

		try (PreparedStatement ps = connection.prepareStatement(q2)) {
			int duration = getParkVisitDurationHours(parkName);

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

	public String[] loginVisitor(String visitorId, String username) {
		String query = "SELECT username, is_guide, full_name FROM visitors WHERE visitor_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, visitorId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				if (rs.getString("username") != null && rs.getString("username").equals(username)) {
					String subNum = "NONE";
					try (PreparedStatement psSub = connection
							.prepareStatement("SELECT sub_id FROM subscriptions WHERE visitor_id = ?")) {
						psSub.setString(1, visitorId);
						ResultSet rsSub = psSub.executeQuery();
						if (rsSub.next())
							subNum = String.valueOf(rsSub.getInt("sub_id"));
					}
					return new String[] {
							(rs.getInt("is_guide") == 1) ? "LOGIN_SUCCESS_GUIDE" : "LOGIN_SUCCESS_REGULAR",
							rs.getString("full_name"), subNum };
				} else
					return new String[] { "WRONG_USERNAME", null, "NONE" };
			} else
				return new String[] { "USER_NOT_FOUND", null, "NONE" };
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "ERROR", null, "NONE" };
		}
	}

	public String[] loginEmployee(String empId, String password) {
		String query = "SELECT password, full_name, role FROM employees WHERE emp_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, empId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				if (rs.getString("password").equals(password)) {
					return new String[] { "LOGIN_SUCCESS_EMPLOYEE", rs.getString("full_name"), rs.getString("role") };
				} else
					return new String[] { "WRONG_PASSWORD", null, null };
			} else
				return new String[] { "USER_NOT_FOUND", null, null };
		} catch (Exception e) {
			return new String[] { "ERROR", null, null };
		}
	}

	public int buySubscription(String visitorId, String firstName, String lastName, String phone, String email,
			int familyMembers, String paymentMethod, String creditCard) {

		String fullName = firstName + " " + lastName;

		String insertQuery = "INSERT INTO subscriptions "
				+ "(visitor_id, first_name, last_name, full_name, phone, email, family_members, payment_method, credit_card) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, visitorId);
			pstmt.setString(2, firstName);
			pstmt.setString(3, lastName);
			pstmt.setString(4, fullName);
			pstmt.setString(5, phone);
			pstmt.setString(6, email);
			pstmt.setInt(7, familyMembers);
			pstmt.setString(8, paymentMethod);
			pstmt.setString(9, creditCard);

			pstmt.executeUpdate();

			String updateVisitor = "UPDATE visitors SET full_name = ?, email = ?, phone = ? WHERE visitor_id = ?";
			try (PreparedStatement updateStmt = connection.prepareStatement(updateVisitor)) {
				updateStmt.setString(1, fullName);
				updateStmt.setString(2, email);
				updateStmt.setString(3, phone);
				updateStmt.setString(4, visitorId);
				updateStmt.executeUpdate();
			}

			ResultSet rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	public String registerVisitor(String visitorId, String username, boolean isGuide, String fullName) {
		String checkQuery = "SELECT visitor_id FROM visitors WHERE visitor_id = ?";
		try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
			checkStmt.setString(1, visitorId);
			if (checkStmt.executeQuery().next())
				return "USER_ALREADY_EXISTS";
		} catch (Exception e) {
		}

		String insertQuery = "INSERT INTO visitors (visitor_id, username, password, email, is_guide, full_name) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
			insertStmt.setString(1, visitorId);
			insertStmt.setString(2, username);
			insertStmt.setString(3, "");
			insertStmt.setString(4, username.replaceAll("\\s+", "").toLowerCase() + "@gonature.com");
			insertStmt.setInt(5, isGuide ? 1 : 0);
			insertStmt.setString(6, fullName);
			if (insertStmt.executeUpdate() > 0)
				return "REGISTER_SUCCESS";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "REGISTER_FAILED";
	}

	// NEW: Logic to either INSERT a new guide or UPDATE an existing visitor to a
	// guide
	public String registerOrUpdateGuide(String visitorId, String password, String fullName) {
		String checkQuery = "SELECT visitor_id FROM visitors WHERE visitor_id = ?";
		boolean exists = false;
		try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
			checkStmt.setString(1, visitorId);
			exists = checkStmt.executeQuery().next();
		} catch (Exception e) {
		}

		if (exists) {
			String updateQuery = "UPDATE visitors SET is_guide = 1, password = ?, full_name = ? WHERE visitor_id = ?";
			try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
				updateStmt.setString(1, password);
				updateStmt.setString(2, fullName);
				updateStmt.setString(3, visitorId);
				if (updateStmt.executeUpdate() > 0)
					return "UPDATE_SUCCESS";
			} catch (Exception e) {
			}
		} else {
			String insertQuery = "INSERT INTO visitors (visitor_id, password, email, is_guide, full_name) VALUES (?, ?, ?, 1, ?)";
			try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
				insertStmt.setString(1, visitorId);
				insertStmt.setString(2, password);
				insertStmt.setString(3, fullName.replaceAll("\\s+", "").toLowerCase() + "@gonature.com");
				insertStmt.setString(4, fullName);
				if (insertStmt.executeUpdate() > 0)
					return "REGISTER_SUCCESS";
			} catch (Exception e) {
			}
		}
		return "FAILED";
	}

	public boolean saveBooking(Booking b) {
		return saveBookingAndReturnCode(b) != null;
	}

	public String saveBookingAndReturnCode(Booking b) {
		String code = generateConfirmationCode();

		boolean casualVisitor = "CASUAL".equals(b.getVisitorId());
		boolean guideGroup = b.isGuideGroup() && (casualVisitor || isVisitorGuide(b.getVisitorId()));
		boolean subscriber = !casualVisitor && isVisitorSubscriber(b.getVisitorId());
		String query = "INSERT INTO bookings "
				+ "(visitor_id, park_name, visit_date, visit_time, visitors_count, email, status, total_price, booking_type, confirmation_code, is_guide_group, is_subscriber) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, b.getVisitorId());
			pstmt.setString(2, b.getParkName());
			pstmt.setDate(3, Date.valueOf(b.getVisitDate()));
			pstmt.setTime(4, Time.valueOf(b.getVisitTime()));
			pstmt.setInt(5, b.getVisitorsCount());
			pstmt.setString(6, b.getEmail());
			pstmt.setString(7, b.getStatus());
			pstmt.setInt(8, b.getPrice());
			pstmt.setString(9, guideGroup ? "Guide" : "Regular Visitor");
			pstmt.setString(10, code);
			pstmt.setInt(11, guideGroup ? 1 : 0);
			pstmt.setInt(12, subscriber ? 1 : 0);

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

					createNotification(
							b.getVisitorId(),
							bookingId == -1 ? null : bookingId,
							"BOOKING_CONFIRMATION",
							message,
							b.getEmail(),
							getVisitorPhone(b.getVisitorId())
					);
				}

				return code;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	private boolean isVisitorSubscriber(String visitorId) {
		String query = "SELECT sub_id FROM subscriptions WHERE visitor_id = ?";

		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, visitorId);

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean isVisitorGuide(String visitorId) {
		String query = "SELECT is_guide FROM visitors WHERE visitor_id = ?";

		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, visitorId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("is_guide") == 1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	private String getVisitorPhone(String visitorId) {
		String q = "SELECT phone FROM visitors WHERE visitor_id = ?";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, visitorId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getString("phone");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	private void createNotification(String visitorId, Integer bookingId, String notificationType,
			String messageText, String email, String phone) {

		String q = "INSERT INTO notifications "
				+ "(visitor_id, booking_id, notification_type, message_text, email, phone, status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, 'Sent')";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, visitorId);

			if (bookingId == null) {
				ps.setNull(2, Types.INTEGER);
			} else {
				ps.setInt(2, bookingId);
			}

			ps.setString(3, notificationType);
			ps.setString(4, messageText);
			ps.setString(5, email);
			ps.setString(6, phone);

			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public ArrayList<ArrayList<Object>> getVisitorNotifications(String visitorId) {
		ArrayList<ArrayList<Object>> list = new ArrayList<>();

		String q = "SELECT notification_id, notification_type, message_text, email, phone, sent_at "
				+ "FROM notifications "
				+ "WHERE visitor_id = ? "
				+ "ORDER BY sent_at DESC, notification_id DESC "
				+ "LIMIT 10";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, visitorId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ArrayList<Object> row = new ArrayList<>();
					row.add(rs.getInt("notification_id"));
					row.add(rs.getString("notification_type"));
					row.add(rs.getString("message_text"));
					row.add(rs.getString("email"));
					row.add(rs.getString("phone"));
					row.add(String.valueOf(rs.getTimestamp("sent_at")));
					list.add(row);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}
	private String generateConfirmationCode() {
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
	public boolean enterWaitingList(Booking b) {
		String query = "INSERT INTO waitinglist (visitor_id, park_name, visit_date, visit_time, visitors_count, visitor_type, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, b.getVisitorId());
			pstmt.setString(2, b.getParkName());
			pstmt.setDate(3, Date.valueOf(b.getVisitDate()));
			pstmt.setTime(4, Time.valueOf(b.getVisitTime()));
			pstmt.setInt(5, b.getVisitorsCount());
			pstmt.setString(6, b.getVisitorType());
			pstmt.setString(7, b.getEmail());
			return pstmt.executeUpdate() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public ArrayList<Booking> getUserBookings(String visitorId) {
		ArrayList<Booking> list = new ArrayList<>();
		String query = "SELECT * FROM bookings WHERE visitor_id = ? AND status != 'Cancelled'";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, visitorId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Booking b = new Booking(rs.getInt("booking_id"), rs.getString("visitor_id"),
							rs.getString("park_name"), rs.getDate("visit_date").toLocalDate(),
							rs.getTime("visit_time").toLocalTime(), rs.getInt("visitors_count"),
							rs.getString("status"));
					b.setPrice(rs.getInt("total_price"));
					b.setVisitorType(rs.getString("booking_type"));
					b.setEmail(rs.getString("email"));
					b.setGuideGroup(rs.getInt("is_guide_group") == 1);
					b.setSubscriber(rs.getInt("is_subscriber") == 1);
					list.add(b);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String wlQuery = "SELECT * FROM waitinglist WHERE visitor_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(wlQuery)) {
			pstmt.setString(1, visitorId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Booking b = new Booking(rs.getInt("waiting_id"), rs.getString("visitor_id"),
							rs.getString("park_name"), rs.getDate("visit_date").toLocalDate(),
							rs.getTime("visit_time").toLocalTime(), rs.getInt("visitors_count"), "Waiting List");
					String visitorType = rs.getString("visitor_type");

					b.setVisitorType(visitorType);
					b.setEmail(rs.getString("email"));
					b.setGuideGroup("Guide".equals(visitorType));
					b.setSubscriber(isVisitorSubscriber(visitorId));
					list.add(b);
				}
			}
		} catch (Exception e) {
		}
		return list;
	}

	public boolean updateBooking(Booking b) {
		String query = "UPDATE bookings SET park_name=?, visit_date=?, visit_time=?, visitors_count=?, email=?, total_price=?, booking_type=?, is_guide_group=?, is_subscriber=? WHERE booking_id=? AND visitor_id=? AND status != 'Cancelled'";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			boolean guideGroup = b.isGuideGroup() && isVisitorGuide(b.getVisitorId());
			boolean subscriber = isVisitorSubscriber(b.getVisitorId());

			pstmt.setString(1, b.getParkName());
			pstmt.setDate(2, Date.valueOf(b.getVisitDate()));
			pstmt.setTime(3, Time.valueOf(b.getVisitTime()));
			pstmt.setInt(4, b.getVisitorsCount());
			pstmt.setString(5, b.getEmail());
			pstmt.setInt(6, b.getPrice());
			pstmt.setString(7, guideGroup ? "Guide" : "Regular Visitor");
			pstmt.setInt(8, guideGroup ? 1 : 0);
			pstmt.setInt(9, subscriber ? 1 : 0);
			pstmt.setInt(10, b.getBookingId());
			pstmt.setString(11, b.getVisitorId());
			return pstmt.executeUpdate() > 0;
		} catch (Exception e) {
			return false;
		}
	}

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
	public void processBookingConfirmations() {
		try {
			String sendReminders = "UPDATE bookings "
					+ "SET reminder_sent_at = CURRENT_TIMESTAMP, "
					+ "confirmation_deadline = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 HOUR) "
					+ "WHERE status = 'Pending' "
					+ "AND visit_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY) "
					+ "AND reminder_sent_at IS NULL";

			try (PreparedStatement ps = connection.prepareStatement(sendReminders)) {
				ps.executeUpdate();
			}
			
			String autoCancel = "UPDATE bookings "
					+ "SET status = 'Cancelled', cancelled_at = CURRENT_TIMESTAMP "
					+ "WHERE status = 'Pending' "
					+ "AND reminder_sent_at IS NOT NULL "
					+ "AND confirmation_deadline IS NOT NULL "
					+ "AND confirmation_deadline <= CURRENT_TIMESTAMP";

			try (PreparedStatement ps = connection.prepareStatement(autoCancel)) {
				ps.executeUpdate();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public int cancelBooking(int id, String visitorId) {
		String priceQuery = "SELECT total_price FROM bookings WHERE booking_id = ? AND visitor_id = ? AND status != 'Cancelled'";
		try (PreparedStatement pstmt = connection.prepareStatement(priceQuery)) {
			pstmt.setInt(1, id);
			pstmt.setString(2, visitorId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					int refund = rs.getInt("total_price");
					PreparedStatement ps = connection.prepareStatement(
							"UPDATE bookings SET status = 'Cancelled', cancelled_at = CURRENT_TIMESTAMP WHERE booking_id = ?");
					ps.setInt(1, id);
					ps.executeUpdate();
					return refund;
				}
			}
		} catch (Exception e) {
		}
		try (PreparedStatement pstmt = connection
				.prepareStatement("DELETE FROM waitinglist WHERE waiting_id = ? AND visitor_id = ?")) {
			pstmt.setInt(1, id);
			pstmt.setString(2, visitorId);
			if (pstmt.executeUpdate() > 0)
				return 0;
		} catch (Exception e) {
		}
		return -1;
	}

	public void manageWaitingListQueue() {
		try {
			connection.prepareStatement(
					"DELETE FROM waitinglist WHERE notified_time IS NOT NULL AND TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) >= 60")
					.executeUpdate();
			String getWaiting = "SELECT * FROM waitinglist WHERE notified_time IS NULL ORDER BY request_time ASC, waiting_id ASC";
			try (PreparedStatement ps = connection.prepareStatement(getWaiting); ResultSet rs = ps.executeQuery()) {
				HashSet<String> blockedSlots = new HashSet<>();
				while (rs.next()) {
					int wId = rs.getInt("waiting_id");
					String park = rs.getString("park_name");
					LocalDate date = rs.getDate("visit_date").toLocalDate();
					LocalTime time = rs.getTime("visit_time").toLocalTime();
					int visitors = rs.getInt("visitors_count");
					String slotKey = park + "_" + date + "_" + time;
					if (blockedSlots.contains(slotKey))
						continue;

					if (countVisitorsAt(park, date, time) + visitors <= getBookableCapacity(park)) {
						PreparedStatement psNotify = connection.prepareStatement(
								"UPDATE waitinglist SET notified_time = CURRENT_TIMESTAMP WHERE waiting_id = ?");
						psNotify.setInt(1, wId);
						psNotify.executeUpdate();
					} else {
						blockedSlots.add(slotKey);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Object> getWaitingListMessage(String visitorId) {
		String query = "SELECT *, "
				+ "60 - TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) AS mins_left "
				+ "FROM waitinglist "
				+ "WHERE visitor_id = ? "
				+ "AND notified_time IS NOT NULL "
				+ "AND TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) < 60 "
				+ "AND TIMESTAMP(visit_date, visit_time) > CURRENT_TIMESTAMP "
				+ "ORDER BY notified_time ASC, waiting_id ASC "
				+ "LIMIT 1";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, visitorId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				ArrayList<Object> msg = new ArrayList<>();
				msg.add(rs.getInt("waiting_id"));
				msg.add(rs.getString("park_name"));
				msg.add(rs.getDate("visit_date").toLocalDate());
				msg.add(rs.getTime("visit_time").toLocalTime());
				msg.add(rs.getLong("mins_left"));
				msg.add(rs.getInt("visitors_count"));
				msg.add(rs.getString("visitor_type"));
				return msg;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean payAndClaimWaitingList(int waitingId, int price) {
		return payAndClaimWaitingListAndReturnCode(waitingId, price) != null;
	}

	public String payAndClaimWaitingListAndReturnCode(int waitingId, int price) {
		try {
			String getQ = "SELECT * FROM waitinglist "
					+ "WHERE waiting_id = ? "
					+ "AND notified_time IS NOT NULL "
					+ "AND TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) < 60 "
					+ "AND TIMESTAMP(visit_date, visit_time) > CURRENT_TIMESTAMP";

			try (PreparedStatement ps = connection.prepareStatement(getQ)) {
				ps.setInt(1, waitingId);
				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					Booking b = new Booking(0, rs.getString("visitor_id"), rs.getString("park_name"),
							rs.getDate("visit_date").toLocalDate(), rs.getTime("visit_time").toLocalTime(),
							rs.getInt("visitors_count"), "Pending");

					String visitorType = rs.getString("visitor_type");

					b.setVisitorType(visitorType);
					b.setEmail(rs.getString("email"));
					b.setGuideGroup("Guide".equals(visitorType));
					b.setSubscriber(isVisitorSubscriber(b.getVisitorId()));
					b.setPrice(price);
					String code = saveBookingAndReturnCode(b);

					if (code == null) {
						return null;
					}

					try (PreparedStatement deletePs = connection.prepareStatement(
							"DELETE FROM waitinglist WHERE waiting_id = ?")) {
						deletePs.setInt(1, waitingId);
						deletePs.executeUpdate();
					}

					return code;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void declineWaitingList(int waitingId) {
		try {
			connection.prepareStatement("DELETE FROM waitinglist WHERE waiting_id = " + waitingId).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public common.Booking getBookingById(int bookingId) {
		String q = "SELECT * FROM bookings WHERE booking_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setInt(1, bookingId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					common.Booking b = new common.Booking(rs.getInt("booking_id"), rs.getString("visitor_id"),
							rs.getString("park_name"), rs.getDate("visit_date").toLocalDate(),
							rs.getTime("visit_time").toLocalTime(), rs.getInt("visitors_count"),
							rs.getString("status"));
					b.setPrice(rs.getInt("total_price"));
					b.setVisitorType(rs.getString("booking_type"));
					b.setEmail(rs.getString("email"));
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
	public common.Booking getBookingByConfirmationCode(String code) {
		String q = "SELECT * FROM bookings WHERE confirmation_code = ?";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, code);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					common.Booking b = new common.Booking(
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

	public int getParkCapacity(String parkName) {
		String q = "SELECT max_capacity FROM parks WHERE park_name = ?";
		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, parkName);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 150; // fallback if park missing
	}

	public int getParkBookingPercent(String parkName) {
		String q = "SELECT booking_percent FROM parks WHERE park_name = ?";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, parkName);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("booking_percent");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 80;
	}

	public int getParkVisitDurationHours(String parkName) {
		String q = "SELECT visit_duration_hours FROM parks WHERE park_name = ?";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, parkName);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("visit_duration_hours");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 4;
	}

	public int getBookableCapacity(String parkName) {
		int capacity = getParkCapacity(parkName);
		int percent = getParkBookingPercent(parkName);

		return (capacity * percent) / 100;
	}

	public boolean updateParkCapacity(String parkName, int newCapacity) {
		String q = "UPDATE parks SET max_capacity = ? WHERE park_name = ?";
		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setInt(1, newCapacity);
			ps.setString(2, parkName);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public String getEmployeeRole(String empId) {
		String q = "SELECT role FROM employees WHERE emp_id = ?";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, empId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getString("role");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean isEmployeeRole(String empId, String requiredRole) {
		String role = getEmployeeRole(empId);
		return requiredRole.equals(role);
	}

	public ArrayList<Object> getParkParams(String parkName) {
		ArrayList<Object> params = new ArrayList<>();

		String q = "SELECT max_capacity, booking_percent, visit_duration_hours FROM parks WHERE park_name = ?";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, parkName);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					params.add(rs.getInt("max_capacity"));
					params.add(rs.getInt("booking_percent"));
					params.add(rs.getInt("visit_duration_hours"));
					return params;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		params.add(150);
		params.add(80);
		params.add(4);
		return params;
	}

	public boolean createParkChangeRequest(String parkName, int newCapacity, int newBookingPercent,
			int newVisitDurationHours, String requestedBy) {

		if (!isEmployeeRole(requestedBy, "PARK_MANAGER")) {
			System.out.println("Only PARK_MANAGER can create park change requests.");
			return false;
		}

		String q = "INSERT INTO park_change_requests "
				+ "(park_name, requested_by, requested_capacity, requested_booking_percent, "
				+ "requested_visit_duration_hours, request_type, status) "
				+ "VALUES (?, ?, ?, ?, ?, 'PARK_PARAMS_CHANGE', 'Pending')";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, parkName);
			ps.setString(2, requestedBy);
			ps.setInt(3, newCapacity);
			ps.setInt(4, newBookingPercent);
			ps.setInt(5, newVisitDurationHours);

			return ps.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean createDiscountRequest(String parkName, String discountName, int discountPercent, String requestedBy) {
		if (!isEmployeeRole(requestedBy, "PARK_MANAGER")) {
			System.out.println("Only PARK_MANAGER can create discount requests.");
			return false;
		}

		if (discountName == null || discountName.trim().isEmpty()) {
			return false;
		}

		if (discountPercent <= 0 || discountPercent > 100) {
			return false;
		}

		String q = "INSERT INTO discount_requests "
				+ "(park_name, requested_by, discount_name, discount_percent, status) "
				+ "VALUES (?, ?, ?, ?, 'Pending')";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, parkName);
			ps.setString(2, requestedBy);
			ps.setString(3, discountName.trim());
			ps.setInt(4, discountPercent);

			return ps.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public ArrayList<ArrayList<Object>> getPendingDiscountRequests() {
		ArrayList<ArrayList<Object>> list = new ArrayList<>();

		String q = "SELECT discount_request_id, park_name, discount_name, discount_percent, requested_by, request_time "
				+ "FROM discount_requests "
				+ "WHERE status = 'Pending' "
				+ "ORDER BY request_time ASC, discount_request_id ASC";

		try (PreparedStatement ps = connection.prepareStatement(q);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				ArrayList<Object> row = new ArrayList<>();
				row.add(rs.getInt("discount_request_id"));
				row.add(rs.getString("park_name"));
				row.add(rs.getString("discount_name"));
				row.add(rs.getInt("discount_percent"));
				row.add(rs.getString("requested_by"));
				row.add(String.valueOf(rs.getTimestamp("request_time")));
				list.add(row);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	public boolean approveDiscountRequest(int requestId, String decisionBy) {
		if (!isEmployeeRole(decisionBy, "DEPT_MANAGER")) {
			System.out.println("Only DEPT_MANAGER can approve discount requests.");
			return false;
		}

		String q = "UPDATE discount_requests "
				+ "SET status = 'Approved', decision_by = ?, decision_time = CURRENT_TIMESTAMP "
				+ "WHERE discount_request_id = ? AND status = 'Pending'";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, decisionBy);
			ps.setInt(2, requestId);

			return ps.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean rejectDiscountRequest(int requestId, String decisionBy) {
		if (!isEmployeeRole(decisionBy, "DEPT_MANAGER")) {
			System.out.println("Only DEPT_MANAGER can reject discount requests.");
			return false;
		}

		String q = "UPDATE discount_requests "
				+ "SET status = 'Rejected', decision_by = ?, decision_time = CURRENT_TIMESTAMP "
				+ "WHERE discount_request_id = ? AND status = 'Pending'";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, decisionBy);
			ps.setInt(2, requestId);

			return ps.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public ArrayList<ArrayList<Object>> getPendingParkChangeRequests() {
		ArrayList<ArrayList<Object>> list = new ArrayList<>();

		String q = "SELECT request_id, park_name, requested_capacity, requested_booking_percent, "
				+ "requested_visit_duration_hours, requested_by, request_type, request_time "
				+ "FROM park_change_requests "
				+ "WHERE status = 'Pending' "
				+ "ORDER BY request_time ASC, request_id ASC";

		try (PreparedStatement ps = connection.prepareStatement(q);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				ArrayList<Object> row = new ArrayList<>();
				row.add(rs.getInt("request_id"));
				row.add(rs.getString("park_name"));
				row.add(rs.getInt("requested_capacity"));
				row.add(rs.getInt("requested_booking_percent"));
				row.add(rs.getInt("requested_visit_duration_hours"));
				row.add(rs.getString("requested_by"));
				row.add(rs.getString("request_type"));
				row.add(String.valueOf(rs.getTimestamp("request_time")));
				list.add(row);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	public boolean approveParkChangeRequest(int requestId, String decisionBy) {
		if (!isEmployeeRole(decisionBy, "DEPT_MANAGER")) {
			System.out.println("Only DEPT_MANAGER can approve park change requests.");
			return false;
		}

		boolean oldAutoCommit = true;

		try {
			oldAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			String parkName;
			int newCapacity;
			int newBookingPercent;
			int newDuration;

			String getQ = "SELECT park_name, requested_capacity, requested_booking_percent, "
					+ "requested_visit_duration_hours "
					+ "FROM park_change_requests "
					+ "WHERE request_id = ? AND status = 'Pending' "
					+ "FOR UPDATE";

			try (PreparedStatement ps = connection.prepareStatement(getQ)) {
				ps.setInt(1, requestId);

				try (ResultSet rs = ps.executeQuery()) {
					if (!rs.next()) {
						connection.rollback();
						return false;
					}

					parkName = rs.getString("park_name");
					newCapacity = rs.getInt("requested_capacity");
					newBookingPercent = rs.getInt("requested_booking_percent");
					newDuration = rs.getInt("requested_visit_duration_hours");
				}
			}

			String updatePark = "UPDATE parks "
					+ "SET max_capacity = ?, booking_percent = ?, visit_duration_hours = ? "
					+ "WHERE park_name = ?";

			try (PreparedStatement ps = connection.prepareStatement(updatePark)) {
				ps.setInt(1, newCapacity);
				ps.setInt(2, newBookingPercent);
				ps.setInt(3, newDuration);
				ps.setString(4, parkName);

				if (ps.executeUpdate() == 0) {
					connection.rollback();
					return false;
				}
			}

			String updateReq = "UPDATE park_change_requests "
					+ "SET status = 'Approved', decision_by = ?, decision_time = CURRENT_TIMESTAMP "
					+ "WHERE request_id = ?";

			try (PreparedStatement ps = connection.prepareStatement(updateReq)) {
				ps.setString(1, decisionBy);
				ps.setInt(2, requestId);
				ps.executeUpdate();
			}

			connection.commit();
			return true;

		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (Exception ignore) {
			}

			e.printStackTrace();
			return false;

		} finally {
			try {
				connection.setAutoCommit(oldAutoCommit);
			} catch (Exception ignore) {
			}
		}
	}

	public boolean rejectParkChangeRequest(int requestId, String decisionBy) {
		if (!isEmployeeRole(decisionBy, "DEPT_MANAGER")) {
			System.out.println("Only DEPT_MANAGER can reject park change requests.");
			return false;
		}

		String q = "UPDATE park_change_requests "
				+ "SET status = 'Rejected', decision_by = ?, decision_time = CURRENT_TIMESTAMP "
				+ "WHERE request_id = ? AND status = 'Pending'";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, decisionBy);
			ps.setInt(2, requestId);

			return ps.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

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

	public java.util.HashMap<String, Integer> reportVisitorsByType(String park, int year, int month) {
		java.util.HashMap<String, Integer> map = new java.util.HashMap<>();
		String q = "SELECT booking_type, SUM(visitors_count) FROM bookings WHERE park_name=? AND YEAR(visit_date)=? "
				+ "AND MONTH(visit_date)=? AND status IN ('Confirmed','Entered','Exited') GROUP BY booking_type";
		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, park);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					map.put(rs.getString(1) == null ? "Unknown" : rs.getString(1), rs.getInt(2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public java.util.ArrayList<Integer> reportCancellations(String park, int year, int month) {
		int cancelled = 0, noShow = 0;
		String c = "SELECT COUNT(*) FROM bookings WHERE park_name=? AND YEAR(visit_date)=? AND MONTH(visit_date)=? AND status='Cancelled'";
		String n = "SELECT COUNT(*) FROM bookings WHERE park_name=? AND YEAR(visit_date)=? AND MONTH(visit_date)=? "
				+ "AND status NOT IN ('Cancelled','Entered','Exited') AND visit_date < CURDATE()";
		try (PreparedStatement ps = connection.prepareStatement(c)) {
			ps.setString(1, park);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					cancelled = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try (PreparedStatement ps = connection.prepareStatement(n)) {
			ps.setString(1, park);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					noShow = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		java.util.ArrayList<Integer> out = new java.util.ArrayList<>();
		out.add(cancelled);
		out.add(noShow);
		return out;
	}
}