package client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import client.gui.ClientDashboard;

/**
 * Main JavaFX entry screen for the GoNature client application.
 * <p>
 * This screen allows users to:
 * <ul>
 *     <li>Connect to the GoNature server.</li>
 *     <li>Log in using an existing account.</li>
 *     <li>Register as a new visitor.</li>
 *     <li>Register as an organized group guide.</li>
 * </ul>
 * After successful authentication, the dashboard screen is opened.
 *
 * @author Bolos Saad
 */
public class ClientMain extends Application {

    /**
     * Current server IP address used by the client.
     */
    public static String serverIP = "localhost";

    /**
     * Starts the JavaFX application and displays the
     * connection and authentication screens.
     *
     * @param primaryStage the primary application window
     */
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("GoNature - Server Connect");

        VBox ipLayout = new VBox(12);
        ipLayout.setPadding(new Insets(30));

        /** Text field used to enter the server IP address. */
        TextField ipInput = new TextField("localhost");

        /** Button used to connect to the server. */
        Button connectBtn = new Button("Connect to Server");

        /** Label used to display connection errors. */
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        ipLayout.getChildren().addAll(
                new Label("Server IP:"),
                ipInput,
                connectBtn,
                errorLabel
        );

        Scene ipScene = new Scene(ipLayout, 300, 200);

        VBox authLayout = new VBox(12);
        authLayout.setPadding(new Insets(30));

        /** Text field for entering the visitor ID. */
        TextField idInput = new TextField();
        idInput.setPromptText("Visitor ID");

        /** Password field for entering the user's password. */
        PasswordField passInput = new PasswordField();
        passInput.setPromptText("Password");

        /** Text field used to enter the full name during registration. */
        TextField nameInput = new TextField();
        nameInput.setPromptText("Full Name (Only for New Registration)");

        /** Check box indicating guide registration. */
        CheckBox guideCheck = new CheckBox("Register as an Organized Group Guide");
        guideCheck.setStyle("-fx-font-weight: bold;");

        /** Button used for user login. */
        Button loginBtn = new Button("Log In");
        loginBtn.setStyle("-fx-font-weight: bold;");

        /** Button used for new user registration. */
        Button regBtn = new Button("New Visitor (Register)");
        regBtn.setStyle("-fx-background-color: #d4edda; -fx-font-weight: bold;");

        HBox buttonsBox = new HBox(10, loginBtn, regBtn);

        /** Label used to display login and registration status messages. */
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        authLayout.getChildren().addAll(
                new Label("Welcome! Please Log In or Register:"),
                idInput,
                passInput,
                nameInput,
                guideCheck,
                buttonsBox,
                statusLabel
        );

        Scene authScene = new Scene(authLayout, 350, 320);

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

        loginBtn.setOnAction(e -> {

            String id = idInput.getText().trim();
            String pass = passInput.getText().trim();

            if (id.isEmpty() || pass.isEmpty()) {
                statusLabel.setText("ID and Password required!");
                return;
            }

            if (!id.matches("\\d{9}")) {
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

                if ("LOGIN_SUCCESS_GUIDE".equals(res) ||
                    "LOGIN_SUCCESS_REGULAR".equals(res)) {

                    ClientDashboard.loggedInVisitorId = id;

                    String fullName = (String) resMsg.getData();

                    ClientDashboard.loggedInName =
                            (fullName != null && !fullName.equals("Unknown"))
                                    ? fullName
                                    : id;

                    ClientDashboard.isAccountGuide =
                            "LOGIN_SUCCESS_GUIDE".equals(res);

                    primaryStage.close();
                    new ClientDashboard().start(new Stage());

                } else if ("WRONG_PASSWORD".equals(res)) {

                    statusLabel.setText("Error: Wrong Password!");

                } else {

                    statusLabel.setText("User not found! Click 'New Visitor'.");
                }

            } catch (Exception ex) {
                statusLabel.setText("Server connection lost.");
            }
        });

        regBtn.setOnAction(e -> {

            String id = idInput.getText().trim();
            String pass = passInput.getText().trim();
            String fullName = nameInput.getText().trim();
            boolean isGuide = guideCheck.isSelected();

            if (id.isEmpty() || pass.isEmpty()) {
                statusLabel.setText("ID and Password required!");
                return;
            }

            if (fullName.isEmpty()) {
                statusLabel.setText("Full Name is required for registration!");
                return;
            }

            if (!id.matches("\\d{9}")) {
                statusLabel.setText("ID is invalid! Must be exactly 9 digits.");
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

                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText(
                            "Registration successful! Now click 'Log In'.");

                } else if ("USER_ALREADY_EXISTS".equals(res)) {

                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText(
                            "ID already registered! Please log in.");
                }

            } catch (Exception ex) {
                statusLabel.setText("Server connection lost.");
            }
        });

        primaryStage.setScene(ipScene);
        primaryStage.show();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}