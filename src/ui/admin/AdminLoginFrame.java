
package ui.admin;

import database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminLoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    private final Color navy = new Color(24, 45, 82);
    private final Color blue = new Color(48, 105, 190);

    public AdminLoginFrame() {
        setTitle("Admin Login - Smart Care");
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

        JLabel title = new JLabel("ADMIN LOGIN");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Smart Patient Appointment System");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(220, 230, 245));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(7));
        header.add(subtitle);

        // Login form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
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

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(blue);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> loginAdmin());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            new ui.common.WelcomeFrame().setVisible(true);
            dispose();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        buttons.setBackground(Color.WHITE);
        buttons.add(loginButton);
        buttons.add(backButton);

        JPanel center = new JPanel(new BorderLayout(5, 8));
        center.setBackground(new Color(245, 248, 252));
        center.setBorder(BorderFactory.createEmptyBorder(22, 25, 20, 25));
        center.add(form, BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        setContentPane(main);
        getRootPane().setDefaultButton(loginButton);
    }

    private void loginAdmin() {
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter your username and password."
            );
            java.util.Arrays.fill(passwordChars, '\0');
            return;
        }

        String sql =
            "SELECT username FROM users " +
            "WHERE username = ? AND password = ? AND role = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, "ADMIN");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(
                        this, "Admin login successful!"
                    );

                    new AdminDashboardFrame().setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Invalid admin credentials.",
                        "Login Failed",
                        JOptionPane.WARNING_MESSAGE
                    );
                    passwordField.setText("");
                }
            }

        } catch (SQLException ex) {
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
