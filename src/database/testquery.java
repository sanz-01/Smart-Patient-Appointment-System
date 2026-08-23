package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class testquery {

    public static void main(String[] args) {

        String sql = "SELECT * FROM patients";

        try (
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                System.out.println(
                    "ID: " + rs.getInt("patient_id") +
                    ", Name: " + rs.getString("name") +
                    ", Age: " + rs.getInt("age") +
                    ", Contact: " + rs.getString("contact")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}