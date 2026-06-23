package server.dao;

import java.sql.*;
import java.util.ArrayList;

public class ParkDAO {

	private final Connection connection;
	private final EmployeeDAO employeeDAO;

	public ParkDAO(Connection connection, EmployeeDAO employeeDAO) {
		this.connection = connection;
		this.employeeDAO = employeeDAO;
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
	public boolean createParkChangeRequest(String parkName, int newCapacity, int newBookingPercent,
			int newVisitDurationHours, String requestedBy) {

		if (!employeeDAO.isEmployeeRole(requestedBy, "PARK_MANAGER")) {
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
		if (!employeeDAO.isEmployeeRole(decisionBy, "DEPT_MANAGER")) {
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
		if (!employeeDAO.isEmployeeRole(decisionBy, "DEPT_MANAGER")) {
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
}
