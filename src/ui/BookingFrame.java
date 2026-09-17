package ui;

import dao.BookingDAO;
import dao.ResourceCategoryDAO;
import dao.ResourceDAO;
import dao.RoomDAO;
import dao.TimeSlotDAO;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookingFrame extends JFrame {
    private final User currentUser;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final ResourceCategoryDAO categoryDAO = new ResourceCategoryDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final TimeSlotDAO slotDAO = new TimeSlotDAO();

    private JTabbedPane tabbedPane;

    // Tab 1: Pending
    private JTable tblPending;
    private DefaultTableModel modelPending;
    private JTextField txtSearchPending;
    private JComboBox<String> cmbTypePending;
    private JLabel lblPendingHeader;
    private List<Booking> cachedPendingList = new ArrayList<>();

    // Tab 2: Approved
    private JTable tblApproved;
    private DefaultTableModel modelApproved;
    private JTextField txtSearchApproved;
    private JComboBox<String> cmbTypeApproved;
    private JLabel lblApprovedHeader;
    private List<Booking> cachedApprovedList = new ArrayList<>();

    // Tab 3: All
    private JTable tblAll;
    private DefaultTableModel modelAll;
    private JTextField txtSearchAll;
    private JComboBox<String> cmbStatusFilterAll;
    private List<Booking> cachedAllList = new ArrayList<>();

    private static final String[] TABLE_COLUMNS = {
        "Booking ID", "Reserved By", "Department", "Resource / Asset", "Room / Venue", "Time Slot", "Date", "Purpose", "Status"
    };

    public BookingFrame(User currentUser) {
        this.currentUser = currentUser;
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("Campus Resource Booking System - Labs & Classrooms Management");
        setSize(1160, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(12, 16, 14, 16));
        mainPanel.setBackground(new Color(248, 250, 252));

        // Header Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(248, 250, 252));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBox.setOpaque(false);
        JLabel lblTitle = new JLabel("Campus Resource Requests: Labs & Classrooms Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblTitle.setForeground(new Color(30, 41, 59));

        JLabel lblSub = new JLabel("Dedicated approval queues for Pending Requests, confirmed schedules for Approved Requests, and complete logs.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));
        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        topActions.setOpaque(false);

        JButton btnNewBooking = new JButton("+ Book Lab / Classroom");
        UIStyle.styleButton(btnNewBooking);
        btnNewBooking.addActionListener(e -> showNewBookingWizard());

        JButton btnGlobalRefresh = new JButton("Refresh All");
        UIStyle.styleButton(btnGlobalRefresh);
        btnGlobalRefresh.addActionListener(e -> loadData());

        topActions.add(btnNewBooking);
        topActions.add(btnGlobalRefresh);

        topPanel.add(titleBox, BorderLayout.CENTER);
        topPanel.add(topActions, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Tabbed Pane setup
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // 1. Pending Panel
        tabbedPane.addTab("⏳ Pending Requests (0)", createPendingPanel());

        // 2. Approved Panel
        tabbedPane.addTab("✔ Approved Requests (0)", createApprovedPanel());

        // 3. All Bookings Panel
        tabbedPane.addTab("📋 All Bookings (0)", createAllBookingsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    // ==========================================
    // 1. PENDING REQUESTS PANEL
    // ==========================================
    private JPanel createPendingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Alert / Header banner
        JPanel banner = new JPanel(new BorderLayout(8, 8));
        banner.setBackground(new Color(254, 243, 199)); // warm amber
        banner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(251, 191, 36), 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));

        lblPendingHeader = new JLabel("⏳ PENDING REQUESTS QUEUE: Review classroom and computer lab reservations awaiting authorization.");
        lblPendingHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPendingHeader.setForeground(new Color(146, 64, 14));

        JLabel lblPendingSub = new JLabel("Click 'Accept / Approve' to confirm the reservation and lock the venue, or 'Reject' to decline.");
        lblPendingSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPendingSub.setForeground(new Color(180, 83, 9));

        JPanel bannerText = new JPanel(new GridLayout(2, 1, 0, 2));
        bannerText.setOpaque(false);
        bannerText.add(lblPendingHeader);
        bannerText.add(lblPendingSub);
        banner.add(bannerText, BorderLayout.CENTER);

        // Filter Bar for Pending
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        filterBar.add(new JLabel("Quick Search:"));
        txtSearchPending = new JTextField(15);
        filterBar.add(txtSearchPending);

        filterBar.add(new JLabel("Venue Type:"));
        cmbTypePending = new JComboBox<>(new String[]{"All Venues & Labs", "Computer Labs Only", "Classrooms Only"});
        filterBar.add(cmbTypePending);

        JButton btnApplyPending = new JButton("Filter");
        UIStyle.styleButton(btnApplyPending);
        btnApplyPending.addActionListener(e -> applyPendingFilter());
        filterBar.add(btnApplyPending);

        JButton btnResetPending = new JButton("Reset");
        UIStyle.styleButton(btnResetPending);
        btnResetPending.addActionListener(e -> {
            txtSearchPending.setText("");
            cmbTypePending.setSelectedIndex(0);
            renderPendingTable(cachedPendingList);
        });
        filterBar.add(btnResetPending);

        JPanel topSection = new JPanel(new BorderLayout(6, 6));
        topSection.setOpaque(false);
        topSection.add(banner, BorderLayout.NORTH);
        topSection.add(filterBar, BorderLayout.CENTER);
        panel.add(topSection, BorderLayout.NORTH);

        // Pending Table
        modelPending = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPending = new JTable(modelPending);
        styleTable(tblPending);

        JScrollPane scrollPane = new JScrollPane(tblPending);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Actions Bar for Pending
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionsBar.setBackground(new Color(248, 250, 252));

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            JButton btnApprove = new JButton("Approve Request");
            UIStyle.styleButton(btnApprove);
            btnApprove.setToolTipText("Approve this reservation request and allocate venue");
            btnApprove.addActionListener(e -> actionApproveFromTable(tblPending, modelPending));
            actionsBar.add(btnApprove);

            JButton btnReject = new JButton("Reject Request");
            UIStyle.styleButton(btnReject);
            btnReject.setToolTipText("Reject this pending reservation");
            btnReject.addActionListener(e -> actionRejectFromTable(tblPending, modelPending));
            actionsBar.add(btnReject);
        }

        JButton btnCancel = new JButton("Cancel Request");
        UIStyle.styleButton(btnCancel);
        btnCancel.setToolTipText("Withdraw pending request");
        btnCancel.addActionListener(e -> actionCancelFromTable(tblPending, modelPending));
        actionsBar.add(btnCancel);

        JButton btnRefresh = new JButton("Refresh Queue");
        UIStyle.styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadData());
        actionsBar.add(btnRefresh);

        panel.add(actionsBar, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 2. APPROVED REQUESTS PANEL
    // ==========================================
    private JPanel createApprovedPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Alert / Header banner
        JPanel banner = new JPanel(new BorderLayout(8, 8));
        banner.setBackground(new Color(220, 252, 231)); // fresh emerald green
        banner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(134, 239, 172), 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));

        lblApprovedHeader = new JLabel("✔ APPROVED RESERVATIONS: Confirmed and active lab & classroom sessions.");
        lblApprovedHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblApprovedHeader.setForeground(new Color(21, 128, 61));

        JLabel lblApprovedSub = new JLabel("The selected venues and equipment are booked and reserved for the scheduled faculty.");
        lblApprovedSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblApprovedSub.setForeground(new Color(22, 101, 52));

        JPanel bannerText = new JPanel(new GridLayout(2, 1, 0, 2));
        bannerText.setOpaque(false);
        bannerText.add(lblApprovedHeader);
        bannerText.add(lblApprovedSub);
        banner.add(bannerText, BorderLayout.CENTER);

        // Filter Bar for Approved
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        filterBar.add(new JLabel("Quick Search:"));
        txtSearchApproved = new JTextField(15);
        filterBar.add(txtSearchApproved);

        filterBar.add(new JLabel("Venue Type:"));
        cmbTypeApproved = new JComboBox<>(new String[]{"All Venues & Labs", "Computer Labs Only", "Classrooms Only"});
        filterBar.add(cmbTypeApproved);

        JButton btnApplyApproved = new JButton("Filter");
        UIStyle.styleButton(btnApplyApproved);
        btnApplyApproved.addActionListener(e -> applyApprovedFilter());
        filterBar.add(btnApplyApproved);

        JButton btnResetApproved = new JButton("Reset");
        UIStyle.styleButton(btnResetApproved);
        btnResetApproved.addActionListener(e -> {
            txtSearchApproved.setText("");
            cmbTypeApproved.setSelectedIndex(0);
            renderApprovedTable(cachedApprovedList);
        });
        filterBar.add(btnResetApproved);

        JPanel topSection = new JPanel(new BorderLayout(6, 6));
        topSection.setOpaque(false);
        topSection.add(banner, BorderLayout.NORTH);
        topSection.add(filterBar, BorderLayout.CENTER);
        panel.add(topSection, BorderLayout.NORTH);

        // Approved Table
        modelApproved = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblApproved = new JTable(modelApproved);
        styleTable(tblApproved);

        JScrollPane scrollPane = new JScrollPane(tblApproved);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Actions Bar for Approved
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionsBar.setBackground(new Color(248, 250, 252));

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            JButton btnComplete = new JButton("Mark Completed");
            UIStyle.styleButton(btnComplete);
            btnComplete.setToolTipText("Session concluded; restore room and resources to Available");
            btnComplete.addActionListener(e -> actionCompleteFromTable(tblApproved, modelApproved));
            actionsBar.add(btnComplete);
        }

        JButton btnCancel = new JButton("Cancel Reservation");
        UIStyle.styleButton(btnCancel);
        btnCancel.setToolTipText("Cancel approved reservation and release resource");
        btnCancel.addActionListener(e -> actionCancelFromTable(tblApproved, modelApproved));
        actionsBar.add(btnCancel);

        JButton btnRefresh = new JButton("Refresh Approved");
        UIStyle.styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadData());
        actionsBar.add(btnRefresh);

        panel.add(actionsBar, BorderLayout.SOUTH);
        return panel;
    }

    // ==========================================
    // 3. ALL BOOKINGS PANEL
    // ==========================================
    private JPanel createAllBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        filterPanel.add(new JLabel("Search:"));
        txtSearchAll = new JTextField(16);
        filterPanel.add(txtSearchAll);

        filterPanel.add(new JLabel("Status:"));
        cmbStatusFilterAll = new JComboBox<>(new String[]{"ALL", "PENDING", "APPROVED", "COMPLETED", "CANCELLED", "REJECTED"});
        filterPanel.add(cmbStatusFilterAll);

        JButton btnFilter = new JButton("Filter");
        UIStyle.styleButton(btnFilter);
        btnFilter.addActionListener(e -> applyAllFilter());
        filterPanel.add(btnFilter);

        JButton btnReset = new JButton("Reset");
        UIStyle.styleButton(btnReset);
        btnReset.addActionListener(e -> {
            txtSearchAll.setText("");
            cmbStatusFilterAll.setSelectedIndex(0);
            loadData();
        });
        filterPanel.add(btnReset);

        panel.add(filterPanel, BorderLayout.NORTH);

        modelAll = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblAll = new JTable(modelAll);
        styleTable(tblAll);

        JScrollPane scrollPane = new JScrollPane(tblAll);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setBackground(new Color(248, 250, 252));

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            JButton btnApprove = new JButton("Approve");
            UIStyle.styleButton(btnApprove);
            btnApprove.addActionListener(e -> actionApproveFromTable(tblAll, modelAll));
            bottomPanel.add(btnApprove);

            JButton btnReject = new JButton("Reject");
            UIStyle.styleButton(btnReject);
            btnReject.addActionListener(e -> actionRejectFromTable(tblAll, modelAll));
            bottomPanel.add(btnReject);

            JButton btnComplete = new JButton("Mark Completed");
            UIStyle.styleButton(btnComplete);
            btnComplete.addActionListener(e -> actionCompleteFromTable(tblAll, modelAll));
            bottomPanel.add(btnComplete);
        }

        JButton btnCancel = new JButton("Cancel Booking");
        UIStyle.styleButton(btnCancel);
        btnCancel.addActionListener(e -> actionCancelFromTable(tblAll, modelAll));
        bottomPanel.add(btnCancel);

        JButton btnRefresh = new JButton("Refresh");
        UIStyle.styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadData());
        bottomPanel.add(btnRefresh);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        // Attach custom status badge renderer to Column 8 ("Status")
        DefaultTableCellRenderer badgeRenderer = UIStyle.createStatusBadgeRenderer();
        table.getColumnModel().getColumn(8).setCellRenderer(badgeRenderer);
        table.getColumnModel().getColumn(8).setPreferredWidth(120);

        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(70);  // Booking ID
        table.getColumnModel().getColumn(1).setPreferredWidth(130); // Reserved By
        table.getColumnModel().getColumn(2).setPreferredWidth(110); // Department
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Resource
        table.getColumnModel().getColumn(4).setPreferredWidth(140); // Room / Venue
        table.getColumnModel().getColumn(5).setPreferredWidth(110); // Time Slot
        table.getColumnModel().getColumn(6).setPreferredWidth(90);  // Date
        table.getColumnModel().getColumn(7).setPreferredWidth(180); // Purpose
    }

    // ==========================================
    // DATA LOADING & SYNCHRONIZATION
    // ==========================================
    public void loadData() {
        List<Booking> rawList;
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            rawList = bookingDAO.getAllBookings();
        } else {
            rawList = bookingDAO.getBookingsByUser(currentUser.getUserId());
        }

        cachedAllList = new ArrayList<>(rawList);
        cachedPendingList = new ArrayList<>();
        cachedApprovedList = new ArrayList<>();

        for (Booking b : rawList) {
            String st = b.getBookingStatus() != null ? b.getBookingStatus().toUpperCase() : "";
            if ("PENDING".equals(st)) {
                cachedPendingList.add(b);
            } else if ("APPROVED".equals(st)) {
                cachedApprovedList.add(b);
            }
        }

        renderPendingTable(cachedPendingList);
        renderApprovedTable(cachedApprovedList);
        renderAllTable(cachedAllList);

        // Update tab header badges
        tabbedPane.setTitleAt(0, "⏳ Pending Requests (" + cachedPendingList.size() + ")");
        tabbedPane.setTitleAt(1, "✔ Approved Requests (" + cachedApprovedList.size() + ")");
        tabbedPane.setTitleAt(2, "📋 All Bookings (" + cachedAllList.size() + ")");

        if (lblPendingHeader != null) {
            lblPendingHeader.setText("⏳ PENDING REQUESTS QUEUE (" + cachedPendingList.size() + " Waiting): Review classroom and computer lab reservations awaiting authorization.");
        }
        if (lblApprovedHeader != null) {
            lblApprovedHeader.setText("✔ APPROVED RESERVATIONS (" + cachedApprovedList.size() + " Active): Confirmed classroom and computer lab reservations.");
        }
    }

    private void renderPendingTable(List<Booking> list) {
        modelPending.setRowCount(0);
        for (Booking b : list) {
            modelPending.addRow(createTableRow(b));
        }
    }

    private void renderApprovedTable(List<Booking> list) {
        modelApproved.setRowCount(0);
        for (Booking b : list) {
            modelApproved.addRow(createTableRow(b));
        }
    }

    private void renderAllTable(List<Booking> list) {
        modelAll.setRowCount(0);
        for (Booking b : list) {
            modelAll.addRow(createTableRow(b));
        }
    }

    private Object[] createTableRow(Booking b) {
        String venue = b.getRoomNumber() != null ? b.getRoomNumber() : "Portable Asset";
        if (b.getBuildingName() != null && !b.getBuildingName().trim().isEmpty() && b.getRoomNumber() != null) {
            venue += " (" + b.getBuildingName() + ")";
        }
        return new Object[]{
            b.getBookingId(),
            b.getUserName(),
            b.getDepartmentName(),
            b.getResourceName(),
            venue,
            b.getSlotName(),
            b.getBookingDate(),
            b.getPurpose(),
            b.getBookingStatus()
        };
    }

    private void applyPendingFilter() {
        String kw = txtSearchPending.getText().trim().toLowerCase();
        String typeFilter = (String) cmbTypePending.getSelectedItem();

        List<Booking> filtered = new ArrayList<>();
        for (Booking b : cachedPendingList) {
            if (!matchesKeyword(b, kw)) continue;
            if (!matchesVenueType(b, typeFilter)) continue;
            filtered.add(b);
        }
        renderPendingTable(filtered);
    }

    private void applyApprovedFilter() {
        String kw = txtSearchApproved.getText().trim().toLowerCase();
        String typeFilter = (String) cmbTypeApproved.getSelectedItem();

        List<Booking> filtered = new ArrayList<>();
        for (Booking b : cachedApprovedList) {
            if (!matchesKeyword(b, kw)) continue;
            if (!matchesVenueType(b, typeFilter)) continue;
            filtered.add(b);
        }
        renderApprovedTable(filtered);
    }

    private boolean matchesKeyword(Booking b, String kw) {
        if (kw.isEmpty()) return true;
        String user = b.getUserName() != null ? b.getUserName().toLowerCase() : "";
        String dept = b.getDepartmentName() != null ? b.getDepartmentName().toLowerCase() : "";
        String res = b.getResourceName() != null ? b.getResourceName().toLowerCase() : "";
        String room = b.getRoomNumber() != null ? b.getRoomNumber().toLowerCase() : "";
        String pur = b.getPurpose() != null ? b.getPurpose().toLowerCase() : "";
        return user.contains(kw) || dept.contains(kw) || res.contains(kw) || room.contains(kw) || pur.contains(kw);
    }

    private boolean matchesVenueType(Booking b, String typeFilter) {
        if (typeFilter == null || typeFilter.contains("All")) return true;
        String room = b.getRoomNumber() != null ? b.getRoomNumber().toLowerCase() : "";
        String res = b.getResourceName() != null ? b.getResourceName().toLowerCase() : "";
        String cat = b.getResourceCategory() != null ? b.getResourceCategory().toLowerCase() : "";

        if (typeFilter.contains("Computer Labs")) {
            return room.contains("lab") || res.contains("lab") || cat.contains("lab");
        } else if (typeFilter.contains("Classrooms")) {
            return (room.contains("cr-") || room.contains("classroom") || res.contains("classroom"))
                   && !room.contains("lab") && !res.contains("lab");
        }
        return true;
    }

    private void applyAllFilter() {
        String kw = txtSearchAll.getText().trim();
        String status = (String) cmbStatusFilterAll.getSelectedItem();
        List<Booking> list = bookingDAO.searchBookings(kw, status, null, null);
        renderAllTable(list);
    }

    // ==========================================
    // ACTION HANDLERS
    // ==========================================
    private void actionApproveFromTable(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int bookingId = (Integer) model.getValueAt(row, 0);
        String applicant = (String) model.getValueAt(row, 1);
        String venue = (String) model.getValueAt(row, 4);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to ACCEPT / APPROVE booking #" + bookingId + " for " + applicant + "?\nVenue: " + venue,
            "Confirm Approval", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (bookingDAO.updateBookingStatus(bookingId, "APPROVED", currentUser.getUserId())) {
                JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " has been successfully APPROVED!\nVenue allocated.", "Approved", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to approve booking. Please check database logs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actionRejectFromTable(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int bookingId = (Integer) model.getValueAt(row, 0);
        String applicant = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to REJECT reservation request #" + bookingId + " from " + applicant + "?",
            "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (bookingDAO.updateBookingStatus(bookingId, "REJECTED", currentUser.getUserId())) {
                JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " has been marked as REJECTED.", "Rejected", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reject booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actionCompleteFromTable(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an approved booking from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int bookingId = (Integer) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Mark booking #" + bookingId + " as COMPLETED?\nThis will release the venue/resource back to Available.",
            "Confirm Completion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (bookingDAO.updateBookingStatus(bookingId, "COMPLETED", currentUser.getUserId())) {
                JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " completed. Venue is now Available!", "Completed", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to complete booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actionCancelFromTable(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int bookingId = (Integer) model.getValueAt(row, 0);

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

    public void showNewBookingWizard() {
        JDialog dlg = new JDialog(this, "Book Campus Resource (Stored Procedure)", true);
        dlg.setSize(540, 580);
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
}
