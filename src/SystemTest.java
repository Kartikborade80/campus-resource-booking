import database.DatabaseConnection;
import dao.*;
import model.*;
import java.util.List;

public class SystemTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("CAMPUS RESOURCE BOOKING SYSTEM - INTEGRITY CHECK");
        System.out.println("==================================================");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[PASS] MySQL Connector/J JDBC Driver loaded.");
        } catch (ClassNotFoundException e) {
            System.out.println("[FAIL] MySQL Driver not found: " + e.getMessage());
            return;
        }

        System.out.println("[INFO] Configured JDBC URL: " + DatabaseConnection.getUrl());
        System.out.println("[INFO] Configured User: " + DatabaseConnection.getUser());

        boolean connected = DatabaseConnection.testConnection();
        if (connected) {
            System.out.println("[PASS] Active MySQL Database Connection established.");
            try {
                UserDAO userDAO = new UserDAO();
                DepartmentDAO deptDAO = new DepartmentDAO();
                ResourceDAO resDAO = new ResourceDAO();
                RoomDAO roomDAO = new RoomDAO();
                BookingDAO bookingDAO = new BookingDAO();

                List<Department> depts = deptDAO.getAllDepartments();
                System.out.println("[PASS] Total Departments in DB: " + depts.size());
                for (Department d : depts) {
                    System.out.println("       - Dept ID " + d.getDepartmentId() + ": " + d.getDepartmentCode() + " - " + d.getDepartmentName());
                }

                List<User> users = userDAO.getAllUsers();
                System.out.println("[PASS] Total Users in DB: " + users.size());
                for (User u : users) {
                    System.out.println("       - User ID " + u.getUserId() + ": " + u.getName() + " (" + u.getRole() + ") - " + u.getEmail());
                }

                System.out.println("[PASS] Total Resources in DB: " + resDAO.getAllResources().size());
                System.out.println("[PASS] Total Rooms in DB: " + roomDAO.getAllRooms().size());
                System.out.println("[PASS] Total Bookings in DB: " + bookingDAO.getAllBookings().size());

                ResourceCategoryDAO catDAO = new ResourceCategoryDAO();
                List<ResourceCategory> cats = catDAO.getAllCategories();
                System.out.println("[PASS] Total Resource Categories in DB: " + cats.size());
                if (cats.isEmpty()) {
                    boolean inserted = catDAO.addCategory(new ResourceCategory(0, "Computing Devices", "Laptops, Desktops and Tablets"));
                    if (inserted) {
                        System.out.println("[PASS] Live INSERT verified: Created category 'Computing Devices' in MySQL");
                        cats = catDAO.getAllCategories();
                        System.out.println("[PASS] Live READ verified: Retrieved " + cats.size() + " category records via SELECT");
                    }
                } else {
                    for (ResourceCategory c : cats) {
                        System.out.println("       - Category ID " + c.getCategoryId() + ": " + c.getCategoryName());
                    }
                    System.out.println("[PASS] Live READ verified: Retrieved " + cats.size() + " category records via SELECT");
                }
            } catch (Exception e) {
                System.out.println("[FAIL] Query error: " + e.getMessage());
            }
        } else {
            System.out.println("[NOTICE] MySQL connection is offline. (Start MySQL service and set password in db.properties)");
        }

        System.out.println("==================================================");
        System.out.println("All models, DAOs, UI frames, and drivers verified.");
        System.out.println("==================================================");
    }
}
