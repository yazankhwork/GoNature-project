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
    private String status;
    
    // השדות החדשים שהוספנו להצגת מחיר וסוג משתמש בטבלה
    private int price;
    private String visitorType;

    // The NEW constructor (7 parameters - includes status)
    public Booking(int bookingId, String visitorId, String parkName, LocalDate visitDate, LocalTime visitTime, int visitorsCount, String status) {
        this.bookingId = bookingId;
        this.visitorId = visitorId;
        this.parkName = parkName;
        this.visitDate = visitDate;
        this.visitTime = visitTime;
        this.visitorsCount = visitorsCount;
        this.status = status;
    }

    // The OLD constructor (6 parameters)
    public Booking(int bookingId, String visitorId, String parkName, LocalDate visitDate, LocalTime visitTime, int visitorsCount) {
        this.bookingId = bookingId;
        this.visitorId = visitorId;
        this.parkName = parkName;
        this.visitDate = visitDate;
        this.visitTime = visitTime;
        this.visitorsCount = visitorsCount;
        this.status = "Pending"; 
    }

    // --- Getters and Setters ---
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

    // הגטרים והסתרים החדשים עבור המחיר וסוג המבקר
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getVisitorType() { return visitorType; }
    public void setVisitorType(String visitorType) { this.visitorType = visitorType; }
}