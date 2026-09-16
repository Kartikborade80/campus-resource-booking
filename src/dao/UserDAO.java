package dao;

import database.DatabaseConnection;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private String lastError = "";

    public String getLastError() {
        return lastError;
    }

    public User authenticate(String email, String password) {
        String sql = "SELECT u.user_id, u.name, u.email, u.password, u.phone, u.role, u.department_id, d.department_name, u.status, u.created_at " +
                     "FROM users u " +
                     "INNER JOIN departments d ON u.department_id = d.department_id " +
                     "WHERE u.email = ? AND u.password = ? AND u.status = 'Active'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim());
            ps.setString(2, password.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setPhone(rs.getString("phone"));
                u.setRole(rs.getString("role"));
                u.setDepartmentId(rs.getInt("department_id"));
                u.setDepartmentName(rs.getString("department_name"));
                u.setStatus(rs.getString("status"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim());
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.name, u.email, u.password, u.phone, u.role, u.department_id, d.department_name, u.status, u.created_at " +
                     "FROM users u " +
                     "INNER JOIN departments d ON u.department_id = d.department_id " +
                     "ORDER BY u.user_id ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setPhone(rs.getString("phone"));
                u.setRole(rs.getString("role"));
                u.setDepartmentId(rs.getInt("department_id"));
                u.setDepartmentName(rs.getString("department_name"));
                u.setStatus(rs.getString("status"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users (name, email, password, phone, role, department_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole());
            ps.setInt(6, user.getDepartmentId());
            ps.setString(7, user.getStatus() != null ? user.getStatus() : "Active");
            lastError = "";
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastError = e.getMessage();
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, password = ?, phone = ?, role = ?, department_id = ?, status = ? WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole());
            ps.setInt(6, user.getDepartmentId());
            ps.setString(7, user.getStatus());
            ps.setInt(8, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public List<User> searchUsers(String keyword, String role, Integer departmentId) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT u.user_id, u.name, u.email, u.password, u.phone, u.role, u.department_id, d.department_name, u.status, u.created_at " +
            "FROM users u INNER JOIN departments d ON u.department_id = d.department_id WHERE 1=1 "
        );
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (u.name LIKE ? OR u.email LIKE ? OR u.phone LIKE ?) ");
        }
        if (role != null && !role.trim().isEmpty() && !role.equals("ALL")) {
            sql.append("AND u.role = ? ");
        }
        if (departmentId != null && departmentId > 0) {
            sql.append("AND u.department_id = ? ");
        }
        sql.append("ORDER BY u.user_id ASC");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql.toString());
            int idx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim() + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
            }
            if (role != null && !role.trim().isEmpty() && !role.equals("ALL")) {
                ps.setString(idx++, role.trim());
            }
            if (departmentId != null && departmentId > 0) {
                ps.setInt(idx++, departmentId);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setPhone(rs.getString("phone"));
                u.setRole(rs.getString("role"));
                u.setDepartmentId(rs.getInt("department_id"));
                u.setDepartmentName(rs.getString("department_name"));
                u.setStatus(rs.getString("status"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }
}
