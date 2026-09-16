USE campus_resource_booking;

DELIMITER $$

DROP TRIGGER IF EXISTS before_booking_insert$$
CREATE TRIGGER before_booking_insert
BEFORE INSERT ON bookings
FOR EACH ROW
BEGIN
    DECLARE v_res_status VARCHAR(20);
    DECLARE v_conflict_count INT DEFAULT 0;

    SELECT status INTO v_res_status
    FROM resources
    WHERE resource_id = NEW.resource_id;

    IF v_res_status = 'MAINTENANCE' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource is currently under maintenance and cannot be booked.';
    END IF;

    IF v_res_status = 'INACTIVE' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Resource is inactive and cannot be booked.';
    END IF;

    SELECT COUNT(*) INTO v_conflict_count
    FROM bookings
    WHERE resource_id = NEW.resource_id
      AND booking_date = NEW.booking_date
      AND slot_id = NEW.slot_id
      AND booking_status IN ('PENDING', 'APPROVED');

    IF v_conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Double-booking conflict: This resource is already booked for the selected date and time slot.';
    END IF;
END$$

DROP TRIGGER IF EXISTS after_booking_insert$$
CREATE TRIGGER after_booking_insert
AFTER INSERT ON bookings
FOR EACH ROW
BEGIN
    IF NEW.booking_status = 'APPROVED' THEN
        UPDATE resources
        SET status = 'BOOKED'
        WHERE resource_id = NEW.resource_id;
    END IF;
END$$

DROP TRIGGER IF EXISTS after_booking_update$$
CREATE TRIGGER after_booking_update
AFTER UPDATE ON bookings
FOR EACH ROW
BEGIN
    DECLARE v_remaining_active INT DEFAULT 0;

    IF NEW.booking_status = 'APPROVED' AND OLD.booking_status <> 'APPROVED' THEN
        UPDATE resources
        SET status = 'BOOKED'
        WHERE resource_id = NEW.resource_id;
    END IF;

    IF NEW.booking_status IN ('CANCELLED', 'REJECTED', 'COMPLETED') AND OLD.booking_status IN ('PENDING', 'APPROVED') THEN
        SELECT COUNT(*) INTO v_remaining_active
        FROM bookings
        WHERE resource_id = NEW.resource_id
          AND booking_status IN ('PENDING', 'APPROVED')
          AND booking_id <> NEW.booking_id;

        IF v_remaining_active = 0 THEN
            UPDATE resources
            SET status = 'AVAILABLE'
            WHERE resource_id = NEW.resource_id AND status = 'BOOKED';
        END IF;
    END IF;
END$$

DROP TRIGGER IF EXISTS after_maintenance_update$$
CREATE TRIGGER after_maintenance_update
AFTER UPDATE ON maintenance
FOR EACH ROW
BEGIN
    IF NEW.maintenance_status = 'Completed' AND OLD.maintenance_status <> 'Completed' THEN
        IF NEW.resource_id IS NOT NULL THEN
            UPDATE resources
            SET status = 'AVAILABLE'
            WHERE resource_id = NEW.resource_id;
        END IF;
        IF NEW.room_id IS NOT NULL THEN
            UPDATE rooms
            SET status = 'AVAILABLE'
            WHERE room_id = NEW.room_id;
        END IF;
    ELSEIF NEW.maintenance_status = 'In Progress' THEN
        IF NEW.resource_id IS NOT NULL THEN
            UPDATE resources
            SET status = 'MAINTENANCE'
            WHERE resource_id = NEW.resource_id;
        END IF;
        IF NEW.room_id IS NOT NULL THEN
            UPDATE rooms
            SET status = 'MAINTENANCE'
            WHERE room_id = NEW.room_id;
        END IF;
    END IF;
END$$

DELIMITER ;
