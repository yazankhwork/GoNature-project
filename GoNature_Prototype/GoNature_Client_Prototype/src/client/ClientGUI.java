package client;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import common.ClientRequest;
import common.Order;

public class ClientGUI extends JFrame {

    private JTextField orderNumberField; // an order number field
    private JTextField orderDateField; // an order Date field
    private JTextField numberOfVisitorsField; // a field for the number of the visitors 
    private JTextField confirmationCodeField; // a field for the confirmation code
    private JTextField subscriberIdField; // a field for subscriber id
    private JTextField dateOfPlacingOrderField; // the date for which the order was placed 

    private JLabel statusLabel; //a label for status 

    private PrototypeClient client; //an instance of PrototypeClient
    private String serverHost; // the index for connecting the other hardware to the current hardware e

    private static final int SERVER_PORT = 5555;

    public ClientGUI() {
        this("localhost"); //call for line 33 
    }

    public ClientGUI(String serverHost) {
        this.serverHost = serverHost;
        //init GUI
        setTitle("GoNature - Client GUI");
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        orderNumberField = new JTextField();
        orderDateField = new JTextField();
        numberOfVisitorsField = new JTextField();
        confirmationCodeField = new JTextField();
        subscriberIdField = new JTextField();
        dateOfPlacingOrderField = new JTextField();
        confirmationCodeField.setEditable(false);
        subscriberIdField.setEditable(false);
        dateOfPlacingOrderField.setEditable(false);
        JButton loadButton = new JButton("Load Order");
        JButton updateButton = new JButton("Update Order");
        JButton clearButton = new JButton("Clear");
        JButton endRunningButton = new JButton("End Running");
        statusLabel = new JLabel("Status: Ready");
        panel.add(new JLabel("Order Number:"));
        panel.add(orderNumberField);
        panel.add(new JLabel("Order Date yyyy-MM-dd:"));
        panel.add(orderDateField);
        panel.add(new JLabel("Number Of Visitors 1-15:"));
        panel.add(numberOfVisitorsField);
        panel.add(new JLabel("Confirmation Code:"));
        panel.add(confirmationCodeField);
        panel.add(new JLabel("Subscriber ID:"));
        panel.add(subscriberIdField);
        panel.add(new JLabel("Date Of Placing Order:"));
        panel.add(dateOfPlacingOrderField);
        panel.add(loadButton);
        panel.add(updateButton);
        panel.add(clearButton);
        panel.add(endRunningButton);
        panel.add(statusLabel);
        panel.add(new JLabel(""));
        add(panel);
        
        //aemots code shel loadOrder , updateButton , clearButton , endRunningButton
        loadButton.addActionListener(e -> loadOrder()); 
        updateButton.addActionListener(e -> updateOrder());
        clearButton.addActionListener(e -> clearFields());
        endRunningButton.addActionListener(e -> endRunning());

        connectToServer();  //connect to server at the end 
    }

    
    //connect to server
    private boolean connectToServer() {
        if (client != null && client.isConnected()) {
            return true;
        }

        try {
            client = new PrototypeClient(serverHost, SERVER_PORT, this);
            client.openConnection();

            statusLabel.setText("Status: Connected to server " + serverHost);
            return true;

        } catch (Exception e) {
            client = null;
            statusLabel.setText("Status: Could not connect to server");

            JOptionPane.showMessageDialog( 
                    this,
                    "Cannot connect to server.\n\nPlease check:\n" +
                            "1. The IP address is correct\n" +
                            "2. The server is running\n" +
                            "3. The port is 5555",
                    "Connection Failed",
                    JOptionPane.ERROR_MESSAGE
            );

            dispose();

            ClientConnectionGUI connectionGUI = new ClientConnectionGUI();
            connectionGUI.setVisible(true);

            return false;
        }
    }

    private void loadOrder() { // a function to load order 
        String orderNumberText = orderNumberField.getText().trim();

        if (orderNumberText.isEmpty()) {
            statusLabel.setText("Status: Please enter order number");
            return;
        }

        if (!isOnlyNumbers(orderNumberText)) {
            statusLabel.setText("Status: Order number must contain numbers only");
            return;
        }

        try {
            int orderNumber = Integer.parseInt(orderNumberText);

            if (!connectToServer()) {
                return;
            }

            ClientRequest request =
                    new ClientRequest("LOAD_ORDER", orderNumber);

            client.sendRequest(request);

            statusLabel.setText("Status: Load request sent");

        } catch (NumberFormatException e) {
            statusLabel.setText("Status: Invalid order number");
        }
    }

    private void updateOrder() { //a function to update order  
        String orderNumberText = orderNumberField.getText().trim();
        String orderDate = orderDateField.getText().trim();
        String numberOfVisitorsText = numberOfVisitorsField.getText().trim();

        if (orderNumberText.isEmpty()) {
            statusLabel.setText("Status: Please enter order number");
            return;
        }

        if (!isOnlyNumbers(orderNumberText)) {
            statusLabel.setText("Status: Order number must contain numbers only");
            return;
        }

        if (orderDate.isEmpty()) {
            statusLabel.setText("Status: Please enter order date");
            return;
        }

        if (!isValidDateFormat(orderDate)) {
            statusLabel.setText("Status: Date must be exactly yyyy-MM-dd");
            return;
        }

        if (isDateInPast(orderDate)) {
            statusLabel.setText("Status: Order date cannot be in the past");
            return;
        }

        if (numberOfVisitorsText.isEmpty()) {
            statusLabel.setText("Status: Please enter number of visitors");
            return;
        }

        if (!isOnlyNumbers(numberOfVisitorsText)) {
            statusLabel.setText("Status: Number of visitors must contain numbers only");
            return;
        }

        try {
            int orderNumber = Integer.parseInt(orderNumberText);
            int numberOfVisitors = Integer.parseInt(numberOfVisitorsText);

            if (numberOfVisitors < 1 || numberOfVisitors > 15) {
                statusLabel.setText("Status: Number of visitors must be between 1 and 15");
                return;
            }

            if (!connectToServer()) {
                return;
            }

            ClientRequest request =
                    new ClientRequest(
                            "UPDATE_ORDER",
                            orderNumber,
                            orderDate,
                            numberOfVisitors
                    );

            client.sendRequest(request);

            statusLabel.setText("Status: Update request sent");

        } catch (NumberFormatException e) {
            statusLabel.setText("Status: Invalid number input");
        }
    }

    private void endRunning() { //a function that checks if the client has not connected yet to server 
        try {
            if (client != null && client.isConnected()) {
                client.closeConnection();
                client = null;
            }

            statusLabel.setText("Status: Client disconnected");

            dispose();

        } catch (Exception e) {
            statusLabel.setText("Status: Failed to end running");
            System.out.println("Failed to close client: " + e.getMessage());
        }
    }

    private boolean isOnlyNumbers(String text) { //a function that checks if the string contains only numbers 
        return text.matches("\\d+");
    }
    
//a function that checks if the date is correct sytnax 
    private boolean isValidDateFormat(String dateText) {
        if (!Pattern.matches("\\d{4}-\\d{2}-\\d{2}", dateText)) {
            return false;
        }

        try {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("uuuu-MM-dd");

            formatter =
                    formatter.withResolverStyle(java.time.format.ResolverStyle.STRICT);

            LocalDate.parse(dateText, formatter);
            return true;

        } catch (DateTimeParseException e) {
            return false;
        }
    }

    
    //a function that checks if the order date has been reservered 
    private boolean isDateInPast(String dateText) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("uuuu-MM-dd");

        formatter =
                formatter.withResolverStyle(java.time.format.ResolverStyle.STRICT);

        LocalDate orderDate = LocalDate.parse(dateText, formatter);
        LocalDate today = LocalDate.now();

        return orderDate.isBefore(today);
    }

    
    //a functions to display order 
    public void displayOrder(Order order) {
        orderNumberField.setText(String.valueOf(order.getOrderNumber()));
        orderDateField.setText(order.getOrderDate());
        numberOfVisitorsField.setText(String.valueOf(order.getNumberOfVisitors()));
        confirmationCodeField.setText(String.valueOf(order.getConfirmationCode()));
        subscriberIdField.setText(String.valueOf(order.getSubscriberId()));
        dateOfPlacingOrderField.setText(order.getDateOfPlacingOrder());
    }
//a function to show status 
    public void showStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }
//a function to clear fields
    private void clearFields() {
        orderNumberField.setText("");
        orderDateField.setText("");
        numberOfVisitorsField.setText("");
        confirmationCodeField.setText("");
        subscriberIdField.setText("");
        dateOfPlacingOrderField.setText("");

        statusLabel.setText("Status: Fields cleared");
    }
}