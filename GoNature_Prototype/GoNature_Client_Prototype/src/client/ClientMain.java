package client;

import javax.swing.SwingUtilities;

/**
 * Main class of the client application.
 * It opens the connection window where the user enters the server IP.
 */
public class ClientMain {

    public static void main(String[] args) {
        // Runs the GUI safely on Swing's Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ClientConnectionGUI connectionGUI = new ClientConnectionGUI();
            
            // Shows the connection window
            connectionGUI.setVisible(true);
        });
    }
}