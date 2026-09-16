package dao;

import database.DatabaseConnection;
import model.Resource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    public List<Resource> getAllResources() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT r.resource_id, r.category_id, rc.category_name, r.resource_name, r.resource_code, " +
                     "r.description, r.capacity, r.location, r.status, r.purchase_date " +
                     "FROM resources r " +
                     "INNER JOIN resource_categories rc ON r.category_id = rc.category_id " +
                     "ORDER BY r.resource_id ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResource(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public Resource getResourceById(int resourceId) {
        String sql = "SELECT r.resource_id, r.category_id, rc.category_name, r.resource_name, r.resource_code, " +
                     "r.description, r.capacity, r.location, r.status, r.purchase_date " +
                     "FROM resources r " +
                     "INNER JOIN resource_categories rc ON r.category_id = rc.category_id " +
                     "WHERE r.resource_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, resourceId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResource(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return null;
    }

    public boolean addResource(Resource r) {
        String sql = "INSERT INTO resources (category_id, resource_name, resource_code, description, capacity, location, status, purchase_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, r.getCategoryId());
            ps.setString(2, r.getResourceName().trim());
            ps.setString(3, r.getResourceCode().trim().toUpperCase());
            ps.setString(4, r.getDescription());
            ps.setInt(5, r.getCapacity() > 0 ? r.getCapacity() : 1);
            ps.setString(6, r.getLocation().trim());
            ps.setString(7, r.getStatus() != null ? r.getStatus() : "AVAILABLE");
            ps.setDate(8, r.getPurchaseDate());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public boolean updateResource(Resource r) {
        String sql = "UPDATE resources SET category_id = ?, resource_name = ?, resource_code = ?, description = ?, " +
                     "capacity = ?, location = ?, status = ?, purchase_date = ? WHERE resource_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, r.getCategoryId());
            ps.setString(2, r.getResourceName().trim());
            ps.setString(3, r.getResourceCode().trim().toUpperCase());
            ps.setString(4, r.getDescription());
            ps.setInt(5, r.getCapacity() > 0 ? r.getCapacity() : 1);
            ps.setString(6, r.getLocation().trim());
            ps.setString(7, r.getStatus());
            ps.setDate(8, r.getPurchaseDate());
            ps.setInt(9, r.getResourceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public boolean deleteResource(int resourceId) {
        String sql = "DELETE FROM resources WHERE resource_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }

    public List<Resource> searchResources(String keyword, Integer categoryId, String status, String location) {
        List<Resource> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.resource_id, r.category_id, rc.category_name, r.resource_name, r.resource_code, " +
            "r.description, r.capacity, r.location, r.status, r.purchase_date " +
            "FROM resources r " +
            "INNER JOIN resource_categories rc ON r.category_id = rc.category_id WHERE 1=1 "
        );
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (r.resource_name LIKE ? OR r.resource_code LIKE ? OR r.description LIKE ?) ");
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND r.category_id = ? ");
        }
        if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
            sql.append("AND r.status = ? ");
        }
        if (location != null && !location.trim().isEmpty()) {
            sql.append("AND r.location LIKE ? ");
        }
        sql.append("ORDER BY r.resource_id ASC");

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
            if (categoryId != null && categoryId > 0) {
                ps.setInt(idx++, categoryId);
            }
            if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
                ps.setString(idx++, status.trim());
            }
            if (location != null && !location.trim().isEmpty()) {
                ps.setString(idx++, "%" + location.trim() + "%");
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResource(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public List<Resource> getAvailableResources(Integer categoryId) {
        List<Resource> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.resource_id, r.category_id, rc.category_name, r.resource_name, r.resource_code, " +
            "r.description, r.capacity, r.location, r.status, r.purchase_date " +
            "FROM resources r " +
            "INNER JOIN resource_categories rc ON r.category_id = rc.category_id " +
            "WHERE r.status = 'AVAILABLE' "
        );
        if (categoryId != null && categoryId > 0) {
            sql.append("AND r.category_id = ? ");
        }
        sql.append("ORDER BY r.resource_name ASC");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql.toString());
            if (categoryId != null && categoryId > 0) {
                ps.setInt(1, categoryId);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResource(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    private Resource mapResource(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setResourceId(rs.getInt("resource_id"));
        r.setCategoryId(rs.getInt("category_id"));
        r.setCategoryName(rs.getString("category_name"));
        r.setResourceName(rs.getString("resource_name"));
        r.setResourceCode(rs.getString("resource_code"));
        r.setDescription(rs.getString("description"));
        r.setCapacity(rs.getInt("capacity"));
        r.setLocation(rs.getString("location"));
        r.setStatus(rs.getString("status"));
        r.setPurchaseDate(rs.getDate("purchase_date"));
        return r;
    }
}
