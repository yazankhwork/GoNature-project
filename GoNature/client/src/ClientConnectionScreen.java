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

/**
 * JavaFX client connection screen for the GoNature system.
 * <p>
 * This screen allows a user to:
 * <ul>
 *     <li>Connect to the server using an IP address.</li>
 *     <li>Log in with an existing account.</li>
 *     <li>Register as a new visitor.</li>
 * </ul>
 * After a successful login, the client dashboard is opened.
 *
 * @author Bolos Saad
 */
public class ClientConnectionScreen extends Application {

    /**
     * IP address of the server currently used by the client.
     */
    public static String serverIP = "localhost";

    /**
     * Starts the JavaFX application and displays the connection screen.
     *
     * @param primaryStage the primary application window
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GoNature - Server Connect");

        // --- מסך 1: חיבור לשרת ---
        VBox ipLayout = new VBox(12);
        ipLayout.setPadding(new Insets(30));
        TextField ipInput = new TextField("localhost");
        Button connectBtn = new Button("Connect to Server");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        ipLayout.getChildren().addAll(new Label("Server IP:"), ipInput, connectBtn, errorLabel);
        Scene ipScene = new Scene(ipLayout, 300, 200);

        // --- מסך 2: התחברות לחשבון / הרשמה ---
        VBox authLayout = new VBox(12);
        authLayout.setPadding(new Insets(30));
        TextField idInput = new TextField();
        idInput.setPromptText("Visitor ID");

        PasswordField passInput = new PasswordField();
        passInput.setPromptText("Password");

        CheckBox guideCheck = new CheckBox("I am an Organized Group Guide");

        Button loginBtn = new Button("Log In");
        loginBtn.setStyle("-fx-font-weight: bold;");

        Button regBtn = new Button("New Visitor (Register)");
        regBtn.setStyle("-fx-background-color: #d4edda; -fx-font-weight: bold;");

        HBox buttonsBox = new HBox(10, loginBtn, regBtn);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        authLayout.getChildren().addAll(
                new Label("Welcome! Please Log In or Register:"),
                idInput,
                passInput,
                guideCheck,
                buttonsBox,
                statusLabel
        );

        Scene authScene = new Scene(authLayout, 350, 280);

        // לוגיקת חיבור
        connectBtn.setOnAction(e -> {
            String ip = ipInput.getText().trim();
            if (ip.isEmpty()) {
                errorLabel.setText("Please enter an IP address.");
                return;
            }

            try (Socket socket = new Socket(ip, 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject(new Message("CONNECT", null));
                Message res = (Message) in.readObject();

                if ("CONNECTED".equals(res.getCommand())) {
                    serverIP = ip;
                    primaryStage.setTitle("GoNature - Secure Login");
                    primaryStage.setScene(authScene);
                }
            } catch (Exception ex) {
                errorLabel.setText("Connection Failed! Is server on?");
            }
        });

        // לוגיקת כניסה למערכת
        loginBtn.setOnAction(e -> {
            String id = idInput.getText().trim();
            String pass = passInput.getText().trim();

            if (id.isEmpty() || pass.isEmpty()) {
                statusLabel.setText("ID and Password required!");
                return;
            }

            try (Socket socket = new Socket(serverIP, 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                ArrayList<String> loginData = new ArrayList<>();
                loginData.add(id);
                loginData.add(pass);

                out.writeObject(new Message("LOGIN", loginData));
                Message res = (Message) in.readObject();

                if ("LOGIN_SUCCESS".equals(res.getCommand())) {
                    ClientDashboard.loggedInVisitorId = id;
                    primaryStage.close();

                    // פותח את ה-Dashboard החדש!
                    new ClientDashboard().start(new Stage());

                } else if ("WRONG_PASSWORD".equals(res.getCommand())) {
                    statusLabel.setText("Error: Wrong Password!");
                } else {
                    statusLabel.setText("User not found! Click 'New Visitor'.");
                }

            } catch (Exception ex) {
                statusLabel.setText("Server connection lost.");
            }
        });

        // לוגיקת הרשמה
        regBtn.setOnAction(e -> {
            String id = idInput.getText().trim();
            String pass = passInput.getText().trim();
            boolean isGuide = guideCheck.isSelected();

            if (id.isEmpty() || pass.isEmpty()) {
                statusLabel.setText("ID and Password required!");
                return;
            }

            try (Socket socket = new Socket(serverIP, 5555);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                ArrayList<Object> regData = new ArrayList<>();
                regData.add(id);
                regData.add(pass);
                regData.add(isGuide);

                out.writeObject(new Message("REGISTER", regData));
                Message res = (Message) in.readObject();

                if ("REGISTER_SUCCESS".equals(res.getCommand())) {
                    statusLabel.setStyle("-fx-text-fill: green;");
                    statusLabel.setText("Registration successful! Now click 'Log In'.");

                } else if ("USER_ALREADY_EXISTS".equals(res.getCommand())) {
                    statusLabel.setStyle("-fx-text-fill: red;");
                    statusLabel.setText("ID already registered! Please log in.");
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