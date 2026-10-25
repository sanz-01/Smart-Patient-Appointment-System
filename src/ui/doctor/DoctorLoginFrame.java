
package ui.doctor;

import java.awt.*;
import javax.swing.*;

public class DoctorLoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    private final Color navy = new Color(24, 45, 82);
    private final Color purple = new Color(87, 103, 190);

    public DoctorLoginFrame() {
        setTitle("Doctor Login - Smart Care");
        setSize(450, 390);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 248, 252));

        // Header
        JPanel header = new JPanel();
        header.setBackground(navy);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(22, 15, 22, 15));

        JLabel title = new JLabel("DOCTOR LOGIN");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Doctor's healthcare portal");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(7));
        header.add(subtitle);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 6, 10, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Password"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(passwordField, gbc);

        // Buttons
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(purple);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> loginDoctor());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            new ui.common.WelcomeFrame().setVisible(true);
            dispose();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        buttons.setBackground(new Color(245, 248, 252));
        buttons.add(loginButton);
        buttons.add(backButton);

        JPanel center = new JPanel(new BorderLayout(10, 12));
        center.setBackground(new Color(245, 248, 252));
        center.setBorder(BorderFactory.createEmptyBorder(22, 25, 20, 25));
        center.add(form, BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        setContentPane(main);
        getRootPane().setDefaultButton(loginButton);
    }

   
private void loginDoctor() {
    String username = usernameField.getText().trim();
    char[] passwordChars = passwordField.getPassword();

    if (username.isEmpty() || passwordChars.length == 0) {
        JOptionPane.showMessageDialog(
            this, "Please enter your username and password."
        );
        java.util.Arrays.fill(passwordChars, '\0');
        return;
    }

    String sql =
        "SELECT doctor_id FROM users " +
        "WHERE username = ? AND password = ? AND role = 'DOCTOR'";

    try (java.sql.Connection con = database.DBConnection.getConnection();
         java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, username);
        ps.setString(2, new String(passwordChars));

        try (java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int doctorId = rs.getInt("doctor_id");

                if (rs.wasNull() || doctorId <= 0) {
                    JOptionPane.showMessageDialog(
                        this,
                        "This doctor account is not linked to a doctor record."
                    );
                    return;
                }

                JOptionPane.showMessageDialog(
                    this, "Doctor login successful!"
                );

                new DoctorDashboardFrame(doctorId).setVisible(true);
                dispose();

            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Invalid doctor username or password.",
                    "Login Failed",
                    JOptionPane.WARNING_MESSAGE
                );
                passwordField.setText("");
            }
        }

    } catch (java.sql.SQLException ex) {
        JOptionPane.showMessageDialog(
            this,
            "Database error: " + ex.getMessage(),
            "Login Error",
            JOptionPane.ERROR_MESSAGE
        );
    } finally {
        java.util.Arrays.fill(passwordChars, '\0');
    }
}
}