package server.dao;

import java.sql.*;
/**
 * Handles employee-related database operations in the GoNature system.
 *
 * This class is responsible for employee authentication
 * and employee role management.
 *
 * @author Group 4
 * @version 1.0
 */
public class EmployeeDAO {
	/**
	 * Active database connection used for employee queries.
	 */
	private final Connection connection;
	/**
	 * Creates a new EmployeeDAO instance.
	 *
	 * @param connection active database connection
	 */
	public EmployeeDAO(Connection connection) {
		this.connection = connection;
	}
	/**
	 * Authenticates an employee using employee ID and password.
	 *
	 * @param empId employee identifier
	 * @param password employee password
	 * @return login result containing status, employee name, role and park
	 */
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
	/**
	 * Retrieves the role of an employee.
	 *
	 * @param empId employee identifier
	 * @return employee role, or null if not found
	 */
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
	/**
	 * Checks whether an employee has a specific role.
	 *
	 * @param empId employee identifier
	 * @param requiredRole role to verify
	 * @return true if the employee has the required role, otherwise false
	 */
	public boolean isEmployeeRole(String empId, String requiredRole) {
		String role = getEmployeeRole(empId);
		return requiredRole.equals(role);
	}
}