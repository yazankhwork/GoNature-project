package server;

import java.sql.*;
import java.util.ArrayList;
import common.Booking;

public class DatabaseController {
    private Connection connection;

    public void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/gonature_db", "root", "Georgesini2001");
            System.out.println("Database connected successfully!");
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE visitors ADD COLUMN password VARCHAR(50) DEFAULT '1234'");
            } catch (SQLException e) { /* Column exists */ }
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("ALTER TABLE bookings ADD COLUMN total_price INT DEFAULT 0");
            } catch (SQLException e) { /* Column exists */ }
            
        } catch (SQLException e) { System.err.println("DB Connection Error: " + e.getMessage()); }
    }

    public String loginVisitor(String visitorId, String password) {
        String query = "SELECT password FROM visitors WHERE visitor_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, visitorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getString("password").equals(password)) return "LOGIN_SUCCESS";
                else return "WRONG_PASSWORD";
            } else return "USER_NOT_FOUND";
        } catch (Exception e) { e.printStackTrace(); return "ERROR"; }
    }

    public String registerVisitor(String visitorId, String password, boolean isGuide) {
        String checkQuery = "SELECT visitor_id FROM visitors WHERE visitor_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, visitorId);
            if (checkStmt.executeQuery().next()) return "USER_ALREADY_EXISTS";
        } catch (Exception e) { e.printStackTrace(); }

        String insertQuery = "INSERT INTO visitors (visitor_id, password, email, is_guide) VALUES (?, ?, 'new@gonature.com', ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setString(1, visitorId);
            insertStmt.setString(2, password);
            insertStmt.setInt(3, isGuide ? 1 : 0); 
            if (insertStmt.executeUpdate() > 0) return "REGISTER_SUCCESS";
        } catch (Exception e) { e.printStackTrace(); }
        return "REGISTER_FAILED";
    }

    public boolean saveBooking(Booking b) {
        int totalPrice = b.getVisitorsCount() * 30; // 30 ILS per ticket
        String query = "INSERT INTO bookings (visitor_id, park_name, visit_date, visit_time, visitors_count, status, total_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, b.getVisitorId());
            pstmt.setString(2, b.getParkName());
            pstmt.setDate(3, Date.valueOf(b.getVisitDate()));
            pstmt.setTime(4, Time.valueOf(b.getVisitTime()));
            pstmt.setInt(5, b.getVisitorsCount());
            pstmt.setString(6, "Pending"); 
            pstmt.setInt(7, totalPrice);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public ArrayList<Booking> getUserBookings(String visitorId) {
        ArrayList<Booking> list = new ArrayList<>();
        String query = "SELECT * FROM bookings WHERE visitor_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, visitorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking(rs.getInt("booking_id"), rs.getString("visitor_id"), rs.getString("park_name"), rs.getDate("visit_date").toLocalDate(), rs.getTime("visit_time").toLocalTime(), rs.getInt("visitors_count"), rs.getString("status"));
                    list.add(b);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateBooking(Booking b) {
        String query = "UPDATE bookings SET park_name=?, visit_date=?, visit_time=?, visitors_count=? WHERE booking_id=? AND visitor_id=? AND status != 'Cancelled'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, b.getParkName()); pstmt.setDate(2, Date.valueOf(b.getVisitDate())); pstmt.setTime(3, Time.valueOf(b.getVisitTime())); pstmt.setInt(4, b.getVisitorsCount()); pstmt.setInt(5, b.getBookingId()); pstmt.setString(6, b.getVisitorId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public int cancelBooking(int bookingId, String visitorId) {
        int refundAmount = 0;
        String priceQuery = "SELECT total_price FROM bookings WHERE booking_id = ? AND visitor_id = ? AND status != 'Cancelled'";
        try (PreparedStatement pstmt = connection.prepareStatement(priceQuery)) {
            pstmt.setInt(1, bookingId); pstmt.setString(2, visitorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) refundAmount = rs.getInt("total_price");
            else return -1;
        } catch (Exception e) { return -1; }

        String cancelQuery = "UPDATE bookings SET status = 'Cancelled' WHERE booking_id = ? AND visitor_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(cancelQuery)) {
            pstmt.setInt(1, bookingId); pstmt.setString(2, visitorId);
            if (pstmt.executeUpdate() > 0) return refundAmount;
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }
}