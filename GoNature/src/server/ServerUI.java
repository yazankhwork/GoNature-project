package server;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ServerUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GoNature - Server Control");

        Label statusLabel = new Label("Status: SERVER IS OFF");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");

        Button startBtn = new Button("Start Server");
        Button stopBtn = new Button("Stop Server");
        stopBtn.setDisable(true); // Disabled initially

        startBtn.setOnAction(e -> {
            new Thread(() -> GoNatureServer.startServer()).start();
            statusLabel.setText("Status: SERVER IS RUNNING");
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 14px;");
            startBtn.setDisable(true);
            stopBtn.setDisable(false);
        });

        stopBtn.setOnAction(e -> {
            GoNatureServer.stopServer();
            statusLabel.setText("Status: SERVER IS OFF");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
            startBtn.setDisable(false);
            stopBtn.setDisable(true);
        });

        HBox buttons = new HBox(10, startBtn, stopBtn);
        VBox layout = new VBox(20, statusLabel, buttons);
        layout.setPadding(new Insets(30));

        primaryStage.setScene(new Scene(layout, 300, 150));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}