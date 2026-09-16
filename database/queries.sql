USE campus_resource_booking;

SELECT 
    b.booking_id,
    u.name AS user_name,
    u.email,
    u.role,
    d.department_name,
    b.booking_date,
    b.booking_status
FROM bookings b
INNER JOIN users u ON b.user_id = u.user_id
INNER JOIN departments d ON u.department_id = d.department_id;

SELECT 
    b.booking_id,
    r.resource_name,
    r.resource_code,
    rc.category_name,
    b.booking_date,
    b.booking_status
FROM bookings b
INNER JOIN resources r ON b.resource_id = r.resource_id
INNER JOIN resource_categories rc ON r.category_id = rc.category_id;

SELECT 
    b.booking_id,
    COALESCE(rm.room_number, 'Portable / Field') AS room_number,
    COALESCE(rm.building_name, 'Campus Wide') AS building_name,
    rm.room_type,
    ts.slot_name,
    ts.start_time,
    ts.end_time,
    b.booking_date
FROM bookings b
LEFT JOIN rooms rm ON b.room_id = rm.room_id
INNER JOIN time_slots ts ON b.slot_id = ts.slot_id;

SELECT 
    b.booking_id,
    u.name AS reserved_by,
    d.department_name,
    r.resource_name,
    rc.category_name,
    COALESCE(rm.room_number, 'External') AS room,
    ts.slot_name AS session_slot,
    b.booking_date,
    b.purpose,
    b.booking_status
FROM bookings b
INNER JOIN users u ON b.user_id = u.user_id
INNER JOIN departments d ON u.department_id = d.department_id
INNER JOIN resources r ON b.resource_id = r.resource_id
INNER JOIN resource_categories rc ON r.category_id = rc.category_id
LEFT JOIN rooms rm ON b.room_id = rm.room_id
INNER JOIN time_slots ts ON b.slot_id = ts.slot_id;

SELECT 
    m.maintenance_id,
    m.issue_title,
    COALESCE(r.resource_name, 'Venue Infrastructure') AS resource_name,
    COALESCE(rm.room_number, 'Independent Asset') AS room_number,
    m.maintenance_status,
    m.cost,
    u.name AS reported_by_name
FROM maintenance m
LEFT JOIN resources r ON m.resource_id = r.resource_id
LEFT JOIN rooms rm ON m.room_id = rm.room_id
INNER JOIN users u ON m.reported_by = u.user_id;

SELECT 
    r.resource_id,
    r.resource_name,
    r.resource_code,
    rc.category_name,
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

SELECT 
    u.user_id,
    u.name,
    u.email,
    d.department_name,
    COUNT(b.booking_id) AS user_booking_count
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

SELECT 
    r.resource_id,
    r.resource_name,
    r.resource_code,
    rc.category_name,
    r.location,
    r.status
FROM resources r
INNER JOIN resource_categories rc ON r.category_id = rc.category_id
WHERE r.resource_id NOT IN (
    SELECT DISTINCT resource_id FROM bookings
);

SELECT 
    rm.room_id,
    rm.room_number,
    rm.building_name,
    rm.capacity,
    COUNT(b.booking_id) AS room_booking_count
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

SELECT 
    d.department_id,
    d.department_name,
    d.department_code,
    COUNT(b.booking_id) AS total_bookings
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

SELECT 
    r.resource_id,
    r.resource_name,
    r.status AS resource_status,
    b.booking_id,
    b.booking_date,
    b.booking_status
FROM resources r
LEFT JOIN bookings b ON r.resource_id = b.resource_id
ORDER BY r.resource_id, b.booking_date DESC;

SELECT 
    d.department_name,
    COUNT(b.booking_id) AS total_bookings,
    COUNT(CASE WHEN b.booking_status = 'APPROVED' THEN 1 END) AS approved_count,
    COUNT(CASE WHEN b.booking_status = 'COMPLETED' THEN 1 END) AS completed_count,
    COUNT(CASE WHEN b.booking_status = 'PENDING' THEN 1 END) AS pending_count
FROM departments d
LEFT JOIN users u ON d.department_id = u.department_id
LEFT JOIN bookings b ON u.user_id = b.user_id
GROUP BY d.department_id, d.department_name
ORDER BY total_bookings DESC;

SELECT 
    rm.room_number,
    rm.building_name,
    rm.room_type,
    COUNT(b.booking_id) AS booking_count
FROM rooms rm
INNER JOIN bookings b ON rm.room_id = b.room_id
GROUP BY rm.room_id, rm.room_number, rm.building_name, rm.room_type
HAVING COUNT(b.booking_id) >= 2
ORDER BY booking_count DESC;

SELECT 
    rc.category_name,
    COUNT(m.maintenance_id) AS incident_count,
    COALESCE(SUM(m.cost), 0.00) AS total_expenditure
FROM resource_categories rc
LEFT JOIN resources r ON rc.category_id = r.category_id
LEFT JOIN maintenance m ON r.resource_id = m.resource_id
GROUP BY rc.category_id, rc.category_name
ORDER BY total_expenditure DESC;

SELECT 
    r.resource_id,
    r.resource_name,
    rc.category_name,
    COUNT(f.feedback_id) AS reviews_count,
    ROUND(AVG(f.rating), 2) AS average_rating
FROM resources r
INNER JOIN resource_categories rc ON r.category_id = rc.category_id
INNER JOIN bookings b ON r.resource_id = b.resource_id
INNER JOIN feedback f ON b.booking_id = f.booking_id
GROUP BY r.resource_id, r.resource_name, rc.category_name
ORDER BY average_rating DESC;
