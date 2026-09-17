package ui;

import dao.BookingDAO;
import dao.ReportDAO;
import model.Booking;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JFrame {
    private final User currentUser;
    private final ReportDAO reportDAO = new ReportDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    private JLabel lblTotalUsers;
    private JLabel lblTotalResources;
    private JLabel lblAvailResources;
    private JLabel lblTotalRooms;
    private JLabel lblTodayBookings;
    private JLabel lblPendingBookings;
    private JLabel lblMaintenance;

    private JTable tblRecent;
    private DefaultTableModel tblModelRecent;

    private JTable tblPending;
    private DefaultTableModel tblModelPending;

    public AdminDashboard(User user) {
        this.currentUser = user;
        initUI();
        refreshMetrics();
        loadRecentBookings();
        loadPendingQueue();
    }

    private void initUI() {
        setTitle("Campus Resource Booking System - Administrator Control Center");
        setSize(1180, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(241, 245, 249));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(16, 25, 16, 25));

        JLabel lblAppTitle = new JLabel("SANJIVANI UNIVERSITY - CAMPUS RESOURCE BOOKING SYSTEM");
        lblAppTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAppTitle.setForeground(Color.WHITE);

        JLabel lblUserGreeting = new JLabel("Logged in as: " + currentUser.getName() + " [ADMIN]");
        lblUserGreeting.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUserGreeting.setForeground(new Color(203, 213, 225));

        headerPanel.add(lblAppTitle, BorderLayout.WEST);
        headerPanel.add(lblUserGreeting, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        navBar.setBackground(Color.WHITE);
        navBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JButton btnNavUsers = createNavButton("Users", e -> new UserFrame().setVisible(true));
        JButton btnNavDepts = createNavButton("Departments", e -> new DepartmentFrame().setVisible(true));
        JButton btnNavResources = createNavButton("Resources", e -> new ResourceFrame().setVisible(true));
        JButton btnNavRooms = createNavButton("Rooms & Venues", e -> new RoomFrame().setVisible(true));
        JButton btnNavBookings = createNavButton("Bookings", e -> new BookingFrame(currentUser).setVisible(true));
        JButton btnNavMaintenance = createNavButton("Maintenance", e -> new MaintenanceFrame(currentUser).setVisible(true));
        JButton btnNavFeedback = createNavButton("Feedback", e -> new FeedbackFrame(currentUser).setVisible(true));
        JButton btnNavReports = createNavButton("Reports & Analytics", e -> new ReportsFrame().setVisible(true));

        JButton btnLogout = new JButton("Logout");
        UIStyle.styleButton(btnLogout);
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        navBar.add(btnNavUsers);
        navBar.add(btnNavDepts);
        navBar.add(btnNavResources);
        navBar.add(btnNavRooms);
        navBar.add(btnNavBookings);
        navBar.add(btnNavMaintenance);
        navBar.add(btnNavFeedback);
        navBar.add(btnNavReports);
        navBar.add(Box.createHorizontalStrut(15));
        navBar.add(btnLogout);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(new Color(241, 245, 249));
        centerPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        JPanel statsGrid = new JPanel(new GridLayout(1, 7, 10, 10));
        statsGrid.setBackground(new Color(241, 245, 249));

        lblTotalUsers = new JLabel("0", SwingConstants.CENTER);
        lblTotalResources = new JLabel("0", SwingConstants.CENTER);
        lblAvailResources = new JLabel("0", SwingConstants.CENTER);
        lblTotalRooms = new JLabel("0", SwingConstants.CENTER);
        lblTodayBookings = new JLabel("0", SwingConstants.CENTER);
        lblPendingBookings = new JLabel("0", SwingConstants.CENTER);
        lblMaintenance = new JLabel("0", SwingConstants.CENTER);

        statsGrid.add(createMetricCard("Total Users", lblTotalUsers, new Color(59, 130, 246)));
        statsGrid.add(createMetricCard("Total Resources", lblTotalResources, new Color(99, 102, 241)));
        statsGrid.add(createMetricCard("Available Assets", lblAvailResources, new Color(16, 185, 129)));
        statsGrid.add(createMetricCard("Total Rooms", lblTotalRooms, new Color(14, 165, 233)));
        statsGrid.add(createMetricCard("Today Bookings", lblTodayBookings, new Color(245, 158, 11)));
        statsGrid.add(createMetricCard("Pending Approvals", lblPendingBookings, new Color(239, 68, 68)));
        statsGrid.add(createMetricCard("Maintenance Logs", lblMaintenance, new Color(107, 114, 128)));

        // Live Bookings & Pending Approvals Split or Tabs
        JTabbedPane dashboardTabs = new JTabbedPane();
        dashboardTabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // TAB 1: Live Bookings Feed
        JPanel recentPanel = new JPanel(new BorderLayout(8, 8));
        recentPanel.setBackground(Color.WHITE);
        recentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            new EmptyBorder(12, 15, 12, 15)
        ));

        JPanel recentTop = new JPanel(new BorderLayout());
        recentTop.setBackground(Color.WHITE);

        JLabel lblRecentTitle = new JLabel("Live Campus Bookings & Schedules");
        lblRecentTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblRecentTitle.setForeground(new Color(30, 41, 59));

        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        quickActions.setBackground(Color.WHITE);

        JButton btnManageBookings = new JButton("Open Bookings Manager ->");
        UIStyle.styleButton(btnManageBookings);
        btnManageBookings.addActionListener(e -> new BookingFrame(currentUser).setVisible(true));

        JButton btnQuickRefresh = new JButton("Refresh");
        UIStyle.styleButton(btnQuickRefresh);
        btnQuickRefresh.addActionListener(e -> {
            refreshMetrics();
            loadRecentBookings();
            loadPendingQueue();
        });

        quickActions.add(btnManageBookings);
        quickActions.add(btnQuickRefresh);
        recentTop.add(lblRecentTitle, BorderLayout.WEST);
        recentTop.add(quickActions, BorderLayout.EAST);
        recentPanel.add(recentTop, BorderLayout.NORTH);

        String[] cols = {"ID", "User", "Dept", "Resource / Lab", "Room Venue", "Slot", "Booking Date", "Status"};
        tblModelRecent = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblRecent = new JTable(tblModelRecent);
        tblRecent.setRowHeight(28);
        tblRecent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblRecent.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblRecent.getTableHeader().setBackground(new Color(241, 245, 249));
        tblRecent.getColumnModel().getColumn(7).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        tblRecent.getColumnModel().getColumn(7).setPreferredWidth(120);

        JScrollPane scrollRecent = new JScrollPane(tblRecent);
        recentPanel.add(scrollRecent, BorderLayout.CENTER);

        // TAB 2: Pending Approval Queue (Direct Actionable)
        JPanel pendingPanel = new JPanel(new BorderLayout(8, 8));
        pendingPanel.setBackground(Color.WHITE);
        pendingPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            new EmptyBorder(12, 15, 12, 15)
        ));

        JPanel pendingTop = new JPanel(new BorderLayout());
        pendingTop.setBackground(new Color(254, 243, 199));
        pendingTop.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(251, 191, 36), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblPendingQueueTitle = new JLabel("Action Required: Review Pending Lab & Classroom Reservation Requests");
        lblPendingQueueTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPendingQueueTitle.setForeground(new Color(146, 64, 14));
        pendingTop.add(lblPendingQueueTitle, BorderLayout.WEST);

        String[] pendingCols = {"ID", "Applicant", "Dept", "Resource / Lab", "Room Venue", "Slot", "Booking Date", "Purpose", "Status"};
        tblModelPending = new DefaultTableModel(pendingCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblPending = new JTable(tblModelPending);
        tblPending.setRowHeight(28);
        tblPending.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblPending.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblPending.getTableHeader().setBackground(new Color(241, 245, 249));
        tblPending.getColumnModel().getColumn(8).setCellRenderer(UIStyle.createStatusBadgeRenderer());
        tblPending.getColumnModel().getColumn(8).setPreferredWidth(120);

        JScrollPane scrollPending = new JScrollPane(tblPending);

        JPanel pendingActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pendingActions.setBackground(Color.WHITE);

        JButton btnApproveDirect = new JButton("Approve Request");
        UIStyle.styleButton(btnApproveDirect);
        btnApproveDirect.addActionListener(e -> {
            int row = tblPending.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a pending request to approve.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int bookingId = (Integer) tblModelPending.getValueAt(row, 0);
            if (bookingDAO.updateBookingStatus(bookingId, "APPROVED", currentUser.getUserId())) {
                JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " APPROVED successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshMetrics();
                loadRecentBookings();
                loadPendingQueue();
            }
        });

        JButton btnRejectDirect = new JButton("Reject Request");
        UIStyle.styleButton(btnRejectDirect);
        btnRejectDirect.addActionListener(e -> {
            int row = tblPending.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a pending request to reject.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int bookingId = (Integer) tblModelPending.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Reject booking request #" + bookingId + "?", "Confirm Rejection", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (bookingDAO.updateBookingStatus(bookingId, "REJECTED", currentUser.getUserId())) {
                    JOptionPane.showMessageDialog(this, "Booking #" + bookingId + " REJECTED.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                    refreshMetrics();
                    loadRecentBookings();
                    loadPendingQueue();
                }
            }
        });

        pendingActions.add(btnApproveDirect);
        pendingActions.add(btnRejectDirect);

        pendingPanel.add(pendingTop, BorderLayout.NORTH);
        pendingPanel.add(scrollPending, BorderLayout.CENTER);
        pendingPanel.add(pendingActions, BorderLayout.SOUTH);

        dashboardTabs.addTab("⏳ Pending Approvals Queue", pendingPanel);
        dashboardTabs.addTab("📋 Live Bookings Feed", recentPanel);

        centerPanel.add(statsGrid, BorderLayout.NORTH);
        centerPanel.add(dashboardTabs, BorderLayout.CENTER);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(navBar, BorderLayout.NORTH);
        contentWrapper.add(centerPanel, BorderLayout.CENTER);

        mainPanel.add(contentWrapper, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JButton createNavButton(String text, java.awt.event.ActionListener l) {
        JButton btn = new JButton(text);
        UIStyle.styleButton(btn);
        btn.addActionListener(l);
        return btn;
    }

    private JPanel createMetricCard(String title, JLabel lblVal, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 8, 12, 8)
            )
        ));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblT.setForeground(new Color(100, 116, 139));
        lblT.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(new Color(30, 41, 59));
        lblVal.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblT);
        card.add(Box.createVerticalStrut(6));
        card.add(lblVal);
        return card;
    }

    private void refreshMetrics() {
        new Thread(() -> {
            Map<String, Integer> counts = reportDAO.getDashboardSummaryCounts();
            SwingUtilities.invokeLater(() -> {
                lblTotalUsers.setText(String.valueOf(counts.getOrDefault("total_users", 0)));
                lblTotalResources.setText(String.valueOf(counts.getOrDefault("total_resources", 0)));
                lblAvailResources.setText(String.valueOf(counts.getOrDefault("available_resources", 0)));
                lblTotalRooms.setText(String.valueOf(counts.getOrDefault("total_rooms", 0)));
                lblTodayBookings.setText(String.valueOf(counts.getOrDefault("today_bookings", 0)));
                lblPendingBookings.setText(String.valueOf(counts.getOrDefault("pending_bookings", 0)));
                lblMaintenance.setText(String.valueOf(counts.getOrDefault("maintenance_resources", 0)));
            });
        }).start();
    }

    private void loadRecentBookings() {
        tblModelRecent.setRowCount(0);
        List<Booking> list = bookingDAO.getAllBookings();
        int max = Math.min(20, list.size());
        for (int i = 0; i < max; i++) {
            Booking b = list.get(i);
            tblModelRecent.addRow(new Object[]{
                b.getBookingId(),
                b.getUserName(),
                b.getDepartmentName(),
                b.getResourceName(),
                b.getRoomNumber() != null ? b.getRoomNumber() : "-",
                b.getSlotName(),
                b.getBookingDate(),
                b.getBookingStatus()
            });
        }
    }

    private void loadPendingQueue() {
        if (tblModelPending == null) return;
        tblModelPending.setRowCount(0);
        List<Booking> list = bookingDAO.getAllBookings();
        for (Booking b : list) {
            if ("PENDING".equalsIgnoreCase(b.getBookingStatus())) {
                String venue = b.getRoomNumber() != null ? b.getRoomNumber() : "Portable Asset";
                tblModelPending.addRow(new Object[]{
                    b.getBookingId(),
                    b.getUserName(),
                    b.getDepartmentName(),
                    b.getResourceName(),
                    venue,
                    b.getSlotName(),
                    b.getBookingDate(),
                    b.getPurpose(),
                    b.getBookingStatus()
                });
            }
        }
    }
}
