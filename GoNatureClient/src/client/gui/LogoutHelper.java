package client.gui;

import client.network.ClientSession;
import common.Message;
import javafx.stage.Stage;
/**
 * Utility class responsible for handling user logout operations.
 *
 * This class clears user session information, notifies the server
 * about the logout operation and returns the user to the login screen.
 *
 * @author Group 4
 * @version 1.0
 */
public class LogoutHelper {
	/**
	 * Logs out the currently connected user.
	 *
	 * The method releases the user lock on the server,
	 * clears all session data, closes the current window
	 * and opens the login screen.
	 *
	 * @param currentStage current application stage
	 */
	public static void logout(Stage currentStage) {
		
		try {
			String idToLogout = "";
			if (ClientSession.loggedInId != null && !ClientSession.loggedInId.isEmpty()) {
				idToLogout = ClientSession.loggedInId;
			} else if (ClientDashboard.loggedInVisitorId != null && !ClientDashboard.loggedInVisitorId.isEmpty() && !ClientDashboard.isGuest) {
				idToLogout = ClientDashboard.loggedInVisitorId; // 
			}

			if (!idToLogout.isEmpty()) {
				ClientSession.send(new Message("LOGOUT", idToLogout));
			}
		} catch (Exception e) {
		}

		ClientDashboard.loggedInVisitorId = "";
		ClientDashboard.loggedInName = "";
		ClientDashboard.isAccountGuide = false;
		ClientDashboard.isSubscriberAccount = false;
		ClientDashboard.subscriptionNumber = "";
		ClientDashboard.isGuest = false;
		ClientSession.loggedInId = "";
		ClientSession.role = "";
		ClientSession.employeeParkName = "";
		WorkerDashboard.loggedInEmpName = "Representative";
		EntryWorkerDashboard.loggedInEmpName = "Worker";
		currentStage.close();
		try {
			new ClientConnectionScreen().start(new Stage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}