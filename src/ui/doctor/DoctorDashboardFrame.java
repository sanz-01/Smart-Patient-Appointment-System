
package ui.doctor;

import dao.DoctorDAO;
import dao.DoctorDAO.AssignedPatient;
import dao.DoctorDAO.ConsultationRecord;
import dao.DoctorDAO.DoctorAppointment;
import dao.DoctorDAO.DoctorProfile;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DoctorDashboardFrame extends JFrame {

    private final int doctorId;
    private final DoctorDAO dao = new DoctorDAO();

    private final Color navy = new Color(24, 45, 82);
    private final Color purple = new Color(87, 103, 190);
    private final Color background = new Color(245, 248, 252);

    private final JLabel welcome = new JLabel("Loading doctor profile...");

    private final DefaultTableModel appointmentModel = model(
        "Appointment ID", "Patient ID", "Patient",
        "Age", "Contact", "Date", "Time", "Status");

    private final DefaultTableModel patientModel = model(
        "Patient ID", "Name", "Age", "Contact",
        "Gender", "Address", "Blood Group");

    private JTable appointmentTable;
    private JTable patientTable;

    private final JComboBox<DoctorAppointment> consultationAppointments =
        new JComboBox<>();

    private final JTextArea diagnosisArea = new JTextArea(4, 35);
    private final JTextArea prescriptionArea = new JTextArea(5, 35);
    private final JTextArea notesArea = new JTextArea(4, 35);

    private boolean loadingCombo = false;

    public DoctorDashboardFrame(int doctorId) {
        this.doctorId = doctorId;

        setTitle("Doctor Dashboard - Smart Care");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(background);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(navy);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("SMART CARE | DOCTOR PORTAL");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            new ui.common.WelcomeFrame().setVisible(true);
            dispose();
        });

        header.add(title, BorderLayout.CENTER);
        header.add(logout, BorderLayout.EAST);

        welcome.setFont(new Font("SansSerif", Font.BOLD, 20));
        welcome.setForeground(navy);
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        welcome.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", createOverviewTab());
        tabs.addTab("My Appointments", createAppointmentsTab());
        tabs.addTab("Patient Details", createPatientsTab());
        tabs.addTab("Write Diagnosis & Prescription",
                    createConsultationTab());

        main.add(header, BorderLayout.NORTH);
        main.add(welcome, BorderLayout.CENTER);
        main.add(tabs, BorderLayout.SOUTH);

        // Put tabs in the main content area, directly beneath the header.
        JPanel content = new JPanel(new BorderLayout());
        content.add(welcome, BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);
        content.setBackground(background);

        main.remove(welcome);
        main.add(content, BorderLayout.CENTER);

        setContentPane(main);

        refreshAll();
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel createOverviewTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(background);

        JLabel info = new JLabel(
            "<html><div style='text-align:center;'>"
            + "<h2>Doctor Workspace</h2>"
            + "Manage your assigned appointments, review patient details, "
            + "and write consultation records and prescriptions."
            + "</div></html>"
        );
        info.setFont(new Font("SansSerif", Font.PLAIN, 15));
        panel.add(info);

        return panel;
    }

    // ---------------- APPOINTMENTS ----------------

    private JPanel createAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        appointmentTable = new JTable(appointmentModel);
        appointmentTable.setAutoCreateRowSorter(true);
        appointmentTable.setRowHeight(24);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshAll());

        JButton approve = new JButton("Approve");
        approve.addActionListener(e -> updateSelectedStatus("Approved"));

        JButton reject = new JButton("Reject");
        reject.addActionListener(e -> updateSelectedStatus("Rejected"));

        JButton complete = new JButton("Mark Completed");
        complete.addActionListener(e -> updateSelectedStatus("Completed"));

        JButton write = new JButton("Write Prescription");
        write.addActionListener(e -> selectAppointmentForConsultation());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(refresh);
        actions.add(approve);
        actions.add(reject);
        actions.add(complete);
        actions.add(write);

        panel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private int selectedAppointmentId() {
        int row = appointmentTable.getSelectedRow();

        if (row < 0) {
            showMessage("Select an appointment first.");
            return -1;
        }

        int modelRow = appointmentTable.convertRowIndexToModel(row);
        return ((Number) appointmentModel.getValueAt(modelRow, 0)).intValue();
    }

    private void updateSelectedStatus(String status) {
        int appointmentId = selectedAppointmentId();
        if (appointmentId < 0) return;

        int answer = JOptionPane.showConfirmDialog(
            this,
            "Change appointment #" + appointmentId + " to " + status + "?",
            "Confirm Status",
            JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) return;

        try {
            boolean updated = dao.updateAppointmentStatus(
                doctorId, appointmentId, status);

            if (updated) {
                showMessage("Appointment status changed to " + status + ".");
            } else {
                showMessage(
                    "The appointment could not be updated. "
                    + "It may no longer be scheduled or approved."
                );
            }

            refreshAll();

        } catch (SQLException ex) {
            showError("Could not update appointment: " + ex.getMessage());
        }
    }

    private void selectAppointmentForConsultation() {
        int appointmentId = selectedAppointmentId();
        if (appointmentId < 0) return;

        for (int i = 0; i < consultationAppointments.getItemCount(); i++) {
            DoctorAppointment item = consultationAppointments.getItemAt(i);

            if (item.appointmentId == appointmentId) {
                consultationAppointments.setSelectedIndex(i);
                break;
            }
        }

        // Selecting an appointment loads its existing consultation, if any.
        loadConsultation();
        JOptionPane.showMessageDialog(
            this,
            "Open the Write Diagnosis & Prescription tab to continue."
        );
    }

    private void refreshAppointments() {
        appointmentModel.setRowCount(0);

        try {
            for (DoctorAppointment a : dao.getAppointments(doctorId)) {
                appointmentModel.addRow(new Object[] {
                    a.appointmentId, a.patientId, a.patientName,
                    a.age, a.contact, a.date, a.time, a.status
                });
            }
        } catch (SQLException ex) {
            showError("Could not load appointments: " + ex.getMessage());
        }
    }

    // ---------------- PATIENT DETAILS ----------------

    private JPanel createPatientsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        patientTable = new JTable(patientModel);
        patientTable.setAutoCreateRowSorter(true);
        patientTable.setRowHeight(24);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshPatients());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(refresh);

        panel.add(new JScrollPane(patientTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshPatients() {
        patientModel.setRowCount(0);

        try {
            for (AssignedPatient p : dao.getAssignedPatients(doctorId)) {
                patientModel.addRow(new Object[] {
                    p.patientId, p.name, p.age, p.contact,
                    p.gender, p.address, p.bloodGroup
                });
            }
        } catch (SQLException ex) {
            showError("Could not load patient details: " + ex.getMessage());
        }
    }

    // ---------------- WRITE DIAGNOSIS / PRESCRIPTION ----------------

    private JPanel createConsultationTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        diagnosisArea.setLineWrap(true);
        diagnosisArea.setWrapStyleWord(true);
        prescriptionArea.setLineWrap(true);
        prescriptionArea.setWrapStyleWord(true);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        JScrollPane diagnosisScroll = new JScrollPane(diagnosisArea);
        JScrollPane prescriptionScroll = new JScrollPane(prescriptionArea);
        JScrollPane notesScroll = new JScrollPane(notesArea);

        addFormRow(form, gbc, 0, "Appointment / Patient:", consultationAppointments);
        addFormRow(form, gbc, 1, "Diagnosis:", diagnosisScroll);
        addFormRow(form, gbc, 2, "Prescription:", prescriptionScroll);
        addFormRow(form, gbc, 3, "Consultation Notes:", notesScroll);

        JButton load = new JButton("Load Existing Record");
        load.addActionListener(e -> loadConsultation());

        JButton clear = new JButton("Clear Form");
        clear.addActionListener(e -> clearForm());

        JButton save = new JButton("Save Consultation & Prescription");
        save.setBackground(purple);
        save.setForeground(Color.WHITE);
        save.addActionListener(e -> saveConsultation());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(load);
        buttons.add(clear);
        buttons.add(save);

        panel.add(new JLabel(
            "Select an appointment, enter the consultation details, and save."
        ), BorderLayout.NORTH);
        panel.add(new JScrollPane(form), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        consultationAppointments.addActionListener(e -> {
            if (!loadingCombo) {
                loadConsultation();
            }
        });

        return panel;
    }

    private void addFormRow(
            JPanel panel, GridBagConstraints gbc,
            int row, String label, Component component) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = component instanceof JScrollPane ? 1 : 0;
        panel.add(component, gbc);

        if (component instanceof JScrollPane) {
            component.setPreferredSize(new Dimension(550, 90));
        }
    }

    private void refreshConsultationAppointments() {
        Integer previousId = null;
        DoctorAppointment previous =
            (DoctorAppointment) consultationAppointments.getSelectedItem();

        if (previous != null) {
            previousId = previous.appointmentId;
        }

        loadingCombo = true;
        consultationAppointments.removeAllItems();

        try {
            for (DoctorAppointment appointment :
                    dao.getAppointments(doctorId)) {
                consultationAppointments.addItem(appointment);

                if (previousId != null
                        && previousId == appointment.appointmentId) {
                    consultationAppointments.setSelectedItem(appointment);
                }
            }
        } catch (SQLException ex) {
            showError("Could not load appointments: " + ex.getMessage());
        } finally {
            loadingCombo = false;
        }

        loadConsultation();
    }

    private void loadConsultation() {
        DoctorAppointment appointment =
            (DoctorAppointment) consultationAppointments.getSelectedItem();

        if (appointment == null) {
            clearForm();
            return;
        }

        try {
            ConsultationRecord record = dao.getConsultation(
                doctorId, appointment.appointmentId);

            if (record == null) {
                clearForm();
                return;
            }

            diagnosisArea.setText(record.diagnosis);
            prescriptionArea.setText(record.prescription);
            notesArea.setText(record.notes);

        } catch (SQLException ex) {
            showError("Could not load consultation: " + ex.getMessage());
        }
    }

    private void clearForm() {
        diagnosisArea.setText("");
        prescriptionArea.setText("");
        notesArea.setText("");
    }

    private void saveConsultation() {
        DoctorAppointment appointment =
            (DoctorAppointment) consultationAppointments.getSelectedItem();

        if (appointment == null) {
            showError("Select an appointment first.");
            return;
        }

        String diagnosis = diagnosisArea.getText().trim();
        String prescription = prescriptionArea.getText().trim();
        String notes = notesArea.getText().trim();

        if (diagnosis.isEmpty() || prescription.isEmpty()) {
            showError("Diagnosis and prescription are required.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
            this,
            "Save diagnosis and prescription for "
                + appointment.patientName + "?",
            "Confirm Consultation",
            JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) return;

        try {
            dao.saveConsultation(
                doctorId, appointment.appointmentId,
                diagnosis, prescription, notes);

            showMessage("Consultation and prescription saved successfully.");

            refreshAppointments();
            refreshPatients();

        } catch (IllegalArgumentException | SQLException ex) {
            showError("Could not save consultation: " + ex.getMessage());
        }
    }

    // ---------------- OVERALL REFRESH ----------------

    private void refreshAll() {
        loadDoctorProfile();
        refreshAppointments();
        refreshPatients();
        refreshConsultationAppointments();
    }

    private void loadDoctorProfile() {
        try {
            DoctorProfile profile = dao.getDoctorById(doctorId);

            if (profile == null) {
                welcome.setText("Doctor profile not found.");
            } else {
                welcome.setText(
                    "Welcome, " + profile.name
                    + " — " + profile.specialization
                );
            }
        } catch (SQLException ex) {
            welcome.setText("Could not load doctor profile.");
            showError("Database error: " + ex.getMessage());
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(
            this, message, "Smart Care", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this, message, "Smart Care Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new DoctorDashboardFrame(1).setVisible(true)
        );
    }
}
