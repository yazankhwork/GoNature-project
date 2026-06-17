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
import client.network.ClientSession;

import java.util.ArrayList;

public class ClientConnectionScreen extends Application {

	public static String serverIP = "localhost";

	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle("GoNature - Server Connect");

		VBox ipLayout = new VBox(15);
		ipLayout.setPadding(new Insets(40)); ipLayout.setAlignment(Pos.CENTER); ipLayout.setStyle("-fx-background-color: #f4fcf4;");

		Label titleLabel = new Label("GoNature Server Connect");
		titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20)); titleLabel.setStyle("-fx-text-fill: #27ae60;");

		TextField ipInput = new TextField("localhost");
		ipInput.setStyle("-fx-border-color: #2ecc71; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		Button connectBtn = new Button("Connect to Server");
		connectBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-font-size: 14px;");

		Label errorLabel = new Label(); errorLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

		ipLayout.getChildren().addAll(titleLabel, new Label("Enter Server IP:"), ipInput, connectBtn, errorLabel);
		Scene ipScene = new Scene(ipLayout, 350, 250);

		VBox authLayout = new VBox(15);
		authLayout.setPadding(new Insets(30)); authLayout.setAlignment(Pos.CENTER); authLayout.setStyle("-fx-background-color: #f4fcf4;");

		Label welcomeLabel = new Label("Welcome to GoNature!");
		welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 22)); welcomeLabel.setStyle("-fx-text-fill: #27ae60;");

		Label subLabel = new Label("Please Log In or Register"); subLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

		TextField idInput = new TextField(); idInput.setPromptText("ID Number"); idInput.setStyle("-fx-border-color: #2ecc71; -fx-background-radius: 5px; -fx-border-radius: 5px;");
		PasswordField passInput = new PasswordField(); passInput.setPromptText("Password"); passInput.setStyle("-fx-border-color: #2ecc71; -fx-background-radius: 5px; -fx-border-radius: 5px;");
		TextField nameInput = new TextField(); nameInput.setPromptText("Full Name (Visitors Only)"); nameInput.setStyle("-fx-border-color: #2ecc71; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		Button loginBtn = new Button("Visitor Login"); loginBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 140px;");
		Button regBtn = new Button("New Visitor (Register)"); regBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 180px;");
		
		Button workerBtn = new Button("Login as Service Rep"); workerBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 180px;");

		HBox buttonsBox = new HBox(15, loginBtn, regBtn); buttonsBox.setAlignment(Pos.CENTER);
		HBox workerBox = new HBox(workerBtn); workerBox.setAlignment(Pos.CENTER);
		Label statusLabel = new Label(); statusLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

		authLayout.getChildren().addAll(welcomeLabel, subLabel, idInput, passInput, nameInput, buttonsBox, workerBox, statusLabel);
		Scene authScene = new Scene(authLayout, 400, 450);

		connectBtn.setOnAction(e -> {
			String ip = ipInput.getText().trim();
			try {
				ClientSession.connect(ip);
				Message connectResp = ClientSession.send(new Message("CONNECT", null));
				if (connectResp != null && "CONNECTED".equals(connectResp.getCommand())) {
					serverIP = ip; primaryStage.setTitle("GoNature - Authentication"); primaryStage.setScene(authScene);
				}
			} catch (Exception ex) { errorLabel.setText("Connection Failed! Is server on?"); }
		});

		workerBtn.setOnAction(e -> {
			String id = idInput.getText().trim(); 
			String pass = passInput.getText().trim(); 
			
			if (id.isEmpty() || pass.isEmpty()) { 
				statusLabel.setText("ID and Password required for Employee Login!"); 
				return; 
			}

			try {

				ArrayList<String> loginData = new ArrayList<>(); 
				loginData.add(id); loginData.add(pass);
				Message resMsg = ClientSession.send(new Message("LOGIN_EMPLOYEE", loginData));
				String res = resMsg.getCommand();

				if ("LOGIN_SUCCESS_EMPLOYEE".equals(res)) {
				    String[] dbData = (String[]) resMsg.getData();
				    String role = (dbData.length > 2 && dbData[2] != null) ? dbData[2] : "SERVICE_REP";
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
				} else if ("WRONG_PASSWORD".equals(res)) { 
					statusLabel.setText("Error: Wrong Password!");
				} else { 
					statusLabel.setText("Employee not found in database!"); 
				}
			} catch (Exception ex) { statusLabel.setText("Server connection lost."); }
		});

		loginBtn.setOnAction(e -> {
			String id = idInput.getText().trim(); String pass = passInput.getText().trim(); String typedName = nameInput.getText().trim(); 
			if (id.isEmpty() || pass.isEmpty() || typedName.isEmpty()) { statusLabel.setText("ID, Password, and Full Name required!"); return; }
			if (!id.matches("\\d{9}")) { statusLabel.setText("ID is invalid! Must be exactly 9 digits."); return; }

			try {

				ArrayList<String> loginData = new ArrayList<>(); loginData.add(id); loginData.add(pass);
				Message resMsg = ClientSession.send(new Message("LOGIN", loginData));
				String res = resMsg.getCommand();

				if ("LOGIN_SUCCESS_GUIDE".equals(res) || "LOGIN_SUCCESS_REGULAR".equals(res)) {
					String[] dbData = (String[]) resMsg.getData();
					String dbFullName = dbData[1];
					if (dbFullName == null || !dbFullName.equals(typedName)) { statusLabel.setText("Error: Full Name does not match!"); return; }

					ClientDashboard.loggedInVisitorId = id;
					ClientDashboard.loggedInName = dbFullName;
					ClientDashboard.isAccountGuide = "LOGIN_SUCCESS_GUIDE".equals(res);
					
					String subNum = dbData[2];
					if (!"NONE".equals(subNum)) {
						ClientDashboard.isSubscriberAccount = true;
						ClientDashboard.subscriptionNumber = subNum;
					} else {
						ClientDashboard.isSubscriberAccount = false;
					}

					primaryStage.close(); new ClientDashboard().start(new Stage());
				} else if ("WRONG_PASSWORD".equals(res)) { statusLabel.setText("Error: Wrong Password!");
				} else { statusLabel.setText("User not found! Click 'New Visitor'."); }
			} catch (Exception ex) { statusLabel.setText("Server connection lost."); }
		});

		regBtn.setOnAction(e -> {
			String id = idInput.getText().trim(); String pass = passInput.getText().trim(); String fullName = nameInput.getText().trim();
			boolean isGuide = false; // FORCED TO FALSE. Only workers can add guides now!
			
			if (id.isEmpty() || pass.isEmpty()) { statusLabel.setText("ID and Password required!"); return; }
			if (!id.matches("\\d{9}")) { statusLabel.setText("ID is invalid!"); return; }
			if (fullName.isEmpty()) { statusLabel.setText("Name cannot be empty!"); return; }
			if (!fullName.matches("^[a-zA-Z\\s]+$")) { statusLabel.setText("Full Name must contain only English letters!"); return; }

			try {

				ArrayList<Object> regData = new ArrayList<>(); regData.add(id); regData.add(pass); regData.add(isGuide); regData.add(fullName);
				Message regResp = ClientSession.send(new Message("REGISTER", regData));
				String res = regResp.getCommand();

				if ("REGISTER_SUCCESS".equals(res)) { statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); statusLabel.setText("Registration successful! Now click 'Log In'."); } 
				else if ("USER_ALREADY_EXISTS".equals(res)) { statusLabel.setText("ID already registered! Please log in."); }
			} catch (Exception ex) { statusLabel.setText("Server connection lost."); }
		});

		primaryStage.setScene(ipScene); primaryStage.show();
	}
}