package client.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import common.Message;
import client.network.ClientSession;

import java.util.ArrayList;

/**
 * Park manager screen. Lets a park manager view and change a park's maximum
 * capacity (the "quota" parameter from the story). In the full story such a
 * change requires department-manager approval; this student version applies it
 * directly and shows a note.
 */
public class ParkManagerScreen extends Application {

	private final ComboBox<String> parkCombo = new ComboBox<>();
	private final Label currentCapacityLabel = new Label("Current capacity: -");
	private final Label currentBookingPercentLabel = new Label("Bookable percent: -");
	private final Label currentDurationLabel = new Label("Visit duration: -");
	private final TextField newCapacityField = new TextField();
	private final TextField newBookingPercentField = new TextField();
	private final TextField newDurationField = new TextField();
	private final TextField discountNameField = new TextField();
	private final TextField discountPercentField = new TextField();
	private final Label statusLabel = new Label();

	private static Message request(Message m) throws Exception {
		return ClientSession.send(m);
	}

	@Override
	public void start(Stage stage) {
		stage.setTitle("GoNature - Park Manager");

		parkCombo.getItems().addAll("Carmel Park", "Jordan Park", "Banias Park", "Safari Zoo", "Ramon Crater",
				"Hula Valley");
		parkCombo.setValue("Carmel Park");
		parkCombo.setOnAction(e -> loadCapacity());

		newCapacityField.setPromptText("New max capacity");
		newBookingPercentField.setPromptText("New booking percent, e.g. 80");
		newDurationField.setPromptText("New visit duration hours, e.g. 4");

		discountNameField.setPromptText("Discount name, e.g. Winter Discount");
		discountPercentField.setPromptText("Discount percent, e.g. 10");

		Button loadBtn = new Button("Load");
		loadBtn.setOnAction(e -> loadCapacity());

		Button updateBtn = new Button("Send Request");
		updateBtn.setOnAction(e -> {
			int newCap;
			int newPercent;
			int newDuration;

			try {
				newCap = Integer.parseInt(newCapacityField.getText().trim());
				newPercent = Integer.parseInt(newBookingPercentField.getText().trim());
				newDuration = Integer.parseInt(newDurationField.getText().trim());
			} catch (NumberFormatException ex) {
				statusLabel.setStyle("-fx-text-fill: red;");
				statusLabel.setText("Capacity, booking percent, and duration must be numbers.");
				return;
			}

			if (newCap <= 0) {
				statusLabel.setStyle("-fx-text-fill: red;");
				statusLabel.setText("Capacity must be positive.");
				return;
			}

			if (newPercent <= 0 || newPercent > 100) {
				statusLabel.setStyle("-fx-text-fill: red;");
				statusLabel.setText("Booking percent must be between 1 and 100.");
				return;
			}

			if (newDuration <= 0) {
				statusLabel.setStyle("-fx-text-fill: red;");
				statusLabel.setText("Visit duration must be positive.");
				return;
			}

			try {
				ArrayList<Object> data = new ArrayList<>();
				data.add(parkCombo.getValue());
				data.add(newCap);
				data.add(newPercent);
				data.add(newDuration);
				data.add(ClientSession.loggedInId);

				Message resp = request(new Message("CREATE_PARK_CHANGE_REQUEST", data));

				if ("REQUEST_CREATED".equals(resp.getCommand())) {
					statusLabel.setStyle("-fx-text-fill: green;");
					statusLabel.setText("Request sent to department manager. Waiting for approval.");
				} else {
					statusLabel.setStyle("-fx-text-fill: red;");
					statusLabel.setText(String.valueOf(resp.getData()));
				}

			} catch (Exception ex) {
				statusLabel.setStyle("-fx-text-fill: red;");
				statusLabel.setText("Connection error.");
			}
		});
		Button discountBtn = new Button("Send Discount Request");
		discountBtn.setOnAction(e -> {
			String discountName = discountNameField.getText().trim();

			int discountPercent;
			try {
				discountPercent = Integer.parseInt(discountPercentField.getText().trim());
			} catch (NumberFormatException ex) {
				statusLabel.setStyle("-fx-text-fill: red;");
				statusLabel.setText("Discount percent must be a number.");
				return;
			}

			if (discountName.isEmpty()) {
				statusLabel.setStyle("-fx-text-fill: red;");
				statusLabel.setText("Discount name is required.");
				return;
			}

			if (discountPercent <= 0 || discountPercent > 100) {
				statusLabel.setStyle("-fx-text-fill: red;");
				statusLabel.setText("Discount percent must be between 1 and 100.");
				return;
			}

			try {
				ArrayList<Object> data = new ArrayList<>();
				data.add(parkCombo.getValue());
				data.add(discountName);
				data.add(discountPercent);
				data.add(ClientSession.loggedInId);

				Message resp = request(new Message("CREATE_DISCOUNT_REQUEST", data));

				if ("DISCOUNT_REQUEST_CREATED".equals(resp.getCommand())) {
					statusLabel.setStyle("-fx-text-fill: green;");
					statusLabel.setText("Discount request sent to department manager.");
					discountNameField.clear();
					discountPercentField.clear();
				} else {
					statusLabel.setStyle("-fx-text-fill: red;");
					statusLabel.setText(String.valueOf(resp.getData()));
				}

			} catch (Exception ex) {
				statusLabel.setStyle("-fx-text-fill: red;");
				statusLabel.setText("Connection error.");
			}
		});
		Button logoutBtn = new Button("Logout");
		logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
		logoutBtn.setOnAction(e -> LogoutHelper.logout(stage));

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(12);
		grid.addRow(0, new Label("Park:"), parkCombo, loadBtn);
		grid.addRow(1, currentCapacityLabel);
		grid.addRow(2, currentBookingPercentLabel);
		grid.addRow(3, currentDurationLabel);

		grid.addRow(4, new Label("New capacity:"), newCapacityField);
		grid.addRow(5, new Label("New booking percent:"), newBookingPercentField);
		grid.addRow(6, new Label("New duration hours:"), newDurationField, updateBtn);
		
		grid.addRow(7, new Label("Discount name:"), discountNameField);
		grid.addRow(8, new Label("Discount percent:"), discountPercentField, discountBtn);

		VBox layout = new VBox(15, grid, statusLabel, logoutBtn);
		layout.setPadding(new Insets(20));
		stage.setScene(new Scene(layout, 650, 430));
		stage.show();

		loadCapacity();
	}

	private void loadCapacity() {
		try {
			Message resp = request(new Message("GET_PARK_PARAMS", parkCombo.getValue()));
			if ("PARK_PARAMS".equals(resp.getCommand())) {
				@SuppressWarnings("unchecked")
				ArrayList<Object> params = (ArrayList<Object>) resp.getData();

				int capacity = (int) params.get(0);
				int bookingPercent = (int) params.get(1);
				int duration = (int) params.get(2);

				currentCapacityLabel.setText("Current capacity: " + capacity);
				currentBookingPercentLabel.setText("Bookable percent: " + bookingPercent + "%");
				currentDurationLabel.setText("Visit duration: " + duration + " hours");

				newCapacityField.setText(String.valueOf(capacity));
				newBookingPercentField.setText(String.valueOf(bookingPercent));
				newDurationField.setText(String.valueOf(duration));
			}
		} catch (Exception ex) {
			currentCapacityLabel.setText("Current capacity: (connection error)");
		}
	}
}