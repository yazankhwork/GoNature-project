package server.dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
/**
 * Handles report-related database operations in the GoNature system.
 *
 * This class generates reports about visitor types, detailed visits,
 * park utilization and cancellations/no-shows.
 *
 * @author Group 4
 * @version 1.0
 */
public class ReportDAO {
	/**
	 * Active database connection used for report queries.
	 */
	private final Connection connection;
	/**
	 * DAO used to retrieve park parameters required for reports.
	 */
	private final ParkDAO parkDAO;
	/**
	 * DAO used to retrieve booking information required for reports.
	 */
	private final BookingDAO bookingDAO;
	/**
	 * Creates a new ReportDAO instance.
	 *
	 * @param connection active database connection
	 * @param parkDAO park DAO used for park data
	 * @param bookingDAO booking DAO used for booking data
	 */
	public ReportDAO(Connection connection, ParkDAO parkDAO, BookingDAO bookingDAO) {
		this.connection = connection;
		this.parkDAO = parkDAO;
		this.bookingDAO = bookingDAO;
	}
	/**
	 * Generates a monthly report of visitors grouped by visit type.
	 *
	 * @param park park name
	 * @param year report year
	 * @param month report month
	 * @return map containing visitor counts by type
	 */
	public java.util.HashMap<String, Integer> reportVisitorsByType(String park, int year, int month) {
		java.util.HashMap<String, Integer> map = new java.util.HashMap<>();

		map.put("Alone", 0);
		map.put("Private Group", 0);
		map.put("Guided Group", 0);

		String q = "SELECT visitors_count, is_guide_group, booking_type "
				+ "FROM bookings "
				+ "WHERE park_name = ? "
				+ "AND YEAR(visit_date) = ? "
				+ "AND MONTH(visit_date) = ? "
				+ "AND status IN ('Confirmed', 'Entered', 'Exited')";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, park);
			ps.setInt(2, year);
			ps.setInt(3, month);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int visitors = rs.getInt("visitors_count");
					boolean guideGroup = rs.getInt("is_guide_group") == 1
							|| "Guide".equals(rs.getString("booking_type"));

					if (guideGroup) {
						map.put("Guided Group", map.get("Guided Group") + visitors);
					} else if (visitors == 1) {
						map.put("Alone", map.get("Alone") + visitors);
					} else {
						map.put("Private Group", map.get("Private Group") + visitors);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return map;
	}
	/**
	 * Generates a report showing days and hours when the park was not full.
	 *
	 * @param park park name
	 * @param year report year
	 * @param month report month
	 * @return report rows containing date, peak visitors, capacity, remaining capacity and not-full hours
	 */
	public ArrayList<ArrayList<Object>> reportParkNotFull(String park, int year, int month) {
		ArrayList<ArrayList<Object>> rows = new ArrayList<>();

		int capacity = parkDAO.getParkCapacity(park);
		if (capacity <= 0) {
			return rows;
		}

		YearMonth ym = YearMonth.of(year, month);

		for (int day = 1; day <= ym.lengthOfMonth(); day++) {
			LocalDate date = ym.atDay(day);

			int peakVisitors = 0;
			int notFullHours = 0;

			for (int hour = 8; hour < 18; hour++) {
				int visitorsAtHour = bookingDAO.countVisitorsAt(park, date, LocalTime.of(hour, 0));

				if (visitorsAtHour > peakVisitors) {
					peakVisitors = visitorsAtHour;
				}

				if (visitorsAtHour < capacity) {
					notFullHours++;
				}
			}

			if (notFullHours > 0) {
				ArrayList<Object> row = new ArrayList<>();
				row.add(String.valueOf(date));
				row.add(peakVisitors);
				row.add(capacity);
				row.add(Math.max(0, capacity - peakVisitors));
				row.add(notFullHours);
				rows.add(row);
			}
		}

		return rows;
	}
	/**
	 * Generates a detailed monthly visit report for a specific park.
	 *
	 * @param park park name
	 * @param year report year
	 * @param month report month
	 * @return detailed visit report rows
	 */
	public ArrayList<ArrayList<Object>> reportDetailedVisits(String park, int year, int month) {
		ArrayList<ArrayList<Object>> rows = new ArrayList<>();

		String q = "SELECT booking_id, visitor_id, park_name, visit_date, visit_time, visitors_count, "
				+ "booking_type, is_guide_group, checkin_time, checkout_time, status "
				+ "FROM bookings "
				+ "WHERE park_name = ? "
				+ "AND YEAR(visit_date) = ? "
				+ "AND MONTH(visit_date) = ? "
				+ "AND status IN ('Entered', 'Exited', 'Confirmed') "
				+ "ORDER BY visit_date, visit_time, booking_id";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, park);
			ps.setInt(2, year);
			ps.setInt(3, month);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int visitorsCount = rs.getInt("visitors_count");
					boolean guideGroup = rs.getInt("is_guide_group") == 1
							|| "Guide".equals(rs.getString("booking_type"));

					String visitType;
					if (guideGroup) {
						visitType = "Guided Group";
					} else if (visitorsCount == 1) {
						visitType = "Alone";
					} else {
						visitType = "Private Group";
					}

					ArrayList<Object> row = new ArrayList<>();
					row.add(rs.getInt("booking_id"));
					row.add(rs.getString("visitor_id"));
					row.add(String.valueOf(rs.getDate("visit_date")));
					row.add(String.valueOf(rs.getTime("visit_time")));
					row.add(visitorsCount);
					row.add(visitType);
					row.add(String.valueOf(rs.getTimestamp("checkin_time")));
					row.add(String.valueOf(rs.getTimestamp("checkout_time")));
					row.add(rs.getString("status"));

					rows.add(row);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rows;
	}
	/**
	 * Generates a monthly cancellation and no-show report.
	 *
	 * @param park park name, or "ALL" for all parks
	 * @param year report year
	 * @param month report month
	 * @return report data including daily rows, total cancellations, total no-shows and average cancellations
	 */
	public ArrayList<Object> reportCancellations(String park, int year, int month) {
		ArrayList<ArrayList<Object>> dailyRows = new ArrayList<>();

		int totalCancelled = 0;
		int totalNoShow = 0;

		String parkCondition = "ALL".equals(park) ? "" : "AND park_name = ? ";

		String q = "SELECT DAY(visit_date) AS visit_day, "
				+ "SUM(CASE WHEN status = 'Cancelled' THEN 1 ELSE 0 END) AS cancelled_count, "
				+ "SUM(CASE WHEN status NOT IN ('Cancelled', 'Entered', 'Exited') "
				+ "AND TIMESTAMP(visit_date, visit_time) < CURRENT_TIMESTAMP THEN 1 ELSE 0 END) AS no_show_count "
				+ "FROM bookings "
				+ "WHERE YEAR(visit_date) = ? "
				+ "AND MONTH(visit_date) = ? "
				+ parkCondition
				+ "GROUP BY DAY(visit_date) "
				+ "ORDER BY visit_day";

		try (PreparedStatement ps = connection.prepareStatement(q)) {
			int index = 1;

			ps.setInt(index++, year);
			ps.setInt(index++, month);

			if (!"ALL".equals(park)) {
				ps.setString(index++, park);
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int day = rs.getInt("visit_day");
					int cancelled = rs.getInt("cancelled_count");
					int noShow = rs.getInt("no_show_count");

					if (cancelled > 0 || noShow > 0) {
						ArrayList<Object> row = new ArrayList<>();
						row.add(String.valueOf(day));
						row.add(cancelled);
						row.add(noShow);
						dailyRows.add(row);

						totalCancelled += cancelled;
						totalNoShow += noShow;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		int daysWithData = dailyRows.size();
		double averageCancelledPerDay = daysWithData == 0 ? 0.0 : (double) totalCancelled / daysWithData;

		ArrayList<Object> result = new ArrayList<>();
		result.add(dailyRows);
		result.add(totalCancelled);
		result.add(totalNoShow);
		result.add(averageCancelledPerDay);

		return result;
	}
}
