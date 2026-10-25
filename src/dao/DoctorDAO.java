package dao;

import database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public DoctorProfile getDoctorById(int doctorId)
            throws SQLException {

        String sql = """
            SELECT doctor_id, name, specialization
            FROM doctors WHERE doctor_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DoctorProfile(
                        rs.getInt("doctor_id"),
                        rs.getString("name"),
                        rs.getString("specialization")
                    );
                }
            }
        }
        return null;
    }

    public List<DoctorAppointment> getAppointments(int doctorId)
            throws SQLException {

        List<DoctorAppointment> list = new ArrayList<>();

        String sql = """
            SELECT a.appointment_id, a.patient_id, p.name,
                   p.age, p.contact, a.appointment_date,
                   a.appointment_time, a.status
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            WHERE a.doctor_id = ?
            ORDER BY a.appointment_date DESC, a.appointment_time
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new DoctorAppointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("contact"),
                        rs.getDate("appointment_date").toString(),
                        rs.getTime("appointment_time").toString(),
                        rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }

    public List<AssignedPatient> getAssignedPatients(int doctorId)
            throws SQLException {

        List<AssignedPatient> list = new ArrayList<>();

        String sql = """
            SELECT DISTINCT p.patient_id, p.name, p.age, p.contact,
                   p.gender, p.address, p.bloodGroup
            FROM patients p
            JOIN appointments a ON a.patient_id = p.patient_id
            WHERE a.doctor_id = ?
            ORDER BY p.name
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AssignedPatient(
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("contact"),
                        rs.getString("gender"),
                        rs.getString("address"),
                        rs.getString("bloodGroup")
                    ));
                }
            }
        }
        return list;
    }

    public boolean updateAppointmentStatus(
            int doctorId, int appointmentId, String newStatus)
            throws SQLException {

        if (!List.of("Approved", "Rejected", "Completed")
                .contains(newStatus)) {
            throw new IllegalArgumentException("Invalid appointment status.");
        }

        String sql = """
            UPDATE appointments
            SET status = ?
            WHERE appointment_id = ?
              AND doctor_id = ?
              AND status IN ('Scheduled', 'Approved')
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, appointmentId);
            ps.setInt(3, doctorId);

            return ps.executeUpdate() == 1;
        }
    }

    public ConsultationRecord getConsultation(
            int doctorId, int appointmentId) throws SQLException {

        String sql = """
            SELECT c.consultation_id, c.diagnosis,
                   c.prescription, c.notes
            FROM consultations c
            JOIN appointments a
              ON a.appointment_id = c.appointment_id
            WHERE a.doctor_id = ? AND a.appointment_id = ?
            ORDER BY c.consultation_id DESC
            LIMIT 1
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setInt(2, appointmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ConsultationRecord(
                        rs.getInt("consultation_id"),
                        rs.getString("diagnosis"),
                        rs.getString("prescription"),
                        rs.getString("notes")
                    );
                }
            }
        }
        return null;
    }

    public void saveConsultation(
            int doctorId, int appointmentId,
            String diagnosis, String prescription, String notes)
            throws SQLException {

        if (diagnosis.isBlank() || prescription.isBlank()) {
            throw new IllegalArgumentException(
                "Diagnosis and prescription are required."
            );
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                // Confirm that this appointment belongs to this doctor.
                String checkSql = """
                    SELECT appointment_id
                    FROM appointments
                    WHERE appointment_id = ? AND doctor_id = ?
                    """;

                try (PreparedStatement ps =
                         con.prepareStatement(checkSql)) {

                    ps.setInt(1, appointmentId);
                    ps.setInt(2, doctorId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                "Appointment not found for this doctor."
                            );
                        }
                    }
                }

                Integer consultationId = null;

                String findSql = """
                    SELECT consultation_id
                    FROM consultations
                    WHERE appointment_id = ?
                    ORDER BY consultation_id DESC
                    LIMIT 1
                    """;

                try (PreparedStatement ps =
                         con.prepareStatement(findSql)) {

                    ps.setInt(1, appointmentId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            consultationId =
                                rs.getInt("consultation_id");
                        }
                    }
                }

                if (consultationId == null) {
                    String insertSql = """
                        INSERT INTO consultations
                            (appointment_id, diagnosis,
                             prescription, notes)
                        VALUES (?, ?, ?, ?)
                        """;

                    try (PreparedStatement ps =
                             con.prepareStatement(insertSql)) {

                        ps.setInt(1, appointmentId);
                        ps.setString(2, diagnosis.trim());
                        ps.setString(3, prescription.trim());
                        ps.setString(4, notes.trim());
                        ps.executeUpdate();
                    }

                } else {
                    String updateSql = """
                        UPDATE consultations
                        SET diagnosis = ?, prescription = ?, notes = ?
                        WHERE consultation_id = ?
                        """;

                    try (PreparedStatement ps =
                             con.prepareStatement(updateSql)) {

                        ps.setString(1, diagnosis.trim());
                        ps.setString(2, prescription.trim());
                        ps.setString(3, notes.trim());
                        ps.setInt(4, consultationId);
                        ps.executeUpdate();
                    }
                }

                con.commit();

            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public static class DoctorProfile {
        public final int doctorId;
        public final String name;
        public final String specialization;

        public DoctorProfile(
                int doctorId, String name, String specialization) {
            this.doctorId = doctorId;
            this.name = name;
            this.specialization = specialization;
        }
    }

    public static class DoctorAppointment {
        public final int appointmentId;
        public final int patientId;
        public final String patientName;
        public final int age;
        public final String contact;
        public final String date;
        public final String time;
        public final String status;

        public DoctorAppointment(
                int appointmentId, int patientId, String patientName,
                int age, String contact, String date, String time,
                String status) {

            this.appointmentId = appointmentId;
            this.patientId = patientId;
            this.patientName = patientName;
            this.age = age;
            this.contact = contact;
            this.date = date;
            this.time = time;
            this.status = status;
        }

        @Override
        public String toString() {
            return "#" + appointmentId + " - " + patientName
                + " - " + date + " " + time + " (" + status + ")";
        }
    }

    public static class AssignedPatient {
        public final int patientId;
        public final String name;
        public final int age;
        public final String contact;
        public final String gender;
        public final String address;
        public final String bloodGroup;

        public AssignedPatient(
                int patientId, String name, int age, String contact,
                String gender, String address, String bloodGroup) {

            this.patientId = patientId;
            this.name = name;
            this.age = age;
            this.contact = contact;
            this.gender = gender;
            this.address = address;
            this.bloodGroup = bloodGroup;
        }
    }

    public static class ConsultationRecord {
        public final int consultationId;
        public final String diagnosis;
        public final String prescription;
        public final String notes;

        public ConsultationRecord(
                int consultationId, String diagnosis,
                String prescription, String notes) {

            this.consultationId = consultationId;
            this.diagnosis = diagnosis;
            this.prescription = prescription;
            this.notes = notes;
        }
    }
}
