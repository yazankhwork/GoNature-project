package server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import server.db.DBController;
import server.db.OrderDBQueries;

public class ServerMain {
	public static void main(String[] args) {
		System.out.println("Before update:");
		OrderDBQueries.printAllOrders();

		OrderDBQueries.updateOrder(1, "2026-05-25", 6);

        System.out.println("After update:");
        OrderDBQueries.printAllOrders();
        PrototypeServer server = new PrototypeServer(5555);
        try {
			server.listen();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
