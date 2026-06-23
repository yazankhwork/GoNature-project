package client.gui;

import client.network.ClientSession;
import common.Message;
import javafx.stage.Stage;

public class LogoutHelper {

	public static void logout(Stage currentStage) {
		
		try {
			// שליחת הודעה לשרת לשחרר את הנעילה של המשתמש
			String idToLogout = "";
			if (ClientSession.loggedInId != null && !ClientSession.loggedInId.isEmpty()) {
				idToLogout = ClientSession.loggedInId; // עובד
			} else if (ClientDashboard.loggedInVisitorId != null && !ClientDashboard.loggedInVisitorId.isEmpty() && !ClientDashboard.isGuest) {
				idToLogout = ClientDashboard.loggedInVisitorId; // מבקר (לא Guest)
			}

			if (!idToLogout.isEmpty()) {
				ClientSession.send(new Message("LOGOUT", idToLogout));
			}
		} catch (Exception e) {
			// התעלמות משגיאות ניתוק למקרה שהשרת נפל
		}

		// Clear visitor data
		ClientDashboard.loggedInVisitorId = "";
		ClientDashboard.loggedInName = "";
		ClientDashboard.isAccountGuide = false;
		ClientDashboard.isSubscriberAccount = false;
		ClientDashboard.subscriptionNumber = "";
		ClientDashboard.isGuest = false;

		// Clear employee/session data
		ClientSession.loggedInId = "";
		ClientSession.role = "";
		ClientSession.employeeParkName = "";
		WorkerDashboard.loggedInEmpName = "Representative";
		EntryWorkerDashboard.loggedInEmpName = "Worker";

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