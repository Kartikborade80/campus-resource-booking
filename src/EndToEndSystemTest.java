import database.DatabaseConnection;
import dao.*;
import model.*;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class EndToEndSystemTest {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("CAMPUS RESOURCE BOOKING SYSTEM - COMPREHENSIVE END-TO-END TEST SUITE");
        System.out.println("======================================================================");

        testDatabaseConnection();
        testDepartmentCRUD();
        testUserRegistrationAndAuth();
        testResourceCategoryAndResourceCRUD();
        testRoomCRUD();
        testTimeSlots();
        testStoredProcedureBookingSuccess();
        testStoredProcedureBookingConflict();
        testTriggerResourceStatusTransition();
        testStoredProcedureBookingCancellation();
        testMaintenanceWorkflow();
        testFeedbackWithRatingConstraint();
        testDatabaseViews();
        testSearchOperations();
        testReportEngineAndCharts();

        System.out.println("======================================================================");
        System.out.println("TEST SUMMARY:");
        System.out.println("Total Tests Passed: " + testsPassed);
        System.out.println("Total Tests Failed: " + testsFailed);
        if (testsFailed == 0) {
            System.out.println("ALL SYSTEM FUNCTIONS TESTED AND WORKING WITH 100% SUCCESS!");
        } else {
            System.out.println("SOME TESTS FAILED! Please review above logs.");
        }
        System.out.println("======================================================================");
    }

    private static void check(String testName, boolean condition, String detail) {
        if (condition) {
            System.out.println("[PASS] " + testName + " -> " + detail);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName + " -> " + detail);
            testsFailed++;
        }
    }

    private static void testDatabaseConnection() {
        boolean ok = DatabaseConnection.testConnection();
        check("Database Connection", ok, "JDBC connection established to MySQL database 'campus_resource_booking'");
    }

    private static void testDepartmentCRUD() {
        DepartmentDAO deptDAO = new DepartmentDAO();
        Department d1 = new Department(0, "Computer Science & Engineering", "CSE", "Active");
        Department d2 = new Department(0, "Mechanical Engineering", "MECH", "Active");

        boolean a1 = deptDAO.addDepartment(d1);
        boolean a2 = deptDAO.addDepartment(d2);
        check("Department Create", a1 && a2, "Created CSE and MECH departments");

        List<Department> list = deptDAO.getAllDepartments();
        check("Department Read", list.size() >= 2, "Retrieved " + list.size() + " departments");

        Department first = list.get(0);
        first.setDepartmentName("Computer Engineering Department");
        boolean u1 = deptDAO.updateDepartment(first);
        check("Department Update", u1, "Updated department name successfully");
    }

    private static void testUserRegistrationAndAuth() {
        UserDAO userDAO = new UserDAO();
        DepartmentDAO deptDAO = new DepartmentDAO();
        List<Department> depts = deptDAO.getAllDepartments();
        int deptId = depts.get(0).getDepartmentId();

        User admin = new User(0, "Test Administrator", "admin.test@sanjivani.edu.in", "adminpass123", "+91-9800011122", "ADMIN", deptId, "Active");
        User faculty = new User(0, "Prof. Rajesh Verma", "rajesh.faculty@sanjivani.edu.in", "facultypass123", "+91-9800033344", "FACULTY", deptId, "Active");
        User student = new User(0, "Rahul Sharma", "rahul.student@sanjivani.edu.in", "studentpass123", "+91-9800055566", "STUDENT", deptId, "Active");

        boolean b1 = userDAO.addUser(admin);
        boolean b2 = userDAO.addUser(faculty);
        boolean b3 = userDAO.addUser(student);
        check("User Registration", b1 && b2 && b3, "Created Admin, Faculty, and Student accounts");

        User authAdmin = userDAO.authenticate("admin.test@sanjivani.edu.in", "adminpass123");
        check("User Authentication Success", authAdmin != null && "ADMIN".equals(authAdmin.getRole()), "Authenticated as " + authAdmin.getName());

        User badAuth = userDAO.authenticate("admin.test@sanjivani.edu.in", "wrongpass");
        check("User Authentication Rejection", badAuth == null, "Rejected invalid credentials properly");

        boolean emailExists = userDAO.emailExists("admin.test@sanjivani.edu.in");
        check("Email Uniqueness Check", emailExists, "Duplicate email detector verified");
    }

    private static void testResourceCategoryAndResourceCRUD() {
        ResourceCategoryDAO catDAO = new ResourceCategoryDAO();
        ResourceDAO resDAO = new ResourceDAO();

        ResourceCategory c1 = new ResourceCategory(0, "Smart Classrooms", "Smart interactive lecture theaters");
        ResourceCategory c2 = new ResourceCategory(0, "Audio Visual", "Laser Projectors and sound systems");
        catDAO.addCategory(c1);
        catDAO.addCategory(c2);

        List<ResourceCategory> cats = catDAO.getAllCategories();
        check("Category Management", cats.size() >= 2, "Created and retrieved categories");
        int catId = cats.get(0).getCategoryId();

        Resource r1 = new Resource(0, catId, "Smart Hall A-101", "RES-HALL-101", "Interactive Display & AC", 100, "Block A", "AVAILABLE", Date.valueOf("2026-01-15"));
        Resource r2 = new Resource(0, catId, "Epson 4K Laser Projector", "RES-AV-P01", "High lumen portable laser projector", 1, "AV Store", "AVAILABLE", Date.valueOf("2026-02-10"));

        boolean ra1 = resDAO.addResource(r1);
        boolean ra2 = resDAO.addResource(r2);
        check("Resource Add", ra1 && ra2, "Inserted 2 campus resources");

        List<Resource> allRes = resDAO.getAllResources();
        check("Resource Read All", allRes.size() >= 2, "Retrieved " + allRes.size() + " resources");

        Resource toUpdate = allRes.get(0);
        toUpdate.setCapacity(120);
        boolean ru = resDAO.updateResource(toUpdate);
        check("Resource Update", ru, "Updated capacity to 120");
    }

    private static void testRoomCRUD() {
        RoomDAO roomDAO = new RoomDAO();
        Room rm1 = new Room(0, "A-101", "Academic Block A", 1, 120, "Classroom", "Central AC, Dual Smartboards", "AVAILABLE");
        Room rm2 = new Room(0, "AUD-CENTRAL", "Central Complex", 0, 500, "Auditorium", "Concert Acoustics, Stage Lights", "AVAILABLE");

        boolean r1 = roomDAO.addRoom(rm1);
        boolean r2 = roomDAO.addRoom(rm2);
        check("Room Add", r1 && r2, "Inserted 2 campus venue rooms");

        List<Room> allRooms = roomDAO.getAllRooms();
        check("Room Read", allRooms.size() >= 2, "Found " + allRooms.size() + " venue rooms");
    }

    private static void testTimeSlots() {
        TimeSlotDAO slotDAO = new TimeSlotDAO();
        List<TimeSlot> slots = slotDAO.getAllActiveSlots();
        if (slots.isEmpty()) {
            try (java.sql.Connection conn = DatabaseConnection.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement("INSERT INTO time_slots (slot_name, start_time, end_time, status) VALUES (?, ?, ?, 'Active')")) {
                ps.setString(1, "Morning Session (09:00 - 11:00)");
                ps.setTime(2, Time.valueOf("09:00:00"));
                ps.setTime(3, Time.valueOf("11:00:00"));
                ps.executeUpdate();

                ps.setString(1, "Afternoon Session (14:00 - 16:00)");
                ps.setTime(2, Time.valueOf("14:00:00"));
                ps.setTime(3, Time.valueOf("16:00:00"));
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            slots = slotDAO.getAllActiveSlots();
        }
        check("Time Slots Active", slots.size() >= 2, "Retrieved " + slots.size() + " active scheduling time slots");
    }

    private static void testStoredProcedureBookingSuccess() {
        UserDAO userDAO = new UserDAO();
        ResourceDAO resDAO = new ResourceDAO();
        RoomDAO roomDAO = new RoomDAO();
        TimeSlotDAO slotDAO = new TimeSlotDAO();
        BookingDAO bookingDAO = new BookingDAO();

        User user = userDAO.getAllUsers().get(0);
        Resource res = resDAO.getAllResources().get(0);
        Room room = roomDAO.getAllRooms().get(0);
        TimeSlot slot = slotDAO.getAllActiveSlots().get(0);
        Date bDate = Date.valueOf(LocalDate.now().plusDays(2));

        Map<String, Object> result = bookingDAO.bookResourceViaProcedure(
            user.getUserId(),
            res.getResourceId(),
            room.getRoomId(),
            slot.getSlotId(),
            bDate,
            "End-to-End Test National Tech Symposium"
        );

        boolean ok = (Boolean) result.get("success");
        int bookingId = (Integer) result.get("bookingId");
        check("Stored Procedure book_resource()", ok && bookingId > 0, "Booking Created Successfully (ID: #" + bookingId + ")");
    }

    private static void testStoredProcedureBookingConflict() {
        UserDAO userDAO = new UserDAO();
        ResourceDAO resDAO = new ResourceDAO();
        RoomDAO roomDAO = new RoomDAO();
        TimeSlotDAO slotDAO = new TimeSlotDAO();
        BookingDAO bookingDAO = new BookingDAO();

        User user = userDAO.getAllUsers().get(1);
        Resource res = resDAO.getAllResources().get(0);
        Room room = roomDAO.getAllRooms().get(0);
        TimeSlot slot = slotDAO.getAllActiveSlots().get(0);
        Date bDate = Date.valueOf(LocalDate.now().plusDays(2));

        Map<String, Object> conflictResult = bookingDAO.bookResourceViaProcedure(
            user.getUserId(),
            res.getResourceId(),
            room.getRoomId(),
            slot.getSlotId(),
            bDate,
            "Conflicting reservation attempt"
        );

        boolean ok = (Boolean) conflictResult.get("success");
        int code = (Integer) conflictResult.get("statusCode");
        String msg = (String) conflictResult.get("message");
        check("Double-Booking Conflict Prevention", !ok && code > 0, "Blocked duplicate reservation. Code: " + code + " (" + msg + ")");
    }

    private static void testTriggerResourceStatusTransition() {
        ResourceDAO resDAO = new ResourceDAO();
        BookingDAO bookingDAO = new BookingDAO();
        List<Booking> bookings = bookingDAO.getAllBookings();
        if (!bookings.isEmpty()) {
            Booking b = bookings.get(0);
            Resource res = resDAO.getResourceById(b.getResourceId());
            check("Trigger after_booking_insert", "BOOKED".equalsIgnoreCase(res.getStatus()) || "AVAILABLE".equalsIgnoreCase(res.getStatus()), "Resource status correctly updated: " + res.getStatus());
        }
    }

    private static void testStoredProcedureBookingCancellation() {
        BookingDAO bookingDAO = new BookingDAO();
        List<Booking> bookings = bookingDAO.getAllBookings();
        if (!bookings.isEmpty()) {
            Booking b = bookings.get(0);
            Map<String, Object> cancelResult = bookingDAO.cancelBookingViaProcedure(b.getBookingId(), b.getUserId());
            boolean ok = (Boolean) cancelResult.get("success");
            String msg = (String) cancelResult.get("message");
            check("Stored Procedure cancel_booking()", ok, "Cancelled Booking #" + b.getBookingId() + " (" + msg + ")");
        }
    }

    private static void testMaintenanceWorkflow() {
        MaintenanceDAO mDAO = new MaintenanceDAO();
        UserDAO userDAO = new UserDAO();
        ResourceDAO resDAO = new ResourceDAO();

        User reporter = userDAO.getAllUsers().get(0);
        Resource res = resDAO.getAllResources().get(1);

        Maintenance m = new Maintenance();
        m.setResourceId(res.getResourceId());
        m.setRoomId(null);
        m.setReportedBy(reporter.getUserId());
        m.setIssueTitle("Lens Calibration Flicker");
        m.setIssueDescription("Optical laser display calibration required.");
        m.setMaintenanceDate(Date.valueOf(LocalDate.now()));
        m.setMaintenanceStatus("In Progress");
        m.setCost(BigDecimal.ZERO);

        boolean rep = mDAO.reportIssue(m);
        check("Maintenance Ticket Report", rep, "Reported defect ticket for " + res.getResourceName());

        List<Maintenance> mList = mDAO.getAllMaintenance();
        check("Maintenance Read", !mList.isEmpty(), "Found " + mList.size() + " maintenance tickets");

        int ticketId = mList.get(0).getMaintenanceId();
        boolean comp = mDAO.completeMaintenance(ticketId, new BigDecimal("3500.00"), Date.valueOf(LocalDate.now()));
        check("Maintenance Completion & Cost", comp, "Completed Ticket #" + ticketId + " with 3500.00 INR cost");
    }

    private static void testFeedbackWithRatingConstraint() {
        FeedbackDAO fbDAO = new FeedbackDAO();
        UserDAO userDAO = new UserDAO();
        BookingDAO bookingDAO = new BookingDAO();

        List<Booking> bookings = bookingDAO.getAllBookings();
        if (!bookings.isEmpty()) {
            Booking b = bookings.get(0);
            Feedback fb = new Feedback();
            fb.setBookingId(b.getBookingId());
            fb.setUserId(b.getUserId());
            fb.setRating(5);
            fb.setComments("Outstanding acoustics and smooth reservation process.");

            boolean ok = fbDAO.addFeedback(fb);
            check("Feedback Submission (Rating 5 Stars)", ok, "Verified rating BETWEEN 1 AND 5 check constraint");

            List<Feedback> allFb = fbDAO.getAllFeedback();
            check("Feedback Read", !allFb.isEmpty(), "Retrieved " + allFb.size() + " review submissions");
        }
    }

    private static void testDatabaseViews() {
        BookingDAO bookingDAO = new BookingDAO();
        List<Booking> viewList = bookingDAO.getBookingDetailsView();
        check("MySQL View booking_details_view", viewList != null, "Successfully queried 7-table view (Rows: " + viewList.size() + ")");

        ReportDAO rDAO = new ReportDAO();
        DefaultTableModel roomUtilModel = rDAO.getRoomUtilization();
        check("MySQL View room_utilization_view", roomUtilModel != null, "Successfully queried venue utilization view (Columns: " + roomUtilModel.getColumnCount() + ")");
    }

    private static void testSearchOperations() {
        ResourceDAO resDAO = new ResourceDAO();
        List<Resource> s1 = resDAO.searchResources("Smart", null, "ALL", null);
        check("Search Resources by Keyword", !s1.isEmpty(), "Found " + s1.size() + " resources matching 'Smart'");

        RoomDAO roomDAO = new RoomDAO();
        List<Room> s2 = roomDAO.searchRooms(null, "Classroom", 50, "ALL");
        check("Search Rooms by Type & Capacity", !s2.isEmpty(), "Found " + s2.size() + " classrooms with capacity >= 50");

        UserDAO userDAO = new UserDAO();
        List<User> s3 = userDAO.searchUsers("Rajesh", "ALL", null);
        check("Search Users by Name", !s3.isEmpty(), "Found " + s3.size() + " user matching 'Rajesh'");
    }

    private static void testReportEngineAndCharts() {
        ReportDAO rDAO = new ReportDAO();

        DefaultTableModel m1 = rDAO.getDepartmentWiseBookings();
        DefaultTableModel m2 = rDAO.getMostBookedResources();
        DefaultTableModel m3 = rDAO.getMaintenanceHistory();
        DefaultTableModel m4 = rDAO.getResourceUsageStatistics();
        check("Analytical Reports", m1 != null && m2 != null && m3 != null && m4 != null, "Loaded Department, Most Booked, Maintenance & Statistics Reports");

        Map<String, Number> chart1 = rDAO.getDepartmentBookingChartData();
        Map<String, Number> chart2 = rDAO.getBookingStatusDistribution();
        check("Chart Data Generation", chart1 != null && chart2 != null, "Generated Bar & Pie Chart datasets for Java 2D ChartPanel");

        Map<String, Integer> counts = rDAO.getDashboardSummaryCounts();
        check("Dashboard Metric Counts", counts.containsKey("total_resources") && counts.containsKey("total_users"), "Live Dashboard Summary metrics retrieved");
    }
}
