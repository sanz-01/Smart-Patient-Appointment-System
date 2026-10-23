
package ui.patient;

import java.awt.*;
import javax.swing.*;

public class PatientRegisterFrame extends JFrame {

    private JTextField txtName, txtAge, txtContact, txtAddress;
    private JComboBox<String> cmbGender, cmbBloodGroup;

    public PatientRegisterFrame() {
        setTitle("Patient Registration");
        setSize(650, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(41, 128, 185));
        header.setPreferredSize(new Dimension(650, 115));

        JLabel title = new JLabel(
            "PATIENT REGISTRATION", SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 27));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
            "Smart Patient Appointment & Consultation Management System",
            SwingConstants.CENTER
        );
        subtitle.setForeground(Color.WHITE);

        header.add(title);
        header.add(subtitle);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(15, 35, 15, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 10, 9, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName = new JTextField(20);
        txtAge = new JTextField(20);
        txtContact = new JTextField(20);
        txtAddress = new JTextField(20);

        cmbGender = new JComboBox<>(
            new String[]{"Select Gender", "Male", "Female", "Other"}
        );

        cmbBloodGroup = new JComboBox<>(
            new String[]{"Select Blood Group", "A+", "A-", "B+", "B-",
                         "AB+", "AB-", "O+", "O-"}
        );

        addField(form, gbc, "Patient Name:", txtName, 0);
        addField(form, gbc, "Age:", txtAge, 1);
        addField(form, gbc, "Gender:", cmbGender, 2);
        addField(form, gbc, "Contact Number:", txtContact, 3);
        addField(form, gbc, "Address:", txtAddress, 4);
        addField(form, gbc, "Blood Group:", cmbBloodGroup, 5);

        // Buttons
        JButton btnRegister = new JButton("REGISTER");
        JButton btnClear = new JButton("CLEAR");

        styleButton(btnRegister);
        styleButton(btnClear);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnClear);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setForeground(new Color(41, 128, 185));

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.setBackground(Color.WHITE);

        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(Color.WHITE);
        loginPanel.add(new JLabel("Already have an account?"));
        loginPanel.add(btnLogin);

        bottom.add(buttonPanel);
        bottom.add(loginPanel);

        btnClear.addActionListener(e -> clearForm());

        btnLogin.addActionListener(e -> {
            new PatientLoginFrame().setVisible(true);
            dispose();
        });

        btnRegister.addActionListener(e -> registerPatient());

        main.add(header, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        add(main);
    }

    private void addField(JPanel panel, GridBagConstraints gbc,
                          String label, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setPreferredSize(new Dimension(300, 38));
        panel.add(field, gbc);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(130, 38));
        button.setFocusPainted(false);
    }

    private void clearForm() {
        txtName.setText("");
        txtAge.setText("");
        txtContact.setText("");
        txtAddress.setText("");
        cmbGender.setSelectedIndex(0);
        cmbBloodGroup.setSelectedIndex(0);
    }

    private void registerPatient() {
        String name = txtName.getText().trim();
        String age = txtAge.getText().trim();
        String contact = txtContact.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty() || age.isEmpty() || contact.isEmpty()
                || address.isEmpty()
                || cmbGender.getSelectedIndex() == 0
                || cmbBloodGroup.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(
                this, "Please fill in all fields."
            );
            return;
        }

        try {
            int ageValue = Integer.parseInt(age);
            if (ageValue < 1 || ageValue > 120) {
                JOptionPane.showMessageDialog(this, "Enter a valid age.");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a number.");
            return;
        }

        if (!contact.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(
                this, "Enter a valid 10-digit contact number."
            );
            return;
        }

        JOptionPane.showMessageDialog(
            this, "Form validated. Database registration is not connected yet."
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new PatientRegisterFrame().setVisible(true)
        );
    }
}
