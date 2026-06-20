package client.network;

import common.Message;

/**
 * Observer Design Pattern - Observer Interface.
 * Any UI Controller that implements this interface can subscribe to live server events.
 */
public interface INetworkObserver {
	void onMessageReceived(Message msg);
}