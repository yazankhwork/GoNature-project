package client.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import common.Message;
import client.network.ClientSession;

import java.util.ArrayList;
/**
 * Dashboard screen for service representatives in the GoNature system.
 *
 * This screen allows service representatives to register family
 * subscriptions and manage certified guide registrations.
 *
 * @author Group 4
 * @version 1.0
 */
public class WorkerDashboard extends Application {
	/**
	 * Name of the currently logged-in service representative.
	 */
	public static String loggedInEmpName = "Representative";
	@Override
	/**
	 * Creates and displays the service representative dashboard.
	 *
	 * @param primaryStage primary JavaFX stage
	 */
	public void start(Stage primaryStage) {
		primaryStage.setTitle("GoNature - Service Representative Dashboard");

		VBox mainLayout = new VBox(15);
		mainLayout.setPadding(new Insets(20));
		mainLayout.setAlignment(Pos.TOP_CENTER);
		mainLayout.setStyle("-fx-background-color: #e8f5e9;");

		Label titleLabel = new Label("🍃 Service Representative Desk 🍃");
		titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
		titleLabel.setStyle("-fx-text-fill: #1b5e20;");

		Label subTitle = new Label("Welcome, " + loggedInEmpName);
		subTitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #388e3c; -fx-font-weight: bold;");

		Button btnLogout = new Button("Logout");
		btnLogout.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		btnLogout.setOnAction(e -> LogoutHelper.logout(primaryStage));

		TabPane tabPane = new TabPane();
		tabPane.setStyle("-fx-background-color: transparent;");

		Tab subTab = new Tab("Family Subscriptions");
		subTab.setClosable(false);

		VBox subLayout = new VBox(15);
		subLayout.setAlignment(Pos.CENTER);
		subLayout.setPadding(new Insets(20));
		subLayout.setStyle("-fx-background-color: #f1f8e9; -fx-border-color: #c8e6c9; -fx-border-radius: 5px;");

		GridPane subGrid = new GridPane();
		subGrid.setVgap(15);
		subGrid.setHgap(10);
		subGrid.setAlignment(Pos.CENTER);

		String inputStyle = "-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px;";

		TextField subVisitorId = new TextField();
		subVisitorId.setPromptText("Visitor ID (9 digits)");
		subVisitorId.setStyle(inputStyle);

		TextField subFirstName = new TextField();
		subFirstName.setPromptText("First Name");
		subFirstName.setStyle(inputStyle);

		TextField subLastName = new TextField();
		subLastName.setPromptText("Last Name");
		subLastName.setStyle(inputStyle);

		TextField subPhone = new TextField();
		subPhone.setPromptText("Phone Number (10 digits)");
		subPhone.setStyle(inputStyle);

		TextField subEmail = new TextField();
		subEmail.setPromptText("Email");
		subEmail.setStyle(inputStyle);

		TextField subFamSize = new TextField();
		subFamSize.setPromptText("Total Family Members");
		subFamSize.setStyle(inputStyle);

		ComboBox<String> subPayment = new ComboBox<>();
		subPayment.getItems().addAll("Cash", "Credit Card");
		subPayment.setValue("Cash");
		subPayment.setStyle(inputStyle);
		
		TextField subCcInput = new TextField();
		subCcInput.setPromptText("Credit Card Number");
		subCcInput.setDisable(true);
		subCcInput.setStyle(inputStyle);

		subPayment.setOnAction(e -> {
			subCcInput.setDisable(!"Credit Card".equals(subPayment.getValue()));
			if (subCcInput.isDisabled())
				subCcInput.clear();
		});

		subGrid.add(new Label("Visitor ID:"), 0, 0);
		subGrid.add(subVisitorId, 1, 0);

		subGrid.add(new Label("First Name:"), 0, 1);
		subGrid.add(subFirstName, 1, 1);

		subGrid.add(new Label("Last Name:"), 0, 2);
		subGrid.add(subLastName, 1, 2);

		subGrid.add(new Label("Phone:"), 0, 3);
		subGrid.add(subPhone, 1, 3);

		subGrid.add(new Label("Email:"), 0, 4);
		subGrid.add(subEmail, 1, 4);

		subGrid.add(new Label("Family Members:"), 0, 5);
		subGrid.add(subFamSize, 1, 5);

		subGrid.add(new Label("Payment Method:"), 0, 6);
		subGrid.add(subPayment, 1, 6);

		subGrid.add(new Label("Card Number:"), 0, 7);
		subGrid.add(subCcInput, 1, 7);

		Button btnRegSub = new Button("Register Subscription & Generate Number");
		btnRegSub.setStyle(
				"-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 300px; -fx-background-radius: 5px;");
		Label subResponse = new Label();
		subResponse.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");

		btnRegSub.setOnAction(e -> {
			String vid = subVisitorId.getText().trim();
			String firstName = subFirstName.getText().trim();
			String lastName = subLastName.getText().trim();
			String phone = subPhone.getText().trim();
			String email = subEmail.getText().trim();

			if (!vid.matches("\\d{9}")) {
				subResponse.setText("Invalid Visitor ID!");
				return;
			}

			if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
				subResponse.setText("Name, phone, and email are required!");
				return;
			}
			
			if (!phone.matches("\\d{10}")) {
				subResponse.setText("Phone must be exactly 10 digits!");
				return;
			}

			if (!email.contains("@")) {
				subResponse.setText("Invalid email!");
				return;
			}

			int famSize;
			try {
				famSize = Integer.parseInt(subFamSize.getText().trim());
			} catch (Exception ex) {
				subResponse.setText("Invalid family size!");
				return;
			}

			if (famSize < 1) {
				subResponse.setText("Family members must be at least 1!");
				return;
			}

			String paymentMethod = subPayment.getValue();
			String creditCard = "Credit Card".equals(paymentMethod) ? subCcInput.getText().trim() : "";

			if ("Credit Card".equals(paymentMethod) && creditCard.isEmpty()) {
				subResponse.setText("Credit card number required!");
				return;
			}

			try {

				ArrayList<Object> subData = new ArrayList<>();
				subData.add(vid);
				subData.add(firstName);
				subData.add(lastName);
				subData.add(phone);
				subData.add(email);
				subData.add(famSize);
				subData.add(paymentMethod);
				subData.add(creditCard);

				Message response = ClientSession.send(new Message("BUY_SUBSCRIPTION", subData));
				
				if ("SUCCESS".equals(response.getCommand())) {
					String subNum = response.getData().toString();
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Subscription Registered");
					alert.setHeaderText("Payment Received (" + subPayment.getValue() + ")");
					alert.setContentText("Subscription successfully registered for Visitor ID: " + vid
							+ "\n\nGenerated Subscription Number: " + subNum);
					alert.showAndWait();
					subResponse.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
					subResponse.setText("Subscription #" + subNum + " created!");
					subVisitorId.clear();
					subFirstName.clear();
					subLastName.clear();
					subPhone.clear();
					subEmail.clear();
					subFamSize.clear();
					subCcInput.clear();
				} else {
					subResponse.setText("Failed to register. Does visitor exist?");
				}
			} catch (Exception ex) {
				subResponse.setText("Server connection error.");
			}
		});
		
		Label subHeader = new Label("Register New Family Subscription");
		subHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b5e20; -fx-font-size: 14px;");
		subLayout.getChildren().addAll(subHeader, subGrid, btnRegSub, subResponse);
		subTab.setContent(subLayout);

		Tab guideTab = new Tab("Certified Guides");
		guideTab.setClosable(false);

		VBox guideLayout = new VBox(15);
		guideLayout.setAlignment(Pos.CENTER);
		guideLayout.setPadding(new Insets(20));
		guideLayout.setStyle("-fx-background-color: #f1f8e9; -fx-border-color: #c8e6c9; -fx-border-radius: 5px;");

		GridPane guideGrid = new GridPane();
		guideGrid.setVgap(15);
		guideGrid.setHgap(10);
		guideGrid.setAlignment(Pos.CENTER);

		TextField guideIdInput = new TextField();
		guideIdInput.setPromptText("ID Number (9 digits)");
		guideIdInput.setStyle(inputStyle);
		
		PasswordField guidePassInput = new PasswordField();
		guidePassInput.setPromptText("Assign Password");
		guidePassInput.setStyle(inputStyle);
		
		TextField guideNameInput = new TextField();
		guideNameInput.setPromptText("Username (for Guide Login)");
		guideNameInput.setStyle(inputStyle);

		guideGrid.add(new Label("Guide ID:"), 0, 0);
		guideGrid.add(guideIdInput, 1, 0);
		guideGrid.add(new Label("Username:"), 0, 1);
		guideGrid.add(guideNameInput, 1, 1);
		guideGrid.add(new Label("Assign Password:"), 0, 2);
		guideGrid.add(guidePassInput, 1, 2);

		Button btnRegGuide = new Button("Approve & Register Certified Guide");
		btnRegGuide.setStyle(
				"-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 300px; -fx-background-radius: 5px;");
		Label guideResponse = new Label();
		guideResponse.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");

		btnRegGuide.setOnAction(e -> {
			String id = guideIdInput.getText().trim();
			String pass = guidePassInput.getText().trim();
			String name = guideNameInput.getText().trim();
			if (id.isEmpty() || pass.isEmpty() || name.isEmpty()) {
				guideResponse.setText("All fields required!");
				return;
			}
			if (!id.matches("\\d{9}")) {
				guideResponse.setText("ID must be 9 digits!");
				return;
			}

			try {
				ArrayList<String> regData = new ArrayList<>();
				regData.add(id);
				regData.add(pass);
				regData.add(name);

				Message guideResp = ClientSession.send(new Message("REGISTER_GUIDE", regData));
				String res = guideResp.getCommand();

				if ("REGISTER_SUCCESS".equals(res)) {
					guideResponse.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
					guideResponse.setText("New Certified Guide added!");
					guideIdInput.clear();
					guidePassInput.clear();
					guideNameInput.clear();
					new Alert(Alert.AlertType.INFORMATION, "New Guide Added Successfully!").showAndWait();
				} else if ("UPDATE_SUCCESS".equals(res)) {
					guideResponse.setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
					guideResponse.setText("Existing Visitor upgraded to Certified Guide!");
					guideIdInput.clear();
					guidePassInput.clear();
					guideNameInput.clear();
					new Alert(Alert.AlertType.INFORMATION, "Existing Visitor Upgraded to Guide Successfully!")
							.showAndWait();
				} else {
					guideResponse.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
					guideResponse.setText("Failed to process guide.");
				}
			} catch (Exception ex) {
				guideResponse.setText("Server connection error.");
			}
		});

		Label guideHeader = new Label("Add New Organized Group Guide to Database");
		guideHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b5e20; -fx-font-size: 14px;");
		guideLayout.getChildren().addAll(guideHeader, guideGrid, btnRegGuide, guideResponse);
		guideTab.setContent(guideLayout);

		// כרטיסייה חדשה למשיכת פרופיל משתמש (User Info Search)
		Tab infoTab = new Tab("User Profile Search");
		infoTab.setClosable(false);

		VBox infoLayout = new VBox(15);
		infoLayout.setAlignment(Pos.CENTER);
		infoLayout.setPadding(new Insets(20));
		infoLayout.setStyle("-fx-background-color: #f1f8e9; -fx-border-color: #c8e6c9; -fx-border-radius: 5px;");

		TextField searchIdInput = new TextField();
		searchIdInput.setPromptText("Enter ID Number (9 digits)");
		searchIdInput.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-pref-width: 250px;");

		Button btnSearch = new Button("Search Information");
		btnSearch.setStyle("-fx-background-color: #f57c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");

		VBox resultBox = new VBox(10);
		resultBox.setAlignment(Pos.CENTER_LEFT);
		resultBox.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-border-color: #81c784; -fx-border-radius: 5px;");
		Label lblResType = new Label("Type: -");
		Label lblResName = new Label("Name: -");
		Label lblResEmail = new Label("Email: -");
		Label lblResPhone = new Label("Phone: -");
		Label lblResExtra = new Label("Extra Info: -");

		lblResType.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1b5e20;");
		lblResName.setStyle("-fx-font-size: 14px;");
		lblResEmail.setStyle("-fx-font-size: 14px;");
		lblResPhone.setStyle("-fx-font-size: 14px;");
		lblResExtra.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0288d1;");

		resultBox.getChildren().addAll(lblResType, lblResName, lblResEmail, lblResPhone, lblResExtra);
		resultBox.setVisible(false); // מוסתר עד שיש תוצאות

		Label infoResponse = new Label();
		infoResponse.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");

		btnSearch.setOnAction(e -> {
			String searchId = searchIdInput.getText().trim();
			if (!searchId.matches("\\d{9}")) {
				infoResponse.setText("ID must be exactly 9 digits!");
				resultBox.setVisible(false);
				return;
			}
			try {
				@SuppressWarnings("unchecked")
				Message response = ClientSession.send(new Message("GET_USER_INFO", searchId));
				if ("USER_INFO_RESULT".equals(response.getCommand()) && response.getData() != null) {
					ArrayList<String> data = (ArrayList<String>) response.getData();
					lblResType.setText("Type: " + data.get(0));
					lblResName.setText("Full Name: " + data.get(1));
					lblResEmail.setText("Email: " + data.get(2));
					lblResPhone.setText("Phone: " + data.get(3));
					lblResExtra.setText("Extra Details: " + data.get(4));
					resultBox.setVisible(true);
					infoResponse.setText("");
				} else {
					infoResponse.setText("User not found in the database.");
					resultBox.setVisible(false);
				}
			} catch (Exception ex) {
				infoResponse.setText("Connection error.");
			}
		});

		Label infoTitle = new Label("View Subscriber / Employee Information");
		infoTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b5e20; -fx-font-size: 16px;");

		HBox searchBox = new HBox(10, searchIdInput, btnSearch);
		searchBox.setAlignment(Pos.CENTER);

		infoLayout.getChildren().addAll(infoTitle, searchBox, infoResponse, resultBox);
		infoTab.setContent(infoLayout);

		// הוספת הכרטיסייה למסך
		tabPane.getTabs().addAll(subTab, guideTab, infoTab);

		mainLayout.getChildren().addAll(titleLabel, subTitle, tabPane, btnLogout);

		Scene scene = new Scene(mainLayout, 650, 650);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}