package ui;

import dao.DepartmentDAO;
import model.Department;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DepartmentFrame extends JFrame {
    private final DepartmentDAO deptDAO = new DepartmentDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    public DepartmentFrame() {
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("Department Management");
        setSize(700, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("Academic & Administrative Departments");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Code", "Department Name", "Status"};
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

        JButton btnAdd = new JButton("+ Add Department");
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
        List<Department> list = deptDAO.getAllDepartments();
        for (Department d : list) {
            tableModel.addRow(new Object[]{
                d.getDepartmentId(),
                d.getDepartmentCode(),
                d.getDepartmentName(),
                d.getStatus()
            });
        }
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog(this, "Add Department", true);
        dlg.setSize(380, 260);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(3, 2, 8, 12));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JTextField txtCode = new JTextField();
        JTextField txtName = new JTextField();
        JComboBox<String> cmbStat = new JComboBox<>(new String[]{"Active", "Inactive"});

        p.add(new JLabel("Department Code (e.g. CSE):")); p.add(txtCode);
        p.add(new JLabel("Department Name:")); p.add(txtName);
        p.add(new JLabel("Status:")); p.add(cmbStat);

        JButton btnSave = new JButton("Save");
        UIStyle.styleButton(btnSave);
        btnSave.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Code and Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Department d = new Department(0, txtName.getText().trim(), txtCode.getText().trim(), (String) cmbStat.getSelectedItem());
            if (deptDAO.addDepartment(d)) {
                JOptionPane.showMessageDialog(dlg, "Department added successfully.");
                dlg.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to add department. Code or name may be duplicate.", "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Select a department to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int deptId = (Integer) tableModel.getValueAt(row, 0);
        String code = (String) tableModel.getValueAt(row, 1);
        String name = (String) tableModel.getValueAt(row, 2);
        String status = (String) tableModel.getValueAt(row, 3);

        JDialog dlg = new JDialog(this, "Edit Department #" + deptId, true);
        dlg.setSize(380, 260);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(3, 2, 8, 12));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JTextField txtCode = new JTextField(code);
        JTextField txtName = new JTextField(name);
        JComboBox<String> cmbStat = new JComboBox<>(new String[]{"Active", "Inactive"});
        cmbStat.setSelectedItem(status);

        p.add(new JLabel("Department Code:")); p.add(txtCode);
        p.add(new JLabel("Department Name:")); p.add(txtName);
        p.add(new JLabel("Status:")); p.add(cmbStat);

        JButton btnSave = new JButton("Update");
        UIStyle.styleButton(btnSave);
        btnSave.addActionListener(e -> {
            Department d = new Department(deptId, txtName.getText().trim(), txtCode.getText().trim(), (String) cmbStat.getSelectedItem());
            if (deptDAO.updateDepartment(d)) {
                JOptionPane.showMessageDialog(dlg, "Department updated successfully.");
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
            JOptionPane.showMessageDialog(this, "Select a department to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int deptId = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete department:\n" + name + " (ID: " + deptId + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (deptDAO.deleteDepartment(deptId)) {
                JOptionPane.showMessageDialog(this, "Department deleted.");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete department: enrolled users exist in this department.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
