package client.network;

import common.Message;

/**
 * Observer interface used in the Observer design pattern.
 *
 * Classes that implement this interface can subscribe to
 * asynchronous server notifications and receive live messages
 * from the GoNature server.
 *
 * @author Group 4
 * @version 1.0
 */
public interface INetworkObserver {
	/**
	 * Called when a new asynchronous message is received from the server.
	 *
	 * @param msg message received from the server
	 */
	void onMessageReceived(Message msg);
}