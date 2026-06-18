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
import common.Booking;
import common.Message;
import client.network.ClientSession;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class ClientDashboard extends Application {

	public static String loggedInVisitorId = "";
	public static String loggedInName = "";
	public static boolean isAccountGuide = false;
	public static boolean isSubscriberAccount = false;
	public static String subscriptionNumber = "";

	private TableView<Booking> table = new TableView<>();
	private ObservableList<Booking> dataList = FXCollections.observableArrayList();
	private Label responseLabel = new Label("Ready");

	private ComboBox<String> parkCombo = new ComboBox<>();
	private DatePicker datePicker = new DatePicker(LocalDate.now());
	private TextField timeInput = new TextField("10:00");
	private TextField visitorsInput = new TextField("1");
	private int selectedBookingId = -1;

	private Label liveCapacityLabel = new Label(
			"Select park, date, and time, then click 'Select' to check availability.");
	private CheckBox chkIsGuide = new CheckBox("Order as Guide");

	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("GoNature - Dashboard");

		Label welcomeLabel = new Label("Welcome, " + loggedInName);
		welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

		if (isAccountGuide) {
			chkIsGuide.setVisible(true);
			chkIsGuide.setSelected(true);
			chkIsGuide.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71; -fx-font-size: 14px;");
		} else {
			chkIsGuide.setVisible(false);
			chkIsGuide.setSelected(false);
		}

		Label visitorsLabel = new Label("Visitors (1-15):");
		visitorsInput.setEditable(true);
		visitorsInput.setStyle("-fx-border-color: #2ecc71; -fx-background-radius: 5px; -fx-border-radius: 5px;");

		Button btnShowPrices = new Button("View Pricing List");
		btnShowPrices.setStyle(
				"-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");

		Button btnLogout = new Button("Logout");
		btnLogout.setStyle(
				"-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");

		if (isSubscriberAccount) {
			welcomeLabel.setText("Welcome, " + loggedInName + " (Sub #" + subscriptionNumber + ")");
		}

		HBox rightAlign = new HBox(10, btnShowPrices, btnLogout);
		rightAlign.setAlignment(Pos.CENTER_RIGHT);
		HBox.setHgrow(rightAlign, Priority.ALWAYS);
		HBox topBar = new HBox(20, welcomeLabel, chkIsGuide, rightAlign);
		topBar.setAlignment(Pos.CENTER_LEFT);

		parkCombo.getItems().addAll("Carmel Park", "Jordan Park", "Banias Park", "Safari Zoo", "Ramon Crater",
				"Hula Valley");
		parkCombo.setValue("Carmel Park");

		TableColumn<Booking, Integer> idCol = new TableColumn<>("Booking ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
		TableColumn<Booking, String> parkCol = new TableColumn<>("Park");
		parkCol.setCellValueFactory(new PropertyValueFactory<>("parkName"));
		TableColumn<Booking, LocalDate> dateCol = new TableColumn<>("Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("visitDate"));
		TableColumn<Booking, LocalTime> timeCol = new TableColumn<>("Time");
		timeCol.setCellValueFactory(new PropertyValueFactory<>("visitTime"));
		TableColumn<Booking, Integer> visCol = new TableColumn<>("Visitors");
		visCol.setCellValueFactory(new PropertyValueFactory<>("visitorsCount"));
		TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
		statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
		TableColumn<Booking, Integer> priceCol = new TableColumn<>("Price Paid");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
		TableColumn<Booking, String> typeCol = new TableColumn<>("Booking Type");
		typeCol.setCellValueFactory(new PropertyValueFactory<>("visitorType"));

		table.getColumns().addAll(idCol, parkCol, dateCol, timeCol, visCol, statusCol, priceCol, typeCol);
		table.setItems(dataList);
		table.setStyle("-fx-selection-bar: #a9dfbf; -fx-background-color: white; -fx-border-color: #27ae60;");

		table.setRowFactory(tv -> new TableRow<Booking>() {
			@Override
			protected void updateItem(Booking item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty)
					setStyle("");
				else {
					if ("Confirmed".equals(item.getStatus()))
						setStyle("-fx-background-color: #d4edda;");
					else if ("Pending".equals(item.getStatus()))
						setStyle("-fx-background-color: #fff3cd;");
					else if ("Waiting List".equals(item.getStatus()))
						setStyle("-fx-background-color: #f8d7da;");
					else if ("Cancelled".equals(item.getStatus()))
						setStyle("-fx-background-color: #e2e3e5;");
					else
						setStyle("-fx-background-color: white;");
				}
			}
		});

		Label openingHoursLabel = new Label("Note: Park Opening Hours are 08:00 to 18:00");
		openingHoursLabel.setStyle("-fx-text-fill: #d35400; -fx-font-weight: bold; -fx-font-size: 13px;");
		liveCapacityLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14px;");

		GridPane inputGrid = new GridPane();
		inputGrid.setHgap(10);
		inputGrid.setVgap(10);
		inputGrid.add(new Label("Park:"), 0, 0);
		inputGrid.add(parkCombo, 1, 0);
		inputGrid.add(new Label("Date:"), 2, 0);
		inputGrid.add(datePicker, 3, 0);
		inputGrid.add(new Label("Time:"), 0, 1);
		inputGrid.add(timeInput, 1, 1);
		inputGrid.add(visitorsLabel, 2, 1);
		inputGrid.add(visitorsInput, 3, 1);

		Button btnSelect = new Button("Select");
		btnSelect.setStyle(
				"-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		Button btnAdd = new Button("Add New Booking");
		btnAdd.setStyle(
				"-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		Button btnUpdate = new Button("Update Selected");
		btnUpdate.setStyle(
				"-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		Button btnCancel = new Button("Cancel Selected");
		btnCancel.setStyle(
				"-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");

		HBox buttonBox = new HBox(15, btnSelect, btnAdd, btnUpdate, btnCancel);
		buttonBox.setPadding(new Insets(10, 0, 10, 0));

		btnShowPrices.setOnAction(e -> {
			Alert pricesAlert = new Alert(Alert.AlertType.INFORMATION);
			pricesAlert.setTitle("GoNature Pricing List");
			pricesAlert.setHeaderText("GoNature Ticket Pricing & Discount Rules\nBase Price per Ticket: 30 ILS");
			pricesAlert.setContentText("1. Regular Pre-booked: 15% discount from the full price.\n\n"
					+ "2. Regular Occasional (Walk-in): Full price (No discount).\n\n"
					+ "3. Group Pre-booked: 25% discount + Extra 12% off for prepayment. The Guide enters for FREE.\n\n"
					+ "4. Group Occasional (Walk-in): 10% discount from the full price. The Guide pays.\n\n"
					+ "5. Subscribers: Receive an additional 10% compound discount on top of any other discounts!");
			pricesAlert.showAndWait();
		});

		btnSelect.setOnAction(e -> {
			checkLiveCapacity();
			responseLabel.setText("Availability status refreshed.");
		});

		btnLogout.setOnAction(e -> {
			loggedInVisitorId = "";
			loggedInName = "";
			primaryStage.close();
			try {
				new ClientConnectionScreen().start(new Stage());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

		table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSelection) -> {
			if (newSelection != null) {
				selectedBookingId = newSelection.getBookingId();
				parkCombo.setValue(newSelection.getParkName());
				datePicker.setValue(newSelection.getVisitDate());
				timeInput.setText(newSelection.getVisitTime().toString());
				visitorsInput.setText(String.valueOf(newSelection.getVisitorsCount()));
				if (isAccountGuide)
					chkIsGuide.setSelected("Guide".equals(newSelection.getVisitorType()));
				checkLiveCapacity();
			}
		});

		btnAdd.setOnAction(e -> {
			if (datePicker.getValue().isBefore(LocalDate.now())) {
				new Alert(Alert.AlertType.ERROR, "You cannot book an order for a past date!").showAndWait();
				return;
			}
			LocalTime parsedTime;
			try {
				parsedTime = LocalTime.parse(timeInput.getText());
			} catch (Exception ex) {
				new Alert(Alert.AlertType.ERROR, "Invalid time format! Use HH:mm").showAndWait();
				return;
			}
			if (datePicker.getValue().isEqual(LocalDate.now()) && parsedTime.isBefore(LocalTime.now())) {
				new Alert(Alert.AlertType.ERROR, "You cannot book for a past time today!").showAndWait();
				return;
			}
			if (parsedTime.isBefore(LocalTime.of(8, 0)) || parsedTime.isAfter(LocalTime.of(18, 0))) {
				new Alert(Alert.AlertType.ERROR, "Park is only open 08:00 to 18:00!").showAndWait();
				return;
			}
			int visitors;
			try {
				visitors = Integer.parseInt(visitorsInput.getText());
			} catch (Exception ex) {
				responseLabel.setText("Visitors must be a number!");
				return;
			}
			if (visitors < 1 || visitors > 15) {
				new Alert(Alert.AlertType.ERROR, "Visitors must be 1 to 15!").showAndWait();
				return;
			}

			Booking b = new Booking(0, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(), parsedTime,
					visitors, "Pending");
			b.setVisitorType(isAccountGuide && chkIsGuide.isSelected() ? "Guide" : "Regular Visitor");
			int calculatedPrice = calculatePrice(visitors, b.getVisitorType(), isSubscriberAccount, true, true);
			b.setPrice(calculatedPrice);

			try {

				Message response = ClientSession.send(new Message("CHECK_AVAILABILITY", b));

				if ("OK".equals(response.getCommand())) {
					Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
							"Total price is " + calculatedPrice + " ILS (Discounts applied).\nProceed to payment?",
							ButtonType.YES, ButtonType.NO);
					alert.showAndWait();
					if (alert.getResult() == ButtonType.YES) {
						sendCommandToServer("ADD_DATA", b);
						loadDataFromServer();
						checkLiveCapacity();
					} else
						responseLabel.setText("Payment cancelled.");
				} else if ("PARTIAL_AVAILABILITY".equals(response.getCommand())) {
					int availableSpots = (int) response.getData();
					int waitlistSpots = visitors - availableSpots;
					ButtonType splitBtn = new ButtonType("Book " + availableSpots + " & Waitlist " + waitlistSpots);
					ButtonType allWaitlistBtn = new ButtonType("Waitlist All " + visitors);
					ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

					Alert splitAlert = new Alert(Alert.AlertType.CONFIRMATION,
							"Only " + availableSpots + " spots left.\nChoose an option:", splitBtn, allWaitlistBtn,
							cancelBtn);
					splitAlert.showAndWait();

					if (splitAlert.getResult() == splitBtn) {
						int splitPrice = calculatePrice(availableSpots, b.getVisitorType(), isSubscriberAccount, true,
								true);
						Alert payAlert = new Alert(Alert.AlertType.CONFIRMATION, "Price for " + availableSpots
								+ " spots is " + splitPrice + " ILS.\nProceed to payment?", ButtonType.YES,
								ButtonType.NO);
						payAlert.showAndWait();
						if (payAlert.getResult() == ButtonType.YES) {
							Booking confirmedB = new Booking(0, b.getVisitorId(), b.getParkName(), b.getVisitDate(),
									b.getVisitTime(), availableSpots, "Pending");
							confirmedB.setVisitorType(b.getVisitorType());
							confirmedB.setPrice(splitPrice);
							Booking waitlistB = new Booking(0, b.getVisitorId(), b.getParkName(), b.getVisitDate(),
									b.getVisitTime(), waitlistSpots, "Waiting List");
							waitlistB.setVisitorType(b.getVisitorType());
							waitlistB.setPrice(0);
							ArrayList<Booking> splitData = new ArrayList<>();
							splitData.add(confirmedB);
							splitData.add(waitlistB);
							sendCommandToServer("ADD_SPLIT_BOOKING", splitData);
							loadDataFromServer();
							checkLiveCapacity();
						} else
							responseLabel.setText("Payment cancelled.");
					} else if (splitAlert.getResult() == allWaitlistBtn) {
						b.setStatus("Waiting List");
						b.setPrice(0);
						sendCommandToServer("ADD_DATA", b);
						loadDataFromServer();
						checkLiveCapacity();
					}
				} else {
					String msg = response.getData() != null ? response.getData().toString() : "Park is full.";
					Alert alertWL = new Alert(Alert.AlertType.CONFIRMATION, msg + "\n\nJoin the Waiting List?",
							ButtonType.YES, ButtonType.NO);
					alertWL.showAndWait();
					if (alertWL.getResult() == ButtonType.YES) {
						b.setStatus("Waiting List");
						b.setPrice(0);
						sendCommandToServer("ADD_DATA", b);
						loadDataFromServer();
						checkLiveCapacity();
					}
				}
			} catch (Exception ex) {
				responseLabel.setText("Server connection error.");
			}
		});

		btnUpdate.setOnAction(e -> {
			Booking selectedBooking = table.getSelectionModel().getSelectedItem();
			if (selectedBooking == null) {
				new Alert(Alert.AlertType.WARNING, "Select a row first!").showAndWait();
				return;
			}
			selectedBookingId = selectedBooking.getBookingId();
			if ("Waiting List".equals(selectedBooking.getStatus())) {
				new Alert(Alert.AlertType.INFORMATION, "You are on the waiting list!").showAndWait();
				return;
			}
			if (datePicker.getValue().isBefore(LocalDate.now())) {
				new Alert(Alert.AlertType.ERROR, "Cannot update to a past date!").showAndWait();
				return;
			}
			LocalTime parsedTime;
			try {
				parsedTime = LocalTime.parse(timeInput.getText());
			} catch (Exception ex) {
				return;
			}
			if (datePicker.getValue().isEqual(LocalDate.now()) && parsedTime.isBefore(LocalTime.now()))
				return;
			int visitors;
			try {
				visitors = Integer.parseInt(visitorsInput.getText());
			} catch (Exception ex) {
				return;
			}
			if (visitors < 1 || visitors > 15)
				return;

			String newType = isAccountGuide && chkIsGuide.isSelected() ? "Guide" : "Regular Visitor";
			int newPrice = calculatePrice(visitors, newType, isSubscriberAccount, true, true);
			int oldPrice = selectedBooking.getPrice();
			int diff = newPrice - oldPrice;

			if (diff > 0) {
				Alert alertPay = new Alert(Alert.AlertType.CONFIRMATION,
						"Additional payment required: " + diff + " ILS.\nProceed to payment?", ButtonType.YES,
						ButtonType.NO);
				alertPay.showAndWait();
				if (alertPay.getResult() != ButtonType.YES)
					return;
			} else if (diff < 0) {
				Alert alertRefund = new Alert(Alert.AlertType.CONFIRMATION,
						"You will receive a refund of: " + Math.abs(diff) + " ILS.\nProceed?", ButtonType.YES,
						ButtonType.NO);
				alertRefund.showAndWait();
				if (alertRefund.getResult() != ButtonType.YES)
					return;
			}

			Booking b = new Booking(selectedBookingId, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(),
					parsedTime, visitors, "Pending");
			b.setVisitorType(newType);
			b.setPrice(newPrice);
			sendCommandToServer("UPDATE_DATA", b);
			loadDataFromServer();
			checkLiveCapacity();
		});

		btnCancel.setOnAction(e -> {
			Booking selectedBooking = table.getSelectionModel().getSelectedItem();
			if (selectedBooking == null)
				return;
			ArrayList<Object> deleteData = new ArrayList<>();
			deleteData.add(selectedBooking.getBookingId());
			deleteData.add(loggedInVisitorId);
			sendCommandToServer("CANCEL_DATA", deleteData);
			loadDataFromServer();
			checkLiveCapacity();
			checkWaitingListInbox();
			selectedBookingId = -1;
		});

		VBox layout = new VBox(15, topBar, table, openingHoursLabel, liveCapacityLabel, inputGrid, buttonBox,
				responseLabel);
		layout.setPadding(new Insets(20));
		layout.setStyle("-fx-background-color: #f4fcf4;");
		primaryStage.setScene(new Scene(layout, 750, 550));
		primaryStage.show();

		loadDataFromServer();
		checkLiveCapacity();
		checkTomorrowBookings();
		checkWaitingListInbox();
	}

	public static int calculatePrice(int totalVisitors, String visitorType, boolean hasSubscription,
			boolean isPrebooked, boolean isPrepaid) {
		double fullPricePerTicket = 30.0;
		double finalPrice = 0.0;
		if ("Guide".equals(visitorType)) {
			if (isPrebooked) {
				int payingVisitors = Math.max(0, totalVisitors - 1);
				finalPrice = (payingVisitors * fullPricePerTicket) * 0.75;
				if (isPrepaid)
					finalPrice = finalPrice * 0.88;
			} else
				finalPrice = (totalVisitors * fullPricePerTicket) * 0.90;
		} else {
			if (isPrebooked)
				finalPrice = (totalVisitors * fullPricePerTicket) * 0.85;
			else
				finalPrice = totalVisitors * fullPricePerTicket;
		}
		if (hasSubscription)
			finalPrice = finalPrice * 0.90;
		return (int) Math.round(finalPrice);
	}

	private void checkTomorrowBookings() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		for (Booking b : dataList) {
			if (b.getVisitDate().equals(tomorrow) && "Pending".equals(b.getStatus())) {
				ButtonType confirmArrivalButton = new ButtonType("Confirm Arrival", ButtonBar.ButtonData.OK_DONE);
				ButtonType cancelBookingButton = new ButtonType("Cancel Booking", ButtonBar.ButtonData.CANCEL_CLOSE);
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
						"Reminder: Upcoming order tomorrow (" + tomorrow + ")\nPark: " + b.getParkName() + "\nTime: "
								+ b.getVisitTime() + "\n\nConfirm arrival or cancel?",
						confirmArrivalButton, cancelBookingButton);
				alert.showAndWait();
				if (alert.getResult() == confirmArrivalButton)
					sendCommandToServer("CONFIRM_ARRIVAL", b.getBookingId());
				else if (alert.getResult() == cancelBookingButton) {
					ArrayList<Object> deleteData = new ArrayList<>();
					deleteData.add(b.getBookingId());
					deleteData.add(loggedInVisitorId);
					sendCommandToServer("CANCEL_DATA", deleteData);
				}
			}
		}
		loadDataFromServer();
	}

	private void checkWaitingListInbox() {
		try {
			Message response = ClientSession.send(new Message("CHECK_WAITINGLIST", loggedInVisitorId));

			if ("HAS_EMPTY_PLACE".equals(response.getCommand())) {
				@SuppressWarnings("unchecked")
				ArrayList<Object> notifData = (ArrayList<Object>) response.getData();
				int waitingId = (int) notifData.get(0);
				String park = (String) notifData.get(1);
				String date = notifData.get(2).toString();
				String time = notifData.get(3).toString();
				long minutesLeft = (long) notifData.get(4);
				int visitors = (int) notifData.get(5);
				String wlVisitorType = (String) notifData.get(6);

				ButtonType orderBtn = new ButtonType("Make Order (Pay)", ButtonBar.ButtonData.OK_DONE);
				ButtonType declineBtn = new ButtonType("Cancel Request", ButtonBar.ButtonData.CANCEL_CLOSE);

				Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
						"An empty place opened up for your Waiting List request:\n\nPark: " + park + "\nDate: " + date
								+ "\nTime: " + time + "\n\nYou have " + minutesLeft
								+ " minutes left to make an order before it passes to the next person.",
						orderBtn, declineBtn);
				alert.setTitle("Spot Available!");
				alert.showAndWait();

				if (alert.getResult() == orderBtn) {
					int finalPrice = calculatePrice(visitors, wlVisitorType, isSubscriberAccount, true, true);
					Alert payAlert = new Alert(Alert.AlertType.CONFIRMATION,
							"Total price is " + finalPrice + " ILS (Discounts applied).\nProceed to payment?",
							ButtonType.YES, ButtonType.NO);
					payAlert.showAndWait();
					if (payAlert.getResult() == ButtonType.YES) {
						ArrayList<Object> payData = new ArrayList<>();
						payData.add(waitingId);
						payData.add(finalPrice);
						sendCommandToServer("PAY_WAITING_LIST", payData);
						loadDataFromServer();
						checkLiveCapacity();
					} else
						responseLabel.setText("Payment skipped. Spot not claimed.");
				} else if (alert.getResult() == declineBtn) {
					sendCommandToServer("DECLINE_WAITING_LIST", waitingId);
					loadDataFromServer();
					checkLiveCapacity();
				}
			}
		} catch (Exception ex) {
		}
	}

	private void checkLiveCapacity() {
		try {
			if (datePicker.getValue() == null) {
				liveCapacityLabel.setText("Please choose a date first.");
				return;
			}

			if (datePicker.getValue().isBefore(LocalDate.now())) {
				liveCapacityLabel.setText("⚠️ Cannot book past dates!");
				return;
			}

			if (timeInput.getText() == null || timeInput.getText().trim().isEmpty()) {
				liveCapacityLabel.setText("Please enter a time first.");
				return;
			}

			LocalTime time = LocalTime.parse(timeInput.getText().trim());

			if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(18, 0))) {
				liveCapacityLabel.setText("Park Status: Closed");
				return;
			}

			Booking b = new Booking(0, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(), time, 0,
					"Pending");

			Message response = ClientSession.send(new Message("GET_AVAILABLE_SPOTS", b));

			if ("AVAILABLE_SPOTS_RESPONSE".equals(response.getCommand())) {
				int emptyTickets = (int) response.getData();

				if (emptyTickets > 0) {
					liveCapacityLabel.setText(
							"✓ " + emptyTickets + " empty spots available! You can proceed with a regular booking.");
				} else {
					liveCapacityLabel
							.setText("⚠️ PARK IS FULL! Hitting 'Add New Booking' will place you on the WAITING LIST.");
				}
			}

		} catch (Exception ex) {
			liveCapacityLabel.setText("Could not check live capacity.");
			ex.printStackTrace();
		}
	}

	private void loadDataFromServer() {
		try {
			Message response = ClientSession.send(new Message("LOAD_DATA", loggedInVisitorId));
			if ("SUCCESS".equals(response.getCommand())) {
				@SuppressWarnings("unchecked")
				ArrayList<Booking> list = (ArrayList<Booking>) response.getData();
				dataList.setAll(list);
			}
		} catch (Exception ex) {
			responseLabel.setText("Connection Error.");
		}
	}

	private void sendCommandToServer(String command, Object data) {
		try {
			Message response = ClientSession.send(new Message(command, data));
			if (response.getData() != null
					&& command.matches("ADD_DATA|CANCEL_DATA|PAY_WAITING_LIST|ADD_SPLIT_BOOKING")) {
				new Alert(Alert.AlertType.INFORMATION, response.getData().toString(), ButtonType.OK).showAndWait();
				if ("SUCCESS_PAID".equals(response.getCommand())) {
					NotificationSimulator.send(loggedInVisitorId + "@gonature.com", null, "Booking Confirmation",
							response.getData().toString());
				}
			}
			responseLabel.setText("Action: " + response.getCommand());
		} catch (Exception ex) {
			responseLabel.setText("Connection Error.");
		}
	}
}