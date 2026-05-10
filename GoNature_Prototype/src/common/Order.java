package common;

import java.io.Serializable;

public class Order implements Serializable {

	private int orderNumber;
	private String orderDate;
	private int numberOfVisitors;
	private int confirmationCode;
	private int subscriberId;
	private String dateOfPlacingOrder;

	public Order(int orderNumber, String orderDate, int numberOfVisitors, int confirmationCode, int subscriberId,
			String dateOfPlacingOrder) {
		this.orderNumber = orderNumber;
		this.orderDate = orderDate;
		this.numberOfVisitors = numberOfVisitors;
		this.confirmationCode = confirmationCode;
		this.subscriberId = subscriberId;
		this.dateOfPlacingOrder = dateOfPlacingOrder;
	}
	
	public int getOrderNumber() {
	    return orderNumber;
	}

	public String getOrderDate() {
	    return orderDate;
	}

	public int getNumberOfVisitors() {
	    return numberOfVisitors;
	}

	public int getConfirmationCode() {
	    return confirmationCode;
	}

	public int getSubscriberId() {
	    return subscriberId;
	}

	public String getDateOfPlacingOrder() {
	    return dateOfPlacingOrder;
	}
}
