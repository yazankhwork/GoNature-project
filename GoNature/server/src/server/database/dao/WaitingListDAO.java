package server.database.dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import common.Booking;

public class WaitingListDAO {

	private final Connection connection;
	private final BookingDAO bookingDAO;
	private final ParkDAO parkDAO;
	private final VisitorDAO visitorDAO;
	private final NotificationDAO notificationDAO;

	public WaitingListDAO(Connection connection, BookingDAO bookingDAO, ParkDAO parkDAO, VisitorDAO visitorDAO,
			NotificationDAO notificationDAO) {
		this.connection = connection;
		this.bookingDAO = bookingDAO;
		this.parkDAO = parkDAO;
		this.visitorDAO = visitorDAO;
		this.notificationDAO = notificationDAO;
	}

	public boolean enterWaitingList(Booking b) {
		String query = "INSERT INTO waitinglist (visitor_id, park_name, visit_date, visit_time, visitors_count, visitor_type, email, telephone) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, b.getVisitorId());
			pstmt.setString(2, b.getParkName());
			pstmt.setDate(3, Date.valueOf(b.getVisitDate()));
			pstmt.setTime(4, Time.valueOf(b.getVisitTime()));
			pstmt.setInt(5, b.getVisitorsCount());
			pstmt.setString(6, b.getVisitorType());
			pstmt.setString(7, b.getEmail());
			pstmt.setString(8, b.getTelephone() == null ? "" : b.getTelephone());
			return pstmt.executeUpdate() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public void manageWaitingListQueue() {
		try {
			// 1. מחיקת מי שהתאריך והשעה של הביקור שלו כבר עברו
			String passedVisits = "SELECT waiting_id, visitor_id, park_name, visit_date, visit_time, visitors_count, email, telephone "
					+ "FROM waitinglist " + "WHERE TIMESTAMP(visit_date, visit_time) <= CURRENT_TIMESTAMP";

			try (PreparedStatement ps = connection.prepareStatement(passedVisits); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int waitingId = rs.getInt("waiting_id");
					String visitorId = rs.getString("visitor_id");
					String parkName = rs.getString("park_name");
					String visitDate = String.valueOf(rs.getDate("visit_date"));
					String visitTime = String.valueOf(rs.getTime("visit_time"));
					int visitorsCount = rs.getInt("visitors_count");
					String email = rs.getString("email");
					String telephone = rs.getString("telephone");

					String message = "Your waiting-list request was closed because the visit time passed.\n" + "Park: "
							+ parkName + "\n" + "Date: " + visitDate + "\n" + "Time: " + visitTime + "\n" + "Visitors: "
							+ visitorsCount;

					String phoneToSend = (telephone != null && !telephone.isEmpty()) ? telephone : visitorDAO.getVisitorPhone(visitorId);
					notificationDAO.createNotification(visitorId, null, "WAITING_LIST_VISIT_PASSED", message, email, phoneToSend);

					try (PreparedStatement deletePs = connection.prepareStatement("DELETE FROM waitinglist WHERE waiting_id = ?")) {
						deletePs.setInt(1, waitingId);
						deletePs.executeUpdate();
					}
				}
			}
			
			// 2. פקיעת תוקף הצעות אחרי שעתיים (120 דקות)
			String expiredOffers = "SELECT waiting_id, visitor_id, park_name, visit_date, visit_time, visitors_count, email, telephone "
					+ "FROM waitinglist " + "WHERE notified_time IS NOT NULL "
					+ "AND TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) >= 120";

			try (PreparedStatement ps = connection.prepareStatement(expiredOffers); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int waitingId = rs.getInt("waiting_id");
					String visitorId = rs.getString("visitor_id");
					String parkName = rs.getString("park_name");
					String visitDate = String.valueOf(rs.getDate("visit_date"));
					String visitTime = String.valueOf(rs.getTime("visit_time"));
					int visitorsCount = rs.getInt("visitors_count");
					String email = rs.getString("email");
					String telephone = rs.getString("telephone");

					String message = "Your waiting-list offer expired because you did not claim it within 2 hours.\n"
							+ "Park: " + parkName + "\n" + "Date: " + visitDate + "\n" + "Time: " + visitTime + "\n"
							+ "Visitors: " + visitorsCount + "\n"
							+ "The spot was passed to the next visitor in the waiting list.";

					String phoneToSend = (telephone != null && !telephone.isEmpty()) ? telephone : visitorDAO.getVisitorPhone(visitorId);
					notificationDAO.createNotification(visitorId, null, "WAITING_LIST_EXPIRED", message, email, phoneToSend);
					
					try (PreparedStatement deletePs = connection.prepareStatement("DELETE FROM waitinglist WHERE waiting_id = ?")) {
						deletePs.setInt(1, waitingId);
						deletePs.executeUpdate();
					}
				}
			}

			// 3. מציאת סלוטים "נעולים" - מישהו בסלוט הזה קיבל הודעה ועדיין חושב!
			// אם הסלוט נעול, אנחנו עוצרים את התור ולא שולחים הודעות לאף אחד אחריו.
			HashMap<String, Boolean> lockedSlots = new HashMap<>();
			String activeNotificationsQuery = "SELECT DISTINCT park_name, visit_date, visit_time FROM waitinglist WHERE notified_time IS NOT NULL";
			
			try (PreparedStatement ps = connection.prepareStatement(activeNotificationsQuery); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String park = rs.getString("park_name");
					LocalDate date = rs.getDate("visit_date").toLocalDate();
					LocalTime time = rs.getTime("visit_time").toLocalTime();
					lockedSlots.put(park + "_" + date + "_" + time, true); // סלוט זה חסום כרגע!
				}
			}

			// 4. ניהול רשימת ההמתנה למי שעדיין לא קיבל הודעה (FIFO חמור ביותר)
			String getWaiting = "SELECT * FROM waitinglist " + "WHERE notified_time IS NULL "
					+ "AND TIMESTAMP(visit_date, visit_time) > CURRENT_TIMESTAMP "
					+ "ORDER BY visit_date ASC, visit_time ASC, request_time ASC, waiting_id ASC";

			try (PreparedStatement ps = connection.prepareStatement(getWaiting); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int wId = rs.getInt("waiting_id");
					String visitorId = rs.getString("visitor_id");
					String park = rs.getString("park_name");
					LocalDate date = rs.getDate("visit_date").toLocalDate();
					LocalTime time = rs.getTime("visit_time").toLocalTime();
					int visitors = rs.getInt("visitors_count");
					String email = rs.getString("email");
					String telephone = rs.getString("telephone");

					String slotKey = park + "_" + date + "_" + time;

					// בדיקה קריטית: אם הסלוט נעול, הבן אדם הזה לא רשאי לקבל הודעה! קופצים להבא.
					if (lockedSlots.getOrDefault(slotKey, false)) {
						continue;
					}

					int currentVis = bookingDAO.countVisitorsAt(park, date, time);
					int max = parkDAO.getBookableCapacity(park);
					int available = Math.max(0, max - currentVis);

					// בודקים אם יש מקום לראשון בתור (והיחיד שאנחנו מתייחסים אליו עכשיו)
					if (visitors <= available) {
						String updateQ = "UPDATE waitinglist SET notified_time = CURRENT_TIMESTAMP WHERE waiting_id = ?";

						try (PreparedStatement psNotify = connection.prepareStatement(updateQ)) {
							psNotify.setInt(1, wId);

							if (psNotify.executeUpdate() > 0) {
								String message = "A place opened for your waiting-list request.\n" + "Park: " + park
										+ "\n" + "Date: " + date + "\n" + "Time: " + time + "\n" + "Visitors: "
										+ visitors + "\n"
										+ "You have 2 hours to make the booking before it passes to the next visitor.";

								String phoneToSend = (telephone != null && !telephone.isEmpty()) ? telephone : visitorDAO.getVisitorPhone(visitorId);
								notificationDAO.createNotification(visitorId, null, "WAITING_LIST_AVAILABLE", message, email, phoneToSend);

								// נעלנו את הסלוט! אי אפשר לשלוח לאנשים הבאים בתור אחריו עד שהוא יחליט.
								lockedSlots.put(slotKey, true);
							}
						}
					} else {
						// אין כרגע מספיק מקום לראשון.
						// ננעל את הסלוט כדי שהאנשים אחריו בתור (שאולי צריכים פחות מקום) לא יעקפו אותו!
						lockedSlots.put(slotKey, true);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Object> getWaitingListMessage(String visitorId) {
		String query = "SELECT *, "
				+ "120 - TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) AS mins_left "
				+ "FROM waitinglist "
				+ "WHERE visitor_id = ? "
				+ "AND notified_time IS NOT NULL "
				+ "AND TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) < 120 "
				+ "AND TIMESTAMP(visit_date, visit_time) > CURRENT_TIMESTAMP "
				+ "ORDER BY notified_time ASC, waiting_id ASC "
				+ "LIMIT 1";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, visitorId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				ArrayList<Object> msg = new ArrayList<>();
				msg.add(rs.getInt("waiting_id"));
				msg.add(rs.getString("park_name"));
				msg.add(rs.getDate("visit_date").toLocalDate());
				msg.add(rs.getTime("visit_time").toLocalTime());
				msg.add(rs.getLong("mins_left"));
				msg.add(rs.getInt("visitors_count"));
				msg.add(rs.getString("visitor_type"));
				return msg;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean payAndClaimWaitingList(int waitingId, int price) {
		return payAndClaimWaitingListAndReturnCode(waitingId, price) != null;
	}

	public String payAndClaimWaitingListAndReturnCode(int waitingId, int price) {
		try {
			String getQ = "SELECT * FROM waitinglist "
					+ "WHERE waiting_id = ? "
					+ "AND notified_time IS NOT NULL "
					+ "AND TIMESTAMPDIFF(MINUTE, notified_time, CURRENT_TIMESTAMP) < 120 "
					+ "AND TIMESTAMP(visit_date, visit_time) > CURRENT_TIMESTAMP";

			try (PreparedStatement ps = connection.prepareStatement(getQ)) {
				ps.setInt(1, waitingId);
				ResultSet rs = ps.executeQuery();

				if (rs.next()) {
					Booking b = new Booking(0, rs.getString("visitor_id"), rs.getString("park_name"),
							rs.getDate("visit_date").toLocalDate(), rs.getTime("visit_time").toLocalTime(),
							rs.getInt("visitors_count"), "Pending");

					String visitorType = rs.getString("visitor_type");

					b.setVisitorType(visitorType);
					b.setEmail(rs.getString("email"));
					b.setTelephone(rs.getString("telephone"));
					b.setGuideGroup("Guide".equals(visitorType));
					b.setSubscriber(visitorDAO.isVisitorSubscriber(b.getVisitorId()));
					b.setPrice(price);
					String code = bookingDAO.saveBookingAndReturnCode(b);

					if (code == null) {
						return null;
					}

					try (PreparedStatement deletePs = connection.prepareStatement(
							"DELETE FROM waitinglist WHERE waiting_id = ?")) {
						deletePs.setInt(1, waitingId);
						deletePs.executeUpdate();
					}

					return code;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void declineWaitingList(int waitingId) {
		try (PreparedStatement ps = connection.prepareStatement(
		        "DELETE FROM waitinglist WHERE waiting_id = ?")) {
		    ps.setInt(1, waitingId);
		    ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Booking> getWaitingEntriesAsBookings(String visitorId) {
		ArrayList<Booking> list = new ArrayList<>();

		String query = "SELECT waiting_id, visitor_id, park_name, visit_date, visit_time, visitors_count, "
				+ "visitor_type, email, telephone, notified_time "
				+ "FROM waitinglist "
				+ "WHERE visitor_id = ? "
				+ "ORDER BY visit_date, visit_time";

		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, visitorId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Booking b = new Booking(
							rs.getInt("waiting_id"),
							rs.getString("visitor_id"),
							rs.getString("park_name"),
							rs.getDate("visit_date").toLocalDate(),
							rs.getTime("visit_time").toLocalTime(),
							rs.getInt("visitors_count"),
							"Waiting List"
					);

					String visitorType = rs.getString("visitor_type");

					b.setVisitorType(visitorType);
					b.setEmail(rs.getString("email"));
					b.setTelephone(rs.getString("telephone"));
					b.setGuideGroup("Guide".equals(visitorType));
					b.setSubscriber(visitorDAO.isVisitorSubscriber(visitorId));
					b.setPrice(0);

					list.add(b);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	public boolean removeWaitingEntry(int waitingId, String visitorId) {
		String query = "DELETE FROM waitinglist "
				+ "WHERE waiting_id = ? "
				+ "AND visitor_id = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, waitingId);
			pstmt.setString(2, visitorId);

			return pstmt.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}