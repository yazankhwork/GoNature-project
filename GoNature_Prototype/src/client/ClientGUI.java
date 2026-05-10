package client;

import javax.swing.*;
import java.awt.*;
import common.ClientRequest;
import common.Order;

public class ClientGUI extends JFrame {

    private JTextField orderNumberField;
    private JTextField orderDateField;
    private JTextField numberOfVisitorsField;
    private JTextField confirmationCodeField;
    private JTextField subscriberIdField;
    private JTextField dateOfPlacingOrderField;
    private JLabel statusLabel;
    private PrototypeClient client;

    public ClientGUI() {
        setTitle("GoNature - Client GUI");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
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

        statusLabel = new JLabel("Status: Ready");

        panel.add(new JLabel("Order Number:"));
        panel.add(orderNumberField);

        panel.add(new JLabel("Order Date:"));
        panel.add(orderDateField);

        panel.add(new JLabel("Number Of Visitors:"));
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
        panel.add(statusLabel);

        add(panel);

        loadButton.addActionListener(e -> loadOrder());
        updateButton.addActionListener(e -> updateOrder());
        clearButton.addActionListener(e -> clearFields());
        connectToServer();
    }
    
    private void connectToServer() {
        try {
            client = new PrototypeClient("localhost", 5555, this);
            client.openConnection();

            statusLabel.setText("Status: Connected to server");

        } catch (Exception e) {
            statusLabel.setText("Status: Could not connect to server");
            System.out.println("Client connection failed: " + e.getMessage());
        }
    }
    private void loadOrder() {
        String orderNumberText = orderNumberField.getText();

        if (orderNumberText.isEmpty()) {
            statusLabel.setText("Status: Please enter order number");
            return;
        }

        try {
            int orderNumber = Integer.parseInt(orderNumberText);

            ClientRequest request = new ClientRequest("LOAD_ORDER", orderNumber);
            client.sendRequest(request);

            statusLabel.setText("Status: Load request sent");

        } catch (NumberFormatException e) {
            statusLabel.setText("Status: Order number must be a number");
        }
    }
    private void updateOrder() {
        String orderNumberText = orderNumberField.getText();
        String orderDate = orderDateField.getText();
        String numberOfVisitorsText = numberOfVisitorsField.getText();

        if (orderNumberText.isEmpty()) {
            statusLabel.setText("Status: Please enter order number");
            return;
        }

        if (orderDate.isEmpty()) {
            statusLabel.setText("Status: Please enter order date");
            return;
        }

        if (numberOfVisitorsText.isEmpty()) {
            statusLabel.setText("Status: Please enter number of visitors");
            return;
        }

        try {
            int orderNumber = Integer.parseInt(orderNumberText);
            int numberOfVisitors = Integer.parseInt(numberOfVisitorsText);

            ClientRequest request = new ClientRequest(
                "UPDATE_ORDER",
                orderNumber,
                orderDate,
                numberOfVisitors
            );

            client.sendRequest(request);

            statusLabel.setText("Status: Update request sent");

        } catch (NumberFormatException e) {
            statusLabel.setText("Status: Order number and visitors must be numbers");
        }
    }
    public void displayOrder(Order order) {
        orderNumberField.setText(String.valueOf(order.getOrderNumber()));
        orderDateField.setText(order.getOrderDate());
        numberOfVisitorsField.setText(String.valueOf(order.getNumberOfVisitors()));
        confirmationCodeField.setText(String.valueOf(order.getConfirmationCode()));
        subscriberIdField.setText(String.valueOf(order.getSubscriberId()));
        dateOfPlacingOrderField.setText(order.getDateOfPlacingOrder());
    }
    public void showStatus(String message) {
        statusLabel.setText(message);
    }
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