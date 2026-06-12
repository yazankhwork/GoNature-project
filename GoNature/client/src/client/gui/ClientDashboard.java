package client.gui;

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
import client.ClientMain;
import common.Booking;
import common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Main dashboard screen of the GoNature client application.
 *
 * Allows visitors to create, update, cancel, and manage bookings.
 *
 * @author Bolos Saad
 */
public class ClientDashboard extends Application {


    public static String loggedInVisitorId = "";
    public static String loggedInName = ""; // שומר את השם המלא כדי להציג אותו בכותרת
    public static boolean isAccountGuide = false;

    private TableView<Booking> table = new TableView<>();
    private ObservableList<Booking> dataList = FXCollections.observableArrayList();
    private Label responseLabel = new Label("Ready");

    private ComboBox<String> parkCombo = new ComboBox<>();
    private DatePicker datePicker = new DatePicker(LocalDate.now());
    private TextField timeInput = new TextField("10:00");
    private TextField visitorsInput = new TextField("1");
    private int selectedBookingId = -1;

    private Label liveCapacityLabel = new Label("Select park, date, and time, then click 'Select' to check availability.");
    
    private CheckBox chkIsGuide = new CheckBox("Order as Guide");

    /**
     * Initializes and displays the dashboard window.
     *
     * @param primaryStage the primary JavaFX stage
     */
    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GoNature - Dashboard");

        // שינוי הכותרת כך שתציג את השם המלא במקום ה-ID
        Label welcomeLabel = new Label("Welcome, " + loggedInName);
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: blue;");

        if (isAccountGuide) {
            chkIsGuide.setVisible(true);
            chkIsGuide.setSelected(true);
            chkIsGuide.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-font-size: 14px;");
        } else {
            chkIsGuide.setVisible(false);
            chkIsGuide.setSelected(false);
        }

        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");
        
        HBox rightAlign = new HBox(btnLogout);
        rightAlign.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightAlign, Priority.ALWAYS);
        
        HBox topBar = new HBox(20, welcomeLabel, chkIsGuide, rightAlign);
        topBar.setAlignment(Pos.CENTER_LEFT);

        parkCombo.getItems().addAll("Carmel Park", "Jordan Park", "Banias Park", "Safari Zoo", "Ramon Crater", "Hula Valley");
        parkCombo.setValue("Carmel Park");

        TableColumn<Booking, Integer> idCol = new TableColumn<>("Order ID"); idCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        TableColumn<Booking, String> parkCol = new TableColumn<>("Park"); parkCol.setCellValueFactory(new PropertyValueFactory<>("parkName"));
        TableColumn<Booking, LocalDate> dateCol = new TableColumn<>("Date"); dateCol.setCellValueFactory(new PropertyValueFactory<>("visitDate"));
        TableColumn<Booking, LocalTime> timeCol = new TableColumn<>("Time"); timeCol.setCellValueFactory(new PropertyValueFactory<>("visitTime"));
        TableColumn<Booking, Integer> visCol = new TableColumn<>("Visitors"); visCol.setCellValueFactory(new PropertyValueFactory<>("visitorsCount"));
        TableColumn<Booking, String> statusCol = new TableColumn<>("Status"); statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<Booking, Integer> priceCol = new TableColumn<>("Price Paid"); priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        TableColumn<Booking, String> typeCol = new TableColumn<>("Booking Type"); typeCol.setCellValueFactory(new PropertyValueFactory<>("visitorType"));

        table.getColumns().addAll(idCol, parkCol, dateCol, timeCol, visCol, statusCol, priceCol, typeCol);
        table.setItems(dataList);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSelection) -> {
            if (newSelection != null) {
                selectedBookingId = newSelection.getBookingId(); parkCombo.setValue(newSelection.getParkName());
                datePicker.setValue(newSelection.getVisitDate()); timeInput.setText(newSelection.getVisitTime().toString());
                visitorsInput.setText(String.valueOf(newSelection.getVisitorsCount()));
                
                if (isAccountGuide) {
                    chkIsGuide.setSelected("Guide".equals(newSelection.getVisitorType()));
                }
                checkLiveCapacity(); 
            }
        });

        Label openingHoursLabel = new Label("Note: Park Opening Hours are 08:00 to 18:00");
        openingHoursLabel.setStyle("-fx-text-fill: #d35400; -fx-font-weight: bold; -fx-font-size: 13px;");

        liveCapacityLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14px;");

        GridPane inputGrid = new GridPane(); inputGrid.setHgap(10); inputGrid.setVgap(10);
        inputGrid.add(new Label("Park:"), 0, 0); inputGrid.add(parkCombo, 1, 0);
        inputGrid.add(new Label("Date:"), 2, 0); inputGrid.add(datePicker, 3, 0);
        inputGrid.add(new Label("Time:"), 0, 1); inputGrid.add(timeInput, 1, 1);
        inputGrid.add(new Label("Visitors (1-15):"), 2, 1); inputGrid.add(visitorsInput, 3, 1);

        Button btnSelect = new Button("Select"); 
        btnSelect.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button btnAdd = new Button("Add New Booking");
        Button btnUpdate = new Button("Update Selected");
        Button btnCancel = new Button("Cancel Selected"); btnCancel.setStyle("-fx-background-color: #ffcccc; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(15, btnSelect, btnAdd, btnUpdate, btnCancel); buttonBox.setPadding(new Insets(10, 0, 10, 0));

        btnSelect.setOnAction(e -> {
            checkLiveCapacity();
            responseLabel.setText("Availability status refreshed.");
        });

        btnLogout.setOnAction(e -> {
            loggedInVisitorId = "";
            loggedInName = "";
            primaryStage.close();
            try {
                new ClientMain().start(new Stage()); 
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        btnAdd.setOnAction(e -> {
            if (datePicker.getValue().isBefore(LocalDate.now())) {
                new Alert(Alert.AlertType.ERROR, "You cannot book an order for a past date!", ButtonType.OK).showAndWait();
                return;
            }

            LocalTime parsedTime;
            try { parsedTime = LocalTime.parse(timeInput.getText()); } 
            catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Invalid time format! Use HH:mm").showAndWait(); return; }

            if (parsedTime.isBefore(LocalTime.of(8, 0)) || parsedTime.isAfter(LocalTime.of(18, 0))) {
                new Alert(Alert.AlertType.ERROR, "The park is only open between 08:00 and 18:00!").showAndWait(); return;
            }

            int visitors = 0;
            try { visitors = Integer.parseInt(visitorsInput.getText()); } 
            catch (NumberFormatException ex) { responseLabel.setText("Visitors must be a valid number!"); return; }
            
            if (visitors < 1 || visitors > 15) {
                new Alert(Alert.AlertType.ERROR, "Number of visitors must be between 1 and 15!", ButtonType.OK).showAndWait();
                return;
            }
            
            Booking b = new Booking(0, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(), parsedTime, visitors, "Pending");
            
            if (isAccountGuide && chkIsGuide.isSelected()) {
                b.setVisitorType("Guide");
            } else {
                b.setVisitorType("Regular Visitor");
            }

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
                    } else {
                        responseLabel.setText("Payment cancelled.");
                    }
                } else {
                    String serverSuggestion = response.getData() != null ? response.getData().toString() : "Park is full.";
                    Alert alertWL = new Alert(Alert.AlertType.CONFIRMATION, serverSuggestion + "\n\nWould you like to join the Waiting List for this hour?", ButtonType.YES, ButtonType.NO);
                    alertWL.showAndWait();
                    if (alertWL.getResult() == ButtonType.YES) {
                        b.setStatus("Waiting List"); 
                        sendCommandToServer("ADD_DATA", b); 
                        loadDataFromServer();
                        checkLiveCapacity();
                    }
                }
            } catch (Exception ex) { responseLabel.setText("Server connection error."); }
        });

        btnUpdate.setOnAction(e -> {
            Booking selectedBooking = table.getSelectionModel().getSelectedItem();
            
            if (selectedBooking == null) { 
                new Alert(Alert.AlertType.WARNING, "Please select a row from the table first before clicking Update!", ButtonType.OK).showAndWait();
                responseLabel.setText("Select a row first!"); 
                return; 
            }

            selectedBookingId = selectedBooking.getBookingId();

            if ("Waiting List".equals(selectedBooking.getStatus())) {
                int emptyTickets = getLiveEmptyTickets(selectedBooking.getParkName(), selectedBooking.getVisitDate(), selectedBooking.getVisitTime());
                if (emptyTickets <= 0) {
                    new Alert(Alert.AlertType.INFORMATION, "Still no available spots for this time slot. Please check back later.").showAndWait();
                    return;
                }
                
                int spotsToTake = Math.min(selectedBooking.getVisitorsCount(), emptyTickets);
                int remaining = selectedBooking.getVisitorsCount() - spotsToTake;
                
                String confirmMsg = "There are " + emptyTickets + " spots available.\n" +
                                   "Would you like to book " + spotsToTake + " tickets now?\n";
                if (remaining > 0) {
                    confirmMsg += "The remaining " + remaining + " tickets will stay on the Waiting List.";
                } else {
                    confirmMsg += "This will fully clear your waiting list entry.";
                }
                
                Alert alertClaim = new Alert(Alert.AlertType.CONFIRMATION, confirmMsg, ButtonType.YES, ButtonType.NO);
                alertClaim.showAndWait();
                if (alertClaim.getResult() == ButtonType.YES) {
                    ArrayList<Object> claimData = new ArrayList<>();
                    claimData.add(selectedBooking.getBookingId());
                    claimData.add(spotsToTake);
                    sendCommandToServer("CLAIM_WAITING_SPOTS", claimData);
                    loadDataFromServer();
                    checkLiveCapacity();
                }
                return;
            }

            if (datePicker.getValue().isBefore(LocalDate.now())) {
                new Alert(Alert.AlertType.ERROR, "You cannot update an order to a past date!", ButtonType.OK).showAndWait();
                return;
            }

            LocalTime parsedTime;
            try { parsedTime = LocalTime.parse(timeInput.getText()); } 
            catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Invalid time format!").showAndWait(); return; }

            int visitors;
            try { visitors = Integer.parseInt(visitorsInput.getText()); } 
            catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Invalid visitors number!").showAndWait(); return; }
            
            if (visitors < 1 || visitors > 15) {
                new Alert(Alert.AlertType.ERROR, "Number of visitors must be between 1 and 15!", ButtonType.OK).showAndWait();
                return;
            }

            int oldVisitors = selectedBooking.getVisitorsCount();
            int diff = visitors - oldVisitors;

            if (diff > 0) {
                int additionalPrice = diff * 30;
                Alert alertPay = new Alert(Alert.AlertType.CONFIRMATION, 
                    "You are adding " + diff + " more visitor(s).\nAdditional payment required: " + additionalPrice + " ILS.\nProceed to payment?", 
                    ButtonType.YES, ButtonType.NO);
                alertPay.showAndWait();
                if (alertPay.getResult() != ButtonType.YES) {
                    responseLabel.setText("Update cancelled.");
                    return;
                }
            } else if (diff < 0) {
                int refundAmount = Math.abs(diff) * 30;
                Alert alertRefund = new Alert(Alert.AlertType.CONFIRMATION, 
                    "You are removing " + Math.abs(diff) + " visitor(s).\nYou will receive a refund of: " + refundAmount + " ILS.\nProceed with update?", 
                    ButtonType.YES, ButtonType.NO);
                alertRefund.showAndWait();
                if (alertRefund.getResult() != ButtonType.YES) {
                    responseLabel.setText("Update cancelled.");
                    return;
                }
            }

            Booking b = new Booking(selectedBookingId, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(), parsedTime, visitors, "Pending");
            
            if (isAccountGuide && chkIsGuide.isSelected()) {
                b.setVisitorType("Guide");
            } else {
                b.setVisitorType("Regular Visitor");
            }
            
            sendCommandToServer("UPDATE_DATA", b); 
            loadDataFromServer();
            checkLiveCapacity();
        });

        btnCancel.setOnAction(e -> {
            Booking selectedBooking = table.getSelectionModel().getSelectedItem();
            if (selectedBooking == null) { 
                new Alert(Alert.AlertType.WARNING, "Please select a row from the table first before clicking Cancel!", ButtonType.OK).showAndWait();
                responseLabel.setText("Select a row first!"); 
                return; 
            }
            
            selectedBookingId = selectedBooking.getBookingId();
            ArrayList<Object> deleteData = new ArrayList<>(); deleteData.add(selectedBookingId); deleteData.add(loggedInVisitorId); 
            sendCommandToServer("CANCEL_DATA", deleteData); 
            loadDataFromServer(); 
            selectedBookingId = -1;
            checkLiveCapacity();
        });

        VBox layout = new VBox(15, topBar, table, openingHoursLabel, liveCapacityLabel, inputGrid, buttonBox, responseLabel);
        layout.setPadding(new Insets(20));
        primaryStage.setScene(new Scene(layout, 750, 550)); primaryStage.show();
        
        loadDataFromServer();
        checkLiveCapacity(); 
        checkTomorrowBookings(); 
    }
    /**
     * Checks whether the currently logged-in visitor has
     * a booking scheduled for tomorrow and displays a
     * reminder notification if one exists.
     */
    private void checkTomorrowBookings() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        for (Booking b : dataList) {
            if (b.getVisitDate().equals(tomorrow) && !"Cancelled".equals(b.getStatus())) {
                new Alert(Alert.AlertType.INFORMATION, 
                    "Reminder: You have an upcoming order tomorrow (" + tomorrow + ") at " + b.getParkName() + " scheduled for " + b.getVisitTime() + "!", 
                    ButtonType.OK).showAndWait();
                break; 
            }
        }
    }
    /**
     * Retrieves the number of available spots for a specific
     * park, date, and time from the server.
     *
     * @param park the name of the park
     * @param date the requested visit date
     * @param time the requested visit time
     * @return the number of available spots, or 0 if the
     *         request fails or no information is available
     */
    private int getLiveEmptyTickets(String park, LocalDate date, LocalTime time) {
        try (Socket socket = new Socket(ClientConnectionScreen.serverIP, 5555); 
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); 
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(new Message("GET_AVAILABLE_SPOTS", new Booking(0, "", park, date, time, 0, "Pending")));
            Message response = (Message) in.readObject();
            if ("AVAILABLE_SPOTS_RESPONSE".equals(response.getCommand())) {
                return (int) response.getData();
            }
        } catch (Exception ex) { }
        return 0;
    }
    /**
    *
    */
    private void checkLiveCapacity() {
        try {
            if (datePicker.getValue().isBefore(LocalDate.now())) {
                liveCapacityLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-font-size: 14px;");
                liveCapacityLabel.setText("⚠️ Cannot book past dates!");
                return;
            }

            LocalTime time = LocalTime.parse(timeInput.getText());
            if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(18, 0))) {
                liveCapacityLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-font-size: 14px;");
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
                    
                    if (emptyTickets > 0) {
                        liveCapacityLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
                        liveCapacityLabel.setText("✓ " + emptyTickets + " empty spots available! You can proceed with a regular booking.");
                    } else {
                        liveCapacityLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-font-size: 14px;");
                        liveCapacityLabel.setText("⚠️ PARK IS FULL! Hitting 'Add New Booking' will place you on the WAITING LIST.");
                    }
                }
            }
        } catch (Exception ex) { }
    }
    /**
    *
    */
    private void loadDataFromServer() {
        try (Socket socket = new Socket(ClientConnectionScreen.serverIP, 5555); ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
            output.writeObject(new Message("LOAD_DATA", loggedInVisitorId));
            Message response = (Message) input.readObject();
            if ("SUCCESS".equals(response.getCommand())) {
                @SuppressWarnings("unchecked")
                ArrayList<Booking> list = (ArrayList<Booking>) response.getData(); 
                dataList.setAll(list);
            }
        } catch (Exception ex) { responseLabel.setText("Connection Error."); }
    }
    /**
    *
    */
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