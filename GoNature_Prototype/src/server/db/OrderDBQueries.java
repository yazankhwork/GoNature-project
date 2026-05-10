package server.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OrderDBQueries {
	public static void printAllOrders() {
        String query = "SELECT * FROM `Order`";

        try {
            Connection conn = DBController.connectToDB();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                System.out.println("Order Number: " + rs.getInt("order_number"));
                System.out.println("Order Date: " + rs.getDate("order_date"));
                System.out.println("Number of Visitors: " + rs.getInt("number_of_visitors"));
                System.out.println("Confirmation Code: " + rs.getInt("confirmation_code"));
                System.out.println("Subscriber ID: " + rs.getInt("subscriber_id"));
                System.out.println("Date of Placing Order: " + rs.getDate("date_of_placing_order"));
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
	public static void updateOrder(int orderNumber, String newDate, int newVisitors) {
	    String query = "UPDATE `Order` SET order_date = ?, number_of_visitors = ? WHERE order_number = ?";

	    try (
	        Connection conn = DBController.connectToDB();
	        java.sql.PreparedStatement ps = conn.prepareStatement(query);
	    ) {
	        ps.setString(1, newDate);
	        ps.setInt(2, newVisitors);
	        ps.setInt(3, orderNumber);

	        int rowsUpdated = ps.executeUpdate();

	        if (rowsUpdated > 0) {
	            System.out.println("Order updated successfully");
	        } else {
	            System.out.println("No order found with this order number");
	        }

	    } catch (SQLException ex) {
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("VendorError: " + ex.getErrorCode());
	    }
	}
}
