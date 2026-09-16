package ui;

import dao.BookingDAO;
import dao.ResourceDAO;
import model.Booking;
import model.Resource;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentDashboard extends JFrame {
    private final User currentUser;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();

    private JLabel lblAvailCount;
    private JLabel lblMyBookingsCount;
    private JLabel lblPendingCount;
    private JLabel lblCompletedCount;

    private JTable tblAvailable;
    private DefaultTableModel modelAvailable;
    private JTable tblMyBookings;
    private DefaultTableModel modelMyBookings;

    private JTextField txtSearchResource;

    public StudentDashboard(User user) {
        this.currentUser = user;
        initUI();
        refreshAllData();
    }

    private void initUI() {
        setTitle("Campus Resource Booking System - " + currentUser.getRole() + " Portal");
        setSize(1080, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(241, 245, 249));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42));
        headerPanel.setBorder(new EmptyBorder(16, 25, 16, 25));

        JLabel lblTitle = new JLabel("CAMPUS RESOURCE BOOKING PORTAL");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblUserInfo = new JLabel("Welcome, " + currentUser.getName() + " (" + currentUser.getRole() + " - " + currentUser.getDepartmentName() + ")");
        lblUserInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUserInfo.setForeground(new Color(203, 213, 225));

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblUserInfo, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(new Color(241, 245, 249));
        contentPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 12, 12));
        statsGrid.setBackground(new Color(241, 245, 249));

        lblAvailCount = new JLabel("0", SwingConstants.CENTER);
        lblMyBookingsCount = new JLabel("0", SwingConstants.CENTER);
        lblPendingCount = new JLabel("0", SwingConstants.CENTER);
        lblCompletedCount = new JLabel("0", SwingConstants.CENTER);

        statsGrid.add(createMetricCard("Available Resources", lblAvailCount, new Color(16, 185, 129)));
        statsGrid.add(createMetricCard("My Total Bookings", lblMyBookingsCount, new Color(59, 130, 246)));
        statsGrid.add(createMetricCard("Pending Approval", lblPendingCount, new Color(245, 158, 11)));
        statsGrid.add(createMetricCard("Completed Bookings", lblCompletedCount, new Color(139, 92, 246)));

        contentPanel.add(statsGrid, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel pnlBrowse = new JPanel(new BorderLayout(8, 8));
        pnlBrowse.setBackground(Color.WHITE);
        pnlBrowse.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        searchBar.setBackground(Color.WHITE);

        searchBar.add(new JLabel("Search Available Resource:"));
        txtSearchResource = new JTextField(18);
        searchBar.add(txtSearchResource);

        JButton btnSearch = new JButton("Search");
        UIStyle.styleButton(btnSearch);
        btnSearch.addActionListener(e -> filterAvailableResources());
        searchBar.add(btnSearch);

        JButton btnBookThis = new JButton("+ Book Resource");
        UIStyle.styleButton(btnBookThis);
        btnBookThis.addActionListener(e -> {
            BookingFrame bf = new BookingFrame(currentUser);
            bf.setVisible(true);
            bf.showNewBookingWizard();
        });
        searchBar.add(btnBookThis);

        pnlBrowse.add(searchBar, BorderLayout.NORTH);

        String[] availCols = {"ID", "Code", "Name", "Category", "Location", "Capacity", "Status"};
        modelAvailable = new DefaultTableModel(availCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblAvailable = new JTable(modelAvailable);
        tblAvailable.setRowHeight(26);
        tblAvailable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblAvailable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblAvailable.getTableHeader().setBackground(new Color(241, 245, 249));

        pnlBrowse.add(new JScrollPane(tblAvailable), BorderLayout.CENTER);
        tabbedPane.addTab("Browse Available Resources", pnlBrowse);

        JPanel pnlMyBookings = new JPanel(new BorderLayout(8, 8));
        pnlMyBookings.setBackground(Color.WHITE);
        pnlMyBookings.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] myCols = {"Booking ID", "Resource", "Room Venue", "Time Slot", "Date", "Purpose", "Status"};
        modelMyBookings = new DefaultTableModel(myCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblMyBookings = new JTable(modelMyBookings);
        tblMyBookings.setRowHeight(26);
        tblMyBookings.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblMyBookings.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblMyBookings.getTableHeader().setBackground(new Color(241, 245, 249));

        pnlMyBookings.add(new JScrollPane(tblMyBookings), BorderLayout.CENTER);

        JPanel myActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        myActions.setBackground(Color.WHITE);

        JButton btnCancelMy = new JButton("Cancel Selected Booking");
        UIStyle.styleButton(btnCancelMy);
        btnCancelMy.addActionListener(e -> cancelSelectedBooking());

        JButton btnFeedback = new JButton("Give Feedback");
        UIStyle.styleButton(btnFeedback);
        btnFeedback.addActionListener(e -> new FeedbackFrame(currentUser).setVisible(true));

        JButton btnRefreshAll = new JButton("Refresh");
        UIStyle.styleButton(btnRefreshAll);
        btnRefreshAll.addActionListener(e -> refreshAllData());

        myActions.add(btnCancelMy);
        myActions.add(btnFeedback);
        myActions.add(btnRefreshAll);

        pnlMyBookings.add(myActions, BorderLayout.SOUTH);
        tabbedPane.addTab("My Bookings History", pnlMyBookings);

        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel footerBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        footerBar.setBackground(Color.WHITE);
        footerBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnLogout = new JButton("Logout");
        UIStyle.styleButton(btnLogout);
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        footerBar.add(btnLogout);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(footerBar, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JPanel createMetricCard(String title, JLabel lblVal, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 10, 12, 10)
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

    public void refreshAllData() {
        new Thread(() -> {
            List<Resource> availList = resourceDAO.getAvailableResources(null);
            List<Booking> myList = bookingDAO.getBookingsByUser(currentUser.getUserId());

            int pending = 0;
            int completed = 0;
            for (Booking b : myList) {
                if ("PENDING".equalsIgnoreCase(b.getBookingStatus())) pending++;
                if ("COMPLETED".equalsIgnoreCase(b.getBookingStatus())) completed++;
            }

            final int pFinal = pending;
            final int cFinal = completed;

            SwingUtilities.invokeLater(() -> {
                lblAvailCount.setText(String.valueOf(availList.size()));
                lblMyBookingsCount.setText(String.valueOf(myList.size()));
                lblPendingCount.setText(String.valueOf(pFinal));
                lblCompletedCount.setText(String.valueOf(cFinal));

                modelAvailable.setRowCount(0);
                for (Resource r : availList) {
                    modelAvailable.addRow(new Object[]{
                        r.getResourceId(),
                        r.getResourceCode(),
                        r.getResourceName(),
                        r.getCategoryName(),
                        r.getLocation(),
                        r.getCapacity(),
                        r.getStatus()
                    });
                }

                modelMyBookings.setRowCount(0);
                for (Booking b : myList) {
                    modelMyBookings.addRow(new Object[]{
                        b.getBookingId(),
                        b.getResourceName(),
                        b.getRoomNumber() != null ? b.getRoomNumber() : "-",
                        b.getSlotName(),
                        b.getBookingDate(),
                        b.getPurpose(),
                        b.getBookingStatus()
                    });
                }
            });
        }).start();
    }

    private void filterAvailableResources() {
        String kw = txtSearchResource.getText().trim();
        List<Resource> list = resourceDAO.searchResources(kw, null, "AVAILABLE", null);
        modelAvailable.setRowCount(0);
        for (Resource r : list) {
            modelAvailable.addRow(new Object[]{
                r.getResourceId(),
                r.getResourceCode(),
                r.getResourceName(),
                r.getCategoryName(),
                r.getLocation(),
                r.getCapacity(),
                r.getStatus()
            });
        }
    }

    private void cancelSelectedBooking() {
        int row = tblMyBookings.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking to cancel.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int bookingId = (Integer) modelMyBookings.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel booking #" + bookingId + "?",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            java.util.Map<String, Object> res = bookingDAO.cancelBookingViaProcedure(bookingId, currentUser.getUserId());
            boolean success = (Boolean) res.get("success");
            String msg = (String) res.get("message");

            if (success) {
                JOptionPane.showMessageDialog(this, msg, "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this, msg, "Cancellation Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
