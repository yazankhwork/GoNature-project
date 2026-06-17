package server.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import server.network.GoNatureServer;

/**
 * Server control window. Lets the operator type the database connection details
 * at runtime (so the same project runs on any laptop) and start/stop the server.
 */
public class ServerGUI extends Application {

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("GoNature - Server Control");

		TextField dbHostField = new TextField("localhost");
		TextField dbUserField = new TextField("root");
		PasswordField dbPassField = new PasswordField();
		dbPassField.setPromptText("Your MySQL password");

		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(8);
		form.addRow(0, new Label("DB Host:"), dbHostField);
		form.addRow(1, new Label("DB User:"), dbUserField);
		form.addRow(2, new Label("DB Password:"), dbPassField);

		Label statusLabel = new Label("Status: SERVER IS OFF");
		statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");

		Button startBtn = new Button("Start Server");
		Button stopBtn = new Button("Stop Server");
		stopBtn.setDisable(true);

		startBtn.setOnAction(e -> {
			String host = dbHostField.getText().trim();
			String user = dbUserField.getText().trim();
			String pass = dbPassField.getText();
			new Thread(() -> GoNatureServer.startServer(host, user, pass)).start();

			statusLabel.setText("Status: SERVER IS RUNNING");
			statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 14px;");
			startBtn.setDisable(true);
			stopBtn.setDisable(false);
		});

		stopBtn.setOnAction(e -> {
			GoNatureServer.stopServer();
			statusLabel.setText("Status: SERVER IS OFF");
			statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
			startBtn.setDisable(false);
			stopBtn.setDisable(true);
		});

		HBox buttons = new HBox(10, startBtn, stopBtn);
		VBox layout = new VBox(15, new Label("Database connection:"), form, buttons, statusLabel);
		layout.setPadding(new Insets(20));

		primaryStage.setScene(new Scene(layout, 360, 250));
		primaryStage.show();
	}

	/**
	 * Launches the server window.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}