
package ui.common;

import java.awt.*;
import javax.swing.*;

public class WelcomeFrame extends JFrame {

    private final Color navy = new Color(24, 45, 82);
    private final Color blue = new Color(48, 105, 190);
    private final Color background = new Color(245, 248, 252);

    public WelcomeFrame() {
        setTitle("Smart Patient Appointment System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(background);

        // Header
        JPanel header = new JPanel();
        header.setBackground(navy);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(28, 20, 28, 20));

        JLabel title = new JLabel("SMART CARE");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Patient Appointment & Consultation Management System");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(220, 230, 245));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);

        // Welcome section
        JPanel center = new JPanel(new BorderLayout(10, 22));
        center.setBackground(background);
        center.setBorder(BorderFactory.createEmptyBorder(28, 35, 35, 35));

        JLabel welcome = new JLabel("Welcome! Select your role to continue");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 21));
        welcome.setForeground(navy);
        welcome.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel cards = new JPanel(new GridLayout(1, 3, 20, 0));
        cards.setBackground(background);

        cards.add(createRoleCard(
                "ADMIN",
                "Manage patients, doctors\nand appointments",
                "Open Admin Login",
                new Color(205, 137, 37),
                () -> {
                     new ui.admin.AdminLoginFrame().setVisible(true);
                     dispose();
}
        ));

        cards.add(createRoleCard(
                "PATIENT",
                "Find doctors, book visits\nand view prescriptions",
                "Patient Login",
                new Color(35, 145, 130),
                () -> {
                    new ui.patient.PatientLoginFrame().setVisible(true);
                    dispose();
                }
        ));

        cards.add(createRoleCard(
                "DOCTOR",
                "Manage appointments,\ndiagnosis and prescriptions",
                "Open Doctor Login",
                new Color(87, 103, 190),
                () -> { new ui.doctor.DoctorLoginFrame().setVisible(true);
    dispose();
}
        ));
    
        center.add(welcome, BorderLayout.NORTH);
        center.add(cards, BorderLayout.CENTER);

        JLabel footer = new JLabel(
                "Secure • Simple • Patient-focused healthcare");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.setForeground(Color.GRAY);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JPanel createRoleCard(
            String role,
            String description,
            String buttonText,
            Color accent,
            Runnable action) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 231, 240)),
                BorderFactory.createEmptyBorder(24, 14, 24, 14)
        ));

        JLabel icon = new JLabel(getRoleSymbol(role));
        icon.setFont(new Font("SansSerif", Font.BOLD, 38));
        icon.setForeground(accent);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        roleLabel.setForeground(accent);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel(
                "<html><div style='text-align:center;'>"
                + description.replace("\n", "<br>")
                + "</div></html>"
        );
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(new Color(90, 100, 115));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton button = new JButton(buttonText);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(accent);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(190, 42));
        button.setPreferredSize(new Dimension(190, 42));
        button.addActionListener(e -> action.run());

        card.add(icon);
        card.add(Box.createVerticalStrut(15));
        card.add(roleLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(desc);
        card.add(Box.createVerticalGlue());
        card.add(button);

        return card;
    }

    private String getRoleSymbol(String role) {
        switch (role) {
            case "ADMIN":
                return "A";
            case "PATIENT":
                return "P";
            default:
                return "D";
        }
    }

    private void showComingSoon(String role) {
        JOptionPane.showMessageDialog(
                this,
                role + " login will be connected in the next step.",
                role + " Login",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new WelcomeFrame().setVisible(true));
    }
}
