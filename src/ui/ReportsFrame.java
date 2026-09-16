package ui;

import dao.ReportDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

public class ReportsFrame extends JFrame {
    private final ReportDAO reportDAO = new ReportDAO();

    private JComboBox<String> cmbReports;
    private JTable reportTable;
    private JLabel lblReportInfo;
    private ChartPanel barChartPanel;
    private ChartPanel pieChartPanel;

    public ReportsFrame() {
        initUI();
        loadSelectedReport();
        loadChartData();
    }

    private void initUI() {
        setTitle("Campus Resource Analytics, Reports & Database Views");
        setSize(1080, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(248, 250, 252));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(248, 250, 252));

        JLabel lblTitle = new JLabel("College Database Analytical Reports");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(30, 41, 59));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        selectorPanel.setBackground(Color.WHITE);
        selectorPanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        selectorPanel.add(new JLabel("Select Report:"));
        String[] reportList = {
            "1. Most Booked Resources",
            "2. Available Resources",
            "3. Department-wise Bookings",
            "4. Daily Booking Report",
            "5. Monthly Booking Report",
            "6. Room Utilization (MySQL View: room_utilization_view)",
            "7. Maintenance History & Cost",
            "8. Cancelled / Rejected Bookings",
            "9. User Booking History",
            "10. Resource Usage Statistics"
        };
        cmbReports = new JComboBox<>(reportList);
        cmbReports.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbReports.setPreferredSize(new Dimension(380, 30));
        cmbReports.addActionListener(e -> loadSelectedReport());
        selectorPanel.add(cmbReports);

        JButton btnExport = new JButton("Export CSV");
        UIStyle.styleButton(btnExport);
        btnExport.addActionListener(e -> exportCurrentReportToCSV());
        selectorPanel.add(btnExport);

        topPanel.add(selectorPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel dataTab = new JPanel(new BorderLayout(8, 8));
        dataTab.setBackground(Color.WHITE);

        lblReportInfo = new JLabel("Displaying report data from MySQL");
        lblReportInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblReportInfo.setBorder(new EmptyBorder(6, 10, 6, 10));
        dataTab.add(lblReportInfo, BorderLayout.NORTH);

        reportTable = new JTable();
        reportTable.setRowHeight(26);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reportTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        reportTable.getTableHeader().setBackground(new Color(241, 245, 249));

        JScrollPane scrollPane = new JScrollPane(reportTable);
        dataTab.add(scrollPane, BorderLayout.CENTER);

        tabbedPane.addTab("Report Data Table", dataTab);

        JPanel chartTab = new JPanel(new GridLayout(1, 2, 15, 15));
        chartTab.setBackground(new Color(248, 250, 252));
        chartTab.setBorder(new EmptyBorder(10, 10, 10, 10));

        barChartPanel = new ChartPanel("Department Booking Volume", ChartPanel.ChartType.BAR, null);
        pieChartPanel = new ChartPanel("Booking Status Distribution", ChartPanel.ChartType.PIE, null);

        chartTab.add(barChartPanel);
        chartTab.add(pieChartPanel);

        tabbedPane.addTab("Analytical Visual Charts (Pure Java Swing 2D)", chartTab);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void loadSelectedReport() {
        int idx = cmbReports.getSelectedIndex();
        DefaultTableModel model = null;
        String info = "";

        switch (idx) {
            case 0:
                model = reportDAO.getMostBookedResources();
                info = "Ranking college resources by overall reservation count and successful completions.";
                break;
            case 1:
                model = reportDAO.getAvailableResourcesReport();
                info = "Live inventory of all items currently marked AVAILABLE for booking.";
                break;
            case 2:
                model = reportDAO.getDepartmentWiseBookings();
                info = "Aggregate distribution of bookings grouped by academic department.";
                break;
            case 3:
                model = reportDAO.getDailyBookingReport();
                info = "Day-by-day reservation timeline and utilization statistics.";
                break;
            case 4:
                model = reportDAO.getMonthlyBookingReport();
                info = "Monthly trend analysis showing unique users and active equipment items.";
                break;
            case 5:
                model = reportDAO.getRoomUtilization();
                info = "Queried directly from MySQL VIEW 'room_utilization_view'.";
                break;
            case 6:
                model = reportDAO.getMaintenanceHistory();
                info = "Equipment servicing log, repair incidents, and total college maintenance expenditure.";
                break;
            case 7:
                model = reportDAO.getCancelledBookings();
                info = "Audit of cancelled or rejected requests for audit and compliance inspection.";
                break;
            case 8:
                model = reportDAO.getUserBookingHistory();
                info = "Total bookings submitted across faculty, staff, and student accounts.";
                break;
            case 9:
                model = reportDAO.getResourceUsageStatistics();
                info = "Category-level breakdown of assets, bookings, and student/faculty ratings.";
                break;
        }

        if (model != null) {
            reportTable.setModel(model);
            lblReportInfo.setText(info + " (Total Rows: " + model.getRowCount() + ")");
        }
    }

    private void loadChartData() {
        new Thread(() -> {
            Map<String, Number> deptData = reportDAO.getDepartmentBookingChartData();
            Map<String, Number> statusData = reportDAO.getBookingStatusDistribution();

            SwingUtilities.invokeLater(() -> {
                barChartPanel.setData("Department Booking Volume", ChartPanel.ChartType.BAR, deptData);
                pieChartPanel.setData("Booking Status Distribution", ChartPanel.ChartType.PIE, statusData);
            });
        }).start();
    }

    private void exportCurrentReportToCSV() {
        DefaultTableModel model = (DefaultTableModel) reportTable.getModel();
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available to export.", "Export Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("report_export.csv"));
        int choice = fc.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                int cols = model.getColumnCount();
                for (int i = 0; i < cols; i++) {
                    pw.print("\"" + model.getColumnName(i) + "\"");
                    if (i < cols - 1) pw.print(",");
                }
                pw.println();

                for (int r = 0; r < model.getRowCount(); r++) {
                    for (int c = 0; c < cols; c++) {
                        Object val = model.getValueAt(r, c);
                        pw.print("\"" + (val != null ? val.toString().replace("\"", "\"\"") : "") + "\"");
                        if (c < cols - 1) pw.print(",");
                    }
                    pw.println();
                }
                JOptionPane.showMessageDialog(this, "Report successfully exported to:\n" + f.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
