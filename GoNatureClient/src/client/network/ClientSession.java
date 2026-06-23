package client.network;

import java.io.IOException;
import common.Message;

public class ClientSession {

	private static GoNatureClient client;
	public static String loggedInId = "";
	public static String role = "";
	public static String employeeParkName = "";

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

	public static synchronized void close() {
		try {
			if (client != null)
				client.closeConnection();
		} catch (IOException ignore) {
		}
		client = null;
	}

	public static boolean isConnected() {
		return client != null && client.isConnected();
	}

	// --- OBSERVER DESIGN PATTERN: Static access to subscribe UI screens ---
	public static void addObserver(INetworkObserver observer) {
		if (client != null) {
			client.attachObserver(observer);
		}
	}

	public static void removeObserver(INetworkObserver observer) {
		if (client != null) {
			client.detachObserver(observer);
		}
	}
}