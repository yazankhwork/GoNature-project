package client.gui;

import client.network.ClientSession;
import javafx.stage.Stage;

public class LogoutHelper {

	public static void logout(Stage currentStage) {
		// Clear visitor data
		ClientDashboard.loggedInVisitorId = "";
		ClientDashboard.loggedInName = "";
		ClientDashboard.isAccountGuide = false;
		ClientDashboard.isSubscriberAccount = false;
		ClientDashboard.subscriptionNumber = "";

		// Clear employee/session data
		ClientSession.loggedInId = "";
		ClientSession.role = "";
		WorkerDashboard.loggedInEmpName = "Representative";
		EntryWorkerDashboard.loggedInEmpName = "Worker";

		// Close socket connection
		// ClientSession.close();

		// Close current screen
		currentStage.close();

		// Go back to connection/login screen
		try {
			new ClientConnectionScreen().start(new Stage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
