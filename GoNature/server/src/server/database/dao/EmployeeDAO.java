package server.database.dao;

import java.sql.*;

public class EmployeeDAO {

	private final Connection connection;

	public EmployeeDAO(Connection connection) {
		this.connection = connection;
	}

	public String[] loginEmployee(String empId, String password) {
		String query = "SELECT password, full_name, role, park_name FROM employees WHERE emp_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, empId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				if (rs.getString("password").equals(password)) {
					return new String[] { "LOGIN_SUCCESS_EMPLOYEE", rs.getString("full_name"), rs.getString("role"), rs.getString("park_name") };
				} else
					return new String[] { "WRONG_PASSWORD", null, null, null };
			} else
				return new String[] { "USER_NOT_FOUND", null, null, null };
		} catch (Exception e) {
			return new String[] { "ERROR", null, null, null };
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
}