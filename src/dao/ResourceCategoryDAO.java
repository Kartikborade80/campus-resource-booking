package dao;

import database.DatabaseConnection;
import model.ResourceCategory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceCategoryDAO {

    public List<ResourceCategory> getAllCategories() {
        List<ResourceCategory> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, description FROM resource_categories ORDER BY category_id ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ResourceCategory(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.close(rs, ps, conn);
        }
        return list;
    }

    public boolean addCategory(ResourceCategory cat) {
        String sql = "INSERT INTO resource_categories (category_name, description) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, cat.getCategoryName().trim());
            ps.setString(2, cat.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(ps, conn);
        }
    }
}
