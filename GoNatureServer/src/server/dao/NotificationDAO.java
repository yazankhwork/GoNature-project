package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;

public class NotificationDAO {

	private final Connection connection;

	public NotificationDAO(Connection connection) {
		this.connection = connection;
	}
	public void createNotification(String visitorId, Integer bookingId, String notificationType,
			String messageText, String email, String phone) {

		String q = "INSERT INTO notifications "
				+ "(visitor_id, booking_id, notification_type, message_text, email, phone, status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, 'Sent')";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, visitorId);

			if (bookingId == null) {
				ps.setNull(2, Types.INTEGER);
			} else {
				ps.setInt(2, bookingId);
			}

			ps.setString(3, notificationType);
			ps.setString(4, messageText);
			ps.setString(5, email);
			ps.setString(6, phone);

			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public ArrayList<ArrayList<Object>> getVisitorNotifications(String visitorId) {
		ArrayList<ArrayList<Object>> list = new ArrayList<>();

		String q = "SELECT notification_id, notification_type, message_text, email, phone, sent_at "
				+ "FROM notifications "
				+ "WHERE visitor_id = ? "
				+ "ORDER BY sent_at DESC, notification_id DESC "
				+ "LIMIT 10";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, visitorId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ArrayList<Object> row = new ArrayList<>();
					row.add(rs.getInt("notification_id"));
					row.add(rs.getString("notification_type"));
					row.add(rs.getString("message_text"));
					row.add(rs.getString("email"));
					row.add(rs.getString("phone"));
					row.add(String.valueOf(rs.getTimestamp("sent_at")));
					list.add(row);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}
}