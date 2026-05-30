package client;

import javax.swing.*;
import java.awt.*;

/**
 * This class creates the first window of the client application.
 * It asks the user to enter the server IP address.
 */
public class ClientConnectionGUI extends JFrame {

    private JTextField ipField;
    private JLabel statusLabel;

    public ClientConnectionGUI() {
        setTitle("GoNature - Connect To Server");
        setSize(450, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel layout
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title label
        JLabel titleLabel = new JLabel("Enter Server IP Address", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Text field for entering the server IP
        ipField = new JTextField();
        ipField.setHorizontalAlignment(JTextField.CENTER);
        ipField.setText("localhost");

        JButton connectButton = new JButton("Connect");

        // Status message label
        statusLabel = new JLabel("Status: Enter IP and press Connect", SwingConstants.CENTER);

        panel.add(titleLabel);
        panel.add(ipField);
        panel.add(connectButton);
        panel.add(statusLabel);

        add(panel);

        // When the user clicks Connect, open the main client GUI
        connectButton.addActionListener(e -> openClientGUI());
    }

    private void openClientGUI() {
        String serverIP = ipField.getText().trim();

        // Check if the IP field is empty
        if (serverIP.isEmpty()) {
            statusLabel.setText("Status: Please enter server IP");
            return;
        }

        // Close this window
        dispose();

        // Open the main client window and pass the server IP to it
        ClientGUI clientGUI = new ClientGUI(serverIP);
        clientGUI.setVisible(true);
    }
}