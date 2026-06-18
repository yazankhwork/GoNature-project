package client.network;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import common.Message;
import ocsf.client.AbstractClient;

/**
 * GoNature client built on the OCSF AbstractClient framework.
 *
 * OCSF is asynchronous: the server's reply arrives on a background thread in
 * handleMessageFromServer. This project's screens work in a simple
 * request-then-response style, so this class wraps OCSF with a blocking
 * sendAndWait(...) that returns the matching reply. One connection is opened at
 * startup and reused for the whole session (no socket per request).
 */
public class GoNatureClient extends AbstractClient {

	/** Holds the single most-recent server response. */
	private final BlockingQueue<Message> responses = new ArrayBlockingQueue<>(1);

	public GoNatureClient(String host, int port) {
		super(host, port);
	}

	/** Called by OCSF on a background thread when the server sends a Message. */
	@Override
	protected void handleMessageFromServer(Object msg) {
		if (msg instanceof Message) {
			responses.offer((Message) msg);
		}
	}

	/**
	 * Sends a request and blocks until the response arrives (max 10 seconds). Safe
	 * to call from the JavaFX thread because it only returns the value; it does not
	 * touch the UI itself.
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

	@Override
	protected void connectionException(Exception exception) {
		System.err.println("Connection to server lost: " + exception.getMessage());
	}
}