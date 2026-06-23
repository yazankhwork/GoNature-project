package common;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Booking implements Serializable {

	private static final long serialVersionUID = 1L;

	private int bookingId;
	private String visitorId;
	private String parkName;
	private LocalDate visitDate;
	private LocalTime visitTime;
	private int visitorsCount;
	private String email;
	private String telephone; // <--- השדה החדש
	private String status;
	private int price;
	private String visitorType;
	private boolean guideGroup;
	private boolean subscriber;

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

	public int getBookingId() { return bookingId; }
	public void setBookingId(int bookingId) { this.bookingId = bookingId; }

	public String getVisitorId() { return visitorId; }
	public void setVisitorId(String visitorId) { this.visitorId = visitorId; }

	public String getParkName() { return parkName; }
	public void setParkName(String parkName) { this.parkName = parkName; }

	public LocalDate getVisitDate() { return visitDate; }
	public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

	public LocalTime getVisitTime() { return visitTime; }
	public void setVisitTime(LocalTime visitTime) { this.visitTime = visitTime; }

	public int getVisitorsCount() { return visitorsCount; }
	public void setVisitorsCount(int visitorsCount) { this.visitorsCount = visitorsCount; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public int getPrice() { return price; }
	public void setPrice(int price) { this.price = price; }

	public String getVisitorType() { return visitorType; }
	public void setVisitorType(String visitorType) { this.visitorType = visitorType; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getTelephone() { return telephone; }
	public void setTelephone(String telephone) { this.telephone = telephone; }

	public boolean isGuideGroup() { return guideGroup; }
	public void setGuideGroup(boolean guideGroup) { this.guideGroup = guideGroup; }

	public boolean isSubscriber() { return subscriber; }
	public void setSubscriber(boolean subscriber) { this.subscriber = subscriber; }
}