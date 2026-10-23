
package database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDB {

    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) {

            if (con != null && !con.isClosed()) {
                System.out.println("SUCCESS: Database connected!");
                System.out.println("Database: "
                        + con.getCatalog());
            }

        } catch (SQLException e) {
            System.out.println("ERROR: Database connection failed!");
            System.out.println("Reason: " + e.getMessage());
        }
    }
}
