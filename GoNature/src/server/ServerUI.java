package server;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Graphical user interface for controlling the GoNature server.
 * <p>
 * This screen allows administrators to:
 * <ul>
 *     <li>Start the server.</li>
 *     <li>Stop the server.</li>
 *     <li>Monitor the current server status.</li>
 * </ul>
 *
 * The interface communicates with the {@link GoNatureServer}
 * class to manage server operations.
 *
 * @author Bolos Saad
 */
public class ServerUI extends Application {

    /**
     * Initializes and displays the server control window.
     *
     * @param primaryStage the primary JavaFX stage
     */
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("GoNature - Server Control");

        /**
         * Displays the current server status.
         */
        Label statusLabel =
                new Label("Status: SERVER IS OFF");

        statusLabel.setStyle(
                "-fx-text-fill: red; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 14px;"
        );

        /**
         * Button used to start the server.
         */
        Button startBtn =
                new Button("Start Server");

        /**
         * Button used to stop the server.
         */
        Button stopBtn =
                new Button("Stop Server");

        stopBtn.setDisable(true);

        startBtn.setOnAction(e -> {

            new Thread(
                    () -> GoNatureServer.startServer()
            ).start();

            statusLabel.setText(
                    "Status: SERVER IS RUNNING"
            );

            statusLabel.setStyle(
                    "-fx-text-fill: green; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 14px;"
            );

            startBtn.setDisable(true);
            stopBtn.setDisable(false);
        });

        stopBtn.setOnAction(e -> {

            GoNatureServer.stopServer();

            statusLabel.setText(
                    "Status: SERVER IS OFF"
            );

            statusLabel.setStyle(
                    "-fx-text-fill: red; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 14px;"
            );

            startBtn.setDisable(false);
            stopBtn.setDisable(true);
        });

        /**
         * Container holding the control buttons.
         */
        HBox buttons =
                new HBox(
                        10,
                        startBtn,
                        stopBtn
                );

        /**
         * Main application layout.
         */
        VBox layout =
                new VBox(
                        20,
                        statusLabel,
                        buttons
                );

        layout.setPadding(
                new Insets(30)
        );

        primaryStage.setScene(
                new Scene(
                        layout,
                        300,
                        150
                )
        );

        primaryStage.show();
    }

    /**
     * Launches the JavaFX server control application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}