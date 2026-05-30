package common;

import java.io.Serializable;

public class ServerResponse implements Serializable {

    private boolean success;
    private String message;
    private Order order;

    public ServerResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ServerResponse(boolean success, String message, Order order) {
        this.success = success;
        this.message = message;
        this.order = order;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Order getOrder() {
        return order;
    }
}