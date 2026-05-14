package client;

import javax.swing.*;
import java.awt.*;

public class ClientConnectionGUI extends JFrame {

    private JTextField ipField;
    private JLabel statusLabel;

    private static final int SERVER_PORT = 5555;

    public ClientConnectionGUI() {
        setTitle("GoNature - Connect To Server");
        setSize(450, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Enter Server IP Address", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        ipField = new JTextField();
        ipField.setHorizontalAlignment(JTextField.CENTER);
        ipField.setText("localhost");

        JButton connectButton = new JButton("Connect");

        statusLabel = new JLabel("Status: Enter IP and press Connect", SwingConstants.CENTER);

        panel.add(titleLabel);
        panel.add(ipField);
        panel.add(connectButton);
        panel.add(statusLabel);

        add(panel);

        connectButton.addActionListener(e -> checkConnectionAndOpenClient());
    }

    private void checkConnectionAndOpenClient() {
        String serverIP = ipField.getText().trim();

        if (serverIP.isEmpty()) {
            statusLabel.setText("Status: Please enter server IP");
            return;
        }

        PrototypeClient testClient = null;

        try {
            statusLabel.setText("Status: Trying to connect...");

            testClient = new PrototypeClient(serverIP, SERVER_PORT, null);
            testClient.openConnection();

            statusLabel.setText("Status: Connected successfully");

            try {
                testClient.closeConnection();
            } catch (Exception ex) {
                System.out.println("Could not close test connection: " + ex.getMessage());
            }

            dispose();

            ClientGUI clientGUI = new ClientGUI(serverIP);
            clientGUI.setVisible(true);

        } catch (Exception e) {
            statusLabel.setText("Status: Wrong IP or server is not running");

            JOptionPane.showMessageDialog(
                    this,
                    "Cannot connect to server.\n\nPlease check:\n"
                            + "1. The IP address is correct\n"
                            + "2. The server is running\n"
                            + "3. The port is 5555",
                    "Connection Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

	
}