package ui;

import dao.MaintenanceDAO;
import dao.ResourceDAO;
import dao.RoomDAO;
import model.Maintenance;
import model.Resource;
import model.Room;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class MaintenanceFrame extends JFrame {
    private final User currentUser;
    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    public MaintenanceFrame(User currentUser) {
        this.currentUser = currentUser;
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("Campus Resource & Room Maintenance Management");
        setSize(980, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("Maintenance Tickets, Repairs & Expenditure");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Ticket ID", "Resource", "Room Venue", "Reported By", "Issue Title", "Reported Date", "Completed", "Status", "Cost (INR)"};
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

        JButton btnReport = new JButton("+ Report Issue");
        UIStyle.styleButton(btnReport);
        btnReport.addActionListener(e -> showReportDialog());

        JButton btnStart = new JButton("Set In Progress");
        UIStyle.styleButton(btnStart);
        btnStart.addActionListener(e -> updateStatus("In Progress"));

        JButton btnComplete = new JButton("Complete Ticket & Log Cost");
        UIStyle.styleButton(btnComplete);
        btnComplete.addActionListener(e -> showCompleteDialog());

        JButton btnRefresh = new JButton("Refresh");
        UIStyle.styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadData());

        bottomPanel.add(btnReport);
        bottomPanel.add(btnStart);
        bottomPanel.add(btnComplete);
        bottomPanel.add(btnRefresh);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<Maintenance> list = maintenanceDAO.getAllMaintenance();
        for (Maintenance m : list) {
            tableModel.addRow(new Object[]{
                m.getMaintenanceId(),
                m.getResourceName(),
                m.getRoomNumber(),
                m.getReportedByName(),
                m.getIssueTitle(),
                m.getMaintenanceDate(),
                m.getCompletionDate() != null ? m.getCompletionDate() : "-",
                m.getMaintenanceStatus(),
                m.getCost()
            });
        }
    }

    private void showReportDialog() {
        JDialog dlg = new JDialog(this, "Report Campus Maintenance Incident", true);
        dlg.setSize(440, 420);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(6, 2, 8, 12));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JComboBox<Object> cmbResource = new JComboBox<>();
        cmbResource.addItem("None (Room Only)");
        for (Resource r : resourceDAO.getAllResources()) {
            cmbResource.addItem(r);
        }

        JComboBox<Object> cmbRoom = new JComboBox<>();
        cmbRoom.addItem("None (Resource Only)");
        for (Room rm : roomDAO.getAllRooms()) {
            cmbRoom.addItem(rm);
        }

        JTextField txtTitle = new JTextField();
        JTextField txtDesc = new JTextField();
        JTextField txtDate = new JTextField(LocalDate.now().toString());

        p.add(new JLabel("Equipment Resource:")); p.add(cmbResource);
        p.add(new JLabel("Room / Facility:")); p.add(cmbRoom);
        p.add(new JLabel("Issue Title:")); p.add(txtTitle);
        p.add(new JLabel("Detailed Description:")); p.add(txtDesc);
        p.add(new JLabel("Incident Date:")); p.add(txtDate);

        JButton btnSave = new JButton("Submit Ticket");
        UIStyle.styleButton(btnSave);
        btnSave.addActionListener(e -> {
            Integer resId = null;
            Object rObj = cmbResource.getSelectedItem();
            if (rObj instanceof Resource) resId = ((Resource) rObj).getResourceId();

            Integer rmId = null;
            Object rmObj = cmbRoom.getSelectedItem();
            if (rmObj instanceof Room) rmId = ((Room) rmObj).getRoomId();

            if (resId == null && rmId == null) {
                JOptionPane.showMessageDialog(dlg, "Select at least a resource or a room.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtTitle.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Issue title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Maintenance m = new Maintenance();
            m.setResourceId(resId);
            m.setRoomId(rmId);
            m.setReportedBy(currentUser.getUserId());
            m.setIssueTitle(txtTitle.getText().trim());
            m.setIssueDescription(txtDesc.getText().trim());
            m.setMaintenanceDate(Date.valueOf(txtDate.getText().trim()));
            m.setMaintenanceStatus("Reported");
            m.setCost(BigDecimal.ZERO);

            if (maintenanceDAO.reportIssue(m)) {
                JOptionPane.showMessageDialog(dlg, "Maintenance ticket reported successfully.");
                dlg.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to submit ticket.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.add(btnSave);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void updateStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a maintenance ticket.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (Integer) tableModel.getValueAt(row, 0);
        if (maintenanceDAO.updateMaintenanceStatus(id, status)) {
            JOptionPane.showMessageDialog(this, "Ticket #" + id + " status set to: " + status);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCompleteDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a maintenance ticket.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (Integer) tableModel.getValueAt(row, 0);

        String costStr = JOptionPane.showInputDialog(this, "Enter total servicing / repair cost (INR):", "2500.00");
        if (costStr == null || costStr.trim().isEmpty()) return;

        BigDecimal cost;
        try {
            cost = new BigDecimal(costStr.trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid cost number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date compDate = Date.valueOf(LocalDate.now());
        if (maintenanceDAO.completeMaintenance(id, cost, compDate)) {
            JOptionPane.showMessageDialog(this, "Ticket #" + id + " completed successfully. Asset returned to Available.");
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to complete ticket.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
