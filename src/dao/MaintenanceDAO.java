package dao;

import database.DatabaseConnection;
import model.Maintenance;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceDAO {

    public List<Maintenance> getAllMaintenance() {
        List<Maintenance> list = new ArrayList<>();
        String sql = "SELECT m.maintenance_id, m.resource_id, r.resource_name, m.room_id, rm.room_number, " +
                     "m.reported_by, u.name AS reported_by_name, m.issue_title, m.issue_description, " +
                     "m.maintenance_date, m.completion_date, m.maintenance_status, m.cost " +
                     "FROM maintenance m " +
                     "LEFT JOIN resources r ON m.resource_id = r.resource_id " +
                     "LEFT JOIN rooms rm ON m.room_id = rm.room_id " +
                     "INNER JOIN users u ON m.reported_by = u.user_id " +
                     "ORDER BY m.maintenance_date DESC, m.maintenance_id DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapMaintenance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public boolean reportIssue(Maintenance m) {
        String sql = "INSERT INTO maintenance (resource_id, room_id, reported_by, issue_title, issue_description, maintenance_date, maintenance_status, cost) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            if (m.getResourceId() != null && m.getResourceId() > 0) {
                ps.setInt(1, m.getResourceId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            if (m.getRoomId() != null && m.getRoomId() > 0) {
                ps.setInt(2, m.getRoomId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, m.getReportedBy());
            ps.setString(4, m.getIssueTitle().trim());
            ps.setString(5, m.getIssueDescription().trim());
            ps.setDate(6, m.getMaintenanceDate());
            ps.setString(7, m.getMaintenanceStatus() != null ? m.getMaintenanceStatus() : "Reported");
            ps.setBigDecimal(8, m.getCost() != null ? m.getCost() : BigDecimal.ZERO);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public boolean updateMaintenanceStatus(int maintenanceId, String status) {
        String sql = "UPDATE maintenance SET maintenance_status = ? WHERE maintenance_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, maintenanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public boolean completeMaintenance(int maintenanceId, BigDecimal cost, Date completionDate) {
        String sql = "UPDATE maintenance SET maintenance_status = 'Completed', cost = ?, completion_date = ? WHERE maintenance_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBigDecimal(1, cost);
            ps.setDate(2, completionDate);
            ps.setInt(3, maintenanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    private Maintenance mapMaintenance(ResultSet rs) throws SQLException {
        Maintenance m = new Maintenance();
        m.setMaintenanceId(rs.getInt("maintenance_id"));
        int resId = rs.getInt("resource_id");
        if (!rs.wasNull()) {
            m.setResourceId(resId);
            m.setResourceName(rs.getString("resource_name"));
        } else {
            m.setResourceName("Room Asset");
        }
        int rmId = rs.getInt("room_id");
        if (!rs.wasNull()) {
            m.setRoomId(rmId);
            m.setRoomNumber(rs.getString("room_number"));
        } else {
            m.setRoomNumber("Portable Device");
        }
        m.setReportedBy(rs.getInt("reported_by"));
        m.setReportedByName(rs.getString("reported_by_name"));
        m.setIssueTitle(rs.getString("issue_title"));
        m.setIssueDescription(rs.getString("issue_description"));
        m.setMaintenanceDate(rs.getDate("maintenance_date"));
        m.setCompletionDate(rs.getDate("completion_date"));
        m.setMaintenanceStatus(rs.getString("maintenance_status"));
        m.setCost(rs.getBigDecimal("cost"));
        return m;
    }
}
