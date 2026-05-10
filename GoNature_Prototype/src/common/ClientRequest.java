package common;

import java.io.Serializable;

public class ClientRequest implements Serializable {

	private String requestType;
	private int orderNumber;
	private String orderDate;
	private int numberOfVisitors;
	
	public ClientRequest(String requestType, int orderNumber) {
	    this.requestType = requestType;
	    this.orderNumber = orderNumber;
	}
	
	public ClientRequest(String requestType, int orderNumber, String orderDate, int numberOfVisitors) {
	    this.requestType = requestType;
	    this.orderNumber = orderNumber;
	    this.orderDate = orderDate;
	    this.numberOfVisitors = numberOfVisitors;
	}
	
	public String getRequestType() {
	    return requestType;
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
}
