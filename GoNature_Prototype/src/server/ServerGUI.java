package server;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {

    private JLabel serverStatusLabel;
    private JLabel clientIpLabel;
    private JLabel clientHostLabel;
    private JLabel connectionStatusLabel;
    private JTextArea logArea;

    private PrototypeServer server;

    private boolean hasConnectedClient = false;
    private Timer noClientTimer;

    private static final int SERVER_PORT = 5555;
    private static final int NO_CLIENT_TIMEOUT = 30000;

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
        try {
            if (server != null) {
                addLog("Server is already running.");
                return;
            }

            hasConnectedClient = false;

            serverStatusLabel.setText("Server Status: Running");
            clientIpLabel.setText("Client IP: Not connected");
            clientHostLabel.setText("Client Host: Not connected");
            connectionStatusLabel.setText("Connection Status: Waiting for client...");

            addLog("Starting server on port " + SERVER_PORT + "...");
            addLog("If no client connects in 30 seconds, server will stop automatically.");

            server = new PrototypeServer(SERVER_PORT, this);
            server.listen();

            startNoClientTimer();

        } catch (Exception e) {
            server = null;
            serverStatusLabel.setText("Server Status: Error");
            connectionStatusLabel.setText("Connection Status: Failed");
            addLog("Server failed to start: " + e.getMessage());
        }
    }

    private void startNoClientTimer() {
        stopNoClientTimer();

        noClientTimer = new Timer(NO_CLIENT_TIMEOUT, e -> {
            if (!hasConnectedClient && server != null) {
                addLog("No client connected within 30 seconds.");
                addLog("Stopping server automatically...");
                stopServer();
            }
        });

        noClientTimer.setRepeats(false);
        noClientTimer.start();
    }

    private void stopNoClientTimer() {
        if (noClientTimer != null) {
            noClientTimer.stop();
            noClientTimer = null;
        }
    }

    private void stopServer() {
        try {
            stopNoClientTimer();

            if (server != null) {
                server.close();
                server = null;
            }

            hasConnectedClient = false;

            serverStatusLabel.setText("Server Status: Stopped");
            clientIpLabel.setText("Client IP: Not connected");
            clientHostLabel.setText("Client Host: Not connected");
            connectionStatusLabel.setText("Connection Status: Disconnected");

            addLog("Server stopped.");

        } catch (Exception e) {
            addLog("Failed to stop server: " + e.getMessage());
        }
    }

    public void clientConnected(String ip, String host) {
        SwingUtilities.invokeLater(() -> {
            hasConnectedClient = true;
            stopNoClientTimer();

            clientIpLabel.setText("Client IP: " + ip);
            clientHostLabel.setText("Client Host: " + host);
            connectionStatusLabel.setText("Connection Status: Connected");

            addLog("Client connected: " + ip + " / " + host);
            addLog("Timer canceled. Server will keep running.");
        });
    }

    public void clientDisconnected() {
        SwingUtilities.invokeLater(() -> {
            hasConnectedClient = false;

            clientIpLabel.setText("Client IP: Not connected");
            clientHostLabel.setText("Client Host: Not connected");
            connectionStatusLabel.setText("Connection Status: Waiting for another client...");

            addLog("Client disconnected.");
            addLog("Starting new 30-second timer for another client...");

            startNoClientTimer();
        });
    }

    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }
}