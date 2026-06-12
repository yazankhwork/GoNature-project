package common;

import java.io.Serializable;

/**
 * Represents a message exchanged between the client and server.
 * <p>
 * A message consists of a command that specifies the requested
 * operation and an optional data object containing the information
 * associated with that command.
 * </p>
 *
 * This class implements {@link Serializable} so that Message
 * objects can be transmitted through ObjectInputStream and
 * ObjectOutputStream.
 *
 * @author Bolos Saad
 */
public class Message implements Serializable {

    /**
     * Serialization version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Command name describing the requested operation.
     */
    private String command;

    /**
     * Data associated with the command.
     */
    private Object data;

    /**
     * Creates a new message.
     *
     * @param command command name
     * @param data data associated with the command
     */
    public Message(String command, Object data) {
        this.command = command;
        this.data = data;
    }

    /**
     * Returns the command associated with this message.
     *
     * @return command name
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the data associated with this message.
     *
     * @return message data
     */
    public Object getData() {
        return data;
    }
}

