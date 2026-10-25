package dao;

import database.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    // ---------------- PATIENT REGISTRATION ----------------

    public boolean registerPatient(
            String name, int age, String gender,
            String contact, String address, String bloodGroup)
            throws SQLException {

        String sql = """
            INSERT INTO patients
            (name, age, gender, contact, address, bloodGroup)
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

    // ---------------- PATIENT LOGIN ----------------

    public int loginPatient(String name, String contact)
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
                return rs.next() ? rs.getInt("patient_id") : -1;
            }
        }
    }

    // ---------------- PATIENT PROFILE ----------------

    public PatientProfile getPatientById(int patientId)
            throws SQLException {

        String sql = """
            SELECT patient_id, name, age, contact,
                   gender, address, bloodGroup
            FROM patients
            WHERE patient_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PatientProfile(
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("contact"),
                        rs.getString("gender"),
                        rs.getString("address"),
                        rs.getString("bloodGroup")
                    );
                }
            }
        }

        return null;
    }

    // ---------------- FIND DOCTORS ----------------

    public List<DoctorInfo> getDoctors() throws SQLException {
        List<DoctorInfo> doctors = new ArrayList<>();

        String sql = """
            SELECT doctor_id, name, specialization
            FROM doctors
            ORDER BY name
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                doctors.add(new DoctorInfo(
                    rs.getInt("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialization")
                ));
            }
        }

        return doctors;
    }

    // ---------------- BOOK APPOINTMENT ----------------

    // Returns false if the requested doctor/time slot is occupied.
    public boolean bookAppointment(
            int patientId, int doctorId,
            LocalDate date, LocalTime time)
            throws SQLException {

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Appointment date cannot be in the past."
            );
        }

        String checkSql = """
            SELECT COUNT(*)
            FROM appointments
            WHERE doctor_id = ?
              AND appointment_date = ?
              AND appointment_time = ?
              AND status NOT IN ('Cancelled', 'Rejected')
            """;

        String insertSql = """
            INSERT INTO appointments
            (patient_id, doctor_id, appointment_date,
             appointment_time, status)
            VALUES (?, ?, ?, ?, 'Scheduled')
            """;

        try (Connection con = DBConnection.getConnection()) {

            try (PreparedStatement check =
                     con.prepareStatement(checkSql)) {

                check.setInt(1, doctorId);
                check.setDate(2, Date.valueOf(date));
                check.setTime(3, Time.valueOf(time));

                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false;
                    }
                }
            }

            try (PreparedStatement ps =
                     con.prepareStatement(insertSql)) {

                ps.setInt(1, patientId);
                ps.setInt(2, doctorId);
                ps.setDate(3, Date.valueOf(date));
                ps.setTime(4, Time.valueOf(time));

                return ps.executeUpdate() == 1;
            }
        }
    }

    // ---------------- MY APPOINTMENTS ----------------

    public List<AppointmentInfo> getAppointmentsForPatient(int patientId)
            throws SQLException {

        List<AppointmentInfo> appointments = new ArrayList<>();

        String sql = """
            SELECT a.appointment_id, d.name, d.specialization,
                   a.appointment_date, a.appointment_time, a.status
            FROM appointments a
            JOIN doctors d ON a.doctor_id = d.doctor_id
            WHERE a.patient_id = ?
            ORDER BY a.appointment_date DESC, a.appointment_time
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new AppointmentInfo(
                        rs.getInt("appointment_id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getDate("appointment_date").toString(),
                        rs.getTime("appointment_time").toString(),
                        rs.getString("status")
                    ));
                }
            }
        }

        return appointments;
    }

    // A patient may cancel their own scheduled or approved appointment.
    public boolean cancelAppointment(int patientId, int appointmentId)
            throws SQLException {

        String sql = """
            UPDATE appointments
            SET status = 'Cancelled'
            WHERE appointment_id = ?
              AND patient_id = ?
              AND status IN ('Scheduled', 'Approved')
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, appointmentId);
            ps.setInt(2, patientId);

            return ps.executeUpdate() == 1;
        }
    }

    // ---------------- DIAGNOSIS AND PRESCRIPTIONS ----------------

    public List<MedicalRecord> getMedicalRecords(int patientId)
            throws SQLException {

        List<MedicalRecord> records = new ArrayList<>();

        String sql = """
            SELECT a.appointment_id, a.appointment_date,
                   d.name AS doctor_name,
                   c.diagnosis, c.prescription, c.notes
            FROM consultations c
            JOIN appointments a
              ON c.appointment_id = a.appointment_id
            JOIN doctors d
              ON a.doctor_id = d.doctor_id
            WHERE a.patient_id = ?
            ORDER BY a.appointment_date DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(new MedicalRecord(
                        rs.getInt("appointment_id"),
                        rs.getDate("appointment_date").toString(),
                        rs.getString("doctor_name"),
                        rs.getString("diagnosis"),
                        rs.getString("prescription"),
                        rs.getString("notes")
                    ));
                }
            }
        }

        return records;
    }

    // ---------------- DATA CLASSES ----------------

    public static class PatientProfile {
        public final int patientId;
        public final String name;
        public final int age;
        public final String contact;
        public final String gender;
        public final String address;
        public final String bloodGroup;

        public PatientProfile(
                int patientId, String name, int age,
                String contact, String gender,
                String address, String bloodGroup) {

            this.patientId = patientId;
            this.name = name;
            this.age = age;
            this.contact = contact;
            this.gender = gender;
            this.address = address;
            this.bloodGroup = bloodGroup;
        }
    }

    public static class DoctorInfo {
        public final int doctorId;
        public final String name;
        public final String specialization;

        public DoctorInfo(int doctorId, String name, String specialization) {
            this.doctorId = doctorId;
            this.name = name;
            this.specialization = specialization;
        }

        @Override
        public String toString() {
            return name + " - " + specialization;
        }
    }

    public static class AppointmentInfo {
        public final int appointmentId;
        public final String doctorName;
        public final String specialization;
        public final String date;
        public final String time;
        public final String status;

        public AppointmentInfo(
                int appointmentId, String doctorName,
                String specialization, String date,
                String time, String status) {

            this.appointmentId = appointmentId;
            this.doctorName = doctorName;
            this.specialization = specialization;
            this.date = date;
            this.time = time;
            this.status = status;
        }
    }

    public static class MedicalRecord {
        public final int appointmentId;
        public final String date;
        public final String doctorName;
        public final String diagnosis;
        public final String prescription;
        public final String notes;

        public MedicalRecord(
                int appointmentId, String date, String doctorName,
                String diagnosis, String prescription, String notes) {

            this.appointmentId = appointmentId;
            this.date = date;
            this.doctorName = doctorName;
            this.diagnosis = diagnosis;
            this.prescription = prescription;
            this.notes = notes;
        }
    }
}