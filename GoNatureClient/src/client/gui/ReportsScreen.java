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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import common.Message;
import client.network.ClientSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 * Dashboard screen for department managers in the GoNature system.
 *
 * This screen allows department managers to generate reports,
 * monitor park capacity, review park change requests and
 * approve or reject discount requests.
 *
 * @author Group 4
 * @version 1.0
 */
public class ReportsScreen extends Application {
	/**
	 * Park selector used for report generation.
	 */
	private final ComboBox<String> parkCombo = new ComboBox<>();
	/**
	 * Month selector used for reports.
	 */
	private final ComboBox<Integer> monthCombo = new ComboBox<>();
	/**
	 * Year selector used for reports.
	 */
	private final ComboBox<Integer> yearCombo = new ComboBox<>();
	/**
	 * Container used to display charts and report results.
	 */
	private final VBox chartHolder = new VBox(10);
	/**
	 * Option for generating cancellation reports for all parks.
	 */
	private final CheckBox allParksCancelCheck = new CheckBox("All parks for cancellations");
	/**
	 * Displays current live park capacity information.
	 */
	private final Label liveCapacityLabel = new Label("Live capacity: -");
	/**
	 * Sends a request to the server.
	 *
	 * @param m request message
	 * @return server response message
	 * @throws Exception if communication fails
	 */
	private static Message request(Message m) throws Exception {
		return ClientSession.send(m);
	}
	/**
	 * Creates and displays the reports dashboard.
	 *
	 * @param stage primary JavaFX stage
	 */
	@Override
	public void start(Stage stage) {
		stage.setTitle("GoNature - Department Manager Reports");

		Label titleLabel = new Label("📊 Department Manager Reports 🌿");
		titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
		titleLabel.setStyle("-fx-text-fill: #1b5e20;");

		parkCombo.getItems().addAll(common.Parks.NAMES);
		parkCombo.setValue("Carmel Park");
		parkCombo.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px;");
		parkCombo.setOnAction(e -> updateLiveCapacity());

		for (int mth = 1; mth <= 12; mth++) {
			monthCombo.getItems().add(mth);
		}
		monthCombo.setValue(LocalDate.now().getMonthValue());
		monthCombo.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px;");

		int thisYear = LocalDate.now().getYear();
		for (int y = thisYear - 2; y <= thisYear; y++) {
			yearCombo.getItems().add(y);
		}
		yearCombo.setValue(thisYear);
		yearCombo.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px;");

		Button visitsBtn = new Button("Visits Report");
		visitsBtn.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		visitsBtn.setOnAction(e -> showVisitsReport());

		Button detailedVisitsBtn = new Button("Detailed Visits");
		detailedVisitsBtn.setStyle("-fx-background-color: #5d4037; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		detailedVisitsBtn.setOnAction(e -> showDetailedVisitsReport());

		Button cancelBtn = new Button("Cancellations Report");
		cancelBtn.setStyle("-fx-background-color: #f57c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		cancelBtn.setOnAction(e -> showCancellationsReport());
		
		Button usageBtn = new Button("Park Usage Report");
		usageBtn.setStyle("-fx-background-color: #00897b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		usageBtn.setOnAction(e -> showParkNotFullReport());
		
		Button requestsBtn = new Button("Park Change Requests");
		requestsBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		requestsBtn.setOnAction(e -> showParkChangeRequests());
		
		Button discountRequestsBtn = new Button("Discount Requests");
		discountRequestsBtn.setStyle("-fx-background-color: #8e24aa; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		discountRequestsBtn.setOnAction(e -> showDiscountRequests());

		Button logoutBtn = new Button("Logout");
		logoutBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		logoutBtn.setOnAction(e -> LogoutHelper.logout(stage));

		HBox combos = new HBox(10, new Label("Park:"), parkCombo, new Label("Month:"), monthCombo, new Label("Year:"), yearCombo, allParksCancelCheck);
		combos.setPadding(new Insets(5, 0, 5, 0));
		
		HBox controls = new HBox(10, visitsBtn, usageBtn, detailedVisitsBtn, cancelBtn, requestsBtn, discountRequestsBtn, logoutBtn);
		controls.setPadding(new Insets(5, 0, 5, 0));

		HBox liveCapacityRow = new HBox(10, liveCapacityLabel);
		liveCapacityRow.setStyle("-fx-padding: 10; -fx-border-color: #81c784; -fx-background-color: #c8e6c9; -fx-border-radius: 5px; -fx-background-radius: 5px;");

		VBox layout = new VBox(15, titleLabel, combos, controls, liveCapacityRow, chartHolder);
		layout.setPadding(new Insets(20));
		layout.setStyle("-fx-background-color: #e8f5e9;");
		stage.setScene(new Scene(layout, 1150, 750)); // הוגדל משמעותית כדי שהגרפים ייראו קטלניים!
		stage.show();
		
		updateLiveCapacity();

		Timeline refreshTimer = new Timeline(new KeyFrame(Duration.seconds(10), e -> updateLiveCapacity()));
		refreshTimer.setCycleCount(Timeline.INDEFINITE);
		refreshTimer.play();

		stage.setOnCloseRequest(e -> refreshTimer.stop());
	}
	@SuppressWarnings("unchecked")
	/**
	 * Retrieves and displays current live park capacity information.
	 */
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

				liveCapacityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b5e20; -fx-font-size: 14px;");
				liveCapacityLabel.setText("Live capacity for " + parkName
						+ ": " + activeVisitors + "/" + maxCapacity
						+ " visitors inside | Free places: " + freePlaces);
			} else {
				liveCapacityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #c62828;");
				liveCapacityLabel.setText("Live capacity: could not load data.");
			}

		} catch (Exception ex) {
			liveCapacityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #c62828;");
			liveCapacityLabel.setText("Live capacity: connection error.");
		}
	}
	/**
	 * Creates a standard report filter.
	 *
	 * @return report filter data
	 */
	private ArrayList<Object> filter() {
		ArrayList<Object> data = new ArrayList<>();
		data.add(parkCombo.getValue());
		data.add(yearCombo.getValue());
		data.add(monthCombo.getValue());
		return data;
	}
	/**
	 * Creates a cancellation report filter.
	 *
	 * @return cancellation report filter data
	 */
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
	/**
	 * Generates and displays the monthly visitors report.
	 */
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
			y.setLabel("Total Visitors");

			BarChart<String, Number> chart = new BarChart<>(x, y);
			chart.setTitle("Total Visits by Type - " + parkCombo.getValue() + " " + monthCombo.getValue() + "/" + yearCombo.getValue());
			chart.setPrefSize(1000, 500); // גרף ענק
			chart.setStyle("-fx-font-size: 14px;");

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
	/**
	 * Generates and displays the detailed visits report.
	 */
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
			area.setPrefWidth(1000);
			area.setPrefHeight(500);
			area.setStyle("-fx-border-color: #81c784; -fx-border-radius: 5px; -fx-font-family: Consolas;");

			chartHolder.getChildren().setAll(area);

		} catch (Exception ex) {
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}
	
	@SuppressWarnings("unchecked")
	private void showParkNotFullReport() {
		try {
			Message resp = request(new Message("REPORT_NOT_FULL", filter()));
			if (!"REPORT_NOT_FULL_RESULT".equals(resp.getCommand())) {
				chartHolder.getChildren().setAll(new Label("Could not load Park Usage report."));
				return;
			}
			ArrayList<ArrayList<Object>> rows = (ArrayList<ArrayList<Object>>) resp.getData();

			CategoryAxis x = new CategoryAxis();
			NumberAxis y = new NumberAxis();
			x.setLabel("Day of the Month");
			y.setLabel("Value (Hours / Places)");

			BarChart<String, Number> chart = new BarChart<>(x, y);
			chart.setTitle("Park Usage: Free Places at Peak & Not-Full Hours - " + parkCombo.getValue());
			chart.setPrefSize(1000, 500); // גרף ענק וקטלני
			chart.setStyle("-fx-font-size: 14px;");

			XYChart.Series<String, Number> freePlacesSeries = new XYChart.Series<>();
			freePlacesSeries.setName("Free Places During Peak Time");

			XYChart.Series<String, Number> notFullHoursSeries = new XYChart.Series<>();
			notFullHoursSeries.setName("Total Not-Full Hours");

			if (rows != null && !rows.isEmpty()) {
				for (ArrayList<Object> row : rows) {
					String fullDate = String.valueOf(row.get(0));
					String dayOnly = fullDate.substring(fullDate.lastIndexOf('-') + 1); // Extracting just the day
					int freePlaces = (int) row.get(3);
					int notFullHours = (int) row.get(4);
					
					freePlacesSeries.getData().add(new XYChart.Data<>(dayOnly, freePlaces));
					notFullHoursSeries.getData().add(new XYChart.Data<>(dayOnly, notFullHours));
				}
			} else {
				freePlacesSeries.getData().add(new XYChart.Data<>("No Data", 0));
			}

			chart.getData().addAll(freePlacesSeries, notFullHoursSeries);
			chartHolder.getChildren().setAll(chart);

		} catch (Exception ex) {
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}

	@SuppressWarnings("unchecked")
	/**
	 * Generates and displays the cancellations and no-show report.
	 */
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
			x.setLabel("Day of Month");
			y.setLabel("Number of Bookings");

			BarChart<String, Number> chart = new BarChart<>(x, y);

			String parkTitle = allParksCancelCheck.isSelected() ? "All Parks" : parkCombo.getValue();
			chart.setTitle("Daily Cancelled & No-show Bookings - " + parkTitle + " "
					+ monthCombo.getValue() + "/" + yearCombo.getValue());
			chart.setPrefSize(1000, 480); // גרף ענק
			chart.setStyle("-fx-font-size: 14px;");

			XYChart.Series<String, Number> cancelledSeries = new XYChart.Series<>();
			cancelledSeries.setName("Cancelled Bookings");

			XYChart.Series<String, Number> noShowSeries = new XYChart.Series<>();
			noShowSeries.setName("No-Show (Did Not Arrive)");

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

			Label summary = new Label("Summary -> Total Cancelled: " + totalCancelled
					+ "  |  Total No-Show: " + totalNoShow
					+ "  |  Average Cancelled Per Day: " + String.format("%.2f", averageCancelledPerDay));
			summary.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b5e20; -fx-font-size: 16px;");

			chartHolder.getChildren().setAll(chart, summary);

		} catch (Exception ex) {
			ex.printStackTrace();
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}
	@SuppressWarnings("unchecked")
	/**
	 * Displays pending park change requests and allows approval or rejection.
	 */
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
			box.setStyle("-fx-background-color: #f1f8e9; -fx-border-color: #81c784; -fx-border-radius: 5px;");

			Label title = new Label("Pending Park Change Requests");
			title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1b5e20;");
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
				info.setStyle("-fx-font-weight: bold;");

				Button approve = new Button("Approve");
				approve.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
				Button reject = new Button("Reject");
				reject.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");

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
				rowBox.setStyle("-fx-border-color: #c8e6c9; -fx-padding: 8; -fx-background-color: white;");
				box.getChildren().add(rowBox);
			}

			ScrollPane scroll = new ScrollPane(box);
			scroll.setFitToWidth(true);
			scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
			chartHolder.getChildren().setAll(scroll);

		} catch (Exception ex) {
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}
	@SuppressWarnings("unchecked")
	/**
	 * Displays pending discount requests and allows approval or rejection.
	 */
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
			box.setStyle("-fx-background-color: #f1f8e9; -fx-border-color: #81c784; -fx-border-radius: 5px;");

			Label title = new Label("Pending Discount Requests");
			title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1b5e20;");
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
				info.setStyle("-fx-font-weight: bold;");

				Button approve = new Button("Approve");
				approve.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
				Button reject = new Button("Reject");
				reject.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");

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
				rowBox.setStyle("-fx-border-color: #c8e6c9; -fx-padding: 8; -fx-background-color: white;");
				box.getChildren().add(rowBox);
			}

			ScrollPane scroll = new ScrollPane(box);
			scroll.setFitToWidth(true);
			scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
			chartHolder.getChildren().setAll(scroll);

		} catch (Exception ex) {
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}
}