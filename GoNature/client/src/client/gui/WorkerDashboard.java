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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class WorkerDashboard extends Application {

    public static String loggedInEmpName = "Representative"; 

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GoNature - Service Representative Dashboard");

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: #f0f3f4;");

        Label titleLabel = new Label("Service Representative Desk");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subTitle = new Label("Welcome, " + loggedInEmpName);
        subTitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #34495e; -fx-font-weight: bold;");

        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLogout.setOnAction(e -> {
            primaryStage.close();
            try { new ClientConnectionScreen().start(new Stage()); } catch (Exception ex) { ex.printStackTrace(); }
        });

        // ==========================================
        // TAB 1: Subscription Registration
        // ==========================================
        TabPane tabPane = new TabPane();
        
        Tab subTab = new Tab("Family Subscriptions");
        subTab.setClosable(false);
        
        VBox subLayout = new VBox(15);
        subLayout.setAlignment(Pos.CENTER);
        subLayout.setPadding(new Insets(20));
        
        GridPane subGrid = new GridPane();
        subGrid.setVgap(15); subGrid.setHgap(10); subGrid.setAlignment(Pos.CENTER);

        TextField subVisitorId = new TextField(); subVisitorId.setPromptText("Visitor ID (9 digits)");
        TextField subFamSize = new TextField(); subFamSize.setPromptText("Total Family Members");
        ComboBox<String> subPayment = new ComboBox<>(); subPayment.getItems().addAll("Cash", "Credit Card"); subPayment.setValue("Cash");
        TextField subCcInput = new TextField(); subCcInput.setPromptText("Credit Card Number"); subCcInput.setDisable(true); 

        subPayment.setOnAction(e -> {
            subCcInput.setDisable(!"Credit Card".equals(subPayment.getValue()));
            if (subCcInput.isDisabled()) subCcInput.clear();
        });

        subGrid.add(new Label("Visitor ID:"), 0, 0); subGrid.add(subVisitorId, 1, 0);
        subGrid.add(new Label("Family Members:"), 0, 1); subGrid.add(subFamSize, 1, 1);
        subGrid.add(new Label("Payment Method:"), 0, 2); subGrid.add(subPayment, 1, 2);
        subGrid.add(new Label("Card Number:"), 0, 3); subGrid.add(subCcInput, 1, 3);

        Button btnRegSub = new Button("Register Subscription & Generate Number");
        btnRegSub.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 300px;");
        Label subResponse = new Label(); subResponse.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

        btnRegSub.setOnAction(e -> {
            String vid = subVisitorId.getText().trim();
            if (!vid.matches("\\d{9}")) { subResponse.setText("Invalid Visitor ID!"); return; }
            int famSize; try { famSize = Integer.parseInt(subFamSize.getText().trim()); } catch (Exception ex) { subResponse.setText("Invalid family size!"); return; }
            String paymentData = "Cash".equals(subPayment.getValue()) ? "PAID_CASH" : subCcInput.getText().trim();
            if ("Credit Card".equals(subPayment.getValue()) && paymentData.isEmpty()) { subResponse.setText("Credit card number required!"); return; }

            try (Socket socket = new Socket(ClientConnectionScreen.serverIP, 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                ArrayList<Object> subData = new ArrayList<>(); subData.add(vid); subData.add(famSize); subData.add(paymentData);
                out.writeObject(new Message("BUY_SUBSCRIPTION", subData));
                Message response = (Message) in.readObject();

                if ("SUCCESS".equals(response.getCommand())) {
                    String subNum = response.getData().toString();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Subscription Registered");
                    alert.setHeaderText("Payment Received (" + subPayment.getValue() + ")");
                    alert.setContentText("Subscription successfully registered for Visitor ID: " + vid + "\n\nGenerated Subscription Number: " + subNum);
                    alert.showAndWait();
                    subResponse.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    subResponse.setText("Subscription #" + subNum + " created!");
                    subVisitorId.clear(); subFamSize.clear(); subCcInput.clear();
                } else { subResponse.setText("Failed to register. Does visitor exist?"); }
            } catch (Exception ex) { subResponse.setText("Server connection error."); }
        });
        subLayout.getChildren().addAll(new Label("Register New Family Subscription"), subGrid, btnRegSub, subResponse);
        subTab.setContent(subLayout);

        // ==========================================
        // TAB 2: Certified Guide Registration
        // ==========================================
        Tab guideTab = new Tab("Certified Guides");
        guideTab.setClosable(false);
        
        VBox guideLayout = new VBox(15);
        guideLayout.setAlignment(Pos.CENTER);
        guideLayout.setPadding(new Insets(20));

        GridPane guideGrid = new GridPane();
        guideGrid.setVgap(15); guideGrid.setHgap(10); guideGrid.setAlignment(Pos.CENTER);

        TextField guideIdInput = new TextField(); guideIdInput.setPromptText("ID Number (9 digits)");
        PasswordField guidePassInput = new PasswordField(); guidePassInput.setPromptText("Assign Password");
        TextField guideNameInput = new TextField(); guideNameInput.setPromptText("Full Name");

        guideGrid.add(new Label("Guide ID:"), 0, 0); guideGrid.add(guideIdInput, 1, 0);
        guideGrid.add(new Label("Full Name:"), 0, 1); guideGrid.add(guideNameInput, 1, 1);
        guideGrid.add(new Label("Assign Password:"), 0, 2); guideGrid.add(guidePassInput, 1, 2);

        Button btnRegGuide = new Button("Approve & Register Certified Guide");
        btnRegGuide.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 300px;");
        Label guideResponse = new Label(); guideResponse.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

        btnRegGuide.setOnAction(e -> {
            String id = guideIdInput.getText().trim(); String pass = guidePassInput.getText().trim(); String name = guideNameInput.getText().trim();
            if (id.isEmpty() || pass.isEmpty() || name.isEmpty()) { guideResponse.setText("All fields required!"); return; }
            if (!id.matches("\\d{9}")) { guideResponse.setText("ID must be 9 digits!"); return; }

            try (Socket socket = new Socket(ClientConnectionScreen.serverIP, 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                ArrayList<String> regData = new ArrayList<>(); 
                regData.add(id); 
                regData.add(pass); 
                regData.add(name);

                out.writeObject(new Message("REGISTER_GUIDE", regData));
                String res = ((Message) in.readObject()).getCommand();

                if ("REGISTER_SUCCESS".equals(res)) { 
                    guideResponse.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); 
                    guideResponse.setText("New Certified Guide added!"); 
                    guideIdInput.clear(); guidePassInput.clear(); guideNameInput.clear();
                    new Alert(Alert.AlertType.INFORMATION, "New Guide Added Successfully!").showAndWait();
                } else if ("UPDATE_SUCCESS".equals(res)) { 
                    guideResponse.setStyle("-fx-text-fill: #8e44ad; -fx-font-weight: bold;"); 
                    guideResponse.setText("Existing Visitor upgraded to Certified Guide!"); 
                    guideIdInput.clear(); guidePassInput.clear(); guideNameInput.clear();
                    new Alert(Alert.AlertType.INFORMATION, "Existing Visitor Upgraded to Guide Successfully!").showAndWait();
                } else { 
                    guideResponse.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;"); 
                    guideResponse.setText("Failed to process guide."); 
                }
            } catch (Exception ex) { guideResponse.setText("Server connection error."); }
        });

        guideLayout.getChildren().addAll(new Label("Add New Organized Group Guide to Database"), guideGrid, btnRegGuide, guideResponse);
        guideTab.setContent(guideLayout);

        tabPane.getTabs().addAll(subTab, guideTab);

        mainLayout.getChildren().addAll(titleLabel, subTitle, tabPane, btnLogout);

        Scene scene = new Scene(mainLayout, 550, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}