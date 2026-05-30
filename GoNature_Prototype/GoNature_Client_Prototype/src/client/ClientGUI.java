package client;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import common.ClientRequest;
import common.Order;

/**
 * ClientGUI is the main client window.
 * It connects to the server, loads orders, updates orders,
 * validates user input, displays order details, and disconnects safely.
 */
public class ClientGUI extends JFrame {

    private JTextField orderNumberField;
    private JTextField orderDateField;
    private JTextField numberOfVisitorsField;
    private JTextField confirmationCodeField;
    private JTextField subscriberIdField;
    private JTextField dateOfPlacingOrderField;

    private JLabel statusLabel;

    private PrototypeClient client;
    private String serverHost;

    private static final int SERVER_PORT = 5555;

    public ClientGUI() {
        this("localhost");
    }

    public ClientGUI(String serverHost) {
        this.serverHost = serverHost;

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

        loadButton.addActionListener(e -> loadOrder());
        updateButton.addActionListener(e -> updateOrder());
        clearButton.addActionListener(e -> clearFields());
        endRunningButton.addActionListener(e -> endRunning());

        connectToServer();
    }

    // Connects the client to the server
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

    // Sends a request to load an order from the server
    private void loadOrder() {
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

            ClientRequest request = new ClientRequest("LOAD_ORDER", orderNumber);
            client.sendRequest(request);

            statusLabel.setText("Status: Load request sent");

        } catch (NumberFormatException e) {
            statusLabel.setText("Status: Invalid order number");
        }
    }

    // Validates input and sends an update request to the server
    private void updateOrder() {
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

            ClientRequest request = new ClientRequest(
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

    // Disconnects the client and closes the window
    private void endRunning() {
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

    // Checks if the text contains numbers only
    private boolean isOnlyNumbers(String text) {
        return text.matches("\\d+");
    }

    // Checks if the date format is valid: yyyy-MM-dd
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

    // Checks if the order date is before today
    private boolean isDateInPast(String dateText) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("uuuu-MM-dd");

        formatter =
                formatter.withResolverStyle(java.time.format.ResolverStyle.STRICT);

        LocalDate orderDate = LocalDate.parse(dateText, formatter);
        LocalDate today = LocalDate.now();

        return orderDate.isBefore(today);
    }

    // Displays order details received from the server
    public void displayOrder(Order order) {
        orderNumberField.setText(String.valueOf(order.getOrderNumber()));
        orderDateField.setText(order.getOrderDate());
        numberOfVisitorsField.setText(String.valueOf(order.getNumberOfVisitors()));
        confirmationCodeField.setText(String.valueOf(order.getConfirmationCode()));
        subscriberIdField.setText(String.valueOf(order.getSubscriberId()));
        dateOfPlacingOrderField.setText(order.getDateOfPlacingOrder());
    }

    // Updates the status label safely in Swing
    public void showStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    // Clears all fields in the GUI
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