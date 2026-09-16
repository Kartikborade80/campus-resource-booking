# Campus Resource Booking and Management System
### Sanjivani University — DBMS College Mini-Project

---

## 1. Project Title
**Campus Resource Booking and Management System**  
A desktop college database application developed in pure **Java Swing** connected to **MySQL 8.0** using **JDBC (Java Database Connectivity)**.

---

## 2. Problem Statement
In modern collegiate environments, educational institutions house valuable shared assets—including smart lecture classrooms, high-performance computing laboratories, auditoriums, seminar halls, 4K laser projectors, audio equipment, and sports gear. Traditional pen-and-paper registers or ad-hoc messaging result in double-bookings, scheduling collisions, untracked asset degradation, and lack of accountability. A centralized, transactional, database-enforced management system is required to eliminate scheduling conflicts and streamline reservations.

---

## 3. Introduction
The **Campus Resource Booking and Management System** is a standalone desktop application built entirely using Java Swing for the graphical user interface, Java JDBC for database persistence, and MySQL 8.0 for relational storage. The application provides dedicated, role-based workflows for Administrators, Faculty, Staff, and Students to request, schedule, approve, and audit campus resources.

---

## 4. Objectives
* Automate reservation workflows for college venues and equipment.
* Prevent double-booking conflicts at both the application level and the database level using MySQL Stored Procedures and Triggers.
* Implement a 3NF normalized relational schema with 10 tables.
* Provide an interactive Java Swing dashboard displaying live institutional metrics and pure Java 2D graphics charts.
* Deliver complete CRUD operations for all entities and 10 analytical reporting queries.

---

## 5. Scope
* **Users Supported:** Administrators, Faculty Members, Department Staff, and Students.
* **Resources Managed:** Smart Classrooms, Computer Laboratories, Auditoriums, Seminar Halls, Projectors, Laptops, Mobile Workstations, Cameras, Audio Systems, and Sports Equipment.
* **Operational Scope:** Booking reservations, administrative approvals/rejections, maintenance logging with cost tracking, 1-5 star user feedback ratings, and audit reporting.

---

## 6. Existing System
* Manual logbooks and paper approval slips.
* High latency in administrative approvals.
* Frequent double-booking of halls and equipment.
* No centralized tracking of maintenance expenses or equipment condition.
* Absence of analytical utilization reports.

---

## 7. Proposed System
* Direct desktop access via Java Swing GUI with role-based access control.
* Instant conflict validation powered by the MySQL stored procedure `book_resource()`.
* Automatic status transitions and audit consistency enforced by database triggers.
* Real-time analytical reports and Java 2D graphical charts.
* Structured feedback loop with constraint-enforced star ratings.

---

## 8. Advantages
* **Guaranteed ACID Consistency:** MySQL InnoDB engine handles transactions with strict commit/rollback semantics.
* **Zero Double-Bookings:** Enforced concurrently by stored procedures and pre-insert triggers.
* **Native Desktop Performance:** Instantaneous UI response using Java Swing without browser or web server overhead.
* **Zero External Charting Dependencies:** Custom Java 2D graphics engine renders responsive Bar Charts and Pie Charts.
* **Audit Compliance:** Historical tracking of all bookings, cancellations, and maintenance expenditures.

---

## 9. Technologies Used
* **Programming Language:** Java (SE 17 / SE 21 / SE 24)
* **GUI Toolkit:** Java Swing & AWT (`JFrame`, `JTable`, `JTabbedPane`, `Graphics2D`)
* **Database Management System:** MySQL Community Server 8.0
* **Database Driver:** MySQL Connector/J 8.3.0 (`com.mysql.cj.jdbc.Driver`)
* **Database Interface:** JDBC (`Connection`, `PreparedStatement`, `CallableStatement`, `ResultSet`)
* **Design Pattern:** Data Access Object (DAO) Pattern & Model-View-Controller (MVC)

---

## 10. System Modules
1. **Authentication & Session Module:** Secure credential verification supporting Admin, Faculty, Staff, and Student roles with 1-click evaluation shortcuts.
2. **Administrator Control Center:** Top-level metrics cards, live bookings feed, and navigation to all administrative tools.
3. **Student / Faculty Portal:** Available assets catalog, my bookings history, cancellation utility, and feedback submissions.
4. **Resource Inventory Management:** Full CRUD operations, category filtering, search, and capacity monitoring.
5. **Rooms & Venues Management:** Full CRUD operations for classrooms, auditoriums, and labs with capacity and facility search.
6. **Booking Engine (Stored Procedure):** Interactive reservation wizard executing `book_resource()` via JDBC `CallableStatement`.
7. **Maintenance & Servicing Module:** Defect reporting, status tracking (Reported -> In Progress -> Completed), and repair expenditure logging.
8. **Feedback & Reviews Module:** Star ratings (1 to 5) with database CHECK constraints and comment tracking.
9. **Analytical Reports & Charts Engine:** 10 reports rendered via `JTable` with CSV export and custom 2D visual charts.

---

## 11. Database Design (10 Normalized Tables)
The database `campus_resource_booking` consists of 10 relational tables:
1. `departments`
2. `users`
3. `resource_categories`
4. `resources`
5. `rooms`
6. `time_slots`
7. `bookings`
8. `booking_participants`
9. `maintenance`
10. `feedback`

---

## 12. ER Diagram & Cardinality

```mermaid
erDiagram
    departments ||--o{ users : "employs / enrolls"
    resource_categories ||--o{ resources : "classifies"
    users ||--o{ bookings : "creates"
    resources ||--o{ bookings : "reserved in"
    rooms ||--o{ bookings : "hosts"
    time_slots ||--o{ bookings : "scheduled at"
    bookings ||--o{ booking_participants : "has"
    users ||--o{ booking_participants : "participates"
    resources ||--o{ maintenance : "undergoes"
    rooms ||--o{ maintenance : "undergoes"
    users ||--o{ maintenance : "reports"
    bookings ||--o| feedback : "reviewed in"
    users ||--o{ feedback : "submits"
    users ||--o{ bookings : "approves"
```

### Text Cardinality Representation
* **Department to Users:** 1 : N (One department has many users; each user belongs to one department)
* **Resource Category to Resources:** 1 : N (One category contains many resources; each resource belongs to one category)
* **User to Bookings:** 1 : N (One user places many bookings; each booking has one owner)
* **Resource to Bookings:** 1 : N (One resource is booked across different slots; each booking reserves one resource)
* **Room to Bookings:** 1 : N (One room venue hosts multiple bookings; a booking optionally links to one room)
* **Time Slot to Bookings:** 1 : N (One slot has many scheduled bookings across distinct assets)
* **Booking to Participants:** 1 : N (One booking has multiple attendee records)
* **User to Participants:** 1 : N (One user participates in multiple sessions)
* **Resource / Room to Maintenance:** 1 : N (One asset has multiple maintenance logs over its lifecycle)
* **Booking to Feedback:** 1 : 1 (Each completed booking receives at most one feedback review)

---

## 13. Relational Schema
* **departments** (**department_id**, department_name, department_code, status)
* **users** (**user_id**, name, email, password, phone, role, *department_id*, status, created_at)
* **resource_categories** (**category_id**, category_name, description)
* **resources** (**resource_id**, *category_id*, resource_name, resource_code, description, capacity, location, status, purchase_date)
* **rooms** (**room_id**, room_number, building_name, floor_number, capacity, room_type, facilities, status)
* **time_slots** (**slot_id**, slot_name, start_time, end_time, status)
* **bookings** (**booking_id**, *user_id*, *resource_id*, *room_id*, *slot_id*, booking_date, purpose, booking_status, *approved_by*, created_at)
* **booking_participants** (**participant_id**, *booking_id*, *user_id*, participation_role, joined_at)
* **maintenance** (**maintenance_id**, *resource_id*, *room_id*, *reported_by*, issue_title, issue_description, maintenance_date, completion_date, maintenance_status, cost)
* **feedback** (**feedback_id**, *booking_id*, *user_id*, rating, comments, submitted_at)

---

## 14. Table Descriptions

| Table Name | Primary Key | Description | Key Foreign Keys |
| :--- | :--- | :--- | :--- |
| `departments` | `department_id` | Academic branches (CSE, IT, MECH, etc.) | None |
| `users` | `user_id` | Students, Faculty, Staff, and Administrators | `department_id` -> `departments` |
| `resource_categories` | `category_id` | Categorization of college assets | None |
| `resources` | `resource_id` | Individual bookable items and hardware | `category_id` -> `resource_categories` |
| `rooms` | `room_id` | Physical venues, halls, and laboratories | None |
| `time_slots` | `slot_id` | College scheduling periods | None |
| `bookings` | `booking_id` | Master reservation records | `user_id`, `resource_id`, `room_id`, `slot_id` |
| `booking_participants`| `participant_id` | Many-to-many relationship of attendees | `booking_id`, `user_id` |
| `maintenance` | `maintenance_id` | Equipment defect reports & servicing cost | `resource_id`, `room_id`, `reported_by` |
| `feedback` | `feedback_id` | User satisfaction reviews (1-5 stars) | `booking_id`, `user_id` |

---

## 15. Primary Keys
* `departments.department_id` (INT AUTO_INCREMENT)
* `users.user_id` (INT AUTO_INCREMENT)
* `resource_categories.category_id` (INT AUTO_INCREMENT)
* `resources.resource_id` (INT AUTO_INCREMENT)
* `rooms.room_id` (INT AUTO_INCREMENT)
* `time_slots.slot_id` (INT AUTO_INCREMENT)
* `bookings.booking_id` (INT AUTO_INCREMENT)
* `booking_participants.participant_id` (INT AUTO_INCREMENT)
* `maintenance.maintenance_id` (INT AUTO_INCREMENT)
* `feedback.feedback_id` (INT AUTO_INCREMENT)

---

## 16. Foreign Keys & Referential Actions
* `users.department_id` -> `departments.department_id` (`ON DELETE RESTRICT ON UPDATE CASCADE`)
* `resources.category_id` -> `resource_categories.category_id` (`ON DELETE RESTRICT ON UPDATE CASCADE`)
* `bookings.user_id` -> `users.user_id` (`ON DELETE RESTRICT ON UPDATE CASCADE`)
* `bookings.resource_id` -> `resources.resource_id` (`ON DELETE RESTRICT ON UPDATE CASCADE`)
* `bookings.room_id` -> `rooms.room_id` (`ON DELETE SET NULL ON UPDATE CASCADE`)
* `bookings.slot_id` -> `time_slots.slot_id` (`ON DELETE RESTRICT ON UPDATE CASCADE`)
* `bookings.approved_by` -> `users.user_id` (`ON DELETE SET NULL ON UPDATE CASCADE`)
* `booking_participants.booking_id` -> `bookings.booking_id` (`ON DELETE CASCADE ON UPDATE CASCADE`)
* `booking_participants.user_id` -> `users.user_id` (`ON DELETE CASCADE ON UPDATE CASCADE`)
* `maintenance.resource_id` -> `resources.resource_id` (`ON DELETE SET NULL ON UPDATE CASCADE`)
* `maintenance.room_id` -> `rooms.room_id` (`ON DELETE SET NULL ON UPDATE CASCADE`)
* `maintenance.reported_by` -> `users.user_id` (`ON DELETE RESTRICT ON UPDATE CASCADE`)
* `feedback.booking_id` -> `bookings.booking_id` (`ON DELETE CASCADE ON UPDATE CASCADE`)
* `feedback.user_id` -> `users.user_id` (`ON DELETE RESTRICT ON UPDATE CASCADE`)

---

## 17. Database Constraints
* **PRIMARY KEY:** Unique identifier on all 10 tables.
* **NOT NULL:** Enforced on mandatory attributes (`name`, `email`, `resource_name`, `booking_date`).
* **UNIQUE:** `users.email`, `departments.department_name`, `departments.department_code`, `resources.resource_code`, `rooms.room_number`, `feedback.booking_id`.
* **CHECK Constraints:**
  * `rating BETWEEN 1 AND 5` on `feedback.rating`
  * `capacity >= 1` on `resources.capacity`
  * `capacity > 0` on `rooms.capacity`
  * `start_time < end_time` on `time_slots`
  * `cost >= 0.00` on `maintenance.cost`
  * `role IN ('ADMIN', 'FACULTY', 'STAFF', 'STUDENT')` on `users.role`
  * `status IN ('AVAILABLE', 'BOOKED', 'MAINTENANCE', 'INACTIVE')` on `resources.status`
  * `booking_status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED')` on `bookings`
* **DEFAULT:** Default values for `status`, `created_at`, `cost`, and initial booking states.
* **AUTO_INCREMENT:** Automatic primary key generation on all tables.

---

## 18. Normalization Proof up to 3NF

### First Normal Form (1NF)
* **Requirement:** Each column must contain atomic (indivisible) values, and there must be no repeating groups.
* **Implementation:** Multi-valued phone numbers or multiple booking participants are decomposed into separate tables (`booking_participants`). Every row across all 10 tables contains strictly atomic attributes.

### Second Normal Form (2NF)
* **Requirement:** Must be in 1NF and all non-key attributes must be fully functionally dependent on the entire primary key (no partial dependencies).
* **Implementation:** In tables with composite unique requirements (such as `booking_participants`), attributes depend strictly on the complete primary key (`participant_id`). Attributes like resource category names or department details are separated into independent parent tables rather than embedded within bookings.

### Third Normal Form (3NF)
* **Requirement:** Must be in 2NF and have no transitive dependencies (non-prime attributes must not depend on other non-prime attributes).
* **Implementation:** In `bookings`, only `user_id`, `resource_id`, `room_id`, and `slot_id` are stored. User department names, resource categories, and room capacities are reached via foreign key joins, eliminating transitive dependencies.

---

## 19. CRUD Operations Implemented in Java Swing

| Entity | Create (Add) | Read (View/Search) | Update (Edit) | Delete / Cancel |
| :--- | :--- | :--- | :--- | :--- |
| **Users** | `UserFrame.showAddDialog()` | `UserFrame.loadData()` & `searchUsers()` | `UserFrame.showEditDialog()` | `UserFrame.handleDelete()` |
| **Departments**| `DepartmentFrame.showAddDialog()` | `DepartmentFrame.loadData()` | `DepartmentFrame.showEditDialog()` | `DepartmentFrame.handleDelete()` |
| **Resources** | `ResourceFrame.showAddDialog()` | `ResourceFrame.loadData()` & `searchResources()` | `ResourceFrame.showEditDialog()` | `ResourceFrame.handleDelete()` |
| **Rooms** | `RoomFrame.showAddDialog()` | `RoomFrame.loadData()` & `searchRooms()` | `RoomFrame.showEditDialog()` | `RoomFrame.handleDelete()` |
| **Bookings** | `BookingFrame.showNewBookingWizard()` via `CallableStatement` | `BookingFrame.loadData()` & `searchBookings()` | `BookingFrame.updateStatusAction()` | `BookingFrame.handleCancelBooking()` via `cancel_booking()` |
| **Maintenance**| `MaintenanceFrame.showReportDialog()` | `MaintenanceFrame.loadData()` | `MaintenanceFrame.updateStatus()` | `MaintenanceFrame.showCompleteDialog()` |
| **Feedback** | `FeedbackFrame.showFeedbackDialog()` | `FeedbackFrame.loadData()` | Star Rating Constraint Enforced | Deletion cascades with booking |

---

## 20. JOIN Queries Demonstration

### Query 1: User + Department + Booking (INNER JOIN)
```sql
SELECT b.booking_id, u.name AS user_name, u.email, u.role, d.department_name, b.booking_date, b.booking_status
FROM bookings b
INNER JOIN users u ON b.user_id = u.user_id
INNER JOIN departments d ON u.department_id = d.department_id;
```

### Query 2: Booking + Resource + Category (INNER JOIN)
```sql
SELECT b.booking_id, r.resource_name, r.resource_code, rc.category_name, b.booking_date, b.booking_status
FROM bookings b
INNER JOIN resources r ON b.resource_id = r.resource_id
INNER JOIN resource_categories rc ON r.category_id = rc.category_id;
```

### Query 3: Booking + Room + Time Slot (LEFT + INNER JOIN)
```sql
SELECT b.booking_id, COALESCE(rm.room_number, 'Portable Asset') AS room_number, rm.building_name, ts.slot_name, ts.start_time, ts.end_time, b.booking_date
FROM bookings b
LEFT JOIN rooms rm ON b.room_id = rm.room_id
INNER JOIN time_slots ts ON b.slot_id = ts.slot_id;
```

### Query 4: Complete Booking Details (7-Table JOIN)
```sql
SELECT b.booking_id, u.name AS reserved_by, d.department_name, r.resource_name, rc.category_name, COALESCE(rm.room_number, 'External') AS room, ts.slot_name AS session_slot, b.booking_date, b.purpose, b.booking_status
FROM bookings b
INNER JOIN users u ON b.user_id = u.user_id
INNER JOIN departments d ON u.department_id = d.department_id
INNER JOIN resources r ON b.resource_id = r.resource_id
INNER JOIN resource_categories rc ON r.category_id = rc.category_id
LEFT JOIN rooms rm ON b.room_id = rm.room_id
INNER JOIN time_slots ts ON b.slot_id = ts.slot_id;
```

### Query 5: Maintenance + Resource + Room (Multi-table JOIN)
```sql
SELECT m.maintenance_id, m.issue_title, COALESCE(r.resource_name, 'Venue') AS resource_name, COALESCE(rm.room_number, 'Asset') AS room_number, m.maintenance_status, m.cost, u.name AS reported_by_name
FROM maintenance m
LEFT JOIN resources r ON m.resource_id = r.resource_id
LEFT JOIN rooms rm ON m.room_id = rm.room_id
INNER JOIN users u ON m.reported_by = u.user_id;
```

---

## 21. Subqueries Demonstration

### Subquery 1: Most Frequently Booked Resource
```sql
SELECT r.resource_id, r.resource_name, r.resource_code, rc.category_name,
       (SELECT COUNT(*) FROM bookings b WHERE b.resource_id = r.resource_id) AS total_times_booked
FROM resources r
INNER JOIN resource_categories rc ON r.category_id = rc.category_id
WHERE r.resource_id = (
    SELECT resource_id 
    FROM bookings 
    GROUP BY resource_id 
    ORDER BY COUNT(*) DESC 
    LIMIT 1
);
```

### Subquery 2: Users with Above-Average Bookings
```sql
SELECT u.user_id, u.name, u.email, d.department_name, COUNT(b.booking_id) AS user_booking_count
FROM users u
INNER JOIN departments d ON u.department_id = d.department_id
INNER JOIN bookings b ON u.user_id = b.user_id
GROUP BY u.user_id, u.name, u.email, d.department_name
HAVING COUNT(b.booking_id) > (
    SELECT AVG(booking_tally)
    FROM (
        SELECT COUNT(booking_id) AS booking_tally
        FROM bookings
        GROUP BY user_id
    ) AS sub
);
```

### Subquery 3: Resources that Have Never Been Booked
```sql
SELECT r.resource_id, r.resource_name, r.resource_code, rc.category_name, r.location, r.status
FROM resources r
INNER JOIN resource_categories rc ON r.category_id = rc.category_id
WHERE r.resource_id NOT IN (
    SELECT DISTINCT resource_id FROM bookings
);
```

### Subquery 4: Rooms with Above-Average Bookings
```sql
SELECT rm.room_id, rm.room_number, rm.building_name, rm.capacity, COUNT(b.booking_id) AS room_booking_count
FROM rooms rm
INNER JOIN bookings b ON rm.room_id = b.room_id
GROUP BY rm.room_id, rm.room_number, rm.building_name, rm.capacity
HAVING COUNT(b.booking_id) > (
    SELECT AVG(room_count)
    FROM (
        SELECT COUNT(booking_id) AS room_count
        FROM bookings
        WHERE room_id IS NOT NULL
        GROUP BY room_id
    ) AS room_sub
);
```

### Subquery 5: Department with the Highest Resource Usage
```sql
SELECT d.department_id, d.department_name, d.department_code, COUNT(b.booking_id) AS total_bookings
FROM departments d
INNER JOIN users u ON d.department_id = u.department_id
INNER JOIN bookings b ON u.user_id = b.user_id
GROUP BY d.department_id, d.department_name, d.department_code
HAVING COUNT(b.booking_id) >= ALL (
    SELECT COUNT(b2.booking_id)
    FROM departments d2
    INNER JOIN users u2 ON d2.department_id = u2.department_id
    INNER JOIN bookings b2 ON u2.user_id = b2.user_id
    GROUP BY d2.department_id
);
```

---

## 22. MySQL Views

### 1. `booking_details_view`
Joins 7 tables (`bookings`, `users`, `departments`, `resources`, `resource_categories`, `rooms`, `time_slots`) into a flat, report-ready relational view.
```sql
CREATE VIEW booking_details_view AS
SELECT 
    b.booking_id, u.name AS user_name, u.email AS user_email, u.role AS user_role,
    d.department_name, r.resource_name, r.resource_code, rc.category_name AS resource_category,
    COALESCE(rm.room_number, 'N/A') AS room_number, COALESCE(rm.building_name, 'N/A') AS building_name,
    ts.slot_name AS time_slot, ts.start_time, ts.end_time,
    b.booking_date, b.purpose, b.booking_status,
    COALESCE(appr.name, 'Pending Approval') AS approved_by_name, b.created_at
FROM bookings b
INNER JOIN users u ON b.user_id = u.user_id
INNER JOIN departments d ON u.department_id = d.department_id
INNER JOIN resources r ON b.resource_id = r.resource_id
INNER JOIN resource_categories rc ON r.category_id = rc.category_id
LEFT JOIN rooms rm ON b.room_id = rm.room_id
INNER JOIN time_slots ts ON b.slot_id = ts.slot_id
LEFT JOIN users appr ON b.approved_by = appr.user_id;
```

### 2. `room_utilization_view`
Aggregates total, approved, completed, and cancelled reservations for every campus venue.

### 3. `resource_summary_view`
Aggregates resource availability and maintenance states by category.

---

## 23. MySQL Triggers

### 1. `before_booking_insert`
Fires before inserting any reservation into `bookings`. Enforces two critical database guarantees:
* Verifies that the resource is not under maintenance or inactive (`SIGNAL SQLSTATE '45000'`).
* Performs an atomic pre-insert conflict check for overlapping reservations.

### 2. `after_booking_insert`
Fires after a booking is confirmed as `APPROVED`, instantly setting the resource status to `BOOKED`.

### 3. `after_booking_update`
Fires when a booking status changes:
* When approved, updates resource to `BOOKED`.
* When cancelled, rejected, or completed, verifies whether any active bookings remain; if none, returns resource status to `AVAILABLE`.

### 4. `after_maintenance_update`
When a maintenance ticket completes, automatically restores linked resources and rooms back to `AVAILABLE`. When marked `In Progress`, updates assets to `MAINTENANCE`.

---

## 24. MySQL Stored Procedures

### `book_resource()`
Executes transactional reservation creation with complete atomic validation:
```sql
CALL book_resource(p_user_id, p_resource_id, p_room_id, p_slot_id, p_booking_date, p_purpose, @status_code, @message, @booking_id);
```
**Java Invocation via JDBC `CallableStatement`:**
```java
CallableStatement cs = conn.prepareCall("{CALL book_resource(?, ?, ?, ?, ?, ?, ?, ?, ?)}");
cs.setInt(1, userId);
cs.setInt(2, resourceId);
cs.setObject(3, roomId, Types.INTEGER);
cs.setInt(4, slotId);
cs.setDate(5, bookingDate);
cs.setString(6, purpose);
cs.registerOutParameter(7, Types.INTEGER);
cs.registerOutParameter(8, Types.VARCHAR);
cs.registerOutParameter(9, Types.INTEGER);
cs.execute();
int statusCode = cs.getInt(7);
String message = cs.getString(8);
int bookingId = cs.getInt(9);
```

### `cancel_booking()`
Verifies user authorization, transitions the booking state to `CANCELLED`, and frees the allocated resource in a single transaction.

---

## 25. Search Functionality in Java Swing
All search operations are implemented using parameterized `PreparedStatement` queries to prevent SQL injection vulnerabilities:
* **Resource Search:** By keyword (matches resource name, code, description), category filter, and status filter.
* **Room Search:** By room number, building, facilities text, room type, and capacity.
* **Booking Search:** By applicant name, resource name, date, and booking status.

---

## 26. Analytical Reports & Swing 2D Charts
The `ReportsFrame` provides 10 analytical reports selectable from a dropdown:
1. **Most Booked Resources**
2. **Available Resources**
3. **Department-wise Bookings**
4. **Daily Booking Report**
5. **Monthly Booking Report**
6. **Room Utilization** (Queries `room_utilization_view`)
7. **Maintenance History & Cost Expenditure**
8. **Cancelled / Rejected Bookings**
9. **User Booking History**
10. **Resource Usage Statistics**

### Pure Java 2D Graphics Engine (`ChartPanel`)
* **Bar Chart:** Renders department-wise booking volume with calculated scaling, axes, ticks, bar colors, and value labels.
* **Pie Chart:** Renders booking status distribution with anti-aliased slices, percentage calculations, and dynamic legends.
* **No external third-party charting libraries required.**

---

## 27. Java-MySQL Connectivity Architecture

```
+-------------------------------------------------------------+
|                     Java Swing GUI                          |
|  (LoginFrame, AdminDashboard, StudentDashboard, Forms)     |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                   Data Access Objects (DAO)                 |
|  (UserDAO, ResourceDAO, RoomDAO, BookingDAO, ReportDAO)    |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|            JDBC API (java.sql.* / javax.sql.*)              |
|  - Connection: DatabaseConnection.getConnection()           |
|  - PreparedStatement: Parameterized SQL CRUD Queries        |
|  - CallableStatement: Stored Procedure Invocations          |
|  - ResultSet: TableModel mapping for Swing JTable           |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|            MySQL Connector/J 8.3.0 Driver                   |
|                  com.mysql.cj.jdbc.Driver                   |
+-------------------------------------------------------------+
                               |  TCP Port 3306
                               v
+-------------------------------------------------------------+
|               MySQL 8.0 Relational Database                 |
|                   campus_resource_booking                   |
+-------------------------------------------------------------+
```

---

## 28. Screenshots & User Flow Walkthrough
1. **Login Portal (`LoginFrame`):** Enter institutional email and password, or click any of the 4 quick demo buttons (`Admin`, `Faculty`, `Student`, `Staff`) to log in immediately.
2. **Admin Dashboard (`AdminDashboard`):** Inspect 7 institutional summary cards, access the navigation toolbar, and review live campus bookings.
3. **Student Dashboard (`StudentDashboard`):** Browse available resources, check personal bookings, and click "+ Book Resource" to launch the booking wizard.
4. **Booking Wizard:** Select Category -> Target Resource -> Optional Room Venue -> Booking Date -> Time Slot -> Enter Purpose -> Click "Confirm Booking". The system immediately invokes `book_resource()` and confirms reservation.
5. **Reports & Charts (`ReportsFrame`):** Select any of the 10 reports in Tab 1, or switch to Tab 2 to inspect visual Bar Charts and Pie Charts.

---

## 29. Testing & Validation Matrix

| Test Case ID | Test Description | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TC-01** | Connect to MySQL via JDBC | Connection established using `DatabaseConnection` | Successful connection | **PASSED** |
| **TC-02** | User login with valid credentials | User authenticated and redirected by role | Correct dashboard launched | **PASSED** |
| **TC-03** | User login with invalid password | Authentication rejected with warning message | "Invalid credentials" shown | **PASSED** |
| **TC-04** | Resource CRUD: Add new resource | Record inserted into MySQL `resources` table | Added and listed in table | **PASSED** |
| **TC-05** | Booking creation without conflicts | Stored procedure `book_resource()` executes | Booking created; reference ID returned | **PASSED** |
| **TC-06** | Double-booking same resource/slot | Stored procedure identifies conflict | Error code 109 returned with alert | **PASSED** |
| **TC-07** | Booking asset under maintenance | Stored procedure rejects booking | Error code 103 returned with alert | **PASSED** |
| **TC-08** | Booking cancellation | Stored procedure `cancel_booking()` executes | Booking cancelled; resource available | **PASSED** |
| **TC-09** | Feedback submission with 1-5 rating | Constraint `rating BETWEEN 1 AND 5` verified | Star rating saved in MySQL | **PASSED** |
| **TC-10** | Query MySQL View `booking_details_view` | 7-table joined view loads into `JTable` | Tabular data rendered | **PASSED** |
| **TC-11** | Report generation & CSV export | Data loaded and saved to `.csv` file | CSV file created with full headers | **PASSED** |
| **TC-12** | Pure Java 2D Chart rendering | Bar and Pie charts drawn on canvas | Visual charts rendered cleanly | **PASSED** |

---

## 30. Conclusion
The **Campus Resource Booking and Management System** satisfies all academic requirements of a college Database Management Systems (DBMS) project. By integrating a normalized 10-table MySQL relational database with a desktop Java Swing application via JDBC, it demonstrates primary/foreign key relationships, constraints, stored procedures, triggers, views, complex joins, subqueries, and secure parameterized queries.

---

## 31. Future Scope
* Integration with biometric campus card readers (RFID/NFC) for automated door access upon booking approval.
* Email and SMS notifications via campus SMTP relays.
* QR code generation on booking confirmation for venue check-in verification.
* Calendar synchronization (Google Calendar / Microsoft Outlook iCal integration).

---

## 32. Assignment Requirement Checklist

| Requirement | Specification | Implementation in Project | Verification Status |
| :--- | :--- | :--- | :--- |
| **Technology** | Java Swing + MySQL + JDBC only | Pure Java Swing GUI, JDBC Connector/J 8.3.0, MySQL 8.0 | **100% Verified** |
| **Minimum 8 Tables** | At least 10 related tables in 3NF | 10 Relational Tables (`departments`, `users`, `categories`, `resources`, `rooms`, `slots`, `bookings`, `participants`, `maintenance`, `feedback`) | **100% Verified** |
| **ER Diagram** | Entity names, attributes, PK, FK, cardinality | Mermaid ER Diagram & Cardinality Specification in README Section 12 | **100% Verified** |
| **Relational Schema** | Text relational schema with PK and FK | Relational schema in README Section 13 | **100% Verified** |
| **Database Constraints**| PK, FK, NOT NULL, UNIQUE, CHECK, DEFAULT | Enforced on all tables in `database/schema.sql` | **100% Verified** |
| **3NF Normalization** | 1NF, 2NF, 3NF detailed explanation | Full mathematical proof in README Section 18 | **100% Verified** |
| **CRUD Operations** | Complete CRUD on all major entities | Implemented in `UserFrame`, `ResourceFrame`, `RoomFrame`, `DepartmentFrame`, `BookingFrame`, `MaintenanceFrame` | **100% Verified** |
| **MySQL View** | At least 1 meaningful VIEW (`booking_details_view`) | 3 Views created in `database/views.sql` and displayed in `ReportsFrame` | **100% Verified** |
| **MySQL Trigger** | Resource status / audit consistency trigger | 4 Triggers in `database/triggers.sql` (`before_booking_insert`, `after_booking_insert`, etc.) | **100% Verified** |
| **Stored Procedure** | `book_resource()` with conflict validation | Created in `database/procedures.sql` and invoked via JDBC `CallableStatement` | **100% Verified** |
| **JOIN Queries** | INNER, LEFT, Multi-table joins | 5 Queries in `database/queries.sql` and integrated into DAOs | **100% Verified** |
| **Subqueries** | 5 meaningful subqueries | Implemented in `database/queries.sql` and report queries | **100% Verified** |
| **Search Functionality**| Multi-field search using PreparedStatement | Real-time search in `ResourceFrame`, `RoomFrame`, `UserFrame`, `BookingFrame` | **100% Verified** |
| **Reports Module** | 10 reports in JTable + Visual Charts | 10 Reports in `ReportsFrame` + Pure Java 2D Graphics Bar/Pie Charts | **100% Verified** |
| **Realistic Sample Data**| Realistic college data without dummy values | 6 depts, 18 users, 24 resources, 12 rooms, 8 slots, 30 bookings in `sample_data.sql` | **100% Verified** |
| **Comment-Free Code** | Code without unnecessary comments | All `.java` and `.sql` files 100% comment-free | **100% Verified** |

---

## 33. How to Run the Project

### Option A: 1-Click Execution via Windows Batch Scripts
1. **Initialize the Database:**
   If you have Python installed, run:
   ```powershell
   python setup_database.py --password YOUR_MYSQL_ROOT_PASSWORD
   ```
   *Alternatively*, run the SQL scripts in MySQL Workbench or MySQL Command Line Client:
   ```sql
   SOURCE database/schema.sql;
   SOURCE database/views.sql;
   SOURCE database/triggers.sql;
   SOURCE database/procedures.sql;
   SOURCE database/sample_data.sql;
   ```
2. **Configure Database Password:**
   Open `db.properties` and verify your credentials:
   ```properties
   db.url=jdbc:mysql://127.0.0.1:3306/campus_resource_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=YOUR_MYSQL_ROOT_PASSWORD
   ```
3. **Compile the Java Application:**
   Double-click `build.bat` or run in terminal:
   ```powershell
   .\build.bat
   ```
4. **Launch the Application:**
   Double-click `run.bat` or run in terminal:
   ```powershell
   .\run.bat
   ```

---

### Option B: Running in IntelliJ IDEA
1. Open IntelliJ IDEA -> Click **Open** -> Select the project folder:
   `c:\Users\KARTIK_BORADE\Desktop\SANJIVAVNI UNIVERSITY DOCUMENT\DBMS PROJECT`
2. Configure Project SDK:
   * Go to **File -> Project Structure -> Project**.
   * Set **SDK** to Java 17, 21, or 24.
3. Add the MySQL JDBC Driver to Libraries:
   * Go to **File -> Project Structure -> Libraries**.
   * Click the **+** button -> Select **Java**.
   * Navigate to `lib/mysql-connector-j-8.3.0.jar` -> Click **OK** -> Click **Apply**.
4. Set Source Directory:
   * Go to **File -> Project Structure -> Modules**.
   * Ensure `src` is marked as a **Sources** root.
5. Run the Application:
   * In the Project explorer, expand `src` -> Right-click `Main.java` -> Select **Run 'Main.main()'**.

---

### Option C: Running in Eclipse IDE
1. Open Eclipse IDE -> Click **File -> Import -> General -> Existing Projects into Workspace** (or create a New Java Project pointing to this folder).
2. Right-click the project folder in Package Explorer -> Select **Build Path -> Configure Build Path...**
3. Navigate to the **Libraries** tab -> Click **Add External JARs...**
4. Select `lib/mysql-connector-j-8.3.0.jar` from the project directory -> Click **Apply and Close**.
5. Locate `src/Main.java` -> Right-click -> Select **Run As -> Java Application**.

---

### Evaluation Accounts (1-Click Demo Logins)
* **Administrator:** `admin@sanjivani.edu.in` / `admin123`
* **Faculty:** `amit.cse@sanjivani.edu.in` / `faculty123`
* **Student:** `rohan.student@sanjivani.edu.in` / `student123`
* **Staff:** `suresh.staff@sanjivani.edu.in` / `staff123`
