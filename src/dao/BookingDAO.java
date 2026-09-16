package dao;

import database.DatabaseConnection;
import model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingDAO {

    public Map<String, Object> bookResourceViaProcedure(int userId, int resourceId, Integer roomId, int slotId, Date bookingDate, String purpose) {
        Map<String, Object> result = new HashMap<>();
        String callSql = "{CALL book_resource(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DatabaseConnection.getConnection();
            cs = conn.prepareCall(callSql);
            cs.setInt(1, userId);
            cs.setInt(2, resourceId);
            if (roomId != null && roomId > 0) {
                cs.setInt(3, roomId);
            } else {
                cs.setNull(3, Types.INTEGER);
            }
            cs.setInt(4, slotId);
            cs.setDate(5, bookingDate);
            cs.setString(6, purpose.trim());

            cs.registerOutParameter(7, Types.INTEGER);
            cs.registerOutParameter(8, Types.VARCHAR);
            cs.registerOutParameter(9, Types.INTEGER);

            cs.execute();

            int statusCode = cs.getInt(7);
            String message = cs.getString(8);
            int bookingId = cs.getInt(9);

            result.put("success", statusCode == 0);
            result.put("statusCode", statusCode);
            result.put("message", message);
            result.put("bookingId", bookingId);
        } catch (SQLException e) {
            result.put("success", false);
            result.put("statusCode", 500);
            result.put("message", "Database error: " + e.getMessage());
            result.put("bookingId", 0);
        } finally {
            DatabaseConnection.close(cs, conn);
        }
        return result;
    }

    public Map<String, Object> cancelBookingViaProcedure(int bookingId, int userId) {
        Map<String, Object> result = new HashMap<>();
        String callSql = "{CALL cancel_booking(?, ?, ?, ?)}";
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = DatabaseConnection.getConnection();
            cs = conn.prepareCall(callSql);
            cs.setInt(1, bookingId);
            cs.setInt(2, userId);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(4, Types.VARCHAR);

            cs.execute();

            int statusCode = cs.getInt(3);
            String message = cs.getString(4);

            result.put("success", statusCode == 0);
            result.put("statusCode", statusCode);
            result.put("message", message);
        } catch (SQLException e) {
            result.put("success", false);
            result.put("statusCode", 500);
            result.put("message", "Database error: " + e.getMessage());
        } finally {
            DatabaseConnection.close(cs, conn);
        }
        return result;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.user_id, u.name AS user_name, u.email AS user_email, d.department_name, " +
                     "b.resource_id, r.resource_name, rc.category_name AS resource_category, " +
                     "b.room_id, rm.room_number, rm.building_name, " +
                     "b.slot_id, ts.slot_name, b.booking_date, b.purpose, b.booking_status, " +
                     "b.approved_by, appr.name AS approved_by_name, b.created_at " +
                     "FROM bookings b " +
                     "INNER JOIN users u ON b.user_id = u.user_id " +
                     "INNER JOIN departments d ON u.department_id = d.department_id " +
                     "INNER JOIN resources r ON b.resource_id = r.resource_id " +
                     "INNER JOIN resource_categories rc ON r.category_id = rc.category_id " +
                     "LEFT JOIN rooms rm ON b.room_id = rm.room_id " +
                     "INNER JOIN time_slots ts ON b.slot_id = ts.slot_id " +
                     "LEFT JOIN users appr ON b.approved_by = appr.user_id " +
                     "ORDER BY b.booking_date DESC, b.booking_id DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public List<Booking> getBookingsByUser(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.booking_id, b.user_id, u.name AS user_name, u.email AS user_email, d.department_name, " +
                     "b.resource_id, r.resource_name, rc.category_name AS resource_category, " +
                     "b.room_id, rm.room_number, rm.building_name, " +
                     "b.slot_id, ts.slot_name, b.booking_date, b.purpose, b.booking_status, " +
                     "b.approved_by, appr.name AS approved_by_name, b.created_at " +
                     "FROM bookings b " +
                     "INNER JOIN users u ON b.user_id = u.user_id " +
                     "INNER JOIN departments d ON u.department_id = d.department_id " +
                     "INNER JOIN resources r ON b.resource_id = r.resource_id " +
                     "INNER JOIN resource_categories rc ON r.category_id = rc.category_id " +
                     "LEFT JOIN rooms rm ON b.room_id = rm.room_id " +
                     "INNER JOIN time_slots ts ON b.slot_id = ts.slot_id " +
                     "LEFT JOIN users appr ON b.approved_by = appr.user_id " +
                     "WHERE b.user_id = ? " +
                     "ORDER BY b.booking_date DESC, b.booking_id DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public boolean updateBookingStatus(int bookingId, String newStatus, Integer approvedBy) {
        String sql = "UPDATE bookings SET booking_status = ?, approved_by = ? WHERE booking_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            if (approvedBy != null && approvedBy > 0) {
                ps.setInt(2, approvedBy);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public List<Booking> searchBookings(String keyword, String status, Date bookingDate, Integer departmentId) {
        List<Booking> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT b.booking_id, b.user_id, u.name AS user_name, u.email AS user_email, d.department_name, " +
            "b.resource_id, r.resource_name, rc.category_name AS resource_category, " +
            "b.room_id, rm.room_number, rm.building_name, " +
            "b.slot_id, ts.slot_name, b.booking_date, b.purpose, b.booking_status, " +
            "b.approved_by, appr.name AS approved_by_name, b.created_at " +
            "FROM bookings b " +
            "INNER JOIN users u ON b.user_id = u.user_id " +
            "INNER JOIN departments d ON u.department_id = d.department_id " +
            "INNER JOIN resources r ON b.resource_id = r.resource_id " +
            "INNER JOIN resource_categories rc ON r.category_id = rc.category_id " +
            "LEFT JOIN rooms rm ON b.room_id = rm.room_id " +
            "INNER JOIN time_slots ts ON b.slot_id = ts.slot_id " +
            "LEFT JOIN users appr ON b.approved_by = appr.user_id WHERE 1=1 "
        );
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (u.name LIKE ? OR r.resource_name LIKE ? OR b.purpose LIKE ?) ");
        }
        if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
            sql.append("AND b.booking_status = ? ");
        }
        if (bookingDate != null) {
            sql.append("AND b.booking_date = ? ");
        }
        if (departmentId != null && departmentId > 0) {
            sql.append("AND d.department_id = ? ");
        }
        sql.append("ORDER BY b.booking_date DESC, b.booking_id DESC");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pat = "%" + keyword.trim() + "%";
                ps.setString(idx++, pat);
                ps.setString(idx++, pat);
                ps.setString(idx++, pat);
            }
            if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
                ps.setString(idx++, status.trim());
            }
            if (bookingDate != null) {
                ps.setDate(idx++, bookingDate);
            }
            if (departmentId != null && departmentId > 0) {
                ps.setInt(idx++, departmentId);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public List<Booking> getBookingDetailsView() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT booking_id, user_name, user_email, user_role, department_name, " +
                     "resource_name, resource_code, resource_category, room_number, building_name, " +
                     "time_slot, start_time, end_time, booking_date, purpose, booking_status, " +
                     "approved_by_name, created_at " +
                     "FROM booking_details_view " +
                     "ORDER BY booking_date DESC, booking_id DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Booking b = new Booking();
                b.setBookingId(rs.getInt("booking_id"));
                b.setUserName(rs.getString("user_name"));
                b.setUserEmail(rs.getString("user_email"));
                b.setDepartmentName(rs.getString("department_name"));
                b.setResourceName(rs.getString("resource_name"));
                b.setResourceCategory(rs.getString("resource_category"));
                b.setRoomNumber(rs.getString("room_number"));
                b.setBuildingName(rs.getString("building_name"));
                b.setSlotName(rs.getString("time_slot"));
                b.setBookingDate(rs.getDate("booking_date"));
                b.setPurpose(rs.getString("purpose"));
                b.setBookingStatus(rs.getString("booking_status"));
                b.setApprovedByName(rs.getString("approved_by_name"));
                b.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setUserName(rs.getString("user_name"));
        b.setUserEmail(rs.getString("user_email"));
        b.setDepartmentName(rs.getString("department_name"));
        b.setResourceId(rs.getInt("resource_id"));
        b.setResourceName(rs.getString("resource_name"));
        b.setResourceCategory(rs.getString("resource_category"));
        int rmId = rs.getInt("room_id");
        if (!rs.wasNull()) {
            b.setRoomId(rmId);
            b.setRoomNumber(rs.getString("room_number"));
            b.setBuildingName(rs.getString("building_name"));
        } else {
            b.setRoomNumber("Portable Asset");
            b.setBuildingName("Campus Wide");
        }
        b.setSlotId(rs.getInt("slot_id"));
        b.setSlotName(rs.getString("slot_name"));
        b.setBookingDate(rs.getDate("booking_date"));
        b.setPurpose(rs.getString("purpose"));
        b.setBookingStatus(rs.getString("booking_status"));
        int apprId = rs.getInt("approved_by");
        if (!rs.wasNull()) {
            b.setApprovedBy(apprId);
            b.setApprovedByName(rs.getString("approved_by_name"));
        } else {
            b.setApprovedByName("Pending Approval");
        }
        b.setCreatedAt(rs.getTimestamp("created_at"));
        return b;
    }
}
