package server.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.time.LocalDate;
import java.time.LocalTime;
import common.Booking;

public class DatabaseController {
	private Connection connection;

	public void connectToDatabase() {
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost/gonature_db?serverTimezone=Asia/Jerusalem", "root", "Georgesini2001");
			System.out.println("Database connected successfully!");
		} catch (SQLException e) { System.err.println("DB Connection Error: " + e.getMessage()); }
	}

	public int countVisitorsAt(String parkName, LocalDate date, LocalTime time) {
		int total = 0;
		String q1 = "SELECT SUM(visitors_count) FROM bookings WHERE park_name = ? AND visit_date = ? AND visit_time = ? AND status NOT IN ('Cancelled', 'Waiting List')";
		try (PreparedStatement ps = connection.prepareStatement(q1)) {
			ps.setString(1, parkName); ps.setDate(2, Date.valueOf(date)); ps.setTime(3, Time.valueOf(time));
			ResultSet rs = ps.executeQuery(); if (rs.next()) total += rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }

		String q2 = "SELECT SUM(visitors_count) FROM waitinglist WHERE park_name = ? AND visit_date = ? AND visit_time = ? AND notified_time IS NOT NULL";
		try (PreparedStatement ps = connection.prepareStatement(q2)) {
			ps.setString(1, parkName); ps.setDate(2, Date.valueOf(date)); ps.setTime(3, Time.valueOf(time));
			ResultSet rs = ps.executeQuery(); if (rs.next()) total += rs.getInt(1);
		} catch (SQLException e) { e.printStackTrace(); }
		return total;
	}

	public String[] loginVisitor(String visitorId, String password) {
		String query = "SELECT password, is_guide, full_name FROM visitors WHERE visitor_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, visitorId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				if (rs.getString("password").equals(password)) {
					String subNum = "NONE";
					try (PreparedStatement psSub = connection.prepareStatement("SELECT sub_id FROM subscriptions WHERE visitor_id = ?")) {
						psSub.setString(1, visitorId);
						ResultSet rsSub = psSub.executeQuery();
						if (rsSub.next()) subNum = String.valueOf(rsSub.getInt("sub_id"));
					}
					return new String[] { (rs.getInt("is_guide") == 1) ? "LOGIN_SUCCESS_GUIDE" : "LOGIN_SUCCESS_REGULAR", rs.getString("full_name"), subNum };
				} else return new String[] { "WRONG_PASSWORD", null, "NONE" };
			} else return new String[] { "USER_NOT_FOUND", null, "NONE" };
		} catch (Exception e) { return new String[] { "ERROR", null, "NONE" }; }
	}

	public String[] loginEmployee(String empId, String password) {
		String query = "SELECT password, full_name, role FROM employees WHERE emp_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, empId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				if (rs.getString("password").equals(password)) {
					return new String[] { "LOGIN_SUCCESS_EMPLOYEE", rs.getString("full_name"), rs.getString("role") };
				} else return new String[] { "WRONG_PASSWORD", null, null };
			} else return new String[] { "USER_NOT_FOUND", null, null };
		} catch (Exception e) { return new String[] { "ERROR", null, null }; }
	}

	public int buySubscription(String visitorId, int familyMembers, String creditCard) {
		String query = "INSERT INTO subscriptions (visitor_id, family_members, credit_card) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, visitorId);
			pstmt.setInt(2, familyMembers);
			pstmt.setString(3, creditCard);
			pstmt.executeUpdate();
			ResultSet rs = pstmt.getGeneratedKeys();
			if (rs.next()) return rs.getInt(1); 
		} catch (Exception e) { e.printStackTrace(); }
		return -1;
	}

	public String registerVisitor(String visitorId, String password, boolean isGuide, String fullName) {
		String checkQuery = "SELECT visitor_id FROM visitors WHERE visitor_id = ?";
		try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
			checkStmt.setString(1, visitorId); if (checkStmt.executeQuery().next()) return "USER_ALREADY_EXISTS";
		} catch (Exception e) {}

		String insertQuery = "INSERT INTO visitors (visitor_id, password, email, is_guide, full_name) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
			insertStmt.setString(1, visitorId); insertStmt.setString(2, password);
			insertStmt.setString(3, fullName.replaceAll("\\s+", "").toLowerCase() + "@gonature.com");
			insertStmt.setInt(4, isGuide ? 1 : 0); insertStmt.setString(5, fullName);
			if (insertStmt.executeUpdate() > 0) return "REGISTER_SUCCESS";
		} catch (Exception e) {}
		return "REGISTER_FAILED";
	}

	// NEW: Logic to either INSERT a new guide or UPDATE an existing visitor to a guide
	public String registerOrUpdateGuide(String visitorId, String password, String fullName) {
		String checkQuery = "SELECT visitor_id FROM visitors WHERE visitor_id = ?";
		boolean exists = false;
		try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
			checkStmt.setString(1, visitorId);
			exists = checkStmt.executeQuery().next();
		} catch (Exception e) {}

		if (exists) {
			String updateQuery = "UPDATE visitors SET is_guide = 1, password = ?, full_name = ? WHERE visitor_id = ?";
			try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
				updateStmt.setString(1, password);
				updateStmt.setString(2, fullName);
				updateStmt.setString(3, visitorId);
				if (updateStmt.executeUpdate() > 0) return "UPDATE_SUCCESS";
			} catch (Exception e) {}
		} else {
			String insertQuery = "INSERT INTO visitors (visitor_id, password, email, is_guide, full_name) VALUES (?, ?, ?, 1, ?)";
			try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
				insertStmt.setString(1, visitorId);
				insertStmt.setString(2, password);
				insertStmt.setString(3, fullName.replaceAll("\\s+", "").toLowerCase() + "@gonature.com");
				insertStmt.setString(4, fullName);
				if (insertStmt.executeUpdate() > 0) return "REGISTER_SUCCESS";
			} catch (Exception e) {}
		}
		return "FAILED";
	}

	public boolean saveBooking(Booking b) {
		String query = "INSERT INTO bookings (visitor_id, park_name, visit_date, visit_time, visitors_count, status, total_price, booking_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, b.getVisitorId()); pstmt.setString(2, b.getParkName());
			pstmt.setDate(3, Date.valueOf(b.getVisitDate())); pstmt.setTime(4, Time.valueOf(b.getVisitTime()));
			pstmt.setInt(5, b.getVisitorsCount()); pstmt.setString(6, b.getStatus());
			pstmt.setInt(7, b.getPrice()); pstmt.setString(8, b.getVisitorType());
			return pstmt.executeUpdate() > 0;
		} catch (Exception e) { return false; }
	}

	public boolean enterWaitingList(Booking b) {
		String query = "INSERT INTO waitinglist (visitor_id, park_name, visit_date, visit_time, visitors_count, visitor_type) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, b.getVisitorId()); pstmt.setString(2, b.getParkName());
			pstmt.setDate(3, Date.valueOf(b.getVisitDate())); pstmt.setTime(4, Time.valueOf(b.getVisitTime()));
			pstmt.setInt(5, b.getVisitorsCount()); pstmt.setString(6, b.getVisitorType());
			return pstmt.executeUpdate() > 0;
		} catch (Exception e) { return false; }
	}

	public ArrayList<Booking> getUserBookings(String visitorId) {
		ArrayList<Booking> list = new ArrayList<>();
		String query = "SELECT * FROM bookings WHERE visitor_id = ? AND status != 'Cancelled'";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, visitorId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Booking b = new Booking(rs.getInt("booking_id"), rs.getString("visitor_id"), rs.getString("park_name"), rs.getDate("visit_date").toLocalDate(), rs.getTime("visit_time").toLocalTime(), rs.getInt("visitors_count"), rs.getString("status"));
					b.setPrice(rs.getInt("total_price")); b.setVisitorType(rs.getString("booking_type")); list.add(b);
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
		
		String wlQuery = "SELECT * FROM waitinglist WHERE visitor_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(wlQuery)) {
			pstmt.setString(1, visitorId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Booking b = new Booking(rs.getInt("waiting_id"), rs.getString("visitor_id"), rs.getString("park_name"), rs.getDate("visit_date").toLocalDate(), rs.getTime("visit_time").toLocalTime(), rs.getInt("visitors_count"), "Waiting List");
					b.setVisitorType(rs.getString("visitor_type")); list.add(b);
				}
			}
		} catch (Exception e) {}
		return list;
	}

	public boolean updateBooking(Booking b) {
		String query = "UPDATE bookings SET park_name=?, visit_date=?, visit_time=?, visitors_count=?, total_price=?, booking_type=? WHERE booking_id=? AND visitor_id=? AND status != 'Cancelled'";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, b.getParkName()); pstmt.setDate(2, Date.valueOf(b.getVisitDate())); pstmt.setTime(3, Time.valueOf(b.getVisitTime()));
			pstmt.setInt(4, b.getVisitorsCount()); pstmt.setInt(5, b.getPrice()); pstmt.setString(6, b.getVisitorType());
			pstmt.setInt(7, b.getBookingId()); pstmt.setString(8, b.getVisitorId());
			return pstmt.executeUpdate() > 0;
		} catch (Exception e) { return false; }
	}

	public boolean confirmArrival(int bookingId) {
		try (PreparedStatement pstmt = connection.prepareStatement("UPDATE bookings SET status = 'Confirmed' WHERE booking_id = ?")) {
			pstmt.setInt(1, bookingId); return pstmt.executeUpdate() > 0;
		} catch (Exception e) { return false; }
	}

	public int cancelBooking(int id, String visitorId) {
		String priceQuery = "SELECT total_price FROM bookings WHERE booking_id = ? AND visitor_id = ? AND status != 'Cancelled'";
		try (PreparedStatement pstmt = connection.prepareStatement(priceQuery)) {
			pstmt.setInt(1, id); pstmt.setString(2, visitorId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					int refund = rs.getInt("total_price");
					PreparedStatement ps = connection.prepareStatement("UPDATE bookings SET status = 'Cancelled' WHERE booking_id = ?");
					ps.setInt(1, id); ps.executeUpdate(); return refund;
				}
			}
		} catch (Exception e) {}
		try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM waitinglist WHERE waiting_id = ? AND visitor_id = ?")) {
			pstmt.setInt(1, id); pstmt.setString(2, visitorId);
			if (pstmt.executeUpdate() > 0) return 0;
		} catch (Exception e) {}
		return -1;
	}

	public void manageWaitingListQueue() {
		try {
			connection.prepareStatement("DELETE FROM waitinglist WHERE notified_time IS NOT NULL AND TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) >= 120").executeUpdate();
			String getWaiting = "SELECT * FROM waitinglist WHERE notified_time IS NULL ORDER BY request_time ASC, waiting_id ASC";
			try (PreparedStatement ps = connection.prepareStatement(getWaiting); ResultSet rs = ps.executeQuery()) {
				HashSet<String> blockedSlots = new HashSet<>();
				while (rs.next()) {
					int wId = rs.getInt("waiting_id");
					String park = rs.getString("park_name"); LocalDate date = rs.getDate("visit_date").toLocalDate(); LocalTime time = rs.getTime("visit_time").toLocalTime();
					int visitors = rs.getInt("visitors_count"); String slotKey = park + "_" + date + "_" + time;
					if (blockedSlots.contains(slotKey)) continue;

					if (countVisitorsAt(park, date, time) + visitors <= 150) {
						PreparedStatement psNotify = connection.prepareStatement("UPDATE waitinglist SET notified_time = CURRENT_TIMESTAMP WHERE waiting_id = ?");
						psNotify.setInt(1, wId); psNotify.executeUpdate();
					} else { blockedSlots.add(slotKey); }
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	public ArrayList<Object> getWaitingListMessage(String visitorId) {
		String query = "SELECT *, 120 - TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) AS mins_left FROM waitinglist WHERE visitor_id = ? AND notified_time IS NOT NULL LIMIT 1";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, visitorId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				ArrayList<Object> msg = new ArrayList<>();
				msg.add(rs.getInt("waiting_id")); msg.add(rs.getString("park_name"));
				msg.add(rs.getDate("visit_date").toLocalDate()); msg.add(rs.getTime("visit_time").toLocalTime());
				msg.add(rs.getLong("mins_left")); msg.add(rs.getInt("visitors_count"));
				msg.add(rs.getString("visitor_type")); return msg;
			}
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public boolean payAndClaimWaitingList(int waitingId, int price) {
		try {
			String getQ = "SELECT * FROM waitinglist WHERE waiting_id = ?";
			try (PreparedStatement ps = connection.prepareStatement(getQ)) {
				ps.setInt(1, waitingId); ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					Booking b = new Booking(0, rs.getString("visitor_id"), rs.getString("park_name"), rs.getDate("visit_date").toLocalDate(), rs.getTime("visit_time").toLocalTime(), rs.getInt("visitors_count"), "Pending");
					b.setVisitorType(rs.getString("visitor_type")); b.setPrice(price); 
					saveBooking(b);
					connection.prepareStatement("DELETE FROM waitinglist WHERE waiting_id = " + waitingId).executeUpdate();
					return true;
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
		return false;
	}

	public void declineWaitingList(int waitingId) {
		try { connection.prepareStatement("DELETE FROM waitinglist WHERE waiting_id = " + waitingId).executeUpdate(); } catch (Exception e) { e.printStackTrace(); }
	}
}