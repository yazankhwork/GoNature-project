package client.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import common.Message;
import javafx.application.Platform;
import ocsf.client.AbstractClient;

public class GoNatureClient extends AbstractClient {

	private final BlockingQueue<Message> responses = new ArrayBlockingQueue<>(1);
	
	// --- OBSERVER DESIGN PATTERN: List of Subscribers ---
	private final List<INetworkObserver> observers = new ArrayList<>();

	public GoNatureClient(String host, int port) {
		super(host, port);
	}

	// --- OBSERVER DESIGN PATTERN: Attach / Detach ---
	public void attachObserver(INetworkObserver observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}

	public void detachObserver(INetworkObserver observer) {
		observers.remove(observer);
	}

	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg instanceof Message) {
			Message m = (Message) msg;
			
			// Observer Pattern: Handle live PUSH messages from the server asynchronously
			if (m.getCommand().startsWith("SERVER_PUSH_")) {
				for (INetworkObserver observer : observers) {
					// Must run on JavaFX Application Thread to update the UI safely
					Platform.runLater(() -> observer.onMessageReceived(m));
				}
			} else {
				// Standard synchronous response for sendAndWait
				responses.offer(m);
			}
		}
	}

	public synchronized Message sendAndWait(Message request) throws IOException {
		responses.clear();
		sendToServer(request);
		try {
			Message resp = responses.poll(10, TimeUnit.SECONDS);
			if (resp == null) {
				throw new IOException("No response from server (timed out).");
			}
			return resp;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while waiting for the server.");
		}
	}

	@Override
	protected void connectionException(Exception exception) {
		System.err.println("Connection to server lost: " + exception.getMessage());
	}
}