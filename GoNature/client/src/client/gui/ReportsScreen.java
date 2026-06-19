package client.gui;

import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import common.Message;
import client.network.ClientSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Department-manager reports screen.
 * <p>
 * Produces the two reports required by the story: a Visits report (number of
 * visitors split by type, shown graphically) and a Cancellations report
 * (cancelled bookings and no-shows for a park and month).
 * </p>
 */
public class ReportsScreen extends Application {

	private final ComboBox<String> parkCombo = new ComboBox<>();
	private final ComboBox<Integer> monthCombo = new ComboBox<>();
	private final ComboBox<Integer> yearCombo = new ComboBox<>();
	private final VBox chartHolder = new VBox(10);
	private final CheckBox allParksCancelCheck = new CheckBox("All parks for cancellations");
	private final Label liveCapacityLabel = new Label("Live capacity: -");
	private static Message request(Message m) throws Exception {
		return ClientSession.send(m);
	}

	@Override
	public void start(Stage stage) {
		stage.setTitle("GoNature - Department Manager Reports");

		parkCombo.getItems().addAll(common.Parks.NAMES);
		parkCombo.setValue("Carmel Park");
		parkCombo.setOnAction(e -> updateLiveCapacity());

		for (int mth = 1; mth <= 12; mth++) {
			monthCombo.getItems().add(mth);
		}
		monthCombo.setValue(LocalDate.now().getMonthValue());

		int thisYear = LocalDate.now().getYear();
		for (int y = thisYear - 2; y <= thisYear; y++) {
			yearCombo.getItems().add(y);
		}
		yearCombo.setValue(thisYear);

		Button visitsBtn = new Button("Visits Report");
		visitsBtn.setOnAction(e -> showVisitsReport());

		Button detailedVisitsBtn = new Button("Detailed Visits");
		detailedVisitsBtn.setOnAction(e -> showDetailedVisitsReport());

		Button cancelBtn = new Button("Cancellations Report");
		cancelBtn.setOnAction(e -> showCancellationsReport());
		Button logoutBtn = new Button("Logout");
		logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
		logoutBtn.setOnAction(e -> LogoutHelper.logout(stage));
		Button requestsBtn = new Button("Park Change Requests");
		requestsBtn.setOnAction(e -> showParkChangeRequests());
		Button discountRequestsBtn = new Button("Discount Requests");
		discountRequestsBtn.setOnAction(e -> showDiscountRequests());

		HBox controls = new HBox(10, new Label("Park:"), parkCombo, new Label("Month:"), monthCombo, new Label("Year:"),
				yearCombo, visitsBtn, detailedVisitsBtn, allParksCancelCheck, cancelBtn, requestsBtn, discountRequestsBtn,
				logoutBtn);

		HBox liveCapacityRow = new HBox(10, liveCapacityLabel);
		liveCapacityRow.setStyle("-fx-padding: 8; -fx-border-color: #cccccc;");

		VBox layout = new VBox(15, controls, liveCapacityRow, chartHolder);
		layout.setPadding(new Insets(20));
		stage.setScene(new Scene(layout, 980, 580));
		stage.show();
		
		updateLiveCapacity();

		Timeline refreshTimer = new Timeline(new KeyFrame(Duration.seconds(10), e -> updateLiveCapacity()));
		refreshTimer.setCycleCount(Timeline.INDEFINITE);
		refreshTimer.play();

		stage.setOnCloseRequest(e -> refreshTimer.stop());
	}
	@SuppressWarnings("unchecked")
	private void updateLiveCapacity() {
		try {
			String parkName = parkCombo.getValue();

			Message paramsResp = request(new Message("GET_PARK_PARAMS", parkName));
			Message activeResp = request(new Message("GET_ACTIVE_VISITORS", parkName));

			if ("PARK_PARAMS".equals(paramsResp.getCommand()) && "ACTIVE_VISITORS".equals(activeResp.getCommand())) {
				ArrayList<Object> params = (ArrayList<Object>) paramsResp.getData();

				int maxCapacity = (int) params.get(0);
				int activeVisitors = (int) activeResp.getData();
				int freePlaces = Math.max(0, maxCapacity - activeVisitors);

				liveCapacityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
				liveCapacityLabel.setText("Live capacity for " + parkName
						+ ": " + activeVisitors + "/" + maxCapacity
						+ " visitors inside | Free places: " + freePlaces);
			} else {
				liveCapacityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
				liveCapacityLabel.setText("Live capacity: could not load data.");
			}

		} catch (Exception ex) {
			liveCapacityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
			liveCapacityLabel.setText("Live capacity: connection error.");
		}
	}

	private ArrayList<Object> filter() {
		ArrayList<Object> data = new ArrayList<>();
		data.add(parkCombo.getValue());
		data.add(yearCombo.getValue());
		data.add(monthCombo.getValue());
		return data;
	}
	private ArrayList<Object> cancellationFilter() {
		ArrayList<Object> data = new ArrayList<>();

		if (allParksCancelCheck.isSelected()) {
			data.add("ALL");
		} else {
			data.add(parkCombo.getValue());
		}

		data.add(yearCombo.getValue());
		data.add(monthCombo.getValue());
		return data;
	}

	@SuppressWarnings("unchecked")
	private void showVisitsReport() {
		try {
			Message resp = request(new Message("REPORT_VISITS", filter()));
			if (!"REPORT_VISITS_RESULT".equals(resp.getCommand())) {
				return;
			}
			HashMap<String, Integer> byType = (HashMap<String, Integer>) resp.getData();

			CategoryAxis x = new CategoryAxis();
			NumberAxis y = new NumberAxis();
			x.setLabel("Visitor Type");
			y.setLabel("Visitors");

			BarChart<String, Number> chart = new BarChart<>(x, y);
			chart.setTitle(
					"Visits - " + parkCombo.getValue() + " " + monthCombo.getValue() + "/" + yearCombo.getValue());

			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName("Realized visitors");
			if (byType.isEmpty()) {
				series.getData().add(new XYChart.Data<>("No data", 0));
			} else {
				for (Map.Entry<String, Integer> en : byType.entrySet()) {
					series.getData().add(new XYChart.Data<>(en.getKey(), en.getValue()));
				}
			}
			chart.getData().add(series);
			chart.setData(FXCollections.observableArrayList(series));

			chartHolder.getChildren().setAll(chart);
		} catch (Exception ex) {
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}
	@SuppressWarnings("unchecked")
	private void showDetailedVisitsReport() {
		try {
			Message resp = request(new Message("REPORT_DETAILED_VISITS", filter()));

			if (!"REPORT_DETAILED_VISITS_RESULT".equals(resp.getCommand())) {
				chartHolder.getChildren().setAll(new Label("Could not load detailed visits report."));
				return;
			}

			ArrayList<ArrayList<Object>> rows = (ArrayList<ArrayList<Object>>) resp.getData();

			StringBuilder sb = new StringBuilder();
			sb.append("Detailed Visits Report\n");
			sb.append("Park: ").append(parkCombo.getValue()).append("\n");
			sb.append("Month: ").append(monthCombo.getValue()).append("/")
					.append(yearCombo.getValue()).append("\n\n");

			if (rows == null || rows.isEmpty()) {
				sb.append("No visits found.");
			} else {
				sb.append("Booking ID | Visitor ID | Date | Time | Count | Type | Check-in | Checkout | Status\n");
				sb.append("--------------------------------------------------------------------------------------\n");

				for (ArrayList<Object> row : rows) {
					sb.append(row.get(0)).append(" | ")
							.append(row.get(1)).append(" | ")
							.append(row.get(2)).append(" | ")
							.append(row.get(3)).append(" | ")
							.append(row.get(4)).append(" | ")
							.append(row.get(5)).append(" | ")
							.append(row.get(6)).append(" | ")
							.append(row.get(7)).append(" | ")
							.append(row.get(8)).append("\n");
				}
			}

			TextArea area = new TextArea(sb.toString());
			area.setEditable(false);
			area.setWrapText(false);
			area.setPrefWidth(850);
			area.setPrefHeight(450);

			chartHolder.getChildren().setAll(area);

		} catch (Exception ex) {
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}

	@SuppressWarnings("unchecked")
	private void showCancellationsReport() {
		try {
			Message resp = request(new Message("REPORT_CANCELLATIONS", cancellationFilter()));
			if (!"REPORT_CANCELLATIONS_RESULT".equals(resp.getCommand())) {
				chartHolder.getChildren().setAll(new Label("Could not load cancellations report."));
				return;
			}

			ArrayList<Object> result = (ArrayList<Object>) resp.getData();

			ArrayList<ArrayList<Object>> dailyRows = (ArrayList<ArrayList<Object>>) result.get(0);
			int totalCancelled = (int) result.get(1);
			int totalNoShow = (int) result.get(2);
			double averageCancelledPerDay = (double) result.get(3);

			CategoryAxis x = new CategoryAxis();
			NumberAxis y = new NumberAxis();
			x.setLabel("Day");
			y.setLabel("Bookings");

			BarChart<String, Number> chart = new BarChart<>(x, y);

			String parkTitle = allParksCancelCheck.isSelected() ? "All Parks" : parkCombo.getValue();
			chart.setTitle("Daily Cancelled / No-show - " + parkTitle + " "
					+ monthCombo.getValue() + "/" + yearCombo.getValue());

			XYChart.Series<String, Number> cancelledSeries = new XYChart.Series<>();
			cancelledSeries.setName("Cancelled");

			XYChart.Series<String, Number> noShowSeries = new XYChart.Series<>();
			noShowSeries.setName("No-show");

			if (dailyRows == null || dailyRows.isEmpty()) {
				cancelledSeries.getData().add(new XYChart.Data<>("No data", 0));
				noShowSeries.getData().add(new XYChart.Data<>("No data", 0));
			} else {
				for (ArrayList<Object> row : dailyRows) {
					String day = String.valueOf(row.get(0));
					int cancelled = (int) row.get(1);
					int noShow = (int) row.get(2);

					cancelledSeries.getData().add(new XYChart.Data<>(day, cancelled));
					noShowSeries.getData().add(new XYChart.Data<>(day, noShow));
				}
			}

			chart.getData().setAll(cancelledSeries, noShowSeries);

			Label summary = new Label("Total cancelled: " + totalCancelled
					+ "    Total no-show: " + totalNoShow
					+ "    Average cancelled per day: " + String.format("%.2f", averageCancelledPerDay));
			summary.setStyle("-fx-font-weight: bold;");

			chartHolder.getChildren().setAll(chart, summary);

		} catch (Exception ex) {
			ex.printStackTrace();
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}
	@SuppressWarnings("unchecked")
	private void showParkChangeRequests() {
		try {
			Message resp = request(new Message("GET_PENDING_PARK_CHANGE_REQUESTS", null));

			if (!"PENDING_PARK_CHANGE_REQUESTS".equals(resp.getCommand())) {
				chartHolder.getChildren().setAll(new Label("Could not load park change requests."));
				return;
			}

			ArrayList<ArrayList<Object>> requests = (ArrayList<ArrayList<Object>>) resp.getData();

			VBox box = new VBox(10);
			box.setPadding(new Insets(10));

			Label title = new Label("Pending Park Change Requests");
			title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
			box.getChildren().add(title);

			if (requests == null || requests.isEmpty()) {
				box.getChildren().add(new Label("No pending requests."));
				chartHolder.getChildren().setAll(box);
				return;
			}

			for (ArrayList<Object> row : requests) {
				int requestId = (int) row.get(0);
				String parkName = String.valueOf(row.get(1));
				int requestedCapacity = (int) row.get(2);
				int requestedPercent = (int) row.get(3);
				int requestedDuration = (int) row.get(4);
				String requestedBy = String.valueOf(row.get(5));
				String requestTime = String.valueOf(row.get(7));

				Label info = new Label("Request #" + requestId
						+ " | Park: " + parkName
						+ " | Capacity: " + requestedCapacity
						+ " | Bookable: " + requestedPercent + "%"
						+ " | Duration: " + requestedDuration + "h"
						+ " | By: " + requestedBy
						+ " | Time: " + requestTime);

				Button approve = new Button("Approve");
				Button reject = new Button("Reject");

				approve.setOnAction(e -> {
					try {
						ArrayList<Object> data = new ArrayList<>();
						data.add(requestId);
						data.add(ClientSession.loggedInId);

						Message r = request(new Message("APPROVE_PARK_CHANGE_REQUEST", data));
						new Alert(Alert.AlertType.INFORMATION, String.valueOf(r.getData())).showAndWait();
						showParkChangeRequests();

					} catch (Exception ex) {
						new Alert(Alert.AlertType.ERROR, "Connection error.").showAndWait();
					}
				});

				reject.setOnAction(e -> {
					try {
						ArrayList<Object> data = new ArrayList<>();
						data.add(requestId);
						data.add(ClientSession.loggedInId);

						Message r = request(new Message("REJECT_PARK_CHANGE_REQUEST", data));
						new Alert(Alert.AlertType.INFORMATION, String.valueOf(r.getData())).showAndWait();
						showParkChangeRequests();

					} catch (Exception ex) {
						new Alert(Alert.AlertType.ERROR, "Connection error.").showAndWait();
					}
				});

				HBox rowBox = new HBox(10, info, approve, reject);
				rowBox.setStyle("-fx-border-color: #cccccc; -fx-padding: 8;");
				box.getChildren().add(rowBox);
			}

			chartHolder.getChildren().setAll(box);

		} catch (Exception ex) {
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}
	@SuppressWarnings("unchecked")
	private void showDiscountRequests() {
		try {
			Message resp = request(new Message("GET_PENDING_DISCOUNT_REQUESTS", null));

			if (!"PENDING_DISCOUNT_REQUESTS".equals(resp.getCommand())) {
				chartHolder.getChildren().setAll(new Label("Could not load discount requests."));
				return;
			}

			ArrayList<ArrayList<Object>> requests = (ArrayList<ArrayList<Object>>) resp.getData();

			VBox box = new VBox(10);
			box.setPadding(new Insets(10));

			Label title = new Label("Pending Discount Requests");
			title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
			box.getChildren().add(title);

			if (requests == null || requests.isEmpty()) {
				box.getChildren().add(new Label("No pending discount requests."));
				chartHolder.getChildren().setAll(box);
				return;
			}

			for (ArrayList<Object> row : requests) {
				int requestId = (int) row.get(0);
				String parkName = String.valueOf(row.get(1));
				String discountName = String.valueOf(row.get(2));
				int discountPercent = (int) row.get(3);
				String requestedBy = String.valueOf(row.get(4));
				String requestTime = String.valueOf(row.get(5));

				Label info = new Label("Request #" + requestId
						+ " | Park: " + parkName
						+ " | Discount: " + discountName
						+ " | Percent: " + discountPercent + "%"
						+ " | By: " + requestedBy
						+ " | Time: " + requestTime);

				Button approve = new Button("Approve");
				Button reject = new Button("Reject");

				approve.setOnAction(e -> {
					try {
						ArrayList<Object> data = new ArrayList<>();
						data.add(requestId);
						data.add(ClientSession.loggedInId);

						Message r = request(new Message("APPROVE_DISCOUNT_REQUEST", data));
						new Alert(Alert.AlertType.INFORMATION, String.valueOf(r.getData())).showAndWait();
						showDiscountRequests();

					} catch (Exception ex) {
						new Alert(Alert.AlertType.ERROR, "Connection error.").showAndWait();
					}
				});

				reject.setOnAction(e -> {
					try {
						ArrayList<Object> data = new ArrayList<>();
						data.add(requestId);
						data.add(ClientSession.loggedInId);

						Message r = request(new Message("REJECT_DISCOUNT_REQUEST", data));
						new Alert(Alert.AlertType.INFORMATION, String.valueOf(r.getData())).showAndWait();
						showDiscountRequests();

					} catch (Exception ex) {
						new Alert(Alert.AlertType.ERROR, "Connection error.").showAndWait();
					}
				});

				HBox rowBox = new HBox(10, info, approve, reject);
				rowBox.setStyle("-fx-border-color: #cccccc; -fx-padding: 8;");
				box.getChildren().add(rowBox);
			}

			chartHolder.getChildren().setAll(box);

		} catch (Exception ex) {
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}
}