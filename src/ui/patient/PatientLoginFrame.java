
package ui.patient;

import java.awt.*;
import javax.swing.*;

public class PatientLoginFrame extends JFrame {

    private JTextField txtName, txtContact;

    public PatientLoginFrame() {
        setTitle("Patient Login");
        setSize(650, 430);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(41, 128, 185));
        header.setPreferredSize(new Dimension(650, 115));

        JLabel title = new JLabel("PATIENT LOGIN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName = new JTextField(20);
        txtContact = new JTextField(20);

        addField(form, gbc, "Patient Name:", txtName, 0);
        addField(form, gbc, "Contact Number:", txtContact, 1);

        // Buttons
        JButton btnLogin = new JButton("LOGIN");
        JButton btnSignup = new JButton("SIGN UP");

        styleButton(btnLogin);
        styleButton(btnSignup);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttons.setBackground(Color.WHITE);
        buttons.add(btnLogin);
        buttons.add(btnSignup);

        btnLogin.addActionListener(e -> {
            String name = txtName.getText().trim();
            String contact = txtContact.getText().trim();

            if (name.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this, "Please enter your name and contact number."
                );
            } else {
                JOptionPane.showMessageDialog(
                    this, "UI validation successful. Database login is not connected yet."
                );
            }
        });

        btnSignup.addActionListener(e -> {
            new PatientRegisterFrame().setVisible(true);
            dispose();
        });

        main.add(header, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        main.add(buttons, BorderLayout.SOUTH);

        add(main);
    }

    private void addField(JPanel panel, GridBagConstraints gbc,
                          String label, JTextField field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setPreferredSize(new Dimension(300, 40));
        panel.add(field, gbc);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(140, 40));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new PatientLoginFrame().setVisible(true)
        );
    }
}
