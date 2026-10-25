package ui.patient;

import dao.PatientDAO;
import dao.PatientDAO.AppointmentInfo;
import dao.PatientDAO.DoctorInfo;
import dao.PatientDAO.MedicalRecord;
import dao.PatientDAO.PatientProfile;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class PatientDashboardFrame extends JFrame {

    private final int patientId;
    private final PatientDAO dao = new PatientDAO();

    private final Color navy = new Color(24, 45, 82);
    private final Color teal = new Color(35, 145, 130);
    private final Color background = new Color(245, 248, 252);

    private final JLabel welcomeName = new JLabel("Loading...");
    private final JLabel[] profileValues = new JLabel[7];

    private final DefaultTableModel doctorModel = model(
        "Doctor ID", "Name", "Specialization");

    private final DefaultTableModel appointmentModel = model(
        "Appointment ID", "Doctor", "Specialization",
        "Date", "Time", "Status");

    private final DefaultTableModel recordModel = model(
        "Appointment ID", "Date", "Doctor",
        "Diagnosis", "Prescription", "Notes");

    private final JComboBox<DoctorInfo> doctorCombo =
        new JComboBox<>();

    private final JTextField dateField =
        new JTextField(LocalDate.now().plusDays(1).toString(), 12);

    private final JTextField timeField =
        new JTextField("10:00", 12);

    private JTable appointmentTable;
    private JTable doctorTable;

    public PatientDashboardFrame(int patientId) {
        this.patientId = patientId;

        setTitle("Patient Dashboard - Smart Care");
        setSize(950, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(background);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(navy);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("SMART CARE | PATIENT PORTAL");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            new ui.common.WelcomeFrame().setVisible(true);
            dispose();
        });

        header.add(title, BorderLayout.CENTER);
        header.add(logout, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", createOverviewTab());
        tabs.addTab("My Profile", createProfileTab());
        tabs.addTab("Find Doctors", createDoctorsTab());
        tabs.addTab("Book Appointment", createBookingTab());
        tabs.addTab("My Appointments", createAppointmentsTab());
        tabs.addTab("Diagnosis & Prescriptions", createRecordsTab());

        main.add(header, BorderLayout.NORTH);
        main.add(tabs, BorderLayout.CENTER);
        setContentPane(main);

        refreshProfile();
        refreshDoctors();
        refreshAppointments();
        refreshMedicalRecords();
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    private JPanel createOverviewTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(background);

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(35, 35, 35, 35));

        JLabel heading = new JLabel("Welcome to your healthcare portal");
        heading.setFont(new Font("SansSerif", Font.BOLD, 23));
        heading.setForeground(navy);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomeName.setFont(new Font("SansSerif", Font.BOLD, 21));
        welcomeName.setForeground(teal);
        welcomeName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel description = new JLabel(
            "<html><div style='text-align:center;'>"
            + "Find doctors, book appointments, check appointment status, "
            + "and view your consultation records using the tabs above."
            + "</div></html>"
        );
        description.setFont(new Font("SansSerif", Font.PLAIN, 14));
        description.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(heading);
        content.add(Box.createVerticalStrut(20));
        content.add(welcomeName);
        content.add(Box.createVerticalStrut(20));
        content.add(description);

        panel.add(content);
        return panel;
    }

    private JPanel createProfileTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        String[] labels = {
            "Patient ID", "Name", "Age", "Contact",
            "Gender", "Address", "Blood Group"
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            panel.add(new JLabel(labels[i] + ":"), gbc);

            gbc.gridx = 1;
            profileValues[i] = new JLabel("Loading...");
            panel.add(profileValues[i], gbc);
        }

        return panel;
    }

    private void refreshProfile() {
        try {
            PatientProfile patient = dao.getPatientById(patientId);

            if (patient == null) {
                welcomeName.setText("Patient profile not found.");
                return;
            }

            welcomeName.setText("Welcome, " + patient.name + "!");

            String[] values = {
                String.valueOf(patient.patientId),
                patient.name,
                String.valueOf(patient.age),
                patient.contact,
                patient.gender,
                patient.address,
                patient.bloodGroup
            };

            for (int i = 0; i < values.length; i++) {
                profileValues[i].setText(
                    values[i] == null || values[i].isBlank()
                    ? "Not provided" : values[i]
                );
            }

        } catch (SQLException ex) {
            showError("Could not load profile: " + ex.getMessage());
        }
    }

    // ---------------- FIND DOCTORS ----------------

    private JPanel createDoctorsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField searchField = new JTextField(22);
        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search by doctor name or specialization:"));
        top.add(searchField);
        top.add(searchButton);
        top.add(refreshButton);

        doctorTable = new JTable(doctorModel);
        doctorTable.setRowHeight(25);

        TableRowSorter<DefaultTableModel> sorter =
            new TableRowSorter<>(doctorModel);
        doctorTable.setRowSorter(sorter);

        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim();

            sorter.setRowFilter(query.isEmpty() ? null :
                RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(query), 1, 2));
        });

        searchField.addActionListener(e -> searchButton.doClick());
        refreshButton.addActionListener(e -> refreshDoctors());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(doctorTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshDoctors() {
        doctorModel.setRowCount(0);
        doctorCombo.removeAllItems();

        try {
            List<DoctorInfo> doctors = dao.getDoctors();

            for (DoctorInfo doctor : doctors) {
                doctorModel.addRow(new Object[] {
                    doctor.doctorId,
                    doctor.name,
                    doctor.specialization
                });

                doctorCombo.addItem(doctor);
            }

        } catch (SQLException ex) {
            showError("Could not load doctors: " + ex.getMessage());
        }
    }

    // ---------------- BOOK APPOINTMENT ----------------

    private JPanel createBookingTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(background);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 8, 12, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(form, gbc, 0, "Select Doctor:", doctorCombo);
        addFormRow(form, gbc, 1, "Appointment Date:", dateField);
        addFormRow(form, gbc, 2, "Appointment Time:", timeField);

        JLabel dateHint = new JLabel("Format: YYYY-MM-DD");
        dateHint.setForeground(Color.GRAY);
        addFormRow(form, gbc, 3, "", dateHint);

        JLabel timeHint = new JLabel("24-hour format, e.g. 10:00 or 14:30");
        timeHint.setForeground(Color.GRAY);
        addFormRow(form, gbc, 4, "", timeHint);

        JButton bookButton = new JButton("Book Appointment");
        bookButton.setBackground(teal);
        bookButton.setForeground(Color.WHITE);
        bookButton.addActionListener(e -> bookAppointment());

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        form.add(bookButton, gbc);

        panel.add(form);
        return panel;
    }

    private void addFormRow(
            JPanel panel, GridBagConstraints gbc,
            int row, String label, Component field) {

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void bookAppointment() {
        DoctorInfo doctor = (DoctorInfo) doctorCombo.getSelectedItem();

        if (doctor == null) {
            showError("No doctor available. Refresh the doctor list.");
            return;
        }

        try {
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            LocalTime time = LocalTime.parse(timeField.getText().trim());

            if (date.isBefore(LocalDate.now())) {
                showError("Choose today or a future date.");
                return;
            }

            boolean booked = dao.bookAppointment(
                patientId, doctor.doctorId, date, time);

            if (booked) {
                JOptionPane.showMessageDialog(
                    this, "Appointment booked successfully!"
                );

                refreshAppointments();
                dateField.setText(LocalDate.now().plusDays(1).toString());
                timeField.setText("10:00");

            } else {
                showError(
                    "That doctor already has an appointment at this time. "
                    + "Choose another time slot."
                );
            }

        } catch (java.time.format.DateTimeParseException ex) {
            showError(
                "Invalid date or time. Use YYYY-MM-DD and HH:MM format."
            );
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Could not book appointment: " + ex.getMessage());
        }
    }

    // ---------------- MY APPOINTMENTS ----------------

    private JPanel createAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        appointmentTable = new JTable(appointmentModel);
        appointmentTable.setRowHeight(25);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshAppointments());

        JButton cancel = new JButton("Cancel Selected Appointment");
        cancel.addActionListener(e -> cancelSelectedAppointment());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(refresh);
        buttons.add(cancel);

        panel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshAppointments() {
        appointmentModel.setRowCount(0);

        try {
            for (AppointmentInfo a :
                    dao.getAppointmentsForPatient(patientId)) {

                appointmentModel.addRow(new Object[] {
                    a.appointmentId,
                    a.doctorName,
                    a.specialization,
                    a.date,
                    a.time,
                    a.status
                });
            }

        } catch (SQLException ex) {
            showError("Could not load appointments: " + ex.getMessage());
        }
    }

    private void cancelSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();

        if (selectedRow < 0) {
            showError("Select an appointment first.");
            return;
        }

        int modelRow =
            appointmentTable.convertRowIndexToModel(selectedRow);

        int appointmentId = ((Number) appointmentModel.getValueAt(
            modelRow, 0)).intValue();

        String status = String.valueOf(
            appointmentModel.getValueAt(modelRow, 5));

        if (!status.equalsIgnoreCase("Scheduled")
                && !status.equalsIgnoreCase("Approved")) {
            showError("Only scheduled or approved appointments can be cancelled.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel this appointment?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean cancelled =
                dao.cancelAppointment(patientId, appointmentId);

            if (cancelled) {
                JOptionPane.showMessageDialog(
                    this, "Appointment cancelled."
                );
            } else {
                showError(
                    "The appointment could not be cancelled. "
                    + "Its status may have changed."
                );
            }

            refreshAppointments();

        } catch (SQLException ex) {
            showError("Could not cancel appointment: " + ex.getMessage());
        }
    }

    // ---------------- MEDICAL RECORDS ----------------

    private JPanel createRecordsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTable table = new JTable(recordModel);
        table.setRowHeight(28);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JButton refresh = new JButton("Refresh Medical Records");
        refresh.addActionListener(e -> refreshMedicalRecords());

        JLabel info = new JLabel(
            "Only consultations recorded for your appointments appear here."
        );

        JPanel top = new JPanel(new BorderLayout());
        top.add(info, BorderLayout.CENTER);
        top.add(refresh, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshMedicalRecords() {
        recordModel.setRowCount(0);

        try {
            for (MedicalRecord r : dao.getMedicalRecords(patientId)) {
                recordModel.addRow(new Object[] {
                    r.appointmentId,
                    r.date,
                    r.doctorName,
                    r.diagnosis,
                    r.prescription,
                    r.notes
                });
            }

        } catch (SQLException ex) {
            showError("Could not load medical records: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this, message, "Information",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new PatientDashboardFrame(1).setVisible(true)
        );
    }
}