
package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();

        try (FileInputStream input =
                 new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new SQLException(
                "Cannot read config.properties", e
            );
        }

        return DriverManager.getConnection(
            properties.getProperty("db.url"),
            properties.getProperty("db.user"),
            properties.getProperty("db.password")
        );
    }
}
