USE campus_resource_booking;

DROP VIEW IF EXISTS booking_details_view;
CREATE VIEW booking_details_view AS
SELECT 
    b.booking_id,
    u.name AS user_name,
    u.email AS user_email,
    u.role AS user_role,
    d.department_name,
    r.resource_name,
    r.resource_code,
    rc.category_name AS resource_category,
    COALESCE(rm.room_number, 'N/A') AS room_number,
    COALESCE(rm.building_name, 'N/A') AS building_name,
    ts.slot_name AS time_slot,
    ts.start_time,
    ts.end_time,
    b.booking_date,
    b.purpose,
    b.booking_status,
    COALESCE(appr.name, 'Pending Approval') AS approved_by_name,
    b.created_at
FROM bookings b
INNER JOIN users u ON b.user_id = u.user_id
INNER JOIN departments d ON u.department_id = d.department_id
INNER JOIN resources r ON b.resource_id = r.resource_id
INNER JOIN resource_categories rc ON r.category_id = rc.category_id
LEFT JOIN rooms rm ON b.room_id = rm.room_id
INNER JOIN time_slots ts ON b.slot_id = ts.slot_id
LEFT JOIN users appr ON b.approved_by = appr.user_id;

DROP VIEW IF EXISTS room_utilization_view;
CREATE VIEW room_utilization_view AS
SELECT 
    rm.room_id,
    rm.room_number,
    rm.building_name,
    rm.room_type,
    rm.capacity,
    COUNT(b.booking_id) AS total_bookings,
    COUNT(CASE WHEN b.booking_status = 'APPROVED' THEN 1 END) AS approved_bookings,
    COUNT(CASE WHEN b.booking_status = 'COMPLETED' THEN 1 END) AS completed_bookings,
    COUNT(CASE WHEN b.booking_status = 'CANCELLED' THEN 1 END) AS cancelled_bookings
FROM rooms rm
LEFT JOIN bookings b ON rm.room_id = b.room_id
GROUP BY rm.room_id, rm.room_number, rm.building_name, rm.room_type, rm.capacity;

DROP VIEW IF EXISTS resource_summary_view;
CREATE VIEW resource_summary_view AS
SELECT 
    rc.category_name,
    COUNT(r.resource_id) AS total_resources,
    COUNT(CASE WHEN r.status = 'AVAILABLE' THEN 1 END) AS available_count,
    COUNT(CASE WHEN r.status = 'BOOKED' THEN 1 END) AS booked_count,
    COUNT(CASE WHEN r.status = 'MAINTENANCE' THEN 1 END) AS maintenance_count,
    COUNT(CASE WHEN r.status = 'INACTIVE' THEN 1 END) AS inactive_count
FROM resource_categories rc
LEFT JOIN resources r ON rc.category_id = r.category_id
GROUP BY rc.category_id, rc.category_name;
