package ui;

import dao.BookingDAO;
import dao.ResourceCategoryDAO;
import dao.ResourceDAO;
import dao.RoomDAO;
import dao.TimeSlotDAO;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BookingFrame extends JFrame {
    private final User currentUser;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final ResourceCategoryDAO categoryDAO = new ResourceCategoryDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final TimeSlotDAO slotDAO = new TimeSlotDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;

    public BookingFrame(User currentUser) {
        this.currentUser = currentUser;
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("Campus Booking System - Manage Bookings");
        setSize(1080, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(248, 250, 252));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("Resource Bookings & Reservation Requests");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        filterPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(16);
        filterPanel.add(txtSearch);

        filterPanel.add(new JLabel("Status:"));
        cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "PENDING", "APPROVED", "COMPLETED", "CANCELLED", "REJECTED"});
        filterPanel.add(cmbStatusFilter);

        JButton btnFilter = new JButton("Filter");
        UIStyle.styleButton(btnFilter);
        btnFilter.addActionListener(e -> applyFilter());
        filterPanel.add(btnFilter);

        JButton btnReset = new JButton("Reset");
        UIStyle.styleButton(btnReset);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatusFilter.setSelectedIndex(0);
            loadData();
        });
        filterPanel.add(btnReset);

        JButton btnNewBooking = new JButton("+ Book Resource (Wizard)");
        UIStyle.styleButton(btnNewBooking);
        btnNewBooking.addActionListener(e -> showNewBookingWizard());
        filterPanel.add(btnNewBooking);

        topPanel.add(filterPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Booking ID", "Reserved By", "Department", "Resource", "Room Venue", "Time Slot", "Date", "Purpose", "Status"};
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
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setBackground(new Color(248, 250, 252));

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            JButton btnApprove = new JButton("Approve");
            UIStyle.styleButton(btnApprove);
            btnApprove.addActionListener(e -> updateStatusAction("APPROVED"));
            bottomPanel.add(btnApprove);

            JButton btnReject = new JButton("Reject");
            UIStyle.styleButton(btnReject);
            btnReject.addActionListener(e -> updateStatusAction("REJECTED"));
            bottomPanel.add(btnReject);

            JButton btnComplete = new JButton("Mark Completed");
            UIStyle.styleButton(btnComplete);
            btnComplete.addActionListener(e -> updateStatusAction("COMPLETED"));
            bottomPanel.add(btnComplete);
        }

        JButton btnCancel = new JButton("Cancel Booking");
        UIStyle.styleButton(btnCancel);
        btnCancel.addActionListener(e -> handleCancelBooking());
        bottomPanel.add(btnCancel);

        JButton btnRefresh = new JButton("Refresh");
        UIStyle.styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadData());
        bottomPanel.add(btnRefresh);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<Booking> list;
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            list = bookingDAO.getAllBookings();
        } else {
            list = bookingDAO.getBookingsByUser(currentUser.getUserId());
        }
        populateTable(list);
    }

    private void applyFilter() {
        String kw = txtSearch.getText().trim();
        String status = (String) cmbStatusFilter.getSelectedItem();

        tableModel.setRowCount(0);
        List<Booking> list = bookingDAO.searchBookings(kw, status, null, null);
        populateTable(list);
    }

    private void populateTable(List<Booking> list) {
        tableModel.setRowCount(0);
        for (Booking b : list) {
            tableModel.addRow(new Object[]{
                b.getBookingId(),
                b.getUserName(),
                b.getDepartmentName(),
                b.getResourceName(),
                b.getRoomNumber() != null ? b.getRoomNumber() : "Portable Asset",
                b.getSlotName(),
                b.getBookingDate(),
                b.getPurpose(),
                b.getBookingStatus()
            });
        }
    }

    public void showNewBookingWizard() {
        JDialog dlg = new JDialog(this, "Book Campus Resource (Stored Procedure)", true);
        dlg.setSize(520, 560);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(7, 2, 8, 12));
        p.setBorder(new EmptyBorder(20, 25, 20, 25));

        JComboBox<ResourceCategory> cmbCategory = new JComboBox<>();
        List<ResourceCategory> categories = categoryDAO.getAllCategories();
        for (ResourceCategory rc : categories) {
            cmbCategory.addItem(rc);
        }

        JComboBox<Resource> cmbResource = new JComboBox<>();
        Runnable refreshResources = () -> {
            cmbResource.removeAllItems();
            ResourceCategory cat = (ResourceCategory) cmbCategory.getSelectedItem();
            if (cat != null) {
                List<Resource> resList = resourceDAO.getAvailableResources(cat.getCategoryId());
                for (Resource r : resList) {
                    cmbResource.addItem(r);
                }
            }
        };
        cmbCategory.addActionListener(e -> refreshResources.run());
        refreshResources.run();

        JComboBox<Object> cmbRoom = new JComboBox<>();
        cmbRoom.addItem("None / Portable Asset");
        for (Room rm : roomDAO.getAvailableRooms()) {
            cmbRoom.addItem(rm);
        }

        JTextField txtDate = new JTextField(LocalDate.now().plusDays(1).toString());

        JComboBox<TimeSlot> cmbSlot = new JComboBox<>();
        for (TimeSlot ts : slotDAO.getAllActiveSlots()) {
            cmbSlot.addItem(ts);
        }

        JTextArea txtPurpose = new JTextArea(3, 20);
        txtPurpose.setLineWrap(true);
        txtPurpose.setWrapStyleWord(true);
        JScrollPane scrollPurpose = new JScrollPane(txtPurpose);

        p.add(new JLabel("1. Resource Category:")); p.add(cmbCategory);
        p.add(new JLabel("2. Target Resource:")); p.add(cmbResource);
        p.add(new JLabel("3. Venue Room (Optional):")); p.add(cmbRoom);
        p.add(new JLabel("4. Booking Date (YYYY-MM-DD):")); p.add(txtDate);
        p.add(new JLabel("5. Time Slot:")); p.add(cmbSlot);
        p.add(new JLabel("6. Booking Purpose:")); p.add(scrollPurpose);

        JLabel lblNote = new JLabel("Auto-validated via MySQL Stored Procedure book_resource()");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNote.setForeground(new Color(100, 116, 139));
        p.add(new JLabel("")); p.add(lblNote);

        JButton btnBook = new JButton("Confirm Booking");
        UIStyle.styleButton(btnBook);
        btnBook.addActionListener(e -> {
            Resource res = (Resource) cmbResource.getSelectedItem();
            if (res == null) {
                JOptionPane.showMessageDialog(dlg, "Please select an available resource.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Integer roomId = null;
            Object roomObj = cmbRoom.getSelectedItem();
            if (roomObj instanceof Room) {
                roomId = ((Room) roomObj).getRoomId();
            }

            Date bDate;
            try {
                bDate = Date.valueOf(txtDate.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid date format. Please use YYYY-MM-DD", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            TimeSlot slot = (TimeSlot) cmbSlot.getSelectedItem();
            if (slot == null) {
                JOptionPane.showMessageDialog(dlg, "Please select a time slot.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String purpose = txtPurpose.getText().trim();
            if (purpose.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please provide a booking purpose.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Map<String, Object> procResult = bookingDAO.bookResourceViaProcedure(
                currentUser.getUserId(),
                res.getResourceId(),
                roomId,
                slot.getSlotId(),
                bDate,
                purpose
            );

            boolean success = (Boolean) procResult.get("success");
            String message = (String) procResult.get("message");
            int bookingId = (Integer) procResult.get("bookingId");

            if (success) {
                JOptionPane.showMessageDialog(dlg,
                    "Booking Successful!\nReference ID: #" + bookingId + "\n" + message,
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg,
                    "Booking Conflict / Error:\n" + message,
                    "Reservation Rejected", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        bp.add(btnBook);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void updateStatusAction(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int bookingId = (Integer) tableModel.getValueAt(row, 0);
        if (bookingDAO.updateBookingStatus(bookingId, newStatus, currentUser.getUserId())) {
            JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " status updated to " + newStatus);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update booking status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancelBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking to cancel.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int bookingId = (Integer) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel booking #" + bookingId + "?",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Map<String, Object> res = bookingDAO.cancelBookingViaProcedure(bookingId, currentUser.getUserId());
            boolean success = (Boolean) res.get("success");
            String msg = (String) res.get("message");

            if (success) {
                JOptionPane.showMessageDialog(this, msg, "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, msg, "Cancellation Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
