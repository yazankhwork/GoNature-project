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
/**
 * Client-side network communication class for the GoNature system.
 *
 * This class manages communication with the server, sends requests,
 * receives responses and supports asynchronous server push messages
 * using the Observer design pattern.
 *
 * @author Group 4
 * @version 1.0
 */
public class GoNatureClient extends AbstractClient {
	/**
	 * Queue used to store synchronous responses received from the server.
	 */
	private final BlockingQueue<Message> responses = new ArrayBlockingQueue<>(1);
	/**
	 * List of observers subscribed to asynchronous network messages.
	 */
	private final List<INetworkObserver> observers = new ArrayList<>();
	/**
	 * Creates a new GoNature client connection.
	 *
	 * @param host server host address
	 * @param port server port
	 */
	public GoNatureClient(String host, int port) {
		super(host, port);
	}
	/**
	 * Adds an observer to receive asynchronous server messages.
	 *
	 * @param observer observer to add
	 */
	public void attachObserver(INetworkObserver observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}
	/**
	 * Removes an observer from the observer list.
	 *
	 * @param observer observer to remove
	 */
	public void detachObserver(INetworkObserver observer) {
		observers.remove(observer);
	}
	/**
	 * Handles messages received from the server.
	 *
	 * Server push messages are sent to registered observers,
	 * while regular response messages are stored in the response queue.
	 *
	 * @param msg message received from the server
	 */
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
	/**
	 * Sends a request to the server and waits for a response.
	 *
	 * @param request request message
	 * @return response message received from the server
	 * @throws IOException if no response is received or communication fails
	 */
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
	/**
	 * Handles connection exceptions between the client and server.
	 *
	 * @param exception connection exception
	 */
	@Override
	protected void connectionException(Exception exception) {
		System.err.println("Connection to server lost: " + exception.getMessage());
	}
}