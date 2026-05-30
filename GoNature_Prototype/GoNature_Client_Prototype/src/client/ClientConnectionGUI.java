package client;

import javax.swing.*;
import java.awt.*;

public class ClientConnectionGUI extends JFrame {

    private JTextField ipField;
    private JLabel statusLabel;

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

        connectButton.addActionListener(e -> openClientGUI());
    }

    private void openClientGUI() {
        String serverIP = ipField.getText().trim();

        if (serverIP.isEmpty()) {
            statusLabel.setText("Status: Please enter server IP");
            return;
        }

        /*
         * Important:
         * Do NOT connect here.
         * This screen only takes the IP.
         * The real connection happens only once inside ClientGUI.
         */

        dispose();

        ClientGUI clientGUI = new ClientGUI(serverIP);
        clientGUI.setVisible(true);
    }
}