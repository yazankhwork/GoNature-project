package server.main;

import server.gui.ServerGUI;
import javafx.application.Application;

/**
 * Main entry point for the GoNature server application. 
 * This class only launches the Server JavaFX screen.
 */
public class ServerMain {

	public static void main(String[] args) {
		Application.launch(ServerGUI.class, args);
	}
}