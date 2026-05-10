package server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBController {
	private static final String URL =
			"jdbc:mysql://localhost:3306/gonature_db?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";

    private static final String USER = "root";
    private static final String PASSWORD = "Y123456y";

    public static Connection connectToDB() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
