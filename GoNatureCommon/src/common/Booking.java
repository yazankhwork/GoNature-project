package common;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
/**
 * Represents a booking in the GoNature system.
 * Stores reservation details such as visitor information,
 * park name, visit date, visit time, number of visitors,
 * booking status, price and visitor type.
 *
 * @author Group 4
 * @version 1.0
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
	 * Identifier of the visitor who created the booking.
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
	 * Visitor email address.
	 */
	private String email;
	/**
	 * Visitor phone number.
	 */
	private String telephone;
	/**
	 * Current booking status.
	 */
	private String status;
	/**
	 * Total booking price.
	 */
	private int price;
	/**
	 * Type of visitor associated with the booking.
	 */
	private String visitorType;
	/**
	 * Indicates whether the booking is for a guided group.
	 */
	private boolean guideGroup;
	/**
	 * Indicates whether the visitor has an active subscription.
	 */
	private boolean subscriber;
	/**
	 * Creates a booking with a specific status.
	 *
	 * @param bookingId unique booking identifier
	 * @param visitorId visitor identifier
	 * @param parkName selected park name
	 * @param visitDate date of the visit
	 * @param visitTime time of the visit
	 * @param visitorsCount number of visitors
	 * @param status booking status
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
	 * Creates a booking with default status "Pending".
	 *
	 * @param bookingId unique booking identifier
	 * @param visitorId visitor identifier
	 * @param parkName selected park name
	 * @param visitDate date of the visit
	 * @param visitTime time of the visit
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
	* Returns the booking identifier.
	*
	* @return booking identifier
	  */
	  public int getBookingId() { return bookingId; }
	/**
	* Sets the booking identifier.
	*
	* @param bookingId booking identifier
	  */
	  public void setBookingId(int bookingId) { this.bookingId = bookingId; }
	/**
	* Returns the visitor identifier.
	*
	* @return visitor identifier
	  */
	  public String getVisitorId() { return visitorId; }
	/**
	* Sets the visitor identifier.
	*
	* @param visitorId visitor identifier
	  */
	  public void setVisitorId(String visitorId) { this.visitorId = visitorId; }
	/**
	* Returns the selected park name.
	*
	* @return park name
	  */
	  public String getParkName() { return parkName; }
	/**
	* Sets the selected park name.
	*
	* @param parkName park name
	  */
	  public void setParkName(String parkName) { this.parkName = parkName; }
	/**
	* Returns the scheduled visit date.
	*
	* @return visit date
	  */
	  public LocalDate getVisitDate() { return visitDate; }
	/**
	* Sets the scheduled visit date.
	*
	* @param visitDate visit date
	  */
	  public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
	/**
	* Returns the scheduled visit time.
	*
	* @return visit time
	  */
	  public LocalTime getVisitTime() { return visitTime; }
	/**
	* Sets the scheduled visit time.
	*
	* @param visitTime visit time
	  */
	  public void setVisitTime(LocalTime visitTime) { this.visitTime = visitTime; }
	/**
	* Returns the number of visitors in the booking.
	*
	* @return visitors count
	  */
	  public int getVisitorsCount() { return visitorsCount; }
	/**
	* Sets the number of visitors in the booking.
	*
	* @param visitorsCount number of visitors
	  */
	  public void setVisitorsCount(int visitorsCount) { this.visitorsCount = visitorsCount; }
	/**
	* Returns the booking status.
	*
	* @return booking status
	  */
	  public String getStatus() { return status; }
	/**
	* Sets the booking status.
	*
	* @param status booking status
	  */
	  public void setStatus(String status) { this.status = status; }
	/**
	* Returns the booking price.
	*
	* @return booking price
	  */
	  public int getPrice() { return price; }
	/**
	* Sets the booking price.
	*
	* @param price booking price
	  */
	  public void setPrice(int price) { this.price = price; }
	/**
	* Returns the visitor type.
	*
	* @return visitor type
	  */
	  public String getVisitorType() { return visitorType; }
	/**
	* Sets the visitor type.
	*
	* @param visitorType visitor type
	  */
	  public void setVisitorType(String visitorType) { this.visitorType = visitorType; }
	/**
	* Returns the visitor email address.
	*
	* @return email address
	  */
	  public String getEmail() { return email; }
	/**
	* Sets the visitor email address.
	*
	* @param email email address
	  */
	  public void setEmail(String email) { this.email = email; }
	/**
	* Returns the visitor phone number.
	*
	* @return phone number
	  */
	  public String getTelephone() { return telephone; }
	/**
	* Sets the visitor phone number.
	*
	* @param telephone phone number
	  */
	  public void setTelephone(String telephone) { this.telephone = telephone; }
	/**
	* Returns whether the booking is for a guided group.
	*
	* @return true if guided group, otherwise false
	  */
	  public boolean isGuideGroup() { return guideGroup; }
	/**
	* Sets whether the booking is for a guided group.
	*
	* @param guideGroup guide group status
	  */
	  public void setGuideGroup(boolean guideGroup) { this.guideGroup = guideGroup; }
	/**
	* Returns whether the visitor has an active subscription.
	*
	* @return true if subscriber, otherwise false
	  */
	  public boolean isSubscriber() { return subscriber; }
	/**
	* Sets the subscription status.
	*
	* @param subscriber subscription status
	  */
	  public void setSubscriber(boolean subscriber) { this.subscriber = subscriber; }
}