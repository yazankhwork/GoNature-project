package client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

    // פקד תצוגה לכמות הכרטיסים הפנויים בזמן אמת
    private Label liveCapacityLabel = new Label("Select park, date, and time, then click 'Select' to check availability.");

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GoNature - Dashboard");

        // --- Top Bar: Welcome Label & Logout Button ---
        Label welcomeLabel = new Label("Welcome, Visitor: " + loggedInVisitorId);
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: blue;");

        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");
        
        HBox rightAlign = new HBox(btnLogout);
        rightAlign.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightAlign, Priority.ALWAYS);
        
        HBox topBar = new HBox(welcomeLabel, rightAlign);
        topBar.setAlignment(Pos.CENTER_LEFT);

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
                checkLiveCapacity(); 
            }
        });

        Label openingHoursLabel = new Label("Note: Park Opening Hours are 08:00 to 18:00");
        openingHoursLabel.setStyle("-fx-text-fill: #d35400; -fx-font-weight: bold; -fx-font-size: 13px;");

        liveCapacityLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 13px;");

        GridPane inputGrid = new GridPane(); inputGrid.setHgap(10); inputGrid.setVgap(10);
        inputGrid.add(new Label("Park:"), 0, 0); inputGrid.add(parkCombo, 1, 0);
        inputGrid.add(new Label("Date:"), 2, 0); inputGrid.add(datePicker, 3, 0);
        inputGrid.add(new Label("Time:"), 0, 1); inputGrid.add(timeInput, 1, 1);
        inputGrid.add(new Label("Visitors (0-15):"), 2, 1); inputGrid.add(visitorsInput, 3, 1);

        // כפתורים
        Button btnSelect = new Button("Select"); // הכפתור החדש שביקשת
        btnSelect.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button btnAdd = new Button("Add New Booking");
        Button btnUpdate = new Button("Update Selected");
        Button btnCancel = new Button("Cancel Selected"); btnCancel.setStyle("-fx-background-color: #ffcccc; -fx-font-weight: bold;");

        // הוספת כפתור ה-Select לפני ה-Add בשורת הכפתורים
        HBox buttonBox = new HBox(15, btnSelect, btnAdd, btnUpdate, btnCancel); buttonBox.setPadding(new Insets(10, 0, 10, 0));

        // --- Action Events ---
        
        // כפתור ה-Select מרענן את ההודעה ומציג כמה כרטיסים פנויים נותרו לפני הלחיצה על Add
        btnSelect.setOnAction(e -> {
            checkLiveCapacity();
            responseLabel.setText("Availability refreshed for selected time.");
        });

        btnLogout.setOnAction(e -> {
            loggedInVisitorId = "";
            primaryStage.close();
            try {
                new ClientMain().start(new Stage()); 
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        btnAdd.setOnAction(e -> {
            LocalTime parsedTime;
            try { parsedTime = LocalTime.parse(timeInput.getText()); } 
            catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Invalid time format! Use HH:mm").showAndWait(); return; }

            if (parsedTime.isBefore(LocalTime.of(8, 0)) || parsedTime.isAfter(LocalTime.of(18, 0))) {
                new Alert(Alert.AlertType.ERROR, "The park is only open between 08:00 and 18:00!").showAndWait(); return;
            }

            int visitors = 0;
            try { visitors = Integer.parseInt(visitorsInput.getText()); } 
            catch (NumberFormatException ex) { responseLabel.setText("Visitors must be a valid number!"); return; }
            
            if (visitors > 15) {
                new Alert(Alert.AlertType.ERROR, "You cannot book more than 15 visitors in a single order!", ButtonType.OK).showAndWait(); return;
            }
            
            Booking b = new Booking(0, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(), parsedTime, visitors, "Pending");
            
            try (Socket socket = new Socket(ClientConnectionScreen.serverIP, 5555); 
                 ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream()); 
                 ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                
                output.writeObject(new Message("CHECK_AVAILABILITY", b));
                Message response = (Message) input.readObject();
                
                if ("OK".equals(response.getCommand())) {
                    int price = visitors * 30; 
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Total price is " + price + " ILS.\nProceed to payment?", ButtonType.YES, ButtonType.NO);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.YES) {
                        sendCommandToServer("ADD_DATA", b); 
                        loadDataFromServer();
                        checkLiveCapacity(); 
                    }
                } else if ("SUGGESTION".equals(response.getCommand())) {
                    new Alert(Alert.AlertType.INFORMATION, response.getData().toString() + "\n\nPlease update your time.", ButtonType.OK).showAndWait();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Park is full!", ButtonType.OK).showAndWait();
                }
            } catch (Exception ex) { responseLabel.setText("Server connection error."); }
        });

        btnUpdate.setOnAction(e -> {
            if (selectedBookingId == -1) { responseLabel.setText("Select a row first!"); return; }
            LocalTime parsedTime;
            try { parsedTime = LocalTime.parse(timeInput.getText()); } 
            catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Invalid time format!").showAndWait(); return; }

            Booking b = new Booking(selectedBookingId, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(), parsedTime, Integer.parseInt(visitorsInput.getText()), "Pending");
            sendCommandToServer("UPDATE_DATA", b); 
            loadDataFromServer();
            checkLiveCapacity();
        });

        btnCancel.setOnAction(e -> {
            if (selectedBookingId == -1) { responseLabel.setText("Select a row first!"); return; }
            ArrayList<Object> deleteData = new ArrayList<>(); deleteData.add(selectedBookingId); deleteData.add(loggedInVisitorId); 
            sendCommandToServer("CANCEL_DATA", deleteData); 
            loadDataFromServer(); 
            selectedBookingId = -1;
            checkLiveCapacity();
        });

        VBox layout = new VBox(15, topBar, table, openingHoursLabel, liveCapacityLabel, inputGrid, buttonBox, responseLabel);
        layout.setPadding(new Insets(20));
        primaryStage.setScene(new Scene(layout, 650, 550)); primaryStage.show();
        
        loadDataFromServer();
        checkLiveCapacity(); 
    }

    private void checkLiveCapacity() {
        try {
            LocalTime time = LocalTime.parse(timeInput.getText());
            if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(18, 0))) {
                liveCapacityLabel.setText("Park Status: Closed");
                return;
            }
            Booking b = new Booking(0, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(), time, 0, "Pending");
            
            try (Socket socket = new Socket(ClientConnectionScreen.serverIP, 5555); 
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); 
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                out.writeObject(new Message("GET_AVAILABLE_SPOTS", b));
                Message response = (Message) in.readObject();
                
                if ("AVAILABLE_SPOTS_RESPONSE".equals(response.getCommand())) {
                    int emptyTickets = (int) response.getData();
                    int currentPeople = 150 - emptyTickets;
                    int maxBrowse = Math.min(15, emptyTickets);
                    
                    liveCapacityLabel.setText("Current People in Park: " + currentPeople + " | Empty Tickets: " + emptyTickets + " (You can browse/book: 0 to " + maxBrowse + ")");
                }
            }
        } catch (Exception ex) {
            // התעלמות משגיאות פורמט זמניות בזמן הקלדה
        }
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