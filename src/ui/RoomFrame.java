package ui;

import dao.RoomDAO;
import model.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoomFrame extends JFrame {
    private final RoomDAO roomDAO = new RoomDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbTypeFilter;
    private JComboBox<String> cmbStatusFilter;

    public RoomFrame() {
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("Room & Venue Management");
        setSize(940, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(248, 250, 252));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("Campus Rooms, Auditoriums & Labs");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        filterPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(14);
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("Type:"));
        cmbTypeFilter = new JComboBox<>(new String[]{"ALL", "Classroom", "Computer Lab", "Seminar Hall", "Auditorium", "Conference Room"});
        filterPanel.add(cmbTypeFilter);

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
            cmbTypeFilter.setSelectedIndex(0);
            cmbStatusFilter.setSelectedIndex(0);
            loadData();
        });
        filterPanel.add(btnReset);

        topPanel.add(filterPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Room No", "Building", "Floor", "Capacity", "Type", "Status", "Facilities"};
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

        JButton btnAdd = new JButton("+ Add Room");
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
        List<Room> list = roomDAO.getAllRooms();
        for (Room r : list) {
            tableModel.addRow(new Object[]{
                r.getRoomId(),
                r.getRoomNumber(),
                r.getBuildingName(),
                r.getFloorNumber(),
                r.getCapacity(),
                r.getRoomType(),
                r.getStatus(),
                r.getFacilities()
            });
        }
    }

    private void applyFilter() {
        String kw = txtSearch.getText().trim();
        String type = (String) cmbTypeFilter.getSelectedItem();
        String stat = (String) cmbStatusFilter.getSelectedItem();

        tableModel.setRowCount(0);
        List<Room> list = roomDAO.searchRooms(kw, type, null, stat);
        for (Room r : list) {
            tableModel.addRow(new Object[]{
                r.getRoomId(),
                r.getRoomNumber(),
                r.getBuildingName(),
                r.getFloorNumber(),
                r.getCapacity(),
                r.getRoomType(),
                r.getStatus(),
                r.getFacilities()
            });
        }
    }

    private void showAddDialog() {
        JDialog dlg = new JDialog(this, "Add New Room Venue", true);
        dlg.setSize(420, 440);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(7, 2, 8, 10));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JTextField txtNumber = new JTextField();
        JTextField txtBuilding = new JTextField();
        JSpinner spFloor = new JSpinner(new SpinnerNumberModel(1, -2, 20, 1));
        JSpinner spCapacity = new JSpinner(new SpinnerNumberModel(50, 1, 2000, 5));
        JComboBox<String> cmbType = new JComboBox<>(new String[]{"Classroom", "Computer Lab", "Seminar Hall", "Auditorium", "Conference Room"});
        JTextField txtFacilities = new JTextField();
        JComboBox<String> cmbStat = new JComboBox<>(new String[]{"AVAILABLE", "BOOKED", "MAINTENANCE", "INACTIVE"});

        p.add(new JLabel("Room Number:")); p.add(txtNumber);
        p.add(new JLabel("Building Name:")); p.add(txtBuilding);
        p.add(new JLabel("Floor Number:")); p.add(spFloor);
        p.add(new JLabel("Seating Capacity:")); p.add(spCapacity);
        p.add(new JLabel("Room Type:")); p.add(cmbType);
        p.add(new JLabel("Facilities (e.g. AC, Projector):")); p.add(txtFacilities);
        p.add(new JLabel("Status:")); p.add(cmbStat);

        JButton btnSave = new JButton("Save Room");
        UIStyle.styleButton(btnSave);
        btnSave.addActionListener(e -> {
            if (txtNumber.getText().trim().isEmpty() || txtBuilding.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Room number and Building name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Room room = new Room();
            room.setRoomNumber(txtNumber.getText().trim());
            room.setBuildingName(txtBuilding.getText().trim());
            room.setFloorNumber((Integer) spFloor.getValue());
            room.setCapacity((Integer) spCapacity.getValue());
            room.setRoomType((String) cmbType.getSelectedItem());
            room.setFacilities(txtFacilities.getText().trim());
            room.setStatus((String) cmbStat.getSelectedItem());

            if (roomDAO.addRoom(room)) {
                JOptionPane.showMessageDialog(dlg, "Room created successfully!");
                dlg.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to create room. Room number might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Select a room to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int roomId = (Integer) tableModel.getValueAt(row, 0);
        Room room = roomDAO.getRoomById(roomId);
        if (room == null) return;

        JDialog dlg = new JDialog(this, "Edit Room #" + roomId, true);
        dlg.setSize(420, 440);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(7, 2, 8, 10));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JTextField txtNumber = new JTextField(room.getRoomNumber());
        JTextField txtBuilding = new JTextField(room.getBuildingName());
        JSpinner spFloor = new JSpinner(new SpinnerNumberModel(room.getFloorNumber(), -2, 20, 1));
        JSpinner spCapacity = new JSpinner(new SpinnerNumberModel(room.getCapacity(), 1, 2000, 5));
        JComboBox<String> cmbType = new JComboBox<>(new String[]{"Classroom", "Computer Lab", "Seminar Hall", "Auditorium", "Conference Room"});
        cmbType.setSelectedItem(room.getRoomType());
        JTextField txtFacilities = new JTextField(room.getFacilities() != null ? room.getFacilities() : "");
        JComboBox<String> cmbStat = new JComboBox<>(new String[]{"AVAILABLE", "BOOKED", "MAINTENANCE", "INACTIVE"});
        cmbStat.setSelectedItem(room.getStatus());

        p.add(new JLabel("Room Number:")); p.add(txtNumber);
        p.add(new JLabel("Building Name:")); p.add(txtBuilding);
        p.add(new JLabel("Floor Number:")); p.add(spFloor);
        p.add(new JLabel("Seating Capacity:")); p.add(spCapacity);
        p.add(new JLabel("Room Type:")); p.add(cmbType);
        p.add(new JLabel("Facilities:")); p.add(txtFacilities);
        p.add(new JLabel("Status:")); p.add(cmbStat);

        JButton btnSave = new JButton("Update Room");
        UIStyle.styleButton(btnSave);
        btnSave.addActionListener(e -> {
            room.setRoomNumber(txtNumber.getText().trim());
            room.setBuildingName(txtBuilding.getText().trim());
            room.setFloorNumber((Integer) spFloor.getValue());
            room.setCapacity((Integer) spCapacity.getValue());
            room.setRoomType((String) cmbType.getSelectedItem());
            room.setFacilities(txtFacilities.getText().trim());
            room.setStatus((String) cmbStat.getSelectedItem());

            if (roomDAO.updateRoom(room)) {
                JOptionPane.showMessageDialog(dlg, "Room updated successfully!");
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
            JOptionPane.showMessageDialog(this, "Select a room to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int roomId = (Integer) tableModel.getValueAt(row, 0);
        String number = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete room: " + number + " (ID: " + roomId + ")?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (roomDAO.deleteRoom(roomId)) {
                JOptionPane.showMessageDialog(this, "Room deleted successfully.");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete room: it is referenced by existing bookings or maintenance records.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
