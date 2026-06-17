package client.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
	private final TextField newCapacityField = new TextField();
	private final Label statusLabel = new Label();

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
		stage.setTitle("GoNature - Park Manager");

		parkCombo.getItems().addAll("Carmel Park", "Jordan Park", "Banias Park", "Safari Zoo", "Ramon Crater",
				"Hula Valley");
		parkCombo.setValue("Carmel Park");
		parkCombo.setOnAction(e -> loadCapacity());

		newCapacityField.setPromptText("New max capacity");

		Button loadBtn = new Button("Load");
		loadBtn.setOnAction(e -> loadCapacity());

		Button updateBtn = new Button("Update Capacity");
		updateBtn.setOnAction(e -> {
			int newCap;
			try {
				newCap = Integer.parseInt(newCapacityField.getText().trim());
			} catch (NumberFormatException ex) {
				statusLabel.setText("Capacity must be a number.");
				return;
			}
			if (newCap <= 0) {
				statusLabel.setText("Capacity must be positive.");
				return;
			}
			try {
				ArrayList<Object> data = new ArrayList<>();
				data.add(parkCombo.getValue());
				data.add(newCap);
				Message resp = request(new Message("UPDATE_PARK_CAPACITY", data));
				if ("SUCCESS".equals(resp.getCommand())) {
					statusLabel.setStyle("-fx-text-fill: green;");
					statusLabel.setText("Capacity updated (pending department-manager approval in the full story).");
					loadCapacity();
				} else {
					statusLabel.setStyle("-fx-text-fill: red;");
					statusLabel.setText("Update failed.");
				}
			} catch (Exception ex) {
				statusLabel.setText("Connection error.");
			}
		});

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(12);
		grid.addRow(0, new Label("Park:"), parkCombo, loadBtn);
		grid.addRow(1, currentCapacityLabel);
		grid.addRow(2, new Label("New capacity:"), newCapacityField, updateBtn);

		VBox layout = new VBox(15, grid, statusLabel);
		layout.setPadding(new Insets(20));
		stage.setScene(new Scene(layout, 480, 220));
		stage.show();

		loadCapacity();
	}

	private void loadCapacity() {
		try {
			Message resp = request(new Message("GET_PARK_PARAMS", parkCombo.getValue()));
			if ("PARK_PARAMS".equals(resp.getCommand())) {
				currentCapacityLabel.setText("Current capacity: " + resp.getData());
			}
		} catch (Exception ex) {
			currentCapacityLabel.setText("Current capacity: (connection error)");
		}
	}
}
