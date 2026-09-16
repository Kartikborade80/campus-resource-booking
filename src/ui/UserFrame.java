package ui;

import dao.DepartmentDAO;
import dao.UserDAO;
import model.Department;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserFrame extends JFrame {
    private final UserDAO userDAO = new UserDAO();
    private final DepartmentDAO deptDAO = new DepartmentDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbRoleFilter;

    public UserFrame() {
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("User Accounts Management");
        setSize(920, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(248, 250, 252));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("Campus User Accounts & Access Control");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        filterPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(14);
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("Role:"));
        cmbRoleFilter = new JComboBox<>(new String[]{"ALL", "ADMIN", "FACULTY", "STAFF", "STUDENT"});
        filterPanel.add(cmbRoleFilter);

        JButton btnFilter = new JButton("Filter");
        UIStyle.styleButton(btnFilter);
        btnFilter.addActionListener(e -> applyFilter());
        filterPanel.add(btnFilter);

        JButton btnReset = new JButton("Reset");
        UIStyle.styleButton(btnReset);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbRoleFilter.setSelectedIndex(0);
            loadData();
        });
        filterPanel.add(btnReset);

        topPanel.add(filterPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Email", "Role", "Department", "Phone", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setBackground(new Color(248, 250, 252));

        JButton btnAdd = new JButton("+ Add User");
        UIStyle.styleButton(btnAdd);
        btnAdd.addActionListener(e -> showAddDialog());

        JButton btnEdit = new JButton("Edit Selected");
        UIStyle.styleButton(btnEdit);
        btnEdit.addActionListener(e -> showEditDialog());

        JButton btnDelete = new JButton("Delete");
        UIStyle.styleButton(btnDelete);
        btnDelete.addActionListener(e -> handleDelete());

        JButton btnRefresh = new JButton("Refresh");
        UIStyle.styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadData());

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnRefresh);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<User> list = userDAO.getAllUsers();
        for (User u : list) {
            tableModel.addRow(new Object[]{
                u.getUserId(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.getDepartmentName(),
                u.getPhone(),
                u.getStatus()
            });
        }
    }

    private void applyFilter() {
        String kw = txtSearch.getText().trim();
        String role = (String) cmbRoleFilter.getSelectedItem();

        tableModel.setRowCount(0);
        List<User> list = userDAO.searchUsers(kw, role, null);
        for (User u : list) {
            tableModel.addRow(new Object[]{
                u.getUserId(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.getDepartmentName(),
                u.getPhone(),
                u.getStatus()
            });
        }
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog(this, "Add New User", true);
        dlg.setSize(420, 440);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(7, 2, 8, 10));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JTextField txtPhone = new JTextField();
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"STUDENT", "FACULTY", "STAFF", "ADMIN"});
        JComboBox<Department> cmbDept = new JComboBox<>();
        for (Department d : deptDAO.getAllDepartments()) {
            cmbDept.addItem(d);
        }
        JComboBox<String> cmbStat = new JComboBox<>(new String[]{"Active", "Inactive", "Suspended"});

        p.add(new JLabel("Full Name:")); p.add(txtName);
        p.add(new JLabel("Email Address:")); p.add(txtEmail);
        p.add(new JLabel("Password:")); p.add(txtPass);
        p.add(new JLabel("Phone:")); p.add(txtPhone);
        p.add(new JLabel("Role:")); p.add(cmbRole);
        p.add(new JLabel("Department:")); p.add(cmbDept);
        p.add(new JLabel("Account Status:")); p.add(cmbStat);

        JButton btnSave = new JButton("Create User");
        UIStyle.styleButton(btnSave);
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name, Email, and Password are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Department dept = (Department) cmbDept.getSelectedItem();
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(pass);
            user.setPhone(txtPhone.getText().trim());
            user.setRole((String) cmbRole.getSelectedItem());
            user.setDepartmentId(dept != null ? dept.getDepartmentId() : 1);
            user.setStatus((String) cmbStat.getSelectedItem());

            if (userDAO.addUser(user)) {
                JOptionPane.showMessageDialog(dlg, "User created successfully!");
                dlg.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to create user. Email may already be registered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.add(btnSave);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int userId = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String email = (String) tableModel.getValueAt(row, 2);
        String role = (String) tableModel.getValueAt(row, 3);
        String deptName = (String) tableModel.getValueAt(row, 4);
        String phone = (String) tableModel.getValueAt(row, 5);
        String status = (String) tableModel.getValueAt(row, 6);

        JDialog dlg = new JDialog(this, "Edit User #" + userId, true);
        dlg.setSize(420, 440);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(7, 2, 8, 10));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JTextField txtName = new JTextField(name);
        JTextField txtEmail = new JTextField(email);
        JPasswordField txtPass = new JPasswordField();
        JTextField txtPhone = new JTextField(phone != null ? phone : "");
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"STUDENT", "FACULTY", "STAFF", "ADMIN"});
        cmbRole.setSelectedItem(role);
        JComboBox<Department> cmbDept = new JComboBox<>();
        for (Department d : deptDAO.getAllDepartments()) {
            cmbDept.addItem(d);
            if (d.getDepartmentName().equalsIgnoreCase(deptName)) {
                cmbDept.setSelectedItem(d);
            }
        }
        JComboBox<String> cmbStat = new JComboBox<>(new String[]{"Active", "Inactive", "Suspended"});
        cmbStat.setSelectedItem(status);

        p.add(new JLabel("Full Name:")); p.add(txtName);
        p.add(new JLabel("Email Address:")); p.add(txtEmail);
        p.add(new JLabel("New Password (blank to keep):")); p.add(txtPass);
        p.add(new JLabel("Phone:")); p.add(txtPhone);
        p.add(new JLabel("Role:")); p.add(cmbRole);
        p.add(new JLabel("Department:")); p.add(cmbDept);
        p.add(new JLabel("Account Status:")); p.add(cmbStat);

        JButton btnSave = new JButton("Update User");
        UIStyle.styleButton(btnSave);
        btnSave.addActionListener(e -> {
            Department dept = (Department) cmbDept.getSelectedItem();
            String newPass = new String(txtPass.getPassword()).trim();

            User user = new User();
            user.setUserId(userId);
            user.setName(txtName.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setPassword(!newPass.isEmpty() ? newPass : "student123");
            user.setPhone(txtPhone.getText().trim());
            user.setRole((String) cmbRole.getSelectedItem());
            user.setDepartmentId(dept != null ? dept.getDepartmentId() : 1);
            user.setStatus((String) cmbStat.getSelectedItem());

            if (userDAO.updateUser(user)) {
                JOptionPane.showMessageDialog(dlg, "User updated successfully!");
                dlg.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.add(btnSave);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int userId = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete user: " + name + " (ID: " + userId + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(userId)) {
                JOptionPane.showMessageDialog(this, "User deleted successfully.");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete user: active bookings or maintenance logs are linked.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
