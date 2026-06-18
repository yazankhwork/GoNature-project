package common;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a booking in the GoNature system.
 * <p>
 * A booking contains visitor information, park details, visit date and time,
 * booking status, payment information, and visitor type.
 * </p>
 *
 * This class implements {@link Serializable} so booking objects can be
 * transferred between the client and server.
 *
 * @author Bolos Saad
 */
public class Booking implements Serializable {

	/**
	 * Serialization version identifier.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Unique booking identifier.
	 */
	private int bookingId;

	/**
	 * ID of the visitor who created the booking.
	 */
	private String visitorId;

	/**
	 * Name of the selected park.
	 */
	private String parkName;

	/**
	 * Scheduled visit date.
	 */
	private LocalDate visitDate;

	/**
	 * Scheduled visit time.
	 */
	private LocalTime visitTime;

	/**
	 * Number of visitors included in the booking.
	 */
	private int visitorsCount;

	/**
	 * Current booking status.
	 */
	private String email;
	
	private String status;

	/**
	 * Total price paid for the booking.
	 */
	private int price;

	/**
	 * Type of visitor associated with the booking.
	 */
	private String visitorType;
	private boolean guideGroup;
	private boolean subscriber;
	/**
	 * Creates a booking with a specified status.
	 *
	 * @param bookingId     unique booking identifier
	 * @param visitorId     visitor ID
	 * @param parkName      selected park name
	 * @param visitDate     visit date
	 * @param visitTime     visit time
	 * @param visitorsCount number of visitors
	 * @param status        booking status
	 */
	public Booking(int bookingId, String visitorId, String parkName, LocalDate visitDate, LocalTime visitTime,
			int visitorsCount, String status) {

		this.bookingId = bookingId;
		this.visitorId = visitorId;
		this.parkName = parkName;
		this.visitDate = visitDate;
		this.visitTime = visitTime;
		this.visitorsCount = visitorsCount;
		this.status = status;
	}

	/**
	 * Creates a booking with the default status "Pending".
	 *
	 * @param bookingId     unique booking identifier
	 * @param visitorId     visitor ID
	 * @param parkName      selected park name
	 * @param visitDate     visit date
	 * @param visitTime     visit time
	 * @param visitorsCount number of visitors
	 */
	public Booking(int bookingId, String visitorId, String parkName, LocalDate visitDate, LocalTime visitTime,
			int visitorsCount) {

		this.bookingId = bookingId;
		this.visitorId = visitorId;
		this.parkName = parkName;
		this.visitDate = visitDate;
		this.visitTime = visitTime;
		this.visitorsCount = visitorsCount;
		this.status = "Pending";
	}

	/**
	 * Returns the booking ID.
	 *
	 * @return booking ID
	 */
	public int getBookingId() {
		return bookingId;
	}

	/**
	 * Sets the booking ID.
	 *
	 * @param bookingId booking ID
	 */
	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}

	/**
	 * Returns the visitor ID.
	 *
	 * @return visitor ID
	 */
	public String getVisitorId() {
		return visitorId;
	}

	/**
	 * Sets the visitor ID.
	 *
	 * @param visitorId visitor ID
	 */
	public void setVisitorId(String visitorId) {
		this.visitorId = visitorId;
	}

	/**
	 * Returns the park name.
	 *
	 * @return park name
	 */
	public String getParkName() {
		return parkName;
	}

	/**
	 * Sets the park name.
	 *
	 * @param parkName park name
	 */
	public void setParkName(String parkName) {
		this.parkName = parkName;
	}

	/**
	 * Returns the visit date.
	 *
	 * @return visit date
	 */
	public LocalDate getVisitDate() {
		return visitDate;
	}

	/**
	 * Sets the visit date.
	 *
	 * @param visitDate visit date
	 */
	public void setVisitDate(LocalDate visitDate) {
		this.visitDate = visitDate;
	}

	/**
	 * Returns the visit time.
	 *
	 * @return visit time
	 */
	public LocalTime getVisitTime() {
		return visitTime;
	}

	/**
	 * Sets the visit time.
	 *
	 * @param visitTime visit time
	 */
	public void setVisitTime(LocalTime visitTime) {
		this.visitTime = visitTime;
	}

	/**
	 * Returns the number of visitors.
	 *
	 * @return number of visitors
	 */
	public int getVisitorsCount() {
		return visitorsCount;
	}

	/**
	 * Sets the number of visitors.
	 *
	 * @param visitorsCount number of visitors
	 */
	public void setVisitorsCount(int visitorsCount) {
		this.visitorsCount = visitorsCount;
	}

	/**
	 * Returns the booking status.
	 *
	 * @return booking status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the booking status.
	 *
	 * @param status booking status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Returns the booking price.
	 *
	 * @return booking price
	 */
	public int getPrice() {
		return price;
	}

	/**
	 * Sets the booking price.
	 *
	 * @param price booking price
	 */
	public void setPrice(int price) {
		this.price = price;
	}

	/**
	 * Returns the visitor type.
	 *
	 * @return visitor type
	 */
	public String getVisitorType() {
		return visitorType;
	}

	/**
	 * Sets the visitor type.
	 *
	 * @param visitorType visitor type
	 */
	public void setVisitorType(String visitorType) {
		this.visitorType = visitorType;
	}
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public boolean isGuideGroup() {
		return guideGroup;
	}

	public void setGuideGroup(boolean guideGroup) {
		this.guideGroup = guideGroup;
	}

	public boolean isSubscriber() {
		return subscriber;
	}

	public void setSubscriber(boolean subscriber) {
		this.subscriber = subscriber;
	}
}