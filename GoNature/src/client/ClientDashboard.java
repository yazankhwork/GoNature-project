package client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import common.Booking;
import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class ClientDashboard extends Application {

    public static String loggedInVisitorId = "";

    private TableView<Booking> table = new TableView<>();
    private ObservableList<Booking> dataList = FXCollections.observableArrayList();
    private Label responseLabel = new Label("Ready");

    private ComboBox<String> parkCombo = new ComboBox<>();
    private DatePicker datePicker = new DatePicker(LocalDate.now());
    private TextField timeInput = new TextField("10:00");
    private TextField visitorsInput = new TextField("1");
    private int selectedBookingId = -1;

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GoNature - Dashboard");

        Label welcomeLabel = new Label("Welcome, Visitor: " + loggedInVisitorId);
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: blue;");

        parkCombo.getItems().addAll("Carmel Park", "Jordan Park", "Banias Park");
        parkCombo.setValue("Carmel Park");

        TableColumn<Booking, Integer> idCol = new TableColumn<>("Order ID"); idCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        TableColumn<Booking, String> parkCol = new TableColumn<>("Park"); parkCol.setCellValueFactory(new PropertyValueFactory<>("parkName"));
        TableColumn<Booking, LocalDate> dateCol = new TableColumn<>("Date"); dateCol.setCellValueFactory(new PropertyValueFactory<>("visitDate"));
        TableColumn<Booking, LocalTime> timeCol = new TableColumn<>("Time"); timeCol.setCellValueFactory(new PropertyValueFactory<>("visitTime"));
        TableColumn<Booking, Integer> visCol = new TableColumn<>("Visitors"); visCol.setCellValueFactory(new PropertyValueFactory<>("visitorsCount"));
        TableColumn<Booking, String> statusCol = new TableColumn<>("Status"); statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, parkCol, dateCol, timeCol, visCol, statusCol);
        table.setItems(dataList);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSelection) -> {
            if (newSelection != null) {
                selectedBookingId = newSelection.getBookingId(); parkCombo.setValue(newSelection.getParkName());
                datePicker.setValue(newSelection.getVisitDate()); timeInput.setText(newSelection.getVisitTime().toString());
                visitorsInput.setText(String.valueOf(newSelection.getVisitorsCount()));
            }
        });

        GridPane inputGrid = new GridPane(); inputGrid.setHgap(10); inputGrid.setVgap(10);
        inputGrid.add(new Label("Park:"), 0, 0); inputGrid.add(parkCombo, 1, 0);
        inputGrid.add(new Label("Date:"), 2, 0); inputGrid.add(datePicker, 3, 0);
        inputGrid.add(new Label("Time:"), 0, 1); inputGrid.add(timeInput, 1, 1);
        inputGrid.add(new Label("Visitors:"), 2, 1); inputGrid.add(visitorsInput, 3, 1);

        Button btnAdd = new Button("Add New Booking");
        Button btnUpdate = new Button("Update Selected");
        Button btnCancel = new Button("Cancel Selected"); btnCancel.setStyle("-fx-background-color: #ffcccc; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(15, btnAdd, btnUpdate, btnCancel); buttonBox.setPadding(new Insets(10, 0, 10, 0));

        btnAdd.setOnAction(e -> {
            int visitors = 0;
            try { visitors = Integer.parseInt(visitorsInput.getText()); } 
            catch (NumberFormatException ex) { responseLabel.setText("Visitors must be a valid number!"); return; }
            
            // חסימה הרמטית להזמנה של מעל 15 אנשים!
            if (visitors > 15) {
                new Alert(Alert.AlertType.ERROR, "You cannot book more than 15 visitors in a single order!", ButtonType.OK).showAndWait();
                return;
            }
            
            int price = visitors * 30; 
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Total price is " + price + " ILS.\nProceed to payment?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();
            
            if (alert.getResult() == ButtonType.YES) {
                Booking b = new Booking(0, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(), LocalTime.parse(timeInput.getText()), visitors, "Pending");
                sendCommandToServer("ADD_DATA", b); loadDataFromServer(); 
            } else responseLabel.setText("Payment cancelled.");
        });

        btnUpdate.setOnAction(e -> {
            if (selectedBookingId == -1) { responseLabel.setText("Select a row first!"); return; }
            if ("Cancelled".equals(table.getSelectionModel().getSelectedItem().getStatus())) {
                new Alert(Alert.AlertType.WARNING, "Cannot update a cancelled booking!", ButtonType.OK).showAndWait(); return;
            }
            Booking b = new Booking(selectedBookingId, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(), LocalTime.parse(timeInput.getText()), Integer.parseInt(visitorsInput.getText()), "Pending");
            sendCommandToServer("UPDATE_DATA", b); loadDataFromServer(); 
        });

        btnCancel.setOnAction(e -> {
            if (selectedBookingId == -1) { responseLabel.setText("Select a row first!"); return; }
            if ("Cancelled".equals(table.getSelectionModel().getSelectedItem().getStatus())) {
                responseLabel.setText("Already cancelled."); return;
            }
            ArrayList<Object> deleteData = new ArrayList<>(); deleteData.add(selectedBookingId); deleteData.add(loggedInVisitorId); 
            sendCommandToServer("CANCEL_DATA", deleteData); loadDataFromServer(); selectedBookingId = -1; 
        });

        VBox layout = new VBox(15, welcomeLabel, table, inputGrid, buttonBox, responseLabel);
        layout.setPadding(new Insets(20));
        primaryStage.setScene(new Scene(layout, 650, 500)); primaryStage.show();
        
        loadDataFromServer();
    }

    private void loadDataFromServer() {
        try (Socket socket = new Socket(ClientConnectionScreen.serverIP, 5555); ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            output.writeObject(new Message("LOAD_DATA", loggedInVisitorId));
            Message response = (Message) input.readObject();
            if ("SUCCESS".equals(response.getCommand())) {
                @SuppressWarnings("unchecked")
                ArrayList<Booking> list = (ArrayList<Booking>) response.getData(); dataList.setAll(list);
            }
        } catch (Exception ex) { responseLabel.setText("Connection Error."); }
    }

    private void sendCommandToServer(String command, Object data) {
        try (Socket socket = new Socket(ClientConnectionScreen.serverIP, 5555); ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            output.writeObject(new Message(command, data));
            Message response = (Message) input.readObject();
            if ("LIMIT_REACHED".equals(response.getCommand()) || "CANCELLED_REFUND".equals(response.getCommand()) || "SUCCESS_PAID".equals(response.getCommand())) {
                new Alert(Alert.AlertType.INFORMATION, response.getData().toString(), ButtonType.OK).showAndWait();
            }
            responseLabel.setText("Action: " + response.getCommand());
        } catch (Exception ex) { responseLabel.setText("Connection Error."); }
    }
}