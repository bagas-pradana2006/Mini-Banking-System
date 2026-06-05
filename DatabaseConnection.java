package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=BankSystem;encrypt=true;trustServerCertificate=true;";
        String user = "Db_Con";
        String password = "12345678";

        try {
            // MANTRA WAJIB: Membangunkan Driver SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver SQL Server tidak ditemukan: " + e.getMessage());
        }

        return DriverManager.getConnection(url, user, password);
    }
}