package client.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Main JavaFX entry screen for the GoNature client application.
 * <p>
 * This screen allows users to:
 * <ul>
 * <li>Connect to the GoNature server.</li>
 * <li>Log in using an existing account.</li>
 * <li>Register as a new visitor.</li>
 * <li>Register as an organized group guide.</li>
 * </ul>
 * After successful authentication, the dashboard screen is opened.
 *
 * @author Bolos Saad
 */
public class ClientConnectionScreen extends Application {

	public static String serverIP = "localhost";

	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle("GoNature - Server Connect");

		// --- SERVER CONNECT SCENE (Nature Theme) ---
		VBox ipLayout = new VBox(15);
		ipLayout.setPadding(new Insets(40));
		ipLayout.setAlignment(Pos.CENTER);
		ipLayout.setStyle("-fx-background-color: #f4fcf4;");

		Label titleLabel = new Label("GoNature Server Connect");
		titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
		titleLabel.setStyle("-fx-text-fill: #27ae60;");

		TextField ipInput = new TextField("localhost");
		ipInput.setStyle("-fx-border-color: #2ecc71; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		Button connectBtn = new Button("Connect to Server");
		connectBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-font-size: 14px;");

		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

		ipLayout.getChildren().addAll(titleLabel, new Label("Enter Server IP:"), ipInput, connectBtn, errorLabel);
		Scene ipScene = new Scene(ipLayout, 350, 250);

		// --- AUTHENTICATION SCENE (Nature Theme) ---
		VBox authLayout = new VBox(15);
		authLayout.setPadding(new Insets(30));
		authLayout.setAlignment(Pos.CENTER);
		authLayout.setStyle("-fx-background-color: #f4fcf4;");

		Label welcomeLabel = new Label("Welcome to GoNature!");
		welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
		welcomeLabel.setStyle("-fx-text-fill: #27ae60;");

		Label subLabel = new Label("Please Log In or Register");
		subLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

		TextField idInput = new TextField();
		idInput.setPromptText("Visitor ID (9 digits)");
		idInput.setStyle("-fx-border-color: #2ecc71; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		PasswordField passInput = new PasswordField();
		passInput.setPromptText("Password");
		passInput.setStyle("-fx-border-color: #2ecc71; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		TextField nameInput = new TextField();
		nameInput.setPromptText("Full Name"); 
		nameInput.setStyle("-fx-border-color: #2ecc71; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		CheckBox guideCheck = new CheckBox("Register as an Organized Group Guide");
		guideCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

		Button loginBtn = new Button("Log In");
		loginBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 100px;");

		Button regBtn = new Button("New Visitor (Register)");
		regBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 180px;");

		HBox buttonsBox = new HBox(15, loginBtn, regBtn);
		buttonsBox.setAlignment(Pos.CENTER);

		Label statusLabel = new Label();
		statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

		authLayout.getChildren().addAll(welcomeLabel, subLabel, idInput, passInput, nameInput, guideCheck, buttonsBox, statusLabel);
		Scene authScene = new Scene(authLayout, 400, 420);

		// --- BUTTON ACTIONS ---

		connectBtn.setOnAction(e -> {
			String ip = ipInput.getText().trim();
			try (Socket socket = new Socket(ip, 5555);
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

				out.writeObject(new Message("CONNECT", null));

				if ("CONNECTED".equals(((Message) in.readObject()).getCommand())) {
					serverIP = ip;
					primaryStage.setTitle("GoNature - Authentication");
					primaryStage.setScene(authScene);
				}
			} catch (Exception ex) {
				errorLabel.setText("Connection Failed! Is server on?");
			}
		});

		// 1. LOG IN BUTTON LOGIC
		loginBtn.setOnAction(e -> {
			String id = idInput.getText().trim();
			String pass = passInput.getText().trim();
			String typedName = nameInput.getText().trim(); 

			if (id.isEmpty() || pass.isEmpty() || typedName.isEmpty()) {
				statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
				statusLabel.setText("ID, Password, and Full Name required to log in!");
				return;
			}

			if (!id.matches("\\d{9}")) {
				statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
				statusLabel.setText("ID is invalid! Must be exactly 9 digits.");
				return;
			}

			try (Socket socket = new Socket(serverIP, 5555);
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

				ArrayList<String> loginData = new ArrayList<>();
				loginData.add(id);
				loginData.add(pass);

				out.writeObject(new Message("LOGIN", loginData));

				Message resMsg = (Message) in.readObject();
				String res = resMsg.getCommand();

				if ("LOGIN_SUCCESS_GUIDE".equals(res) || "LOGIN_SUCCESS_REGULAR".equals(res)) {
					
					String dbFullName = (String) resMsg.getData();

					if (dbFullName == null || !dbFullName.equals(typedName)) {
						statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
						statusLabel.setText("Error: Full Name does not match our records!");
						return; 
					}

					ClientDashboard.loggedInVisitorId = id;
					ClientDashboard.loggedInName = dbFullName;
					ClientDashboard.isAccountGuide = "LOGIN_SUCCESS_GUIDE".equals(res);

					primaryStage.close();
					new ClientDashboard().start(new Stage());
				} else if ("WRONG_PASSWORD".equals(res)) {
					statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
					statusLabel.setText("Error: Wrong Password!");
				} else {
					statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
					statusLabel.setText("User not found! Click 'New Visitor'.");
				}
			} catch (Exception ex) {
				statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
				statusLabel.setText("Server connection lost.");
			}
		});

		// 2. REGISTER BUTTON LOGIC (Flexible Name Validation)
		regBtn.setOnAction(e -> {
			String id = idInput.getText().trim();
			String pass = passInput.getText().trim();
			String fullName = nameInput.getText().trim();
			boolean isGuide = guideCheck.isSelected();

			if (id.isEmpty() || pass.isEmpty()) {
				statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
				statusLabel.setText("ID and Password required for registration!");
				return;
			}

			if (!id.matches("\\d{9}")) {
				statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
				statusLabel.setText("ID is invalid! Must be exactly 9 digits.");
				return;
			}

			// --- FLEXIBLE NAME VALIDATION ---
			// Allows any English letters (with or without spaces).
			if (!fullName.matches("^[a-zA-Z\\s]+$")) {
				statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
				statusLabel.setText("Full Name must contain only English letters!");
				return;
			}

			try (Socket socket = new Socket(serverIP, 5555);
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

				ArrayList<Object> regData = new ArrayList<>();
				regData.add(id);
				regData.add(pass);
				regData.add(isGuide);
				regData.add(fullName);

				out.writeObject(new Message("REGISTER", regData));

				String res = ((Message) in.readObject()).getCommand();

				if ("REGISTER_SUCCESS".equals(res)) {
					statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); 
					statusLabel.setText("Registration successful! Now click 'Log In'.");
				} else if ("USER_ALREADY_EXISTS".equals(res)) {
					statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;"); 
					statusLabel.setText("ID already registered! Please log in.");
				}
			} catch (Exception ex) {
				statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");
				statusLabel.setText("Server connection lost.");
			}
		});

		primaryStage.setScene(ipScene);
		primaryStage.show();
	}
}