package server.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import server.network.GoNatureServer;

import java.io.OutputStream;
import java.io.PrintStream;
/**
 * Graphical user interface for managing the GoNature server.
 *
 * This class allows the operator to start and stop the server,
 * enter database connection details, choose the server port
 * and view live server console output inside the application window.
 *
 * @author Group 4
 * @version 1.0
 */
public class ServerGUI extends Application {
	/**
	 * Text area used to display live server console output.
	 */
	private TextArea consoleArea;
	/**
	 * Button used to start the server.
	 */
	private Button startBtn;
	/**
	 * Button used to stop the server.
	 */
	private Button stopBtn;
	/**
	 * Initializes and displays the server management window.
	 *
	 * @param primaryStage main JavaFX application stage
	 */
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("GoNature - Server Manager");

		VBox layout = new VBox(15);
		layout.setPadding(new Insets(20));
		layout.setStyle("-fx-background-color: #e8f5e9;"); 

		Label title = new Label("🌲 GoNature Server 🌿");
		title.setFont(Font.font("System", FontWeight.BOLD, 26));
		title.setStyle("-fx-text-fill: #2e7d32;"); 

		GridPane dbGrid = new GridPane();
		dbGrid.setHgap(10);
		dbGrid.setVgap(10);
		dbGrid.setAlignment(Pos.CENTER);

		TextField portInput = new TextField("5555");
		portInput.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px;");
		TextField dbUserInput = new TextField("root");
		dbUserInput.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px;");
		PasswordField dbPassInput = new PasswordField();
		dbPassInput.setPromptText("DB Password");
		dbPassInput.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		dbGrid.add(new Label("Port:"), 0, 0);
		dbGrid.add(portInput, 1, 0);
		dbGrid.add(new Label("DB User:"), 0, 1);
		dbGrid.add(dbUserInput, 1, 1);
		dbGrid.add(new Label("DB Pass:"), 0, 2);
		dbGrid.add(dbPassInput, 1, 2);

		HBox buttons = new HBox(15);
		buttons.setAlignment(Pos.CENTER);
		startBtn = new Button("▶ Start Server");
		startBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
		stopBtn = new Button("⏹ Stop Server");
		stopBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
		stopBtn.setDisable(true);
		buttons.getChildren().addAll(startBtn, stopBtn);

		consoleArea = new TextArea();
		consoleArea.setEditable(false);
		consoleArea.setStyle("-fx-control-inner-background: #1b1b1b; -fx-text-fill: #4caf50; -fx-font-family: Consolas; -fx-font-size: 13px;");
		VBox.setVgrow(consoleArea, Priority.ALWAYS);

		redirectSystemStreams();

		startBtn.setOnAction(e -> {
			int port = Integer.parseInt(portInput.getText().trim());
			String user = dbUserInput.getText().trim();
			String pass = dbPassInput.getText().trim();

			if (GoNatureServer.startServer("localhost", user, pass, port)) {
				startBtn.setDisable(true);
				stopBtn.setDisable(false);
			} else {
				System.out.println("Error: " + GoNatureServer.getLastError());
			}
		});

		stopBtn.setOnAction(e -> {
			if (GoNatureServer.stopServer()) {
				startBtn.setDisable(false);
				stopBtn.setDisable(true);
			}
		});

		layout.getChildren().addAll(title, dbGrid, buttons, new Label("Server Live Console:"), consoleArea);
		primaryStage.setScene(new Scene(layout, 650, 500));
		primaryStage.show();

		primaryStage.setOnCloseRequest(e -> {
			GoNatureServer.stopServer();
			System.exit(0);
		});
	}
	/**
	 * Redirects standard output and error streams to the GUI console area.
	 */
	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			/**
			 * Writes a single byte to the GUI console.
			 *
			 * @param b byte to write
			 */
			@Override
			public void write(int b) {
				Platform.runLater(() -> consoleArea.appendText(String.valueOf((char) b)));
			}
			/**
			 * Writes a byte array segment to the GUI console.
			 *
			 * @param b byte array
			 * @param off starting offset
			 * @param len number of bytes to write
			 */
			@Override
			public void write(byte[] b, int off, int len) {
				Platform.runLater(() -> consoleArea.appendText(new String(b, off, len)));
			}
		};
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
<<<<<<< HEAD
	/**
	 * Application entry point.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
=======


>>>>>>> branch 'main' of https://github.com/yazankhwork/GoNature-project.git
}