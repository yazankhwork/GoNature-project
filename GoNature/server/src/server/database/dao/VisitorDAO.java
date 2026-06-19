package server.database.dao;

import java.sql.*;

public class VisitorDAO {

	private final Connection connection;

	public VisitorDAO(Connection connection) {
		this.connection = connection;
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
	public boolean isVisitorGuide(String visitorId) {
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
	public String getVisitorPhone(String visitorId) {
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
	public boolean isVisitorSubscriber(String visitorId) {
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
}
