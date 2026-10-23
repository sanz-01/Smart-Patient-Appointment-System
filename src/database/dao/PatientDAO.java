
package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PatientDAO {

    public boolean registerPatient(
            String name, int age, String gender,
            String contact, String address, String bloodGroup)
            throws SQLException {

        String sql = """
            INSERT INTO patients
            (name, age, gender, contact, address, blood_group)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, contact);
            ps.setString(5, address);
            ps.setString(6, bloodGroup);

            return ps.executeUpdate() == 1;
        }
    }

    public boolean loginPatient(String name, String contact)
            throws SQLException {

        String sql = """
            SELECT patient_id
            FROM patients
            WHERE name = ? AND contact = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, contact);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
