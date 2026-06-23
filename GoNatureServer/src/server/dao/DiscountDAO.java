package server.dao;

import java.sql.*;
import java.util.ArrayList;

public class DiscountDAO {

	private final Connection connection;
	private final EmployeeDAO employeeDAO;

	public DiscountDAO(Connection connection, EmployeeDAO employeeDAO) {
		this.connection = connection;
		this.employeeDAO = employeeDAO;
	}

	public boolean createDiscountRequest(String parkName, String discountName, int discountPercent, String startDate, String endDate, String requestedBy) {
		if (!employeeDAO.isEmployeeRole(requestedBy, "PARK_MANAGER")) {
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
				+ "(park_name, requested_by, discount_name, discount_percent, start_date, end_date, status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, 'Pending')";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, parkName);
			ps.setString(2, requestedBy);
			ps.setString(3, discountName.trim());
			ps.setInt(4, discountPercent);
			ps.setDate(5, java.sql.Date.valueOf(startDate));
			ps.setDate(6, java.sql.Date.valueOf(endDate));

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
		if (!employeeDAO.isEmployeeRole(decisionBy, "DEPT_MANAGER")) {
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
		if (!employeeDAO.isEmployeeRole(decisionBy, "DEPT_MANAGER")) {
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

	public int getApprovedDiscountPercent(String parkName) {
		String q = "SELECT COALESCE(MAX(discount_percent), 0) AS discount_percent "
				+ "FROM discount_requests "
				+ "WHERE park_name = ? "
				+ "AND status = 'Approved' "
				+ "AND CURRENT_DATE BETWEEN start_date AND end_date";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, parkName);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("discount_percent");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}
}