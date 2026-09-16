package dao;

import database.DatabaseConnection;
import model.TimeSlot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotDAO {

    public List<TimeSlot> getAllActiveSlots() {
        List<TimeSlot> list = new ArrayList<>();
        String sql = "SELECT slot_id, slot_name, start_time, end_time, status FROM time_slots WHERE status = 'Active' ORDER BY start_time ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new TimeSlot(
                    rs.getInt("slot_id"),
                    rs.getString("slot_name"),
                    rs.getTime("start_time"),
                    rs.getTime("end_time"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }
}
