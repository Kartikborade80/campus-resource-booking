package dao;

import database.DatabaseConnection;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class ReportDAO {

    public DefaultTableModel getMostBookedResources() {
        String sql = "SELECT r.resource_id, r.resource_name, r.resource_code, rc.category_name, " +
                     "COUNT(b.booking_id) AS booking_count, " +
                     "COUNT(CASE WHEN b.booking_status = 'COMPLETED' THEN 1 END) AS completed_count " +
                     "FROM resources r " +
                     "INNER JOIN resource_categories rc ON r.category_id = rc.category_id " +
                     "LEFT JOIN bookings b ON r.resource_id = b.resource_id " +
                     "GROUP BY r.resource_id, r.resource_name, r.resource_code, rc.category_name " +
                     "ORDER BY booking_count DESC";
        return executeQueryToModel(sql);
    }

    public DefaultTableModel getAvailableResourcesReport() {
        String sql = "SELECT r.resource_id, r.resource_name, r.resource_code, rc.category_name, r.capacity, r.location, r.status " +
                     "FROM resources r " +
                     "INNER JOIN resource_categories rc ON r.category_id = rc.category_id " +
                     "WHERE r.status = 'AVAILABLE' " +
                     "ORDER BY rc.category_name, r.resource_name";
        return executeQueryToModel(sql);
    }

    public DefaultTableModel getDepartmentWiseBookings() {
        String sql = "SELECT d.department_id, d.department_name, d.department_code, " +
                     "COUNT(b.booking_id) AS total_bookings, " +
                     "COUNT(CASE WHEN b.booking_status = 'APPROVED' THEN 1 END) AS approved, " +
                     "COUNT(CASE WHEN b.booking_status = 'PENDING' THEN 1 END) AS pending, " +
                     "COUNT(CASE WHEN b.booking_status = 'COMPLETED' THEN 1 END) AS completed, " +
                     "COUNT(CASE WHEN b.booking_status = 'CANCELLED' THEN 1 END) AS cancelled " +
                     "FROM departments d " +
                     "LEFT JOIN users u ON d.department_id = u.department_id " +
                     "LEFT JOIN bookings b ON u.user_id = b.user_id " +
                     "GROUP BY d.department_id, d.department_name, d.department_code " +
                     "ORDER BY total_bookings DESC";
        return executeQueryToModel(sql);
    }

    public DefaultTableModel getDailyBookingReport() {
        String sql = "SELECT b.booking_date, COUNT(b.booking_id) AS total_bookings, " +
                     "COUNT(CASE WHEN b.booking_status = 'APPROVED' THEN 1 END) AS approved, " +
                     "COUNT(CASE WHEN b.booking_status = 'PENDING' THEN 1 END) AS pending, " +
                     "COUNT(CASE WHEN b.booking_status = 'COMPLETED' THEN 1 END) AS completed " +
                     "FROM bookings b " +
                     "GROUP BY b.booking_date " +
                     "ORDER BY b.booking_date DESC";
        return executeQueryToModel(sql);
    }

    public DefaultTableModel getMonthlyBookingReport() {
        String sql = "SELECT DATE_FORMAT(b.booking_date, '%Y-%m') AS booking_month, " +
                     "COUNT(b.booking_id) AS total_bookings, " +
                     "COUNT(DISTINCT b.user_id) AS unique_users, " +
                     "COUNT(DISTINCT b.resource_id) AS unique_resources " +
                     "FROM bookings b " +
                     "GROUP BY DATE_FORMAT(b.booking_date, '%Y-%m') " +
                     "ORDER BY booking_month DESC";
        return executeQueryToModel(sql);
    }

    public DefaultTableModel getRoomUtilization() {
        String sql = "SELECT room_id, room_number, building_name, room_type, capacity, " +
                     "total_bookings, approved_bookings, completed_bookings, cancelled_bookings " +
                     "FROM room_utilization_view " +
                     "ORDER BY total_bookings DESC";
        return executeQueryToModel(sql);
    }

    public DefaultTableModel getMaintenanceHistory() {
        String sql = "SELECT m.maintenance_id, COALESCE(r.resource_name, 'Room') AS target_resource, " +
                     "COALESCE(rm.room_number, 'Field Asset') AS venue, " +
                     "m.issue_title, m.maintenance_date, m.completion_date, m.maintenance_status, m.cost, " +
                     "u.name AS reported_by_name " +
                     "FROM maintenance m " +
                     "LEFT JOIN resources r ON m.resource_id = r.resource_id " +
                     "LEFT JOIN rooms rm ON m.room_id = rm.room_id " +
                     "INNER JOIN users u ON m.reported_by = u.user_id " +
                     "ORDER BY m.maintenance_date DESC";
        return executeQueryToModel(sql);
    }

    public DefaultTableModel getCancelledBookings() {
        String sql = "SELECT b.booking_id, u.name AS user_name, r.resource_name, b.booking_date, " +
                     "ts.slot_name, b.purpose, b.booking_status, b.created_at " +
                     "FROM bookings b " +
                     "INNER JOIN users u ON b.user_id = u.user_id " +
                     "INNER JOIN resources r ON b.resource_id = r.resource_id " +
                     "INNER JOIN time_slots ts ON b.slot_id = ts.slot_id " +
                     "WHERE b.booking_status IN ('CANCELLED', 'REJECTED') " +
                     "ORDER BY b.booking_date DESC";
        return executeQueryToModel(sql);
    }

    public DefaultTableModel getUserBookingHistory() {
        String sql = "SELECT u.user_id, u.name, u.email, u.role, d.department_name, " +
                     "COUNT(b.booking_id) AS total_bookings, " +
                     "COUNT(CASE WHEN b.booking_status = 'APPROVED' THEN 1 END) AS approved, " +
                     "COUNT(CASE WHEN b.booking_status = 'COMPLETED' THEN 1 END) AS completed " +
                     "FROM users u " +
                     "INNER JOIN departments d ON u.department_id = d.department_id " +
                     "LEFT JOIN bookings b ON u.user_id = b.user_id " +
                     "GROUP BY u.user_id, u.name, u.email, u.role, d.department_name " +
                     "ORDER BY total_bookings DESC";
        return executeQueryToModel(sql);
    }

    public DefaultTableModel getResourceUsageStatistics() {
        String sql = "SELECT rc.category_name, " +
                     "COUNT(DISTINCT r.resource_id) AS total_resources, " +
                     "COUNT(b.booking_id) AS total_bookings, " +
                     "ROUND(AVG(f.rating), 2) AS avg_rating " +
                     "FROM resource_categories rc " +
                     "LEFT JOIN resources r ON rc.category_id = r.category_id " +
                     "LEFT JOIN bookings b ON r.resource_id = b.resource_id " +
                     "LEFT JOIN feedback f ON b.booking_id = f.booking_id " +
                     "GROUP BY rc.category_id, rc.category_name " +
                     "ORDER BY total_bookings DESC";
        return executeQueryToModel(sql);
    }

    public Map<String, Number> getDepartmentBookingChartData() {
        Map<String, Number> data = new LinkedHashMap<>();
        String sql = "SELECT d.department_code, COUNT(b.booking_id) AS tally " +
                     "FROM departments d " +
                     "LEFT JOIN users u ON d.department_id = u.department_id " +
                     "LEFT JOIN bookings b ON u.user_id = b.user_id " +
                     "GROUP BY d.department_id, d.department_code " +
                     "ORDER BY tally DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("department_code"), rs.getInt("tally"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return data;
    }

    public Map<String, Number> getBookingStatusDistribution() {
        Map<String, Number> data = new LinkedHashMap<>();
        String sql = "SELECT booking_status, COUNT(*) AS tally FROM bookings GROUP BY booking_status";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("booking_status"), rs.getInt("tally"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return data;
    }

    public Map<String, Integer> getDashboardSummaryCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM users) AS total_users, " +
                     "(SELECT COUNT(*) FROM resources) AS total_resources, " +
                     "(SELECT COUNT(*) FROM resources WHERE status = 'AVAILABLE') AS available_resources, " +
                     "(SELECT COUNT(*) FROM rooms) AS total_rooms, " +
                     "(SELECT COUNT(*) FROM bookings WHERE booking_date = CURDATE()) AS today_bookings, " +
                     "(SELECT COUNT(*) FROM bookings WHERE booking_status = 'PENDING') AS pending_bookings, " +
                     "(SELECT COUNT(*) FROM resources WHERE status = 'MAINTENANCE') AS maintenance_resources";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                counts.put("total_users", rs.getInt("total_users"));
                counts.put("total_resources", rs.getInt("total_resources"));
                counts.put("available_resources", rs.getInt("available_resources"));
                counts.put("total_rooms", rs.getInt("total_rooms"));
                counts.put("today_bookings", rs.getInt("today_bookings"));
                counts.put("pending_bookings", rs.getInt("pending_bookings"));
                counts.put("maintenance_resources", rs.getInt("maintenance_resources"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return counts;
    }

    private DefaultTableModel executeQueryToModel(String sql) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            Vector<String> colNames = new Vector<>();
            for (int i = 1; i <= colCount; i++) {
                String label = meta.getColumnLabel(i);
                label = label.replace("_", " ").toUpperCase();
                colNames.add(label);
            }
            model.setColumnIdentifiers(colNames);

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getObject(i));
                }
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return model;
    }
}
