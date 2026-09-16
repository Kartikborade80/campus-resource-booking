package dao;

import database.DatabaseConnection;
import model.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT room_id, room_number, building_name, floor_number, capacity, room_type, facilities, status " +
                     "FROM rooms ORDER BY room_id ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRoom(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public Room getRoomById(int roomId) {
        String sql = "SELECT room_id, room_number, building_name, floor_number, capacity, room_type, facilities, status " +
                     "FROM rooms WHERE room_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, roomId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRoom(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return null;
    }

    public boolean addRoom(Room r) {
        String sql = "INSERT INTO rooms (room_number, building_name, floor_number, capacity, room_type, facilities, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, r.getRoomNumber().trim());
            ps.setString(2, r.getBuildingName().trim());
            ps.setInt(3, r.getFloorNumber());
            ps.setInt(4, r.getCapacity());
            ps.setString(5, r.getRoomType());
            ps.setString(6, r.getFacilities());
            ps.setString(7, r.getStatus() != null ? r.getStatus() : "AVAILABLE");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public boolean updateRoom(Room r) {
        String sql = "UPDATE rooms SET room_number = ?, building_name = ?, floor_number = ?, capacity = ?, " +
                     "room_type = ?, facilities = ?, status = ? WHERE room_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, r.getRoomNumber().trim());
            ps.setString(2, r.getBuildingName().trim());
            ps.setInt(3, r.getFloorNumber());
            ps.setInt(4, r.getCapacity());
            ps.setString(5, r.getRoomType());
            ps.setString(6, r.getFacilities());
            ps.setString(7, r.getStatus());
            ps.setInt(8, r.getRoomId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public List<Room> searchRooms(String keyword, String roomType, Integer minCapacity, String status) {
        List<Room> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT room_id, room_number, building_name, floor_number, capacity, room_type, facilities, status FROM rooms WHERE 1=1 "
        );
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (room_number LIKE ? OR building_name LIKE ? OR facilities LIKE ?) ");
        }
        if (roomType != null && !roomType.trim().isEmpty() && !roomType.equals("ALL")) {
            sql.append("AND room_type = ? ");
        }
        if (minCapacity != null && minCapacity > 0) {
            sql.append("AND capacity >= ? ");
        }
        if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
            sql.append("AND status = ? ");
        }
        sql.append("ORDER BY room_id ASC");

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
            if (roomType != null && !roomType.trim().isEmpty() && !roomType.equals("ALL")) {
                ps.setString(idx++, roomType.trim());
            }
            if (minCapacity != null && minCapacity > 0) {
                ps.setInt(idx++, minCapacity);
            }
            if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
                ps.setString(idx++, status.trim());
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRoom(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public List<Room> getAvailableRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT room_id, room_number, building_name, floor_number, capacity, room_type, facilities, status " +
                     "FROM rooms WHERE status = 'AVAILABLE' ORDER BY building_name, room_number ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRoom(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    private Room mapRoom(ResultSet rs) throws SQLException {
        return new Room(
            rs.getInt("room_id"),
            rs.getString("room_number"),
            rs.getString("building_name"),
            rs.getInt("floor_number"),
            rs.getInt("capacity"),
            rs.getString("room_type"),
            rs.getString("facilities"),
            rs.getString("status")
        );
    }
}
