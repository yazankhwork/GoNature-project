package client;

import client.gui.ClientConnectionScreen;
import javafx.application.Application;

/**
 * Main entry point for the GoNature client application. This class only
 * launches the first JavaFX screen.
 */
public class ClientMain {

	public static void main(String[] args) {
		Application.launch(ClientConnectionScreen.class, args);
	}
}