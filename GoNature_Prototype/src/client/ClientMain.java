package client;

import javax.swing.SwingUtilities;

public class ClientMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientConnectionGUI connectionGUI = new ClientConnectionGUI();
            connectionGUI.setVisible(true);
        });
    }
}