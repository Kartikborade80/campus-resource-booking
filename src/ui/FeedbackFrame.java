package ui;

import dao.BookingDAO;
import dao.FeedbackDAO;
import model.Booking;
import model.Feedback;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FeedbackFrame extends JFrame {
    private final User currentUser;
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    public FeedbackFrame(User currentUser) {
        this.currentUser = currentUser;
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("Resource Feedback & Satisfaction Ratings");
        setSize(880, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("Campus Resource Utilization Ratings & Reviews");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Booking ID", "Submitted By", "Resource", "Rating (1-5)", "Comments", "Submitted At"};
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

        JButton btnAdd = new JButton("+ Submit Feedback");
        UIStyle.styleButton(btnAdd);
        btnAdd.addActionListener(e -> showFeedbackDialog());

        JButton btnRefresh = new JButton("Refresh");
        UIStyle.styleButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadData());

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnRefresh);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<Feedback> list = feedbackDAO.getAllFeedback();
        for (Feedback f : list) {
            String stars = "★".repeat(Math.max(1, f.getRating())) + " (" + f.getRating() + "/5)";
            tableModel.addRow(new Object[]{
                f.getFeedbackId(),
                f.getBookingId(),
                f.getUserName(),
                f.getResourceName(),
                stars,
                f.getComments(),
                f.getSubmittedAt()
            });
        }
    }

    private void showFeedbackDialog() {
        List<Booking> myBookings = bookingDAO.getBookingsByUser(currentUser.getUserId());
        if (myBookings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no bookings to review.", "No Bookings", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(this, "Submit Resource Feedback", true);
        dlg.setSize(440, 360);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 12));
        p.setBorder(new EmptyBorder(15, 20, 15, 20));

        JComboBox<Booking> cmbBooking = new JComboBox<>();
        for (Booking b : myBookings) {
            if (!feedbackDAO.hasFeedbackForBooking(b.getBookingId())) {
                cmbBooking.addItem(b);
            }
        }

        if (cmbBooking.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "You have already submitted feedback for all your bookings.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<Integer> cmbRating = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        JTextArea txtComments = new JTextArea(3, 20);
        txtComments.setLineWrap(true);
        txtComments.setWrapStyleWord(true);
        JScrollPane scrollComm = new JScrollPane(txtComments);

        p.add(new JLabel("Select Booking:")); p.add(cmbBooking);
        p.add(new JLabel("Rating (1 to 5 Stars):")); p.add(cmbRating);
        p.add(new JLabel("Review Comments:")); p.add(scrollComm);

        JLabel lblCheck = new JLabel("Database constraint: rating BETWEEN 1 AND 5");
        lblCheck.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblCheck.setForeground(new Color(100, 116, 139));
        p.add(new JLabel("")); p.add(lblCheck);

        JButton btnSubmit = new JButton("Submit Review");
        UIStyle.styleButton(btnSubmit);
        btnSubmit.addActionListener(e -> {
            Booking selBooking = (Booking) cmbBooking.getSelectedItem();
            if (selBooking == null) return;

            Feedback fb = new Feedback();
            fb.setBookingId(selBooking.getBookingId());
            fb.setUserId(currentUser.getUserId());
            fb.setRating((Integer) cmbRating.getSelectedItem());
            fb.setComments(txtComments.getText().trim());

            if (feedbackDAO.addFeedback(fb)) {
                JOptionPane.showMessageDialog(dlg, "Thank you! Your feedback has been recorded.");
                dlg.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dlg, "Feedback could not be submitted.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(p, BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.add(btnSubmit);
        dlg.add(bp, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
