package ui;

import dao.ResourceCategoryDAO;
import dao.ResourceDAO;
import model.Resource;
import model.ResourceCategory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class ResourceFrame extends JFrame {
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final ResourceCategoryDAO categoryDAO = new ResourceCategoryDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<Object> cmbCategoryFilter;
    private JComboBox<String> cmbStatusFilter;
    private JTextField txtSearch;

    public ResourceFrame() {
        initUI();
        loadCategories();
        loadData();
    }

    private void initUI() {
        setTitle("Resource Management");
        setSize(980, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(248, 250, 252));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("Campus Resources Inventory");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        filterPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(14);
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("Category:"));
        cmbCategoryFilter = new JComboBox<>();
        cmbCategoryFilter.addItem("ALL");
        filterPanel.add(cmbCategoryFilter);

        filterPanel.add(new JLabel("Status:"));
        cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "AVAILABLE", "BOOKED", "MAINTENANCE", "INACTIVE"});
        filterPanel.add(cmbStatusFilter);

        JButton btnFilter = new JButton("Filter");
        UIStyle.styleButton(btnFilter);
        btnFilter.addActionListener(e -> applyFilter());
        filterPanel.add(btnFilter);

        JButton btnReset = new JButton("Reset");
        UIStyle.styleButton(btnReset);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategoryFilter.setSelectedIndex(0);
            cmbStatusFilter.setSelectedIndex(0);
            loadData();
        });
        filterPanel.add(btnReset);

        topPanel.add(filterPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Code", "Name", "Category", "Location", "Capacity", "Status", "Purchased"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setBackground(new Color(248, 250, 252));

        JButton btnAdd = new JButton("+ Add Resource");
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

    private void loadCategories() {
        List<ResourceCategory> cats = categoryDAO.getAllCategories();
        for (ResourceCategory c : cats) {
            cmbCategoryFilter.addItem(c);
        }
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<Resource> list = resourceDAO.getAllResources();
        for (Resource r : list) {
            tableModel.addRow(new Object[]{
                r.getResourceId(),
                r.getResourceCode(),
                r.getResourceName(),
                r.getCategoryName(),
                r.getLocation(),
                r.getCapacity(),
                r.getStatus(),
                r.getPurchaseDate() != null ? r.getPurchaseDate().toString() : "-"
            });
        }
    }

    private void applyFilter() {
        String keyword = txtSearch.getText().trim();
        Integer catId = null;
        Object selectedCat = cmbCategoryFilter.getSelectedItem();
        if (selectedCat instanceof ResourceCategory) {
            catId = ((ResourceCategory) selectedCat).getCategoryId();
        }
        String status = (String) cmbStatusFilter.getSelectedItem();

        tableModel.setRowCount(0);
        List<Resource> list = resourceDAO.searchResources(keyword, catId, status, null);
        for (Resource r : list) {
            tableModel.addRow(new Object[]{
                r.getResourceId(),
                r.getResourceCode(),
                r.getResourceName(),
                r.getCategoryName(),
                r.getLocation(),
                r.getCapacity(),
                r.getStatus(),
                r.getPurchaseDate() != null ? r.getPurchaseDate().toString() : "-"
            });
        }
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog(this, "Add New Resource", true);
        dlg.setSize(440, 480);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(8, 2, 8, 10));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JComboBox<ResourceCategory> cmbCat = new JComboBox<>();
        for (ResourceCategory rc : categoryDAO.getAllCategories()) {
            cmbCat.addItem(rc);
        }
        JTextField txtCode = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtDesc = new JTextField();
        JSpinner spCapacity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        JTextField txtLoc = new JTextField();
        JComboBox<String> cmbStat = new JComboBox<>(new String[]{"AVAILABLE", "BOOKED", "MAINTENANCE", "INACTIVE"});
        JTextField txtPurch = new JTextField("2026-01-01");

        p.add(new JLabel("Category:")); p.add(cmbCat);
        p.add(new JLabel("Resource Code:")); p.add(txtCode);
        p.add(new JLabel("Resource Name:")); p.add(txtName);
        p.add(new JLabel("Description:")); p.add(txtDesc);
        p.add(new JLabel("Capacity:")); p.add(spCapacity);
        p.add(new JLabel("Location:")); p.add(txtLoc);
        p.add(new JLabel("Status:")); p.add(cmbStat);
        p.add(new JLabel("Purchase Date (YYYY-MM-DD):")); p.add(txtPurch);

        JButton btnSave = new JButton("Save Resource");
        UIStyle.styleButton(btnSave);
        btnSave.addActionListener(e -> {
            if (txtCode.getText().trim().isEmpty() || txtName.getText().trim().isEmpty() || txtLoc.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Code, Name, and Location are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ResourceCategory cat = (ResourceCategory) cmbCat.getSelectedItem();
            Date pDate = null;
            try {
                if (!txtPurch.getText().trim().isEmpty()) {
                    pDate = Date.valueOf(txtPurch.getText().trim());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid date format. Use YYYY-MM-DD", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Resource res = new Resource();
            res.setCategoryId(cat != null ? cat.getCategoryId() : 1);
            res.setResourceCode(txtCode.getText().trim());
            res.setResourceName(txtName.getText().trim());
            res.setDescription(txtDesc.getText().trim());
            res.setCapacity((Integer) spCapacity.getValue());
            res.setLocation(txtLoc.getText().trim());
            res.setStatus((String) cmbStat.getSelectedItem());
            res.setPurchaseDate(pDate);

            if (resourceDAO.addResource(res)) {
                JOptionPane.showMessageDialog(dlg, "Resource added successfully!");
                dlg.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to add resource. Code might be duplicate.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JPanel bP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bP.add(btnSave);
        dlg.add(bP, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a resource to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int resId = (Integer) tableModel.getValueAt(row, 0);
        Resource res = resourceDAO.getResourceById(resId);
        if (res == null) return;

        JDialog dlg = new JDialog(this, "Edit Resource #" + resId, true);
        dlg.setSize(440, 480);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(8, 2, 8, 10));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JComboBox<ResourceCategory> cmbCat = new JComboBox<>();
        for (ResourceCategory rc : categoryDAO.getAllCategories()) {
            cmbCat.addItem(rc);
            if (rc.getCategoryId() == res.getCategoryId()) {
                cmbCat.setSelectedItem(rc);
            }
        }
        JTextField txtCode = new JTextField(res.getResourceCode());
        JTextField txtName = new JTextField(res.getResourceName());
        JTextField txtDesc = new JTextField(res.getDescription() != null ? res.getDescription() : "");
        JSpinner spCapacity = new JSpinner(new SpinnerNumberModel(res.getCapacity(), 1, 1000, 1));
        JTextField txtLoc = new JTextField(res.getLocation());
        JComboBox<String> cmbStat = new JComboBox<>(new String[]{"AVAILABLE", "BOOKED", "MAINTENANCE", "INACTIVE"});
        cmbStat.setSelectedItem(res.getStatus());
        JTextField txtPurch = new JTextField(res.getPurchaseDate() != null ? res.getPurchaseDate().toString() : "");

        p.add(new JLabel("Category:")); p.add(cmbCat);
        p.add(new JLabel("Resource Code:")); p.add(txtCode);
        p.add(new JLabel("Resource Name:")); p.add(txtName);
        p.add(new JLabel("Description:")); p.add(txtDesc);
        p.add(new JLabel("Capacity:")); p.add(spCapacity);
        p.add(new JLabel("Location:")); p.add(txtLoc);
        p.add(new JLabel("Status:")); p.add(cmbStat);
        p.add(new JLabel("Purchase Date:")); p.add(txtPurch);

        JButton btnSave = new JButton("Update Resource");
        UIStyle.styleButton(btnSave);
        btnSave.addActionListener(e -> {
            ResourceCategory cat = (ResourceCategory) cmbCat.getSelectedItem();
            Date pDate = null;
            try {
                if (!txtPurch.getText().trim().isEmpty()) {
                    pDate = Date.valueOf(txtPurch.getText().trim());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid date format. Use YYYY-MM-DD", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            res.setCategoryId(cat != null ? cat.getCategoryId() : res.getCategoryId());
            res.setResourceCode(txtCode.getText().trim());
            res.setResourceName(txtName.getText().trim());
            res.setDescription(txtDesc.getText().trim());
            res.setCapacity((Integer) spCapacity.getValue());
            res.setLocation(txtLoc.getText().trim());
            res.setStatus((String) cmbStat.getSelectedItem());
            res.setPurchaseDate(pDate);

            if (resourceDAO.updateResource(res)) {
                JOptionPane.showMessageDialog(dlg, "Resource updated successfully!");
                dlg.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JPanel bP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bP.add(btnSave);
        dlg.add(bP, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a resource to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int resId = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete resource:\n" + name + " (ID: " + resId + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (resourceDAO.deleteResource(resId)) {
                JOptionPane.showMessageDialog(this, "Resource deleted successfully.");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete resource: it has linked bookings or maintenance records.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
