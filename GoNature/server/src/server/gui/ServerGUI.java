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
 * Server control window. The operator types DB host/user/password and the port
 * the server should listen on; Start connects the DB and calls listen(). The
 * status label shows the real result (running, DB error, or port in use).
 */
public class ServerGUI extends Application {

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("GoNature - Server Control");

		TextField dbHostField = new TextField("localhost");
		TextField dbUserField = new TextField("root");
		PasswordField dbPassField = new PasswordField();
		dbPassField.setPromptText("Your MySQL password");
		TextField portField = new TextField("5555");

		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(8);
		form.addRow(0, new Label("DB Host:"), dbHostField);
		form.addRow(1, new Label("DB User:"), dbUserField);
		form.addRow(2, new Label("DB Password:"), dbPassField);
		form.addRow(3, new Label("Server Port:"), portField);

		Label statusLabel = new Label("Status: SERVER IS OFF");
		statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");

		Button startBtn = new Button("Start Server");
		Button stopBtn = new Button("Stop Server");
		stopBtn.setDisable(true);

		startBtn.setOnAction(e -> {
			int port;
			try {
				port = Integer.parseInt(portField.getText().trim());
			} catch (NumberFormatException ex) {
				statusLabel.setText("Status: INVALID PORT");
				return;
			}

			boolean ok = GoNatureServer.startServer(dbHostField.getText().trim(), dbUserField.getText().trim(),
					dbPassField.getText(), port);

			if (ok) {
				statusLabel.setText("Status: SERVER IS RUNNING (port " + port + ")");
				statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 14px;");
				startBtn.setDisable(true);
				stopBtn.setDisable(false);
			} else {
				statusLabel.setText("Status: " + GoNatureServer.getLastError());
				statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
			}
		});

		stopBtn.setOnAction(e -> {
			GoNatureServer.stopServer();
			statusLabel.setText("Status: SERVER IS OFF");
			statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
			startBtn.setDisable(false);
			stopBtn.setDisable(true);
		});

		HBox buttons = new HBox(10, startBtn, stopBtn);
		VBox layout = new VBox(15, new Label("Server settings:"), form, buttons, statusLabel);
		layout.setPadding(new Insets(20));

		primaryStage.setScene(new Scene(layout, 380, 300));
		primaryStage.setOnCloseRequest(ev -> GoNatureServer.stopServer());
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}