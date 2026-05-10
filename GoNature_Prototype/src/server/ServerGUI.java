package server;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {

    private JLabel serverStatusLabel;
    private JLabel clientIpLabel;
    private JLabel clientHostLabel;
    private JLabel connectionStatusLabel;
    private JTextArea logArea;

    public ServerGUI() {
        setTitle("GoNature - Server GUI");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        serverStatusLabel = new JLabel("Server Status: Stopped");
        clientIpLabel = new JLabel("Client IP: Not connected");
        clientHostLabel = new JLabel("Client Host: Not connected");
        connectionStatusLabel = new JLabel("Connection Status: Disconnected");

        JButton startButton = new JButton("Start Server");
        JButton stopButton = new JButton("Stop Server");
        JButton clearLogButton = new JButton("Clear Log");

        logArea = new JTextArea();
        logArea.setEditable(false);

        JPanel topPanel = new JPanel(new GridLayout(7, 1, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        topPanel.add(serverStatusLabel);
        topPanel.add(clientIpLabel);
        topPanel.add(clientHostLabel);
        topPanel.add(connectionStatusLabel);
        topPanel.add(startButton);
        topPanel.add(stopButton);
        topPanel.add(clearLogButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        clearLogButton.addActionListener(e -> logArea.setText(""));
    }

    private void startServer() {
        serverStatusLabel.setText("Server Status: Running");
        connectionStatusLabel.setText("Connection Status: Waiting for client...");
        addLog("Server started successfully"); 
    }

    private void stopServer() {
        /*
         Later:
         Here we will stop the real server.
        */

        serverStatusLabel.setText("Server Status: Stopped");
        clientIpLabel.setText("Client IP: Not connected");
        clientHostLabel.setText("Client Host: Not connected");
        connectionStatusLabel.setText("Connection Status: Disconnected");
    }

    public void updateClientInfo(String ip, String host) {
        clientIpLabel.setText("Client IP: " + ip);
        clientHostLabel.setText("Client Host: " + host);
        connectionStatusLabel.setText("Connection Status: Connected");

        addLog("Client connected: " + ip + " / " + host);
    }

    public void addLog(String message) {
        logArea.append(message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
}