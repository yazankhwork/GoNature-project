package client;

import java.io.IOException;

public class ClientMain {

    public static void main(String[] args) {
        try {
            PrototypeClient client = new PrototypeClient("localhost", 5555);
            client.openConnection();

            System.out.println("Client connected to server");

        } catch (IOException e) {
            System.out.println("Client connection failed: " + e.getMessage());
        }
    }
}
