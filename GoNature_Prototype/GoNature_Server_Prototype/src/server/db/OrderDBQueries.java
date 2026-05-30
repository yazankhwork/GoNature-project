package server.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import common.Order;

public class OrderDBQueries {
    
    public static void printAllOrders() {
        // Changed table name to Orders
        String query = "SELECT * FROM Orders";

        try {
            Connection conn = DBController.connectToDB();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                // Updated all column names to match the database exactly
                System.out.println("Order Number: " + rs.getInt("orderNumber"));
                System.out.println("Order Date: " + rs.getDate("orderDate"));
                System.out.println("Number of Visitors: " + rs.getInt("numberOfVisitors"));
                System.out.println("Confirmation Code: " + rs.getInt("confirmationCode"));
                System.out.println("Subscriber ID: " + rs.getInt("subscriberId"));
                System.out.println("Date of Placing Order: " + rs.getDate("dateOfPlacingOrder"));
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

    public static boolean updateOrder(int orderNumber, String newDate, int newVisitors) {
        // Changed table name and column names to match the database
        String query = "UPDATE Orders SET orderDate = ?, numberOfVisitors = ? WHERE orderNumber = ?";

        try (Connection conn = DBController.connectToDB();
                PreparedStatement ps = conn.prepareStatement(query);) {
            ps.setString(1, newDate);
            ps.setInt(2, newVisitors);
            ps.setInt(3, orderNumber);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Order updated successfully");
                return true;
            } else {
                System.out.println("No order found with this order number");
                return false;
            }

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            return false;
        }
    }

    public static Order getOrderByNumber(int orderNumber) {
        // Changed table name and column name
        String query = "SELECT * FROM Orders WHERE orderNumber = ?";

        try (
            Connection conn = DBController.connectToDB();
            PreparedStatement ps = conn.prepareStatement(query);
        ) {
            ps.setInt(1, orderNumber);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Updated all column names to match the database exactly
                Order order = new Order(
                    rs.getInt("orderNumber"),
                    rs.getDate("orderDate").toString(),
                    rs.getInt("numberOfVisitors"),
                    rs.getInt("confirmationCode"),
                    rs.getInt("subscriberId"),
                    rs.getDate("dateOfPlacingOrder").toString()
                );

                return order;
            }

        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

        return null;
    }
}