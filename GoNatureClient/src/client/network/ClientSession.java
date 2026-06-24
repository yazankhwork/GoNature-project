package client.network;

import java.io.IOException;
import common.Message;
/**
 * Manages the client session and communication with the GoNature server.
 *
 * This class maintains a single client connection, provides methods
 * for sending requests to the server and stores information about
 * the currently logged-in user.
 *
 * It also supports the Observer design pattern for notifying GUI screens
 * about network events.
 *
 * @author Group 4
 * @version 1.0
 */
public class ClientSession {
	/**
	 * Active client connection instance.
	 */
	private static GoNatureClient client;
	/**
	 * Identifier of the currently logged-in user.
	 */
	public static String loggedInId = "";
	/**
	 * Role of the currently logged-in user.
	 */
	public static String role = "";
	/**
	 * Park assigned to the currently logged-in employee.
	 */
	public static String employeeParkName = "";
	/**
	 * Establishes a connection to the server.
	 *
	 * @param host server host address
	 * @param port server port
	 * @throws IOException if the connection fails
	 */
	public static synchronized void connect(String host, int port) throws IOException {
		if (client != null && client.isConnected()) {
			try {
				client.closeConnection();
			} catch (IOException ignore) {
			}
		}
		client = new GoNatureClient(host, port);
		client.openConnection();
	}
	/**
	 * Sends a request to the server and waits for a response.
	 *
	 * @param request request message
	 * @return server response message
	 */
	public static synchronized Message send(Message request) {
		try {
			if (client == null || !client.isConnected()) {
				return new Message("ERROR", "Not connected to server.");
			}
			return client.sendAndWait(request);
		} catch (IOException e) {
			return new Message("ERROR", e.getMessage());
		}
	}
	/**
	 * Closes the current client connection.
	 */
	public static synchronized void close() {
		try {
			if (client != null)
				client.closeConnection();
		} catch (IOException ignore) {
		}
		client = null;
	}
	/**
	 * Checks whether the client is connected to the server.
	 *
	 * @return true if connected, otherwise false
	 */
	public static boolean isConnected() {
		return client != null && client.isConnected();
	}

	/**
	 * Registers an observer for network events.
	 *
	 * @param observer observer to register
	 */	
	public static void addObserver(INetworkObserver observer) {
		if (client != null) {
			client.attachObserver(observer);
		}
	}
	/**
	 * Removes a registered network observer.
	 *
	 * @param observer observer to remove
	 */
	public static void removeObserver(INetworkObserver observer) {
		if (client != null) {
			client.detachObserver(observer);
		}
	}
}