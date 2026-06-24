package client.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import common.Message;
import client.network.ClientSession;

import java.util.ArrayList;
import java.util.Optional;
/**
 * Main login and connection screen of the GoNature client application.
 *
 * This screen allows users to connect to the server, log in as visitors,
 * employees or guests, and register new visitor accounts.
 *
 * Depending on the user role, the appropriate dashboard is opened
 * after successful authentication.
 *
 * @author Group 4
 * @version 1.0
 */
public class ClientConnectionScreen extends Application {
	/**
	 * Current server IP address used by the client.
	 */
	public static String serverIP = "localhost";
	/**
	 * Creates and displays the connection and login interface.
	 *
	 * @param primaryStage primary JavaFX stage
	 */
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


		VBox authLayout = new VBox();
		authLayout.setAlignment(Pos.CENTER);
		authLayout.setStyle("-fx-background-color: #e8f5e9;");

		VBox card = new VBox(20);
		card.setMaxWidth(450);
		card.setAlignment(Pos.CENTER);
		card.setPadding(new Insets(40, 30, 40, 30));
		card.setStyle("-fx-background-color: white; "
				+ "-fx-background-radius: 15px; "
				+ "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 5);");

		Label welcomeLabel = new Label("🌿 Welcome to GoNature 🌿");
		welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 26));
		welcomeLabel.setStyle("-fx-text-fill: #1b5e20;");

		Label subLabel = new Label("Please select your login type");
		subLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

		HBox toggleBox = new HBox(10);
		toggleBox.setAlignment(Pos.CENTER);
		Button btnVisMode = new Button("Visitor");
		Button btnEmpMode = new Button("Employee");

		String activeStyle = "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-pref-width: 120px; -fx-pref-height: 35px;";
		String inactiveStyle = "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-pref-width: 120px; -fx-pref-height: 35px;";

		btnVisMode.setStyle(activeStyle);
		btnEmpMode.setStyle(inactiveStyle);
		toggleBox.getChildren().addAll(btnVisMode, btnEmpMode);

		StackPane formsPane = new StackPane();

		VBox visForm = new VBox(15);
		visForm.setAlignment(Pos.CENTER);
		
		TextField visIdField = new TextField();
		visIdField.setPromptText("Enter your 9-digit ID");
		visIdField.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-pref-height: 40px; -fx-font-size: 14px;");

		Button btnVisLogin = new Button("Visitor Login");
		btnVisLogin.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 250px; -fx-pref-height: 40px; -fx-font-size: 14px;");

		Label lblOr = new Label("- OR -");
		lblOr.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");

		Button btnGuest = new Button("Continue as Guest");
		btnGuest.setStyle("-fx-background-color: transparent; -fx-border-color: #2e7d32; -fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-pref-width: 250px; -fx-pref-height: 40px; -fx-font-size: 14px;");

		visForm.getChildren().addAll(new Label("Visitor Access"), visIdField, btnVisLogin, lblOr, btnGuest);

		VBox empForm = new VBox(15);
		empForm.setAlignment(Pos.CENTER);
		empForm.setVisible(false); 

		TextField empUserField = new TextField();
		empUserField.setPromptText("Employee Username / ID");
		empUserField.setStyle("-fx-border-color: #0288d1; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-pref-height: 40px; -fx-font-size: 14px;");

		PasswordField empPassField = new PasswordField();
		empPassField.setPromptText("Password");
		empPassField.setStyle("-fx-border-color: #0288d1; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-pref-height: 40px; -fx-font-size: 14px;");

		Button btnEmpLogin = new Button("Employee Login");
		btnEmpLogin.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-pref-width: 250px; -fx-pref-height: 40px; -fx-font-size: 14px;");

		empForm.getChildren().addAll(new Label("Staff & Management Access"), empUserField, empPassField, btnEmpLogin);

		formsPane.getChildren().addAll(visForm, empForm);

		Label statusLabel = new Label();
		statusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 14px;");

		card.getChildren().addAll(welcomeLabel, subLabel, toggleBox, new Separator(), formsPane, statusLabel);
		authLayout.getChildren().add(card);
		
		Scene authScene = new Scene(authLayout, 650, 550);


		btnVisMode.setOnAction(e -> {
			visForm.setVisible(true);
			empForm.setVisible(false);
			btnVisMode.setStyle(activeStyle);
			btnEmpMode.setStyle(inactiveStyle);
			statusLabel.setText("");
		});

		btnEmpMode.setOnAction(e -> {
			visForm.setVisible(false);
			empForm.setVisible(true);
			btnVisMode.setStyle(inactiveStyle);
			btnEmpMode.setStyle(activeStyle);
			statusLabel.setText("");
		});


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


		// --- לוגיקת רישום האורח החדשה בלחיצה אחת ---
		btnGuest.setOnAction(e -> {
			String id = visIdField.getText().trim();
			if (!id.matches("\\d{9}")) {
				statusLabel.setText("Valid 9-digit ID required for Guest Login!");
				return;
			}

			try {
				ArrayList<String> guestData = new ArrayList<>();
				guestData.add(id);
				guestData.add(""); // אימייל ריק, יעודכן ב-Edit Profile
				guestData.add(""); // טלפון ריק, יעודכן ב-Edit Profile

				Message resp = ClientSession.send(new Message("REGISTER_GUEST", guestData));
				String serverStatus = (String) resp.getData();

				if ("ALREADY_EXISTS".equals(serverStatus)) {
					statusLabel.setText("ID has previous orders! Please use 'Visitor Login'.");
					return;
				} else if ("REGISTERED".equals(serverStatus)) {
					ClientDashboard.loggedInVisitorId = id;
					ClientDashboard.loggedInName = "Guest";
					ClientDashboard.loggedInEmail = ""; 
					ClientDashboard.loggedInPhone = ""; 
					ClientDashboard.isGuest = true;
					ClientDashboard.isAccountGuide = false;
					ClientDashboard.isSubscriberAccount = false;
					ClientDashboard.subscriptionNumber = "NONE";
					ClientDashboard.familyMembers = 1;

					primaryStage.close();
					new ClientDashboard().start(new Stage());
				} else {
					statusLabel.setText("Failed to register guest in system.");
				}
			} catch (Exception ex) {
				statusLabel.setText("Server connection lost.");
			}
		});


		btnVisLogin.setOnAction(e -> {
			String id = visIdField.getText().trim();
			if (!id.matches("\\d{9}")) {
				statusLabel.setText("Valid 9-digit ID required for Visitor Login!");
				return;
			}

			try {
				Message response = ClientSession.send(new Message("GET_USER_INFO", id));
				
				if ("USER_INFO_RESULT".equals(response.getCommand()) && response.getData() != null) {
					@SuppressWarnings("unchecked")
					ArrayList<String> data = (ArrayList<String>) response.getData();
					String type = data.get(0);
					String name = data.get(1);
					
					if (type.equals("Employee")) {
						statusLabel.setText("This ID belongs to an employee. Use Employee Login.");
						return;
					}
					
					// --- Guide Password Verification Logic ---
					if (type.equals("Certified Guide")) {
						Dialog<String> pwdDialog = new Dialog<>();
						pwdDialog.setTitle("Guide Authentication");
						pwdDialog.setHeaderText("Hello " + name + ",\nAs a Certified Guide, please enter your password:");
						
						PasswordField pwdField = new PasswordField();
						pwdField.setPromptText("Password");
						pwdField.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-pref-height: 35px;");
						
						VBox vbox = new VBox(pwdField);
						vbox.setPadding(new Insets(10, 0, 0, 0));
						pwdDialog.getDialogPane().setContent(vbox);
						
						ButtonType loginBtnType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
						pwdDialog.getDialogPane().getButtonTypes().addAll(loginBtnType, ButtonType.CANCEL);
						
						pwdDialog.setResultConverter(b -> {
							if (b == loginBtnType) {
								return pwdField.getText();
							}
							return null;
						});
						
						Optional<String> pwdResult = pwdDialog.showAndWait();
						if (pwdResult.isPresent()) {
							String pass = pwdResult.get().trim();
							if (pass.isEmpty()) {
								statusLabel.setText("Password cannot be empty.");
								return;
							}
							
							ArrayList<String> verifyData = new ArrayList<>();
							verifyData.add(id);
							verifyData.add(pass);
							
							Message verifyResp = ClientSession.send(new Message("VERIFY_GUIDE_PASS", verifyData));
							if (!"VERIFY_SUCCESS".equals(verifyResp.getCommand())) {
								statusLabel.setText("Incorrect password for Guide.");
								return; // Halt login process
							}
						} else {
							// User cancelled the password dialog
							return; 
						}
					}
					// --- End Guide Logic ---

					ClientDashboard.loggedInVisitorId = id;
					ClientDashboard.loggedInName = name;
					ClientDashboard.loggedInEmail = data.get(2); 
					ClientDashboard.loggedInPhone = data.get(3); 
					ClientDashboard.isGuest = false;
					ClientDashboard.isAccountGuide = "Certified Guide".equals(type);

					if ("Family Subscriber".equals(type)) {
						ClientDashboard.isSubscriberAccount = true;
						String extra = data.get(4); 
						try {
							String[] parts = extra.split("\\|");
							String subPart = parts[0].replaceAll("[^0-9]", "");
							String memPart = parts[1].replaceAll("[^0-9]", "");
							ClientDashboard.subscriptionNumber = subPart;
							ClientDashboard.familyMembers = Integer.parseInt(memPart);
						} catch (Exception ex) {
							ClientDashboard.subscriptionNumber = "0";
							ClientDashboard.familyMembers = 1;
						}
					} else {
						ClientDashboard.isSubscriberAccount = false;
						ClientDashboard.subscriptionNumber = "NONE";
						ClientDashboard.familyMembers = 1;
					}

					primaryStage.close();
					new ClientDashboard().start(new Stage());
				} else {
					statusLabel.setText("ID not found. Please 'Continue as Guest' to register automatically.");
				}
			} catch (Exception ex) {
				statusLabel.setText("Server connection lost.");
			}
		});


		btnEmpLogin.setOnAction(e -> {
			String empId = empUserField.getText().trim();
			String pass = empPassField.getText().trim();

			if (empId.isEmpty() || pass.isEmpty()) {
				statusLabel.setText("Enter Username/ID and Password to login!");
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