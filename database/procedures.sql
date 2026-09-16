USE campus_resource_booking;

DELIMITER $$

DROP PROCEDURE IF EXISTS book_resource$$
CREATE PROCEDURE book_resource(
    IN p_user_id INT,
    IN p_resource_id INT,
    IN p_room_id INT,
    IN p_slot_id INT,
    IN p_booking_date DATE,
    IN p_purpose TEXT,
    OUT p_status_code INT,
    OUT p_message VARCHAR(255),
    OUT p_booking_id INT
)
proc_label: BEGIN
    DECLARE v_res_exists INT DEFAULT 0;
    DECLARE v_res_status VARCHAR(20);
    DECLARE v_res_name VARCHAR(120);
    DECLARE v_room_status VARCHAR(20);
    DECLARE v_user_role VARCHAR(20);
    DECLARE v_conflict_count INT DEFAULT 0;
    DECLARE v_room_conflict INT DEFAULT 0;
    DECLARE v_initial_status VARCHAR(20) DEFAULT 'PENDING';

    SET p_status_code = 1;
    SET p_message = 'Initialization error';
    SET p_booking_id = 0;

    SELECT role INTO v_user_role 
    FROM users 
    WHERE user_id = p_user_id AND status = 'Active';

    IF v_user_role IS NULL THEN
        SET p_status_code = 101;
        SET p_message = 'User does not exist or account is inactive.';
        LEAVE proc_label;
    END IF;

    SELECT resource_name, status INTO v_res_name, v_res_status
    FROM resources
    WHERE resource_id = p_resource_id;

    IF v_res_status IS NULL THEN
        SET p_status_code = 102;
        SET p_message = 'Requested resource does not exist.';
        LEAVE proc_label;
    END IF;

    IF v_res_status = 'MAINTENANCE' THEN
        SET p_status_code = 103;
        SET p_message = CONCAT('Resource "', v_res_name, '" is currently under maintenance.');
        LEAVE proc_label;
    END IF;

    IF v_res_status = 'INACTIVE' THEN
        SET p_status_code = 104;
        SET p_message = CONCAT('Resource "', v_res_name, '" is decommissioned / inactive.');
        LEAVE proc_label;
    END IF;

    IF p_room_id IS NOT NULL AND p_room_id > 0 THEN
        SELECT status INTO v_room_status
        FROM rooms
        WHERE room_id = p_room_id;

        IF v_room_status IS NULL THEN
            SET p_status_code = 105;
            SET p_message = 'Specified room venue does not exist.';
            LEAVE proc_label;
        END IF;

        IF v_room_status = 'MAINTENANCE' THEN
            SET p_status_code = 106;
            SET p_message = 'Selected room is currently under maintenance.';
            LEAVE proc_label;
        END IF;

        IF v_room_status = 'INACTIVE' THEN
            SET p_status_code = 107;
            SET p_message = 'Selected room is currently inactive.';
            LEAVE proc_label;
        END IF;

        SELECT COUNT(*) INTO v_room_conflict
        FROM bookings
        WHERE room_id = p_room_id
          AND booking_date = p_booking_date
          AND slot_id = p_slot_id
          AND booking_status IN ('PENDING', 'APPROVED');

        IF v_room_conflict > 0 THEN
            SET p_status_code = 108;
            SET p_message = 'Room conflict: The venue is already reserved for this date and time slot.';
            LEAVE proc_label;
        END IF;
    ELSE
        SET p_room_id = NULL;
    END IF;

    SELECT COUNT(*) INTO v_conflict_count
    FROM bookings
    WHERE resource_id = p_resource_id
      AND booking_date = p_booking_date
      AND slot_id = p_slot_id
      AND booking_status IN ('PENDING', 'APPROVED');

    IF v_conflict_count > 0 THEN
        SET p_status_code = 109;
        SET p_message = CONCAT('Resource conflict: "', v_res_name, '" is already booked for this time slot.');
        LEAVE proc_label;
    END IF;

    IF v_user_role IN ('ADMIN', 'FACULTY') THEN
        SET v_initial_status = 'APPROVED';
    ELSE
        SET v_initial_status = 'PENDING';
    END IF;

    START TRANSACTION;

    INSERT INTO bookings (
        user_id, resource_id, room_id, slot_id,
        booking_date, purpose, booking_status, approved_by
    ) VALUES (
        p_user_id, p_resource_id, p_room_id, p_slot_id,
        p_booking_date, p_purpose, v_initial_status,
        CASE WHEN v_initial_status = 'APPROVED' THEN p_user_id ELSE NULL END
    );

    SET p_booking_id = LAST_INSERT_ID();

    INSERT INTO booking_participants (booking_id, user_id, participation_role)
    VALUES (p_booking_id, p_user_id, 'Organizer');

    IF v_initial_status = 'APPROVED' THEN
        UPDATE resources SET status = 'BOOKED' WHERE resource_id = p_resource_id;
    END IF;

    COMMIT;

    SET p_status_code = 0;
    IF v_initial_status = 'APPROVED' THEN
        SET p_message = 'Booking confirmed and approved automatically.';
    ELSE
        SET p_message = 'Booking submitted successfully. Pending administrator approval.';
    END IF;
END$$

DROP PROCEDURE IF EXISTS cancel_booking$$
CREATE PROCEDURE cancel_booking(
    IN p_booking_id INT,
    IN p_user_id INT,
    OUT p_status_code INT,
    OUT p_message VARCHAR(255)
)
proc_label: BEGIN
    DECLARE v_curr_status VARCHAR(20);
    DECLARE v_owner_id INT;
    DECLARE v_user_role VARCHAR(20);
    DECLARE v_resource_id INT;

    SET p_status_code = 1;
    SET p_message = 'Unable to process cancellation.';

    SELECT role INTO v_user_role FROM users WHERE user_id = p_user_id;
    SELECT booking_status, user_id, resource_id 
    INTO v_curr_status, v_owner_id, v_resource_id
    FROM bookings
    WHERE booking_id = p_booking_id;

    IF v_curr_status IS NULL THEN
        SET p_status_code = 201;
        SET p_message = 'Booking not found.';
        LEAVE proc_label;
    END IF;

    IF v_user_role <> 'ADMIN' AND v_owner_id <> p_user_id THEN
        SET p_status_code = 202;
        SET p_message = 'Unauthorized: You can only cancel your own bookings.';
        LEAVE proc_label;
    END IF;

    IF v_curr_status IN ('CANCELLED', 'REJECTED', 'COMPLETED') THEN
        SET p_status_code = 203;
        SET p_message = CONCAT('Cannot cancel: Booking is already ', v_curr_status);
        LEAVE proc_label;
    END IF;

    START TRANSACTION;

    UPDATE bookings
    SET booking_status = 'CANCELLED'
    WHERE booking_id = p_booking_id;

    UPDATE resources
    SET status = 'AVAILABLE'
    WHERE resource_id = v_resource_id AND status = 'BOOKED';

    COMMIT;

    SET p_status_code = 0;
    SET p_message = 'Booking has been successfully cancelled.';
END$$

DROP PROCEDURE IF EXISTS get_department_booking_stats$$
CREATE PROCEDURE get_department_booking_stats(
    IN p_department_id INT
)
BEGIN
    SELECT 
        d.department_name,
        d.department_code,
        COUNT(b.booking_id) AS total_bookings,
        COUNT(CASE WHEN b.booking_status = 'APPROVED' THEN 1 END) AS approved_count,
        COUNT(CASE WHEN b.booking_status = 'PENDING' THEN 1 END) AS pending_count,
        COUNT(CASE WHEN b.booking_status = 'COMPLETED' THEN 1 END) AS completed_count,
        COUNT(CASE WHEN b.booking_status = 'CANCELLED' THEN 1 END) AS cancelled_count
    FROM departments d
    LEFT JOIN users u ON d.department_id = u.department_id
    LEFT JOIN bookings b ON u.user_id = b.user_id
    WHERE (p_department_id IS NULL OR d.department_id = p_department_id)
    GROUP BY d.department_id, d.department_name, d.department_code
    ORDER BY total_bookings DESC;
END$$

DELIMITER ;
