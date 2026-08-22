package ui.patient;

import javax.swing.*;
import java.awt.*;

public class PatientLoginFrame extends JFrame {

    private JTextField txtContact;
    private JTextField txtName;
    private JButton btnLogin;
    private JButton btnRegister;

    public PatientLoginFrame() {
        setTitle("Patient Portal - Login");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 10, 10));

        JLabel lblName = new JLabel(" Patient Name:");
        txtName = new JTextField();

        JLabel lblContact = new JLabel(" Contact Number:");
        txtContact = new JTextField();

        btnLogin = new JButton("Login");
        btnRegister = new JButton("New Patient? Register");

        add(lblName);
        add(txtName);
        add(lblContact);
        add(txtContact);
        add(btnLogin);
        add(btnRegister);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PatientLoginFrame().setVisible(true);
        });
    }
}