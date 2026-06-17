package client.gui;

import javafx.application.Application;
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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

	private static Message request(Message m) throws Exception {
		try (Socket s = new Socket(ClientConnectionScreen.serverIP, 5555);
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {
			out.writeObject(m);
			out.flush();
			return (Message) in.readObject();
		}
	}

	@Override
	public void start(Stage stage) {
		stage.setTitle("GoNature - Department Manager Reports");

		parkCombo.getItems().addAll("Carmel Park", "Jordan Park", "Banias Park", "Safari Zoo", "Ramon Crater",
				"Hula Valley");
		parkCombo.setValue("Carmel Park");

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

		Button cancelBtn = new Button("Cancellations Report");
		cancelBtn.setOnAction(e -> showCancellationsReport());

		HBox controls = new HBox(10, new Label("Park:"), parkCombo, new Label("Month:"), monthCombo, new Label("Year:"),
				yearCombo, visitsBtn, cancelBtn);

		VBox layout = new VBox(15, controls, chartHolder);
		layout.setPadding(new Insets(20));
		stage.setScene(new Scene(layout, 720, 520));
		stage.show();
	}

	private ArrayList<Object> filter() {
		ArrayList<Object> data = new ArrayList<>();
		data.add(parkCombo.getValue());
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
			chart.setTitle("Visits - " + parkCombo.getValue() + " " + monthCombo.getValue() + "/" + yearCombo.getValue());

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
	private void showCancellationsReport() {
		try {
			Message resp = request(new Message("REPORT_CANCELLATIONS", filter()));
			if (!"REPORT_CANCELLATIONS_RESULT".equals(resp.getCommand())) {
				return;
			}
			ArrayList<Integer> nums = (ArrayList<Integer>) resp.getData();
			int cancelled = nums.size() > 0 ? nums.get(0) : 0;
			int noShow = nums.size() > 1 ? nums.get(1) : 0;

			CategoryAxis x = new CategoryAxis();
			NumberAxis y = new NumberAxis();
			x.setLabel("Outcome");
			y.setLabel("Count");

			BarChart<String, Number> chart = new BarChart<>(x, y);
			chart.setTitle("Cancellations - " + parkCombo.getValue() + " " + monthCombo.getValue() + "/"
					+ yearCombo.getValue());

			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName("Bookings");
			series.getData().add(new XYChart.Data<>("Cancelled", cancelled));
			series.getData().add(new XYChart.Data<>("No-show (not cancelled)", noShow));
			chart.getData().add(series);

			Label summary = new Label("Cancelled: " + cancelled + "    No-shows: " + noShow);
			summary.setStyle("-fx-font-weight: bold;");
			chartHolder.getChildren().setAll(chart, summary);
		} catch (Exception ex) {
			chartHolder.getChildren().setAll(new Label("Connection error."));
		}
	}
}
