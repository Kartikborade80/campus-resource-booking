package ui;

import dao.DepartmentDAO;
import dao.UserDAO;
import database.DatabaseConnection;
import model.Department;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class LoginFrame extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private JButton btnRegisterAdmin;
    private JLabel lblStatus;
    private final UserDAO userDAO = new UserDAO();
    private final DepartmentDAO deptDAO = new DepartmentDAO();

    public LoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Campus Resource Booking System - Login & Sign In");
        setSize(520, 620);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(25, 25, 20, 25));

        JLabel lblTitle = new JLabel("CAMPUS RESOURCE BOOKING");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("College Resource & Venue Management Portal");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(lblSub);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 35, 15, 35));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblLoginHeader = new JLabel("Sign In to Your Account");
        lblLoginHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblLoginHeader.setForeground(new Color(30, 41, 59));
        formPanel.add(lblLoginHeader, gbc);

        gbc.gridy = 1;
        JLabel lblEmail = new JLabel("Campus Email Address");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(lblEmail, gbc);

        gbc.gridy = 2;
        txtEmail = new JTextField(24);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtEmail.setPreferredSize(new Dimension(280, 36));
        formPanel.add(txtEmail, gbc);

        gbc.gridy = 3;
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(lblPass, gbc);

        gbc.gridy = 4;
        txtPassword = new JPasswordField(24);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setPreferredSize(new Dimension(280, 36));
        formPanel.add(txtPassword, gbc);

        gbc.gridy = 5;
        JCheckBox chkShow = new JCheckBox("Show Password");
        chkShow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkShow.setBackground(Color.WHITE);
        chkShow.addActionListener(e -> {
            if (chkShow.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('\u2022');
            }
        });
        formPanel.add(chkShow, gbc);

        gbc.gridy = 6;
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        btnLogin = new JButton("Login");
        UIStyle.styleButton(btnLogin);
        btnLogin.setPreferredSize(new Dimension(120, 36));
        btnLogin.addActionListener(e -> handleLogin());

        btnExit = new JButton("Exit");
        UIStyle.styleButton(btnExit);
        btnExit.addActionListener(e -> System.exit(0));

        btnPanel.add(btnLogin);
        btnPanel.add(btnExit);
        formPanel.add(btnPanel, gbc);

        gbc.gridy = 7;
        btnRegisterAdmin = new JButton("+ Register New Admin Account");
        UIStyle.styleButton(btnRegisterAdmin);
        btnRegisterAdmin.addActionListener(e -> showRegisterAdminDialog());
        formPanel.add(btnRegisterAdmin, gbc);

        gbc.gridy = 8;
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(new Color(220, 38, 38));
        formPanel.add(lblStatus, gbc);

        JPanel demoPanel = new JPanel();
        demoPanel.setLayout(new BoxLayout(demoPanel, BoxLayout.Y_AXIS));
        demoPanel.setBackground(new Color(248, 250, 252));
        demoPanel.setBorder(new EmptyBorder(12, 20, 16, 20));

        JLabel lblDemo = new JLabel("1-Click Evaluation Accounts:");
        lblDemo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDemo.setForeground(new Color(71, 85, 105));
        lblDemo.setAlignmentX(Component.CENTER_ALIGNMENT);
        demoPanel.add(lblDemo);
        demoPanel.add(Box.createVerticalStrut(8));

        JPanel demoBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        demoBtns.setBackground(new Color(248, 250, 252));

        JButton btnDemoAdmin = createDemoButton("Admin", "admin@sanjivani.edu.in", "admin123");
        JButton btnDemoFaculty = createDemoButton("Faculty", "amit.cse@sanjivani.edu.in", "faculty123");
        JButton btnDemoStudent = createDemoButton("Student", "rohan.student@sanjivani.edu.in", "student123");
        JButton btnDemoStaff = createDemoButton("Staff", "suresh.staff@sanjivani.edu.in", "staff123");

        demoBtns.add(btnDemoAdmin);
        demoBtns.add(btnDemoFaculty);
        demoBtns.add(btnDemoStudent);
        demoBtns.add(btnDemoStaff);
        demoPanel.add(demoBtns);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(demoPanel, BorderLayout.SOUTH);

        add(mainPanel);

        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        txtEmail.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        txtEmail.setText("");
        txtPassword.setText("");

        checkDbConnection();
    }

    private JButton createDemoButton(String title, String email, String pass) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(30, 58, 138));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            txtEmail.setText(email);
            txtPassword.setText(pass);
            lblStatus.setText("Demo credentials for " + title + " loaded.");
            lblStatus.setForeground(new Color(37, 99, 235));
        });
        return btn;
    }

    private void checkDbConnection() {
        new Thread(() -> {
            boolean connected = DatabaseConnection.testConnection();
            SwingUtilities.invokeLater(() -> {
                if (connected) {
                    lblStatus.setText("Database Connected Successfully.");
                    lblStatus.setForeground(new Color(22, 163, 74));
                } else {
                    lblStatus.setText("Warning: MySQL database connection failed. Check db.properties");
                    lblStatus.setForeground(new Color(220, 38, 38));
                }
            });
        }).start();
    }

    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            lblStatus.setText("Please enter both email and password.");
            lblStatus.setForeground(new Color(220, 38, 38));
            return;
        }

        btnLogin.setEnabled(false);
        lblStatus.setText("Authenticating credentials...");
        lblStatus.setForeground(new Color(71, 85, 105));

        new Thread(() -> {
            User user = userDAO.authenticate(email, pass);
            SwingUtilities.invokeLater(() -> {
                btnLogin.setEnabled(true);
                if (user != null) {
                    lblStatus.setText("Login successful! Welcome " + user.getName());
                    lblStatus.setForeground(new Color(22, 163, 74));
                    dispose();

                    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                        new AdminDashboard(user).setVisible(true);
                    } else {
                        new StudentDashboard(user).setVisible(true);
                    }
                } else {
                    lblStatus.setText("Invalid email, password, or inactive account.");
                    lblStatus.setForeground(new Color(220, 38, 38));
                }
            });
        }).start();
    }

    private void showRegisterAdminDialog() {
        JDialog dlg = new JDialog(this, "Register New Administrator", true);
        dlg.setSize(480, 530);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel p = new JPanel(new GridLayout(8, 2, 8, 10));
        p.setBorder(new EmptyBorder(20, 25, 20, 25));

        JTextField txtName = new JTextField();
        JTextField txtRegEmail = new JTextField();
        JPasswordField txtRegPass = new JPasswordField();
        JPasswordField txtConfirmPass = new JPasswordField();
        JTextField txtPhone = new JTextField("+91-");
        JComboBox<Department> cmbDept = new JComboBox<>();
        List<Department> departments = deptDAO.getAllDepartments();
        if (departments.isEmpty()) {
            Department defaultDept = new Department(0, "General Administration", "ADMIN", "Active");
            deptDAO.addDepartment(defaultDept);
            departments = deptDAO.getAllDepartments();
        }
        for (Department d : departments) {
            cmbDept.addItem(d);
        }
        JTextField txtPasscode = new JTextField("ADMIN2026");

        p.add(new JLabel("Admin Full Name:")); p.add(txtName);
        p.add(new JLabel("Institutional Email:")); p.add(txtRegEmail);
        p.add(new JLabel("Password:")); p.add(txtRegPass);
        p.add(new JLabel("Confirm Password:")); p.add(txtConfirmPass);
        p.add(new JLabel("Phone Number:")); p.add(txtPhone);
        p.add(new JLabel("Assigned Department:")); p.add(cmbDept);
        p.add(new JLabel("Security Passcode:")); p.add(txtPasscode);

        JLabel lblHint = new JLabel("Default Security Passcode is ADMIN2026");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(new Color(100, 116, 139));
        p.add(new JLabel("")); p.add(lblHint);

        JButton btnSubmit = new JButton("Create Admin Account");
        UIStyle.styleButton(btnSubmit);

        btnSubmit.addActionListener(e -> {
            if (!DatabaseConnection.testConnection()) {
                String errMsg = DatabaseConnection.getLastErrorMessage();
                String details = (errMsg != null && !errMsg.trim().isEmpty()) ? errMsg : "Unable to establish connection to MySQL server.";
                JOptionPane.showMessageDialog(dlg, "database not connected\n\n" + details, "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = txtName.getText().trim();
            String email = txtRegEmail.getText().trim();
            String pass = new String(txtRegPass.getPassword()).trim();
            String confirm = new String(txtConfirmPass.getPassword()).trim();
            String phone = txtPhone.getText().trim();
            String passcode = txtPasscode.getText().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name, Email, and Password are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(dlg, "Please provide a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(dlg, "Password and Confirm Password do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!"ADMIN2026".equalsIgnoreCase(passcode)) {
                JOptionPane.showMessageDialog(dlg, "Invalid Admin Security Passcode. Default is ADMIN2026.", "Security Authorization Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (userDAO.emailExists(email)) {
                JOptionPane.showMessageDialog(dlg, "An account with email '" + email + "' already exists.", "Duplicate Email", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int deptId = 0;
            Department dept = (Department) cmbDept.getSelectedItem();
            if (dept != null && dept.getDepartmentId() > 0) {
                deptId = dept.getDepartmentId();
            }
            if (deptId <= 0) {
                List<Department> existingDepts = deptDAO.getAllDepartments();
                if (existingDepts.isEmpty()) {
                    Department defaultAdminDept = new Department(0, "General Administration", "ADMIN", "Active");
                    deptDAO.addDepartment(defaultAdminDept);
                    existingDepts = deptDAO.getAllDepartments();
                }
                if (!existingDepts.isEmpty()) {
                    deptId = existingDepts.get(0).getDepartmentId();
                }
            }

            User newAdmin = new User();
            newAdmin.setName(name);
            newAdmin.setEmail(email);
            newAdmin.setPassword(pass);
            newAdmin.setPhone(phone);
            newAdmin.setRole("ADMIN");
            newAdmin.setDepartmentId(deptId);
            newAdmin.setStatus("Active");

            if (userDAO.addUser(newAdmin)) {
                JOptionPane.showMessageDialog(dlg,
                    "Administrator account created successfully!\n\nName: " + name + "\nEmail: " + email + "\nRole: ADMIN\n\nYou can now log in.",
                    "Admin Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
                txtEmail.setText(email);
                txtPassword.setText(pass);
                lblStatus.setText("Admin account created for " + name + "! Ready to login.");
                lblStatus.setForeground(new Color(22, 163, 74));
                dlg.dispose();
            } else {
                String err = userDAO.getLastError();
                if (err == null || err.trim().isEmpty()) {
                    err = "database not connected or database query failed.";
                }
                JOptionPane.showMessageDialog(dlg, "database not connected\n\n" + err, "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        bp.add(btnSubmit);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
