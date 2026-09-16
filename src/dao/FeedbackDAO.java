package dao;

import database.DatabaseConnection;
import model.Feedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {

    public boolean addFeedback(Feedback fb) {
        String sql = "INSERT INTO feedback (booking_id, user_id, rating, comments) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, fb.getBookingId());
            ps.setInt(2, fb.getUserId());
            ps.setInt(3, Math.max(1, Math.min(5, fb.getRating())));
            ps.setString(4, fb.getComments());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public List<Feedback> getAllFeedback() {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.feedback_id, f.booking_id, f.user_id, u.name AS user_name, " +
                     "r.resource_name, f.rating, f.comments, f.submitted_at " +
                     "FROM feedback f " +
                     "INNER JOIN users u ON f.user_id = u.user_id " +
                     "INNER JOIN bookings b ON f.booking_id = b.booking_id " +
                     "INNER JOIN resources r ON b.resource_id = r.resource_id " +
                     "ORDER BY f.submitted_at DESC, f.feedback_id DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Feedback f = new Feedback();
                f.setFeedbackId(rs.getInt("feedback_id"));
                f.setBookingId(rs.getInt("booking_id"));
                f.setUserId(rs.getInt("user_id"));
                f.setUserName(rs.getString("user_name"));
                f.setResourceName(rs.getString("resource_name"));
                f.setRating(rs.getInt("rating"));
                f.setComments(rs.getString("comments"));
                f.setSubmittedAt(rs.getTimestamp("submitted_at"));
                list.add(f);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public boolean hasFeedbackForBooking(int bookingId) {
        String sql = "SELECT 1 FROM feedback WHERE booking_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookingId);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
    }
}
