package client;

import client.gui.ClientConnectionScreen;
import javafx.application.Application;

/**
 * Entry point of the GoNature client application.
 *
 * This class launches the JavaFX user interface and opens
 * the client connection screen.
 *
 * @author Group 4
 * @version 1.0
 */
public class ClientMain {
	/**
	 * Launches the GoNature client application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		Application.launch(ClientConnectionScreen.class, args);
	}
}