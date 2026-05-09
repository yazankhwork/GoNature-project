package client;

import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {

    private JTextField orderNumberField;
    private JTextField orderDateField;
    private JTextField numberOfVisitorsField;
    private JTextField confirmationCodeField;
    private JTextField subscriberIdField;
    private JTextField dateOfPlacingOrderField;
    private JLabel statusLabel;

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
    }

    private void loadOrder() {
        String orderNumber = orderNumberField.getText();

        if (orderNumber.isEmpty()) {
            statusLabel.setText("Status: Please enter order number");
            return;
        }

        /*
         Later:
         The client will send LOAD_ORDER request to the server.
         The server will read the order from the database and return the data.
        */

        // Temporary data for GUI test only
        orderDateField.setText("2026-05-25");
        numberOfVisitorsField.setText("6");
        confirmationCodeField.setText("12345");
        subscriberIdField.setText("2001");
        dateOfPlacingOrderField.setText("2026-05-01");

        statusLabel.setText("Status: Order loaded successfully");
    }

    private void updateOrder() {
        String orderNumber = orderNumberField.getText();
        String orderDate = orderDateField.getText();
        String numberOfVisitors = numberOfVisitorsField.getText();

        if (orderNumber.isEmpty()) {
            statusLabel.setText("Status: Please enter order number");
            return;
        }

        if (orderDate.isEmpty()) {
            statusLabel.setText("Status: Please enter order date");
            return;
        }

        if (numberOfVisitors.isEmpty()) {
            statusLabel.setText("Status: Please enter number of visitors");
            return;
        }

        /*
         Later:
         The client will send UPDATE_ORDER request to the server.
         Only these fields should be updated:
         order_date
         number_of_visitors
        */

        statusLabel.setText("Status: Update request sent successfully");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            gui.setVisible(true);
        });
    }
}