package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    // 1. Сложили сме порта :1433 след localhost
    // 2. Коригирахме името на MSSQLSERVER01
    // 3. Използваме двойна наклонена черта \\
	private static final String CONNECTION_URL = 
		    "jdbc:sqlserver://localhost:1433;" +
		    "databaseName=AutoMasterDB;" +
		    "integratedSecurity=true;" + 
		    "encrypt=true;" +
		    "trustServerCertificate=true;";

    public static Connection getConnection() {
        try {
            // За новите версии на драйвера този ред вече не е задължителен, но не пречи
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            Connection conn = DriverManager.getConnection(CONNECTION_URL);
            if (conn != null) {
                System.out.println("Успешно свързване с AutoMasterDB!");
            }
            return conn;
            
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Грешка при връзка с БД: " + e.getMessage());
            return null;
        }
    }
}