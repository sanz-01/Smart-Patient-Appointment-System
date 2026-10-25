
package ui.admin;

import database.DBConnection;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboardFrame extends JFrame {

    private final Color navy = new Color(24, 45, 82);
    private final Color background = new Color(245, 248, 252);
    private final Color accent = new Color(48, 105, 190);

    public AdminDashboardFrame() {
        setTitle("Admin Dashboard - Smart Care");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(background);
        main.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(navy);
        header.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("ADMIN DASHBOARD");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Smart Patient Appointment System");
        subtitle.setForeground(Color.WHITE);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            new ui.common.WelcomeFrame().setVisible(true);
            dispose();
        });

        header.add(heading, BorderLayout.CENTER);
        header.add(logout, BorderLayout.EAST);

        JLabel welcome = new JLabel("Welcome, Administrator!");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcome.setForeground(navy);
        welcome.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel cards = new JPanel(new GridLayout(2, 2, 18, 18));
        cards.setBackground(background);

        cards.add(createCard(
                "Patient Details",
                "View, add, update and delete patients",
                this::showPatients
        ));

        cards.add(createCard(
                "Doctor Details",
                "View and manage doctor records",
                this::showDoctors
        ));

        cards.add(createCard(
                "Appointments",
                "View appointments and update status",
                this::showAppointments
        ));

        cards.add(createCard(
                "Reports",
                "View system counts and appointment summary",
                this::showReports
        ));

        JPanel center = new JPanel(new BorderLayout(15, 20));
        center.setOpaque(false);
        center.add(welcome, BorderLayout.NORTH);
        center.add(cards, BorderLayout.CENTER);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        setContentPane(main);
    }

    private JPanel createCard(
            String title, String description, Runnable action) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 238)),
                BorderFactory.createEmptyBorder(20, 18, 20, 18)
        ));

        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(navy);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel(
                "<html><div style='text-align:center;'>"
                + description + "</div></html>"
        );
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton button = new JButton("Open");
        button.setBackground(accent);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.addActionListener(e -> action.run());

        card.add(label);
        card.add(Box.createVerticalStrut(12));
        card.add(desc);
        card.add(Box.createVerticalGlue());
        card.add(button);

        return card;
    }

    private DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void loadTable(DefaultTableModel model, String sql) {
        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            int count = model.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[count];

                for (int i = 0; i < count; i++) {
                    row[i] = rs.getObject(i + 1);
                }

                model.addRow(row);
            }

        } catch (SQLException ex) {
            showError("Could not load records: " + ex.getMessage());
        }
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        return table;
    }

    private JButton actionButton(
            String label, java.awt.event.ActionListener action) {
        JButton button = new JButton(label);
        button.addActionListener(action);
        return button;
    }

    private void showTableWindow(
            String title, JTable table, JPanel buttons) {

        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(850, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ---------------- PATIENT MANAGEMENT ----------------

    private void showPatients() {
        DefaultTableModel model = tableModel(
                "Patient ID", "Name", "Age", "Contact",
                "Gender", "Address", "Blood Group");

        JTable table = createTable(model);

        String sql = "SELECT patient_id, name, age, contact, " +
                     "gender, address, bloodGroup " +
                     "FROM patients ORDER BY patient_id";

        Runnable refresh = () -> loadTable(model, sql);
        refresh.run();

        JPanel buttons = new JPanel(new FlowLayout());

        buttons.add(actionButton("Add Patient", e -> {
            Object[] data = patientForm(null);

            if (data != null) {
                savePatient(null, data);
                refresh.run();
            }
        }));

        buttons.add(actionButton("Edit Patient", e -> {
            int row = table.getSelectedRow();

            if (row < 0) {
                showError("Select a patient first.");
                return;
            }

            int r = table.convertRowIndexToModel(row);

            int id = ((Number) model.getValueAt(r, 0)).intValue();

            Object[] old = {
                model.getValueAt(r, 1),
                model.getValueAt(r, 2),
                model.getValueAt(r, 3),
                model.getValueAt(r, 4),
                model.getValueAt(r, 5),
                model.getValueAt(r, 6)
            };

            Object[] data = patientForm(old);

            if (data != null) {
                savePatient(id, data);
                refresh.run();
            }
        }));

        buttons.add(actionButton("Delete Patient", e -> {
            int row = table.getSelectedRow();

            if (row < 0) {
                showError("Select a patient first.");
                return;
            }

            int r = table.convertRowIndexToModel(row);
            int id = ((Number) model.getValueAt(r, 0)).intValue();

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Delete this patient record?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer == JOptionPane.YES_OPTION) {
                deleteRecord(
                        "DELETE FROM patients WHERE patient_id = ?", id);
                refresh.run();
            }
        }));

        buttons.add(actionButton("Refresh", e -> refresh.run()));

        showTableWindow("Patient Details", table, buttons);
    }

    private Object[] patientForm(Object[] old) {
        JTextField name = new JTextField(old == null ? "" :
                String.valueOf(old[0]), 18);
        JTextField age = new JTextField(old == null ? "" :
                String.valueOf(old[1]), 18);
        JTextField contact = new JTextField(old == null ? "" :
                String.valueOf(old[2]), 18);
        JTextField gender = new JTextField(old == null || old[3] == null ?
                "" : String.valueOf(old[3]), 18);
        JTextField address = new JTextField(old == null || old[4] == null ?
                "" : String.valueOf(old[4]), 18);
        JTextField blood = new JTextField(old == null || old[5] == null ?
                "" : String.valueOf(old[5]), 18);

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));

        panel.add(new JLabel("Name:"));
        panel.add(name);
        panel.add(new JLabel("Age:"));
        panel.add(age);
        panel.add(new JLabel("Contact:"));
        panel.add(contact);
        panel.add(new JLabel("Gender:"));
        panel.add(gender);
        panel.add(new JLabel("Address:"));
        panel.add(address);
        panel.add(new JLabel("Blood Group:"));
        panel.add(blood);

        int result = JOptionPane.showConfirmDialog(
                this, panel,
                old == null ? "Add Patient" : "Edit Patient",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        if (name.getText().trim().isEmpty()
                || age.getText().trim().isEmpty()
                || contact.getText().trim().isEmpty()) {
            showError("Name, age and contact are required.");
            return null;
        }

        try {
            int a = Integer.parseInt(age.getText().trim());

            if (a < 0) {
                showError("Age cannot be negative.");
                return null;
            }

            return new Object[] {
                name.getText().trim(),
                a,
                contact.getText().trim(),
                gender.getText().trim(),
                address.getText().trim(),
                blood.getText().trim()
            };

        } catch (NumberFormatException ex) {
            showError("Age must be a valid number.");
            return null;
        }
    }

    private void savePatient(Integer id, Object[] data) {
        String sql;

        if (id == null) {
            sql = "INSERT INTO patients " +
                  "(name, age, contact, gender, address, bloodGroup) " +
                  "VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE patients SET name=?, age=?, contact=?, " +
                  "gender=?, address=?, bloodGroup=? WHERE patient_id=?";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, (String) data[0]);
            ps.setInt(2, (Integer) data[1]);
            ps.setString(3, (String) data[2]);
            ps.setString(4, (String) data[3]);
            ps.setString(5, (String) data[4]);
            ps.setString(6, (String) data[5]);

            if (id != null) {
                ps.setInt(7, id);
            }

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this, id == null ?
                    "Patient added successfully." :
                    "Patient updated successfully.");

        } catch (SQLException ex) {
            showError("Could not save patient: " + ex.getMessage());
        }
    }

    // ---------------- DOCTOR MANAGEMENT ----------------

    private void showDoctors() {
        DefaultTableModel model = tableModel(
                "Doctor ID", "Name", "Specialization");

        JTable table = createTable(model);

        String sql = "SELECT doctor_id, name, specialization " +
                     "FROM doctors ORDER BY doctor_id";

        Runnable refresh = () -> loadTable(model, sql);
        refresh.run();

        JPanel buttons = new JPanel(new FlowLayout());

        buttons.add(actionButton("Add Doctor", e -> {
            String name = prompt("Doctor name:");
            if (name == null || name.trim().isEmpty()) return;

            String specialization = prompt("Specialization:");
            if (specialization == null ||
                    specialization.trim().isEmpty()) return;

            String username = prompt("Login username:");
            if (username == null || username.trim().isEmpty()) return;

            JPasswordField passwordField = new JPasswordField(18);

            int result = JOptionPane.showConfirmDialog(
                    this, passwordField, "Doctor login password",
                    JOptionPane.OK_CANCEL_OPTION);

            if (result != JOptionPane.OK_OPTION) return;

            String password = new String(passwordField.getPassword());

            if (password.isEmpty()) {
                showError("Password is required.");
                return;
            }

            addDoctor(name.trim(), specialization.trim(),
                      username.trim(), password);
            refresh.run();
        }));

        buttons.add(actionButton("Edit Doctor", e -> {
            int row = table.getSelectedRow();

            if (row < 0) {
                showError("Select a doctor first.");
                return;
            }

            int r = table.convertRowIndexToModel(row);
            int id = ((Number) model.getValueAt(r, 0)).intValue();

            String oldName = String.valueOf(model.getValueAt(r, 1));
            String oldSpec = String.valueOf(model.getValueAt(r, 2));

            String name = prompt("Doctor name:", oldName);
            if (name == null || name.trim().isEmpty()) return;

            String specialization = prompt(
                    "Specialization:", oldSpec);

            if (specialization == null ||
                    specialization.trim().isEmpty()) return;

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                    "UPDATE doctors SET name=?, specialization=? " +
                    "WHERE doctor_id=?")) {

                ps.setString(1, name.trim());
                ps.setString(2, specialization.trim());
                ps.setInt(3, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        this, "Doctor updated successfully.");

            } catch (SQLException ex) {
                showError("Could not update doctor: " + ex.getMessage());
            }

            refresh.run();
        }));

        buttons.add(actionButton("Delete Doctor", e -> {
            int row = table.getSelectedRow();

            if (row < 0) {
                showError("Select a doctor first.");
                return;
            }

            int r = table.convertRowIndexToModel(row);
            int id = ((Number) model.getValueAt(r, 0)).intValue();

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Delete this doctor and their login account?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer == JOptionPane.YES_OPTION) {
                deleteDoctor(id);
                refresh.run();
            }
        }));

        buttons.add(actionButton("Refresh", e -> refresh.run()));

        showTableWindow("Doctor Details", table, buttons);
    }

    private void addDoctor(
            String name, String specialization,
            String username, String password) {

        String insertDoctor = "INSERT INTO doctors " +
                "(name, specialization, `Password`) VALUES (?, ?, ?)";

        String insertUser = "INSERT INTO users " +
                "(username, password, role, doctor_id) VALUES (?, ?, 'DOCTOR', ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                int doctorId;

                try (PreparedStatement ps = con.prepareStatement(
                        insertDoctor, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, name);
                    ps.setString(2, specialization);
                    ps.setString(3, password);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Could not obtain the new doctor ID.");
                        }
                        doctorId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps =
                        con.prepareStatement(insertUser)) {
                    ps.setString(1, username);
                    ps.setString(2, password);
                    ps.setInt(3, doctorId);
                    ps.executeUpdate();
                }

                con.commit();

                JOptionPane.showMessageDialog(
                        this,
                        "Doctor and login account created successfully.\n" +
                        "Username: " + username);

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            showError("Could not add doctor: " + ex.getMessage());
        }
    }

    private void deleteDoctor(int doctorId) {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                // Remove associated login first.
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM users WHERE doctor_id=?")) {
                    ps.setInt(1, doctorId);
                    ps.executeUpdate();
                }

                // Existing appointments may prevent deletion by foreign keys.
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM doctors WHERE doctor_id=?")) {
                    ps.setInt(1, doctorId);
                    ps.executeUpdate();
                }

                con.commit();

                JOptionPane.showMessageDialog(
                        this, "Doctor deleted successfully.");

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            showError("Could not delete doctor. They may have appointments " +
                      "linked to their record.\n" + ex.getMessage());
        }
    }

    // ---------------- APPOINTMENT MANAGEMENT ----------------

    private void showAppointments() {
        DefaultTableModel model = tableModel(
                "Appointment ID", "Patient", "Doctor",
                "Date", "Time", "Status");

        JTable table = createTable(model);

        String sql = "SELECT a.appointment_id, p.name, d.name, " +
                "a.appointment_date, a.appointment_time, a.status " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id=p.patient_id " +
                "JOIN doctors d ON a.doctor_id=d.doctor_id " +
                "ORDER BY a.appointment_date, a.appointment_time";

        Runnable refresh = () -> loadTable(model, sql);
        refresh.run();

        JPanel buttons = new JPanel(new FlowLayout());

        buttons.add(actionButton("Update Status", e -> {
            int row = table.getSelectedRow();

            if (row < 0) {
                showError("Select an appointment first.");
                return;
            }

            int r = table.convertRowIndexToModel(row);
            int id = ((Number) model.getValueAt(r, 0)).intValue();

            String[] statuses = {
                "Scheduled", "Approved", "Rejected",
                "Completed", "Cancelled"
            };

            Object selected = JOptionPane.showInputDialog(
                    this,
                    "Choose appointment status:",
                    "Update Appointment",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    statuses,
                    model.getValueAt(r, 5)
            );

            if (selected == null) return;

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                    "UPDATE appointments SET status=? " +
                    "WHERE appointment_id=?")) {

                ps.setString(1, selected.toString());
                ps.setInt(2, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(
                        this, "Appointment status updated.");

            } catch (SQLException ex) {
                showError("Could not update appointment: " +
                          ex.getMessage());
            }

            refresh.run();
        }));

        buttons.add(actionButton("Refresh", e -> refresh.run()));

        showTableWindow("Appointment Management", table, buttons);
    }

    // ---------------- REPORTS ----------------

    private void showReports() {
        StringBuilder report = new StringBuilder();

        try (Connection con = DBConnection.getConnection()) {
            report.append("SMART CARE — SYSTEM REPORT\n");
            report.append("============================\n\n");

            report.append("Total patients: ")
                    .append(count(con, "SELECT COUNT(*) FROM patients"))
                    .append("\n");

            report.append("Total doctors: ")
                    .append(count(con, "SELECT COUNT(*) FROM doctors"))
                    .append("\n");

            report.append("Total appointments: ")
                    .append(count(con, "SELECT COUNT(*) FROM appointments"))
                    .append("\n");

            report.append("Total consultations: ")
                    .append(count(con, "SELECT COUNT(*) FROM consultations"))
                    .append("\n\n");

            report.append("APPOINTMENTS BY STATUS\n");
            report.append("----------------------\n");

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT status, COUNT(*) AS total " +
                         "FROM appointments GROUP BY status ORDER BY status")) {

                while (rs.next()) {
                    report.append(rs.getString("status"))
                            .append(": ")
                            .append(rs.getInt("total"))
                            .append("\n");
                }
            }

        } catch (SQLException ex) {
            showError("Could not generate report: " + ex.getMessage());
            return;
        }

        JTextArea area = new JTextArea(report.toString(), 18, 38);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "System Reports",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private int count(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // ---------------- SHARED HELPERS ----------------

    private void deleteRecord(String sql, int id) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this, "Record deleted successfully.");

        } catch (SQLException ex) {
            showError("Could not delete record. It may be linked to " +
                      "appointments or consultations.\n" + ex.getMessage());
        }
    }

    private String prompt(String message) {
        return JOptionPane.showInputDialog(this, message);
    }

    private String prompt(String message, String initialValue) {
        return (String) JOptionPane.showInputDialog(
                this, message, "Edit Record",
                JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
