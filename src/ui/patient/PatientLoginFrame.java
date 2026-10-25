
package ui.patient;

import dao.PatientDAO;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class PatientLoginFrame extends JFrame {

    private final JTextField nameField = new JTextField(18);
    private final JTextField contactField = new JTextField(18);

    private final Color navy = new Color(24, 45, 82);
    private final Color teal = new Color(35, 145, 130);

    public PatientLoginFrame() {
        setTitle("Patient Login - Smart Care");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(navy);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(22, 15, 22, 15));

        JLabel title = new JLabel("PATIENT LOGIN");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Your healthcare portal");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(7));
        header.add(subtitle);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 6, 10, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Patient Name"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Contact Number"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(contactField, gbc);

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(teal);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> loginPatient());

        JButton registerButton = new JButton("Sign Up");
        registerButton.addActionListener(e -> {
            new PatientRegisterFrame().setVisible(true);
            dispose();
        });

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            new ui.common.WelcomeFrame().setVisible(true);
            dispose();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        buttons.add(loginButton);
        buttons.add(registerButton);
        buttons.add(backButton);

        JPanel center = new JPanel(new BorderLayout(10, 12));
        center.setBorder(BorderFactory.createEmptyBorder(22, 25, 20, 25));
        center.add(form, BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        setContentPane(main);
        getRootPane().setDefaultButton(loginButton);
    }

    private void loginPatient() {
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();

        if (name.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please enter your name and contact number."
            );
            return;
        }

        try {
            PatientDAO dao = new PatientDAO();
            int patientId = dao.loginPatient(name, contact);

            if (patientId > 0) {
                JOptionPane.showMessageDialog(
                    this, "Patient login successful!"
                );

                new PatientDashboardFrame(patientId).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Patient not found. Check your details or register first.",
                    "Login Failed",
                    JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Database error: " + ex.getMessage(),
                "Login Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
