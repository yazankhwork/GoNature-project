package client.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import common.Message;
import client.network.ClientSession;

import java.util.ArrayList;
import java.util.Optional;

public class ClientConnectionScreen extends Application {

	public static String serverIP = "localhost";

	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle("GoNature - Access Portal");

		VBox ipLayout = new VBox(15);
		ipLayout.setPadding(new Insets(40));
		ipLayout.setAlignment(Pos.CENTER);
		ipLayout.setStyle("-fx-background-color: #e8f5e9;");

		Label titleLabel = new Label("🌲 GoNature Network 🍃");
		titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
		titleLabel.setStyle("-fx-text-fill: #2e7d32;");

		TextField ipInput = new TextField("localhost");
		ipInput.setStyle("-fx-border-color: #66bb6a; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		TextField portInput = new TextField("5555");
		portInput.setStyle("-fx-border-color: #66bb6a; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		Button connectBtn = new Button("Connect to Nature");
		connectBtn.setStyle(
				"-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-font-size: 14px;");

		Label errorLabel = new Label();
		errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");

		ipLayout.getChildren().addAll(titleLabel, new Label("Enter Server IP:"), ipInput, new Label("Port:"), portInput,
				connectBtn, errorLabel);
		Scene ipScene = new Scene(ipLayout, 350, 250);

		VBox authLayout = new VBox(15);
		authLayout.setPadding(new Insets(30));
		authLayout.setAlignment(Pos.CENTER);
		authLayout.setStyle("-fx-background-color: #e8f5e9;");

		Label welcomeLabel = new Label("🌿 Welcome to GoNature 🌿");
		welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 26));
		welcomeLabel.setStyle("-fx-text-fill: #1b5e20;");

		Label subLabel = new Label("Explore the outdoors with us");
		subLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-style: italic;");

		TextField usernameInput = new TextField();
		usernameInput.setPromptText("Username (Visitor) / ID Number (Employee)");
		usernameInput.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-pref-height: 35px;");
		
		PasswordField passwordInput = new PasswordField();
		passwordInput.setPromptText("Password");
		passwordInput.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-pref-height: 35px;");

		Button guestBtn = new Button("Login as Guest");
		guestBtn.setStyle(
				"-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 140px; -fx-pref-height: 35px;");
				
		Button loginBtn = new Button("Visitor Login");
		loginBtn.setStyle(
				"-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 140px; -fx-pref-height: 35px;");
				
		Button regBtn = new Button("New Visitor (Register)");
		regBtn.setStyle(
				"-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 180px; -fx-pref-height: 35px;");

		Button workerBtn = new Button("Service Rep / Admin");
		workerBtn.setStyle(
				"-fx-background-color: #5d4037; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 180px; -fx-pref-height: 35px;");

		HBox buttonsBox = new HBox(15, guestBtn, loginBtn, regBtn);
		buttonsBox.setAlignment(Pos.CENTER);
		HBox workerBox = new HBox(workerBtn);
		workerBox.setAlignment(Pos.CENTER);
		
		Label statusLabel = new Label();
		statusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 14px;");

		authLayout.getChildren().addAll(welcomeLabel, subLabel, usernameInput, passwordInput, buttonsBox, workerBox, statusLabel);
		Scene authScene = new Scene(authLayout, 550, 450);

		connectBtn.setOnAction(e -> {
			String ip = ipInput.getText().trim();
			try {
				int port = Integer.parseInt(portInput.getText().trim());
				ClientSession.connect(ip, port);
				Message connectResp = ClientSession.send(new Message("CONNECT", null));
				if (connectResp != null && "CONNECTED".equals(connectResp.getCommand())) {
					serverIP = ip;
					primaryStage.setTitle("GoNature - Access Portal");
					primaryStage.setScene(authScene);
				}
			} catch (Exception ex) {
				errorLabel.setText("Connection Failed! Is server on?");
			}
		});

		guestBtn.setOnAction(e -> {
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Guest Login");
			dialog.setHeaderText("Welcome Guest!\nPlease enter your ID to continue.");
			dialog.setContentText("ID Number (9 digits):");

			Optional<String> result = dialog.showAndWait();
			result.ifPresent(id -> {
				id = id.trim();
				if (id.isEmpty() || !id.matches("\\d{9}")) {
					statusLabel.setText("Valid 9-digit ID required for Guest Login!");
					return;
				}
				ClientDashboard.loggedInVisitorId = id;
				ClientDashboard.loggedInName = "Guest";
				ClientDashboard.isAccountGuide = false;
				ClientDashboard.isSubscriberAccount = false;
				ClientDashboard.isGuest = true;
				ClientDashboard.familyMembers = 1;
				
				primaryStage.close();
				new ClientDashboard().start(new Stage());
			});
		});

		loginBtn.setOnAction(e -> {
			String username = usernameInput.getText().trim();
			String password = passwordInput.getText().trim();

			if (username.isEmpty() || password.isEmpty()) {
				statusLabel.setText("Username and Password required for Visitor Login!");
				return;
			}

			try {
				ArrayList<String> loginData = new ArrayList<>();
				loginData.add(username);
				loginData.add(password);
				Message resMsg = ClientSession.send(new Message("LOGIN", loginData));
				String res = resMsg.getCommand();
				
				if ("LOGIN_RESPONSE".equals(res)) {
					String[] dbData = (String[]) resMsg.getData();
					
					if ("ALREADY_LOGGED_IN".equals(dbData[0])) {
						statusLabel.setText("User is already logged in from another device!");
						return;
					}
					
					if ("LOGIN_SUCCESS_GUIDE".equals(dbData[0]) || "LOGIN_SUCCESS_REGULAR".equals(dbData[0])) {
						String dbFullName = dbData[1];
						String dbVisitorId = dbData[3]; 
						
						ClientDashboard.loggedInVisitorId = dbVisitorId;
						ClientDashboard.loggedInName = dbFullName;
						ClientDashboard.isAccountGuide = "LOGIN_SUCCESS_GUIDE".equals(dbData[0]);
						ClientDashboard.isGuest = false;

						String subNum = dbData[2];
						if (!"NONE".equals(subNum)) {
							ClientDashboard.isSubscriberAccount = true;
							ClientDashboard.subscriptionNumber = subNum;
							ClientDashboard.familyMembers = Integer.parseInt(dbData[4]);
						} else {
							ClientDashboard.isSubscriberAccount = false;
							ClientDashboard.familyMembers = 1;
						}

						primaryStage.close();
						new ClientDashboard().start(new Stage());
					} else if ("WRONG_USERNAME".equals(dbData[0]) || "AUTH_FAILED".equals(dbData[0])) {
						statusLabel.setText("Error: Wrong Username or Password!");
					} else {
						statusLabel.setText("User not found! Click 'New Visitor'.");
					}
				}
			} catch (Exception ex) {
				statusLabel.setText("Server connection lost.");
			}
		});

		regBtn.setOnAction(e -> {
			String username = usernameInput.getText().trim();
			String password = passwordInput.getText().trim();

			if (username.isEmpty() || password.isEmpty()) {
				statusLabel.setText("Enter your desired Username & Password first, then click Register.");
				return;
			}
			if (!username.matches("^[a-zA-Z0-9_\\s]+$")) {
				statusLabel.setText("Username can only contain letters, numbers, and spaces.");
				return;
			}

			Dialog<String[]> dialog = new Dialog<>();
			dialog.setTitle("New Visitor Registration");
			dialog.setHeaderText("Welcome " + username + "!\nPlease provide your ID and Email to complete registration.");

			ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 20, 10, 10));

			TextField idField = new TextField();
			idField.setPromptText("9-digit ID");
			TextField emailField = new TextField();
			emailField.setPromptText("example@email.com");

			grid.add(new Label("ID Number:"), 0, 0);
			grid.add(idField, 1, 0);
			grid.add(new Label("Email:"), 0, 1);
			grid.add(emailField, 1, 1);

			dialog.getDialogPane().setContent(grid);

			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == registerButtonType) {
					return new String[] { idField.getText().trim(), emailField.getText().trim() };
				}
				return null;
			});

			Optional<String[]> result = dialog.showAndWait();
			result.ifPresent(res -> {
				String id = res[0];
				String email = res[1];

				if (!id.matches("\\d{9}")) {
					statusLabel.setText("Registration Failed: ID must be exactly 9 digits.");
					return;
				}
				if (email.isEmpty() || !email.contains("@")) {
					statusLabel.setText("Registration Failed: Valid email is required.");
					return;
				}

				try {
					ArrayList<String> regData = new ArrayList<>();
					regData.add(id);
					regData.add(username);
					regData.add(password);
					regData.add(email);
					regData.add(""); 

					Message regResp = ClientSession.send(new Message("REGISTER", regData));
					String serverRes = regResp.getCommand();
					
					if ("REGISTER_RESPONSE".equals(serverRes)) {
						if ("REGISTER_SUCCESS".equals(regResp.getData().toString())) {
							statusLabel.setStyle("-fx-text-fill: #2e7d32;");
							statusLabel.setText("Registered successfully! You can now log in.");
						} else {
							statusLabel.setText("Registration Failed: ID or Username already exists.");
						}
					}
				} catch (Exception ex) {
					statusLabel.setText("Server connection lost.");
				}
			});
		});

		workerBtn.setOnAction(e -> {
			String empId = usernameInput.getText().trim();
			String pass = passwordInput.getText().trim();

			if (empId.isEmpty() || pass.isEmpty()) {
				statusLabel.setText("Enter Employee ID and Password to login!");
				return;
			}

			try {
				ArrayList<String> loginData = new ArrayList<>();
				loginData.add(empId);
				loginData.add(pass);
				Message resMsg = ClientSession.send(new Message("LOGIN_EMPLOYEE", loginData));
				String res = resMsg.getCommand();

				if ("LOGIN_RESPONSE".equals(res)) {
					String[] dbData = (String[]) resMsg.getData();
					
					if ("ALREADY_LOGGED_IN".equals(dbData[0])) {
						statusLabel.setText("Employee is already logged in from another device!");
						return;
					}
					
					if (dbData[0].contains("SUCCESS")) {
						ClientSession.loggedInId = empId;
						String role = dbData[2];
						ClientSession.role = role;
						ClientSession.employeeParkName = dbData[3]; 
						
						primaryStage.close();
						switch (role) {
						case "ENTRY_WORKER":
							EntryWorkerDashboard.loggedInEmpName = dbData[1];
							new EntryWorkerDashboard().start(new Stage());
							break;
						case "PARK_MANAGER":
							new ParkManagerScreen().start(new Stage());
							break;
						case "DEPT_MANAGER":
							new ReportsScreen().start(new Stage());
							break;
						default:
							WorkerDashboard.loggedInEmpName = dbData[1];
							new WorkerDashboard().start(new Stage());
						}
					} else {
						statusLabel.setText("Employee auth failed. Wrong ID or Password.");
					}
				}
			} catch (Exception ex) {
				statusLabel.setText("Server connection lost.");
			}
		});

		if (ClientSession.isConnected()) {
			primaryStage.setTitle("GoNature - Access Portal");
			primaryStage.setScene(authScene);
		} else {
			primaryStage.setTitle("GoNature - Server Connect");
			primaryStage.setScene(ipScene);
		}

		primaryStage.show();
	}
}