package client.gui;

import common.Booking;
import common.Message;
import client.network.ClientSession;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entry-worker dashboard: gate check-in by booking ID, check-out, casual
 * walk-in admission, and a live count of visitors currently in the park.
 * Written in the same code-built JavaFX + string-command style as the rest of
 * this project (no FXML), so it drops straight into the existing client.
 */
public class EntryWorkerDashboard extends Application {

	/** Set by the login screen before this dashboard opens. */
	public static String loggedInEmpName = "Worker";

	private final ComboBox<String> parkCombo = new ComboBox<>();
	private final Label result = new Label("Ready.");

	/**
	 * One request, one response, matching this project's socket-per-action style.
	 */
	private static Message request(Message m) throws Exception {
		return ClientSession.send(m);
	}

	@Override
	public void start(Stage stage) {
		stage.setTitle("GoNature - Entry Worker");

		Label title = new Label("Entry Worker Station");
		title.setFont(Font.font("System", FontWeight.BOLD, 22));
		title.setStyle("-fx-text-fill: #2c3e50;");
		Label subtitle = new Label("Welcome, " + loggedInEmpName);

		Button logout = new Button("Logout");
		logout.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");
		logout.setOnAction(e -> LogoutHelper.logout(stage));
		HBox top = new HBox(20, title, subtitle, logout);
		HBox.setHgrow(subtitle, Priority.ALWAYS);

		parkCombo.getItems().addAll(common.Parks.NAMES);
		parkCombo.setValue("Carmel Park");

		Button active = new Button("Visitors In Park Now");
		active.setOnAction(e -> {
			try {
				Message r = request(new Message("GET_ACTIVE_VISITORS", parkCombo.getValue()));
				if ("ACTIVE_VISITORS".equals(r.getCommand())) {
					result.setText("In " + parkCombo.getValue() + " now: " + r.getData() + " visitor(s).");
				}
			} catch (Exception ex) {
				result.setText("Server connection error.");
			}
		});

		// --- Check-in / Check-out by booking ID ---
		TextField bookingId = new TextField();
		bookingId.setPromptText("Confirmation Code");
		Button checkIn = new Button("Check In");
		Button checkOut = new Button("Check Out");

		checkIn.setOnAction(e -> {
			String code = bookingId.getText().trim();
			if (code.isEmpty()) {
				new Alert(Alert.AlertType.ERROR, "Enter confirmation code.").showAndWait();
				return;
			}

			try {
				Message r = request(new Message("CHECKIN", code));
				if ("CHECKIN_OK".equals(r.getCommand())) {
					result.setText(String.valueOf(r.getData()));
				} else {
					new Alert(Alert.AlertType.ERROR, String.valueOf(r.getData())).showAndWait();
				}
			} catch (Exception ex) {
				result.setText("Server connection error.");
			}
		});

		checkOut.setOnAction(e -> {
			String code = bookingId.getText().trim();
			if (code.isEmpty()) {
				new Alert(Alert.AlertType.ERROR, "Enter confirmation code.").showAndWait();
				return;
			}

			try {
				Message r = request(new Message("CHECKOUT", code));
				result.setText("CHECKOUT_OK".equals(r.getCommand()) ? "Check-out registered." : "Check-out failed.");
			} catch (Exception ex) {
				result.setText("Server connection error.");
			}
		});

		HBox entryRow = new HBox(10, new Label("Confirmation Code:"), bookingId, checkIn, checkOut);

		// --- Casual walk-in ---
		TextField casualCount = new TextField("1");
		CheckBox casualGuideGroup = new CheckBox("Casual group with guide");
		Button casual = new Button("Admit Walk-in");
		casual.setOnAction(e -> {
			int n;
			try {
				n = Integer.parseInt(casualCount.getText().trim());
			} catch (NumberFormatException ex) {
				new Alert(Alert.AlertType.ERROR, "Visitors must be a number.").showAndWait();
				return;
			}
			if (n < 1) {
				new Alert(Alert.AlertType.ERROR, "Visitors must be at least 1.").showAndWait();
				return;
			}

			if (casualGuideGroup.isSelected() && (n < 2 || n > 15)) {
				new Alert(Alert.AlertType.ERROR,
						"Casual group with guide must include 2 to 15 people including the guide.").showAndWait();
				return;
			}
			boolean guideGroup = casualGuideGroup.isSelected();

			Booking b = new Booking(0, "CASUAL", parkCombo.getValue(), LocalDate.now(),
					LocalTime.now().withSecond(0).withNano(0), n, "Entered");

			b.setVisitorType(guideGroup ? "Guide" : "Regular Visitor");
			b.setGuideGroup(guideGroup);
			b.setSubscriber(false);
			try {
				Message r = request(new Message("CASUAL_VISIT", b));
				if ("CASUAL_OK".equals(r.getCommand())) {
					result.setText(String.valueOf(r.getData()));
				} else {
					new Alert(Alert.AlertType.ERROR, String.valueOf(r.getData())).showAndWait();
				}
			} catch (Exception ex) {
				result.setText("Server connection error.");
			}
		});
		HBox casualRow = new HBox(10, new Label("Walk-in visitors:"), casualCount, casualGuideGroup, casual);

		result.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
		result.setWrapText(true);

		GridPane parkRow = new GridPane();
		parkRow.setHgap(10);
		parkRow.addRow(0, new Label("Park:"), parkCombo, active);

		VBox layout = new VBox(15, top, new Separator(), parkRow, new Separator(), entryRow, casualRow, new Separator(),
				result);
		layout.setPadding(new Insets(20));
		stage.setScene(new Scene(layout, 640, 380));
		stage.show();
	}
}