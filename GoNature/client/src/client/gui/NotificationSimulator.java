package client.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Simulates sending an email / SMS notification, as required by the project
 * story (actual sending is optional; the minimum requirement is a popup titled
 * "Simulation" that also shows the destination email / phone).
 */
public class NotificationSimulator {

	/**
	 * Shows a simulation popup representing a message that the system would send.
	 *
	 * @param email   destination email address (may be {@code null})
	 * @param phone   destination phone number (may be {@code null})
	 * @param subject short subject line
	 * @param body    message body
	 */
	public static void send(String email, String phone, String subject, String body) {
		StringBuilder sb = new StringBuilder();
		sb.append("To Email: ").append(email == null ? "(none)" : email).append("\n");
		sb.append("To SMS:   ").append(phone == null ? "(none)" : phone).append("\n\n");
		sb.append("Subject: ").append(subject).append("\n\n");
		sb.append(body);

		Alert alert = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
		alert.setTitle("Simulation");
		alert.setHeaderText("Simulation - Email / SMS sent by the system");
		alert.showAndWait();
	}
}
