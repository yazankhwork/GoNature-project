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

public class EntryWorkerDashboard extends Application {

	public static String loggedInEmpName = "Worker";

	private final ComboBox<String> parkCombo = new ComboBox<>();
	private final Label result = new Label("Ready.");

	private static Message request(Message m) throws Exception {
		return ClientSession.send(m);
	}

	@Override
	public void start(Stage stage) {
		stage.setTitle("GoNature - Entry Worker");

		Label title = new Label("🌲 Entry Worker Station 🌿");
		title.setFont(Font.font("System", FontWeight.BOLD, 22));
		title.setStyle("-fx-text-fill: #1b5e20;");
		Label subtitle = new Label("Welcome, " + loggedInEmpName);
		subtitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #388e3c;");

		Button logout = new Button("Logout");
		logout.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		logout.setOnAction(e -> LogoutHelper.logout(stage));
		HBox top = new HBox(20, title, subtitle, logout);
		HBox.setHgrow(subtitle, Priority.ALWAYS);

		parkCombo.getItems().addAll(common.Parks.NAMES);
		parkCombo.setValue(ClientSession.employeeParkName);
		parkCombo.setDisable(true);
		parkCombo.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		Button active = new Button("Visitors In Park Now");
		active.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
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

		TextField bookingId = new TextField();
		bookingId.setPromptText("Confirmation Code");
		bookingId.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px;");
		
		Button checkIn = new Button("Check In");
		checkIn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		Button checkOut = new Button("Check Out");
		checkOut.setStyle("-fx-background-color: #f57c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");

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

		TextField casualCount = new TextField("1");
		casualCount.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px;");
		CheckBox casualGuideGroup = new CheckBox("Casual group with guide");
		
		Button casual = new Button("Admit Walk-in");
		casual.setStyle("-fx-background-color: #5d4037; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
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

		result.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b5e20; -fx-font-size: 14px;");
		result.setWrapText(true);

		GridPane parkRow = new GridPane();
		parkRow.setHgap(10);
		parkRow.addRow(0, new Label("Park:"), parkCombo, active);

		VBox layout = new VBox(15, top, new Separator(), parkRow, new Separator(), entryRow, casualRow, new Separator(),
				result);
		layout.setPadding(new Insets(20));
		layout.setStyle("-fx-background-color: #e8f5e9;");
		stage.setScene(new Scene(layout, 640, 380));
		stage.show();
	}
}