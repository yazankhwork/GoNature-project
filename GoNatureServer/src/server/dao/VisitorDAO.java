package server.dao;

import java.sql.*;
import java.util.ArrayList;
/**
 * Handles visitor-related database operations in the GoNature system.
 *
 * This class is responsible for visitor login, registration,
 * guide registration, subscription management and visitor information queries.
 *
 * @author Group 4
 * @version 1.0
 */
public class VisitorDAO {
	/**
	 * Active database connection used for visitor queries.
	 */
	private final Connection connection;
	/**
	 * Creates a new VisitorDAO instance.
	 *
	 * @param connection active database connection
	 */
	public VisitorDAO(Connection connection) {
		this.connection = connection;
	}
	/**
	 * Retrieves full profile information for a visitor.
	 *
	 * @param visitorId visitor identifier
	 * @return list containing visitor profile details, or null if not found
	 */
	public ArrayList<String> getVisitorInfo(String visitorId) {
		ArrayList<String> info = new ArrayList<>();
		String q1 = "SELECT full_name, email, phone, is_guide FROM visitors WHERE visitor_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(q1)) {
			ps.setString(1, visitorId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String name = rs.getString("full_name");
					String email = rs.getString("email");
					String phone = rs.getString("phone");
					boolean isGuide = rs.getInt("is_guide") == 1;

					String type = isGuide ? "Certified Guide" : "Regular Visitor";
					String extra = "No active family subscription";

					String q2 = "SELECT sub_id, family_members FROM subscriptions WHERE visitor_id = ?";
					try (PreparedStatement ps2 = connection.prepareStatement(q2)) {
						ps2.setString(1, visitorId);
						try (ResultSet rs2 = ps2.executeQuery()) {
							if (rs2.next()) {
								type = "Family Subscriber";
								extra = "Sub #: " + rs2.getInt("sub_id") + " | Total Members: " + rs2.getInt("family_members");
							}
						}
					}

					info.add(type);
					info.add(name);
					info.add(email);
					info.add(phone);
					info.add(extra);
					return info;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Authenticates a visitor using username and password.
	 *
	 * @param username visitor username
	 * @param password visitor password
	 * @return login result data containing status, full name, subscription number and visitor ID
	 */
	public String[] loginVisitor(String username, String password) {
		String query = "SELECT visitor_id, is_guide, full_name FROM visitors WHERE username = ? AND password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				String visitorId = rs.getString("visitor_id");
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
						rs.getString("full_name"), subNum, visitorId };
			} else
				return new String[] { "AUTH_FAILED", null, "NONE", null };
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "ERROR", null, "NONE", null };
		}
	}
	/**
	 * Authenticates a visitor using ONLY their visitor ID.
	 *
	 * @param visitorId visitor identifier
	 * @return login result data containing status, full name, subscription number and visitor ID
	 */
	public String[] loginVisitorById(String visitorId) {
		String query = "SELECT is_guide, full_name FROM visitors WHERE visitor_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, visitorId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
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
						rs.getString("full_name"), subNum, visitorId };
			} else
				return new String[] { "USER_NOT_FOUND", null, "NONE", null };
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "ERROR", null, "NONE", null };
		}
	}
	/**
	 * Registers a new regular visitor in the system.
	 *
	 * @param visitorId visitor identifier
	 * @param username visitor username
	 * @param password visitor password
	 * @param email visitor email address
	 * @param phone visitor phone number
	 * @return registration result message
	 */
	public String registerVisitor(String visitorId, String username, String password, String email, String phone) {
		String checkQuery = "SELECT visitor_id FROM visitors WHERE visitor_id = ? OR username = ?";
		try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
			checkStmt.setString(1, visitorId);
			checkStmt.setString(2, username);
			if (checkStmt.executeQuery().next())
				return "USER_ALREADY_EXISTS";
		} catch (Exception e) {
		}

		String insertQuery = "INSERT INTO visitors (visitor_id, username, password, email, phone, is_guide, full_name) VALUES (?, ?, ?, ?, ?, 0, ?)";
		try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
			insertStmt.setString(1, visitorId);
			insertStmt.setString(2, username);
			insertStmt.setString(3, password);
			insertStmt.setString(4, email);
			insertStmt.setString(5, phone);
			insertStmt.setString(6, username);
			if (insertStmt.executeUpdate() > 0)
				return "REGISTER_SUCCESS";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "REGISTER_FAILED";
	}
	/**
	 * Registers a guest visitor in the database so they can log in normally next time.
	 *
	 * @param visitorId visitor identifier
	 * @return ALREADY_EXISTS if found, REGISTERED if success, FAILED otherwise
	 */
	public String registerGuest(String visitorId) {
		String checkQuery = "SELECT visitor_id FROM visitors WHERE visitor_id = ?";
		try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
			checkStmt.setString(1, visitorId);
			if (checkStmt.executeQuery().next()) {
				return "ALREADY_EXISTS"; 
			}
		} catch (Exception e) {}

		String insertQuery = "INSERT INTO visitors (visitor_id, username, password, email, phone, is_guide, full_name) VALUES (?, ?, '', '', '', 0, 'Guest')";
		try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
			insertStmt.setString(1, visitorId);
			insertStmt.setString(2, visitorId); 
			if (insertStmt.executeUpdate() > 0) {
				return "REGISTERED";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "FAILED";
	}
	/**
	 * Updates the visitor's profile information.
	 *
	 * @param visitorId visitor identifier
	 * @param fullName new full name
	 * @param email new email
	 * @param phone new phone
	 * @return true if successful, false otherwise
	 */
	public boolean updateProfile(String visitorId, String fullName, String email, String phone) {
		String query = "UPDATE visitors SET full_name = ?, email = ?, phone = ? WHERE visitor_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, fullName);
			pstmt.setString(2, email);
			pstmt.setString(3, phone);
			pstmt.setString(4, visitorId);
			return pstmt.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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
		String updateQuery = "INSERT INTO visitors (visitor_id, username, password, email, is_guide, full_name) VALUES (?, ?, ?, ?, 1, ?) "
				+ "ON DUPLICATE KEY UPDATE is_guide=1, password=?, username=?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
			pstmt.setString(1, visitorId);
			pstmt.setString(2, username);
			pstmt.setString(3, password);
			pstmt.setString(4, username.replaceAll("\\s+", "").toLowerCase() + "@gonature.com");
			pstmt.setString(5, username);
			pstmt.setString(6, password);
			pstmt.setString(7, username);
			if (pstmt.executeUpdate() > 0)
				return "REGISTER_SUCCESS";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "FAILED";
	}
	/**
	 * Verifies the password of a certified guide.
	 *
	 * @param visitorId guide identifier
	 * @param password guide password
	 * @return true if the password matches, false otherwise
	 */
	public boolean verifyGuidePassword(String visitorId, String password) {
		String query = "SELECT visitor_id FROM visitors WHERE visitor_id = ? AND password = ? AND is_guide = 1";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, visitorId);
			ps.setString(2, password);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * Checks whether a visitor is registered as a guide.
	 *
	 * @param visitorId visitor identifier
	 * @return true if the visitor is a guide, otherwise false
	 */
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
	/**
	 * Retrieves the phone number of a visitor.
	 *
	 * @param visitorId visitor identifier
	 * @return visitor phone number, or null if not found
	 */
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
	/**
	 * Creates a new subscription for a visitor and updates visitor contact details.
	 *
	 * @param visitorId visitor identifier
	 * @param firstName subscriber first name
	 * @param lastName subscriber last name
	 * @param phone subscriber phone number
	 * @param email subscriber email address
	 * @param familyMembers number of family members in the subscription
	 * @param paymentMethod payment method
	 * @param creditCard credit card details
	 * @return generated subscription ID, or -1 if creation failed
	 */
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
	/**
	 * Checks whether a visitor has an active subscription.
	 *
	 * @param visitorId visitor identifier
	 * @return true if the visitor has a subscription, otherwise false
	 */
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