package client.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import common.Message;
import client.network.ClientSession;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
	private final Label visitorsInsideLabel = new Label("Visitors currently inside: -");
	private final Label freeCapacityLabel = new Label("Free places by max capacity: -");
	private final TextField newCapacityField = new TextField();
	private final TextField newBookingPercentField = new TextField();
	private final TextField newDurationField = new TextField();
	private final TextField discountNameField = new TextField();
	private final TextField discountPercentField = new TextField();
	private final ComboBox<Integer> reportMonthCombo = new ComboBox<>();
	private final ComboBox<Integer> reportYearCombo = new ComboBox<>();
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
		for (int m = 1; m <= 12; m++) {
			reportMonthCombo.getItems().add(m);
		}
		reportMonthCombo.setValue(LocalDate.now().getMonthValue());

		int thisYear = LocalDate.now().getYear();
		for (int y = thisYear - 2; y <= thisYear; y++) {
			reportYearCombo.getItems().add(y);
		}
		reportYearCombo.setValue(thisYear);

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
		Button monthlyVisitorsReportBtn = new Button("Monthly Visitors Report");
		monthlyVisitorsReportBtn.setOnAction(e -> showMonthlyVisitorsReport());
		Button notFullReportBtn = new Button("Park Not-Full Report");
		notFullReportBtn.setOnAction(e -> showParkNotFullReport());
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
		grid.addRow(4, visitorsInsideLabel);
		grid.addRow(5, freeCapacityLabel);

		grid.addRow(6, new Label("New capacity:"), newCapacityField);
		grid.addRow(7, new Label("New booking percent:"), newBookingPercentField);
		grid.addRow(8, new Label("New duration hours:"), newDurationField, updateBtn);

		grid.addRow(9, new Label("Discount name:"), discountNameField);
		grid.addRow(10, new Label("Discount percent:"), discountPercentField, discountBtn);
		grid.addRow(11, new Label("Report month/year:"), reportMonthCombo, reportYearCombo, monthlyVisitorsReportBtn);
		grid.addRow(12, new Label("Capacity report:"), notFullReportBtn);
		
		VBox layout = new VBox(15, grid, statusLabel, logoutBtn);
		layout.setPadding(new Insets(20));
		stage.setScene(new Scene(layout, 780, 620));
		stage.show();

		loadCapacity();
		Timeline refreshTimer = new Timeline(new KeyFrame(Duration.seconds(10), e -> loadCapacity()));
		refreshTimer.setCycleCount(Timeline.INDEFINITE);
		refreshTimer.play();

		stage.setOnCloseRequest(e -> refreshTimer.stop());
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

				Message activeResp = request(new Message("GET_ACTIVE_VISITORS", parkCombo.getValue()));
				if ("ACTIVE_VISITORS".equals(activeResp.getCommand())) {
					int activeVisitors = (int) activeResp.getData();
					visitorsInsideLabel.setText("Visitors currently inside: " + activeVisitors);
					freeCapacityLabel.setText("Free places by max capacity: " + Math.max(0, capacity - activeVisitors));
				}
			}
		} catch (Exception ex) {
			currentCapacityLabel.setText("Current capacity: (connection error)");
			visitorsInsideLabel.setText("Visitors currently inside: (connection error)");
			freeCapacityLabel.setText("Free places by max capacity: -");
		}
	}
	@SuppressWarnings("unchecked")
	private void showMonthlyVisitorsReport() {
		try {
			ArrayList<Object> data = new ArrayList<>();
			data.add(parkCombo.getValue());
			data.add(reportYearCombo.getValue());
			data.add(reportMonthCombo.getValue());

			Message resp = request(new Message("REPORT_VISITS", data));

			if (!"REPORT_VISITS_RESULT".equals(resp.getCommand())) {
				new Alert(Alert.AlertType.ERROR, "Could not load monthly visitors report.").showAndWait();
				return;
			}

			HashMap<String, Integer> report = (HashMap<String, Integer>) resp.getData();

			int total = 0;
			StringBuilder sb = new StringBuilder();

			sb.append("Monthly Visitors Report\n");
			sb.append("Park: ").append(parkCombo.getValue()).append("\n");
			sb.append("Month: ").append(reportMonthCombo.getValue()).append("/").append(reportYearCombo.getValue()).append("\n\n");

			for (Map.Entry<String, Integer> entry : report.entrySet()) {
				sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" visitors\n");
				total += entry.getValue();
			}

			sb.append("\nTotal visitors: ").append(total);

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Monthly Visitors Report");
			alert.setHeaderText("Visitors by Type");
			alert.setContentText(sb.toString());
			alert.showAndWait();

		} catch (Exception ex) {
			new Alert(Alert.AlertType.ERROR, "Connection error while loading report.").showAndWait();
		}
	}
	@SuppressWarnings("unchecked")
	private void showParkNotFullReport() {
		try {
			ArrayList<Object> data = new ArrayList<>();
			data.add(parkCombo.getValue());
			data.add(reportYearCombo.getValue());
			data.add(reportMonthCombo.getValue());

			Message resp = request(new Message("REPORT_NOT_FULL", data));

			if (!"REPORT_NOT_FULL_RESULT".equals(resp.getCommand())) {
				new Alert(Alert.AlertType.ERROR, "Could not load not-full report.").showAndWait();
				return;
			}

			ArrayList<ArrayList<Object>> rows = (ArrayList<ArrayList<Object>>) resp.getData();

			StringBuilder sb = new StringBuilder();
			sb.append("Park Not-Full Report\n");
			sb.append("Park: ").append(parkCombo.getValue()).append("\n");
			sb.append("Month: ").append(reportMonthCombo.getValue()).append("/")
					.append(reportYearCombo.getValue()).append("\n\n");

			if (rows == null || rows.isEmpty()) {
				sb.append("No not-full periods found.");
			} else {
				sb.append("Date | Peak Visitors | Capacity | Free at Peak | Not-Full Hours\n");
				sb.append("------------------------------------------------------------\n");

				for (ArrayList<Object> row : rows) {
					sb.append(row.get(0)).append(" | ")
							.append(row.get(1)).append(" | ")
							.append(row.get(2)).append(" | ")
							.append(row.get(3)).append(" | ")
							.append(row.get(4)).append("\n");
				}
			}

			TextArea area = new TextArea(sb.toString());
			area.setEditable(false);
			area.setWrapText(false);
			area.setPrefWidth(700);
			area.setPrefHeight(450);

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Park Not-Full Report");
			alert.setHeaderText("Times when park was not full");
			alert.getDialogPane().setContent(area);
			alert.showAndWait();

		} catch (Exception ex) {
			new Alert(Alert.AlertType.ERROR, "Connection error while loading not-full report.").showAndWait();
		}
	}
}