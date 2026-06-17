package client.network;

import java.io.IOException;

import common.Message;

/**
 * Holds ONE shared OCSF connection used by every client screen, so the client
 * connects once and reuses that connection for all commands (no socket per
 * request). All screens call ClientSession.send(...).
 */
public class ClientSession {

	private static final int PORT = 5555;
	private static GoNatureClient client;

	/** Optional shared login info that any screen can read. */
	public static String loggedInId = "";
	public static String role = "";

	/** Opens (or re-opens) the single connection to the given host. */
	public static synchronized void connect(String host) throws IOException {
		if (client != null && client.isConnected()) {
			try { client.closeConnection(); } catch (IOException ignore) { }
		}
		client = new GoNatureClient(host, PORT);
		client.openConnection();
	}

	/**
	 * Sends a request and returns the response. Never returns null: on any error
	 * it returns a Message with command "ERROR" so screens never crash.
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

	/** Closes the shared connection (used on logout/exit). */
	public static synchronized void close() {
		try { if (client != null) client.closeConnection(); } catch (IOException ignore) { }
		client = null;
	}

	public static boolean isConnected() {
		return client != null && client.isConnected();
	}
}
