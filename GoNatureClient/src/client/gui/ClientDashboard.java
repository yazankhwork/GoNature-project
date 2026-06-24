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
import client.network.INetworkObserver;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
/**
 * Main dashboard of the GoNature client application.
 *
 * This screen allows visitors to create, update and cancel bookings,
 * manage waiting-list requests, view notifications, check park availability
 * and monitor their booking history.
 *
 * The dashboard also receives live server notifications using
 * the Observer design pattern.
 *
 * @author Group 4
 * @version 1.0
 */
public class ClientDashboard extends Application implements INetworkObserver {
	/**
	 * Identifier of the currently logged-in visitor.
	 */
	public static String loggedInVisitorId = "";
	/**
	 * Name of the currently logged-in visitor.
	 */
	public static String loggedInName = "";
	/**
	 * Indicates whether the current account belongs to a certified guide.
	 */
	public static boolean isAccountGuide = false;
	/**
	 * Indicates whether the current visitor has an active subscription.
	 */
	public static boolean isSubscriberAccount = false;
	/**
	 * Subscription number of the current visitor.
	 */
	public static String subscriptionNumber = "";
	/**
	 * Indicates whether the current session is a guest session.
	 */
	public static boolean isGuest = false;
	/**
	 * Number of family members covered by the subscription.
	 */
	public static int familyMembers = 1;
	/**
	 * Table displaying visitor bookings.
	 */
	private TableView<Booking> table = new TableView<>();
	/**
	 * Observable list used as the booking table data source.
	 */
	private ObservableList<Booking> dataList = FXCollections.observableArrayList();
	/**
	 * Label used to display operation results and status messages.
	 */
	private Label responseLabel = new Label("Ready");
	/**
	 * Park selector used for booking creation.
	 */
	private ComboBox<String> parkCombo = new ComboBox<>();
	/**
	 * Date selector used for booking reservations.
	 */
	private DatePicker datePicker = new DatePicker(LocalDate.now());
	/**
	 * Input field for visit time.
	 */
	private TextField timeInput = new TextField("10:00");
	/**
	 * Input field for number of visitors.
	 */
	private TextField visitorsInput = new TextField("1");
	/**
	 * Input field for visitor email address.
	 */
	private TextField emailInput = new TextField();
	/**
	 * Input field for visitor phone number.
	 */
	private TextField phoneInput = new TextField();
	/**
	 * Identifier of the currently selected booking.
	 */
	private int selectedBookingId = -1;
	/**
	 * Displays live park capacity and availability information.
	 */
	private Label liveCapacityLabel = new Label(
			"Select park, date, and time, then click 'Select' to check availability.");
	/**
	 * Indicates whether the booking is for a guided group.
	 */
	private CheckBox chkIsGuide = new CheckBox("This visit is for a group with a guide");
	/**
	 * Creates and displays the main dashboard interface.
	 *
	 * @param primaryStage primary JavaFX stage
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("GoNature - Dashboard");

		ClientSession.addObserver(this);
		
		primaryStage.setOnCloseRequest(e -> {
			ClientSession.removeObserver(this);
		});

		Label welcomeLabel = new Label("🌿 Welcome, " + loggedInName);
		welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1b5e20;");

		if (isAccountGuide) {
			chkIsGuide.setVisible(true);
			chkIsGuide.setSelected(false);
			chkIsGuide.setStyle("-fx-font-weight: bold; -fx-text-fill: #388e3c; -fx-font-size: 14px;");
		} else {
			chkIsGuide.setVisible(false);
			chkIsGuide.setSelected(false);
		}

		int maxV = 1;
		if (isAccountGuide) maxV = 15;
		else if (isSubscriberAccount) maxV = familyMembers;
		else maxV = 1;

		Label visitorsLabel = new Label("Visitors (Max " + maxV + "):");
		visitorsInput.setEditable(true);
		visitorsInput.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px;");
		
		emailInput.setPromptText("example@email.com");
		emailInput.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px;");
		
		phoneInput.setPromptText("Optional - 10 digits");
		phoneInput.setStyle("-fx-border-color: #81c784; -fx-background-radius: 5px; -fx-border-radius: 5px;");
		
		Button btnShowPrices = new Button("View Pricing List");
		btnShowPrices.setStyle(
				"-fx-background-color: #5d4037; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		Button btnNotifications = new Button("Notifications");
		btnNotifications.setStyle(
				"-fx-background-color: #0277bd; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		Button btnLogout = new Button("Logout");
		btnLogout.setStyle(
				"-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");

		if (isSubscriberAccount) {
			welcomeLabel.setText("🌿 Welcome, " + loggedInName + " (Sub #" + subscriptionNumber + ")");
		}
		if (isGuest) {
			welcomeLabel.setText("🌿 Welcome, Guest (" + loggedInVisitorId + ")");
		}

		HBox rightAlign = new HBox(10, btnShowPrices, btnNotifications, btnLogout);
		rightAlign.setAlignment(Pos.CENTER_RIGHT);
		HBox.setHgrow(rightAlign, Priority.ALWAYS);
		HBox topBar = new HBox(20, welcomeLabel, chkIsGuide, rightAlign);
		topBar.setAlignment(Pos.CENTER_LEFT);

		parkCombo.getItems().addAll(common.Parks.NAMES);
		parkCombo.setValue("Carmel Park");
		parkCombo.setStyle("-fx-border-color: #81c784;");
		datePicker.setStyle("-fx-border-color: #81c784;");

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
		TableColumn<Booking, Integer> priceCol = new TableColumn<>("Ticket Price");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
		TableColumn<Booking, String> typeCol = new TableColumn<>("Booking Type");
		typeCol.setCellValueFactory(new PropertyValueFactory<>("visitorType"));
		TableColumn<Booking, String> emailCol = new TableColumn<>("Email");
		emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
		TableColumn<Booking, String> phoneCol = new TableColumn<>("Phone");
		phoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
		
		table.getColumns().addAll(idCol, parkCol, dateCol, timeCol, visCol, statusCol, priceCol, typeCol, emailCol, phoneCol);
		table.setItems(dataList);
		table.setStyle("-fx-selection-bar: #c8e6c9; -fx-background-color: white; -fx-border-color: #4caf50;");

		table.setRowFactory(tv -> new TableRow<Booking>() {
			@Override
			protected void updateItem(Booking item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty)
					setStyle("");
				else {
					if ("Confirmed".equals(item.getStatus()))
						setStyle("-fx-background-color: #c8e6c9;"); 
					else if ("Pending".equals(item.getStatus()))
						setStyle("-fx-background-color: #fff9c4;"); 
					else if ("Waiting List".equals(item.getStatus()))
						setStyle("-fx-background-color: #ffccbc;"); 
					else if ("Cancelled".equals(item.getStatus()))
						setStyle("-fx-background-color: #f5f5f5;");
					else
						setStyle("-fx-background-color: white;");
				}
			}
		});

		Label openingHoursLabel = new Label("Note: Park Opening Hours are 08:00 to 18:00");
		openingHoursLabel.setStyle("-fx-text-fill: #e65100; -fx-font-weight: bold; -fx-font-size: 13px;");
		liveCapacityLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-font-size: 14px;");

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
		inputGrid.add(new Label("Email:"), 0, 2);
		inputGrid.add(emailInput, 1, 2);
		inputGrid.add(new Label("Phone:"), 2, 2);
		inputGrid.add(phoneInput, 3, 2);

		Button btnSelect = new Button("Check Availability");
		btnSelect.setStyle(
				"-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		Button btnAdd = new Button("Add New Booking");
		btnAdd.setStyle(
				"-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		Button btnUpdate = new Button("Update Selected");
		btnUpdate.setStyle(
				"-fx-background-color: #f57c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
		Button btnCancel = new Button("Cancel Selected");
		btnCancel.setStyle(
				"-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");

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
					+ "5. Subscribers: Receive an additional 10% compound discount on top of any other discounts!\n\n"
					+ "6. Approved park discounts: If the department manager approved a park discount, it is applied to the final bill.");
			pricesAlert.showAndWait();
		});
		btnNotifications.setOnAction(e -> showRecentNotifications(true));

		btnSelect.setOnAction(e -> {
			checkLiveCapacity();
			responseLabel.setText("Availability status refreshed.");
		});

		btnLogout.setOnAction(e -> {
			ClientSession.removeObserver(this);
			LogoutHelper.logout(primaryStage);
		});

		table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSelection) -> {
			if (newSelection != null) {
				selectedBookingId = newSelection.getBookingId();
				parkCombo.setValue(newSelection.getParkName());
				datePicker.setValue(newSelection.getVisitDate());
				timeInput.setText(newSelection.getVisitTime().toString());
				visitorsInput.setText(String.valueOf(newSelection.getVisitorsCount()));
				emailInput.setText(newSelection.getEmail() == null ? "" : newSelection.getEmail());
				phoneInput.setText(newSelection.getTelephone() == null ? "" : newSelection.getTelephone());
				chkIsGuide.setSelected(newSelection.isGuideGroup());
				if (isAccountGuide)
					chkIsGuide.setSelected("Guide".equals(newSelection.getVisitorType()));
				checkLiveCapacity();
			}
		});

		final int finalMaxV = maxV;
		
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
			
			// וולידציה חדשה לפי סוג משתמש
			if (visitors < 1 || visitors > finalMaxV) {
				new Alert(Alert.AlertType.ERROR, "Limit Exceeded! As a " 
						+ (isAccountGuide ? "Guide" : (isSubscriberAccount ? "Family Subscriber" : "Regular Visitor")) 
						+ ", you can only book for 1 to " + finalMaxV + " visitors.").showAndWait();
				return;
			}

			String email = emailInput.getText().trim();
			if (email.isEmpty() || !email.contains("@")) {
				new Alert(Alert.AlertType.ERROR, "Enter a valid email address.").showAndWait();
				return;
			}
			
			String phone = phoneInput.getText().trim();
			if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
				new Alert(Alert.AlertType.ERROR, "Phone must be exactly 10 digits!").showAndWait();
				return;
			}

			boolean guideGroup = isAccountGuide && chkIsGuide.isSelected();

			Booking b = new Booking(0, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(), parsedTime,
					visitors, "Pending");

			b.setEmail(email);
			b.setTelephone(phone);
			b.setVisitorType(guideGroup ? "Guide" : "Regular Visitor");
			b.setGuideGroup(guideGroup);
			b.setSubscriber(isSubscriberAccount);

			if (isGuest) {
				b.setVisitorType("Regular Visitor");
				b.setGuideGroup(false);
				b.setSubscriber(false);
			}

			int approvedDiscount = isGuest ? 0 : getApprovedDiscountPercent(parkCombo.getValue());
			boolean getsPrebookDiscount = !isGuest; 
			int calculatedPrice = calculatePrice(visitors, b.getVisitorType(), b.isSubscriber(), getsPrebookDiscount, true);
			calculatedPrice = applyApprovedParkDiscount(calculatedPrice, approvedDiscount);
			b.setPrice(calculatedPrice);
			
			try {
				Message response = ClientSession.send(new Message("CHECK_AVAILABILITY", b));

				if ("OK".equals(response.getCommand())) {
					Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
							"Total ticket price is " + calculatedPrice + " ILS" + (isGuest ? " (Full Price - No Discounts for Guests)." : " (Discounts applied).")
									+ approvedDiscountText(approvedDiscount)
									+ "\nConfirm Booking?",
							ButtonType.YES, ButtonType.NO);
					alert.showAndWait();
					if (alert.getResult() == ButtonType.YES) {
						sendCommandToServer("ADD_DATA", b);
						if (!isGuest) loadDataFromServer();
						checkLiveCapacity();
						if (isGuest) {
							new Alert(Alert.AlertType.INFORMATION, "Guest Booking Complete. Please save your Confirmation Code shown!").showAndWait();
						}
					} else
						responseLabel.setText("Booking cancelled.");
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
						int splitApprovedDiscount = isGuest ? 0 : getApprovedDiscountPercent(b.getParkName());
						int splitPrice = calculatePrice(availableSpots, b.getVisitorType(), b.isSubscriber(), getsPrebookDiscount, true);
						splitPrice = applyApprovedParkDiscount(splitPrice, splitApprovedDiscount);
						Alert payAlert = new Alert(Alert.AlertType.CONFIRMATION, "Ticket price for " + availableSpots
								+ " spots is " + splitPrice + " ILS."
								+ approvedDiscountText(splitApprovedDiscount)
								+ "\nConfirm Booking?", ButtonType.YES,
								ButtonType.NO);
						payAlert.showAndWait();
						if (payAlert.getResult() == ButtonType.YES) {
							Booking confirmedB = new Booking(0, b.getVisitorId(), b.getParkName(), b.getVisitDate(),
									b.getVisitTime(), availableSpots, "Pending");

							confirmedB.setVisitorType(b.getVisitorType());
							confirmedB.setEmail(b.getEmail());
							confirmedB.setTelephone(b.getTelephone());
							confirmedB.setGuideGroup(b.isGuideGroup());
							confirmedB.setSubscriber(b.isSubscriber());
							confirmedB.setPrice(splitPrice);

							Booking waitlistB = new Booking(0, b.getVisitorId(), b.getParkName(), b.getVisitDate(),
									b.getVisitTime(), waitlistSpots, "Waiting List");

							waitlistB.setVisitorType(b.getVisitorType());
							waitlistB.setEmail(b.getEmail());
							waitlistB.setTelephone(b.getTelephone());
							waitlistB.setGuideGroup(b.isGuideGroup());
							waitlistB.setSubscriber(b.isSubscriber());
							waitlistB.setPrice(0);
							ArrayList<Booking> splitData = new ArrayList<>();
							splitData.add(confirmedB);
							splitData.add(waitlistB);
							sendCommandToServer("ADD_SPLIT_BOOKING", splitData);
							if (!isGuest) loadDataFromServer();
							checkLiveCapacity();
						} else
							responseLabel.setText("Booking cancelled.");
					} else if (splitAlert.getResult() == allWaitlistBtn) {
						b.setStatus("Waiting List");
						b.setPrice(0);
						sendCommandToServer("ADD_DATA", b);
						if (!isGuest) loadDataFromServer();
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
						if (!isGuest) loadDataFromServer();
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
			
			// וולידציה חדשה לפי סוג משתמש
			if (visitors < 1 || visitors > finalMaxV) {
				new Alert(Alert.AlertType.ERROR, "Limit Exceeded! As a " 
						+ (isAccountGuide ? "Guide" : (isSubscriberAccount ? "Family Subscriber" : "Regular Visitor")) 
						+ ", you can only book for 1 to " + finalMaxV + " visitors.").showAndWait();
				return;
			}
			
			String email = emailInput.getText().trim();
			if (email.isEmpty() || !email.contains("@")) {
				new Alert(Alert.AlertType.ERROR, "Enter a valid email address.").showAndWait();
				return;
			}
			
			String phone = phoneInput.getText().trim();
			if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
				new Alert(Alert.AlertType.ERROR, "Phone must be exactly 10 digits!").showAndWait();
				return;
			}
			
			boolean guideGroup = isAccountGuide && chkIsGuide.isSelected();

			String newType = guideGroup ? "Guide" : "Regular Visitor";
			int approvedDiscount = getApprovedDiscountPercent(parkCombo.getValue());
			int newPrice = calculatePrice(visitors, newType, isSubscriberAccount, true, true);
			newPrice = applyApprovedParkDiscount(newPrice, approvedDiscount);
			int oldPrice = selectedBooking.getPrice();
			int diff = newPrice - oldPrice;

			if (diff > 0) {
				Alert alertPay = new Alert(Alert.AlertType.CONFIRMATION,
						"New ticket price is higher by: " + diff + " ILS.\nUpdate Booking?", ButtonType.YES,
						ButtonType.NO);
				alertPay.showAndWait();
				if (alertPay.getResult() != ButtonType.YES)
					return;
			} else if (diff < 0) {
				Alert alertRefund = new Alert(Alert.AlertType.CONFIRMATION,
						"New ticket price is lower by: " + Math.abs(diff) + " ILS.\nUpdate Booking?", ButtonType.YES,
						ButtonType.NO);
				alertRefund.showAndWait();
				if (alertRefund.getResult() != ButtonType.YES)
					return;
			}

			Booking b = new Booking(selectedBookingId, loggedInVisitorId, parkCombo.getValue(), datePicker.getValue(),
					parsedTime, visitors, "Pending");

			b.setEmail(email);
			b.setTelephone(phone);
			b.setVisitorType(newType);
			b.setGuideGroup(guideGroup);
			b.setSubscriber(isSubscriberAccount);
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

		if (isGuest) {
			btnShowPrices.setDisable(true);
			btnNotifications.setDisable(true);
			btnUpdate.setDisable(true);
			btnCancel.setDisable(true);
			table.setPlaceholder(new Label("Guests cannot view booking history."));
		} else {
			loadDataFromServer();
			checkTomorrowBookings();
			checkWaitingListInbox();
			showRecentNotifications(false);
		}

		VBox layout = new VBox(15, topBar, table, openingHoursLabel, liveCapacityLabel, inputGrid, buttonBox,
				responseLabel);
		layout.setPadding(new Insets(20));
		layout.setStyle("-fx-background-color: #e8f5e9;"); 
		primaryStage.setScene(new Scene(layout, 820, 550));
		primaryStage.show();

		checkLiveCapacity();
	}
	/**
	 * Handles asynchronous notifications received from the server.
	 *
	 * @param msg message received from the server
	 */
	@Override
	public void onMessageReceived(Message msg) {
		if (msg.getCommand().equals("SERVER_PUSH_NOTIFICATION")) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION, msg.getData().toString());
			alert.setTitle("Live Server Update");
			alert.setHeaderText("New Notification from GoNature");
			alert.show();
			
			loadDataFromServer();
		}
	}
	/**
	 * Calculates the booking price according to visitor type,
	 * subscription status and discount rules.
	 *
	 * @param totalVisitors number of visitors
	 * @param visitorType visitor type
	 * @param hasSubscription indicates whether the visitor has a subscription
	 * @param isPrebooked indicates whether the booking was made in advance
	 * @param isPrepaid indicates whether the booking was prepaid
	 * @return calculated ticket price
	 */
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
	/**
	 * Retrieves the approved discount percentage for a specific park.
	 *
	 * @param parkName park name
	 * @return approved discount percentage, or 0 if none exists
	 */
	private int getApprovedDiscountPercent(String parkName) {
		try {
			Message resp = ClientSession.send(new Message("GET_APPROVED_DISCOUNT", parkName));

			if ("APPROVED_DISCOUNT".equals(resp.getCommand())) {
				return (int) resp.getData();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}
	/**
	 * Applies an approved park discount to the given price.
	 *
	 * @param price original price
	 * @param discountPercent discount percentage
	 * @return price after discount
	 */
	private int applyApprovedParkDiscount(int price, int discountPercent) {
		if (discountPercent <= 0) {
			return price;
		}

		return (int) Math.round(price * (1 - discountPercent / 100.0));
	}
	/**
	 * Builds a text message describing the approved park discount.
	 *
	 * @param discountPercent discount percentage
	 * @return discount description text, or an empty string if no discount exists
	 */
	private String approvedDiscountText(int discountPercent) {
		if (discountPercent <= 0) {
			return "";
		}

		return "\nApproved park discount applied: " + discountPercent + "%";
	}
	/**
	 * Checks pending bookings scheduled for tomorrow and asks the visitor
	 * to confirm arrival or cancel the booking.
	 */
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
	/**
	 * Checks whether the visitor has an active waiting-list offer
	 * and allows the visitor to claim or decline it.
	 */
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

				ButtonType orderBtn = new ButtonType("Claim Spot", ButtonBar.ButtonData.OK_DONE);
				ButtonType declineBtn = new ButtonType("Cancel Request", ButtonBar.ButtonData.CANCEL_CLOSE);

				Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
						"An empty place opened up for your Waiting List request:\n\nPark: " + park + "\nDate: " + date
								+ "\nTime: " + time + "\n\nYou have " + minutesLeft
								+ " minutes left to make an order before it passes to the next person.",
						orderBtn, declineBtn);
				alert.setTitle("Spot Available!");
				alert.showAndWait();

				if (alert.getResult() == orderBtn) {
					int approvedDiscount = getApprovedDiscountPercent(park);
					int finalPrice = calculatePrice(visitors, wlVisitorType, isSubscriberAccount, true, true);
					finalPrice = applyApprovedParkDiscount(finalPrice, approvedDiscount);
					Alert payAlert = new Alert(Alert.AlertType.CONFIRMATION,
							"Total ticket price is " + finalPrice + " ILS (Discounts applied)."
									+ approvedDiscountText(approvedDiscount)
									+ "\nConfirm Booking?",
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
						responseLabel.setText("Action skipped. Spot not claimed.");
				} else if (alert.getResult() == declineBtn) {
					sendCommandToServer("DECLINE_WAITING_LIST", waitingId);
					loadDataFromServer();
					checkLiveCapacity();
				}
			}
		} catch (Exception ex) {
		}
	}
	/**
	 * Checks the current available capacity for the selected park,
	 * date and time, and updates the capacity label.
	 */
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
	/**
	 * Loads the visitor's booking data from the server
	 * and updates the bookings table.
	 */
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
	/**
	 * Sends a command with data to the server and handles the response.
	 *
	 * @param command command name
	 * @param data data object sent with the command
	 */
	private void sendCommandToServer(String command, Object data) {
		try {
			Message response = ClientSession.send(new Message(command, data));
			if (response.getData() != null
					&& command.matches("ADD_DATA|CANCEL_DATA|PAY_WAITING_LIST|ADD_SPLIT_BOOKING")) {
				new Alert(Alert.AlertType.INFORMATION, response.getData().toString(), ButtonType.OK).showAndWait();
				if ("SUCCESS_PAID".equals(response.getCommand())) {
					String toEmail = loggedInVisitorId + "@gonature.com";
					String toPhone = null;

					if (data instanceof Booking) {
						Booking booking = (Booking) data;
						if (booking.getEmail() != null && !booking.getEmail().trim().isEmpty()) {
							toEmail = booking.getEmail().trim();
						}
						if (booking.getTelephone() != null && !booking.getTelephone().trim().isEmpty()) {
							toPhone = booking.getTelephone().trim();
						}
					} else if (data instanceof ArrayList<?>) {
						ArrayList<?> list = (ArrayList<?>) data;
						if (!list.isEmpty() && list.get(0) instanceof Booking) {
							Booking booking = (Booking) list.get(0);
							if (booking.getEmail() != null && !booking.getEmail().trim().isEmpty()) {
								toEmail = booking.getEmail().trim();
							}
							if (booking.getTelephone() != null && !booking.getTelephone().trim().isEmpty()) {
								toPhone = booking.getTelephone().trim();
							}
						}
					}

					NotificationSimulator.send(toEmail, toPhone, "Booking Confirmation",
							response.getData().toString());
				}
			}
			responseLabel.setText("Action: " + response.getCommand());
		} catch (Exception ex) {
			responseLabel.setText("Connection Error.");
		}
	}
	/**
	 * Loads and displays recent visitor notifications.
	 *
	 * @param showWhenEmpty true to show a message when no notifications exist
	 */
	@SuppressWarnings("unchecked")
	private void showRecentNotifications(boolean showWhenEmpty) {
		try {
			Message response = ClientSession.send(new Message("GET_VISITOR_NOTIFICATIONS", loggedInVisitorId));

			if (!"VISITOR_NOTIFICATIONS".equals(response.getCommand())) {
				new Alert(Alert.AlertType.ERROR, "Could not load notifications.").showAndWait();
				return;
			}

			ArrayList<ArrayList<Object>> notifications = (ArrayList<ArrayList<Object>>) response.getData();

			if (notifications == null || notifications.isEmpty()) {
				if (showWhenEmpty) {
					new Alert(Alert.AlertType.INFORMATION, "No notifications found.").showAndWait();
				}
				return;
			}

			StringBuilder sb = new StringBuilder();

			for (ArrayList<Object> row : notifications) {
				String type = String.valueOf(row.get(1));
				String message = String.valueOf(row.get(2));
				String email = String.valueOf(row.get(3));
				String phone = String.valueOf(row.get(4));
				String sentAt = String.valueOf(row.get(5));

				sb.append("Type: ").append(type).append("\n");
				sb.append("Sent At: ").append(sentAt).append("\n");
				sb.append("Email: ").append(email).append("\n");
				sb.append("SMS Phone: ").append(phone).append("\n");
				sb.append(message).append("\n");
				sb.append("-----------------------------\n");
			}

			TextArea area = new TextArea(sb.toString());
			area.setEditable(false);
			area.setWrapText(true);
			area.setPrefWidth(600);
			area.setPrefHeight(400);

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("My Notifications");
			alert.setHeaderText("Recent GoNature Notifications");
			alert.getDialogPane().setContent(area);
			alert.showAndWait();

		} catch (Exception ex) {
			new Alert(Alert.AlertType.ERROR, "Connection error while loading notifications.").showAndWait();
		}
	}
}