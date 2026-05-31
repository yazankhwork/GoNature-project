package common;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String command;
    private Object data;

    public Message(String command, Object data) {
        this.command = command;
        this.data = data;
    }

    public String getCommand() { return command; }
    public Object getData() { return data; }
}