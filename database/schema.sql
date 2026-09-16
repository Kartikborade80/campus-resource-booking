CREATE DATABASE IF NOT EXISTS campus_resource_booking
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE campus_resource_booking;

DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS maintenance;
DROP TABLE IF EXISTS booking_participants;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS time_slots;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS resource_categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS departments;

CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    department_code VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    CONSTRAINT chk_department_status CHECK (status IN ('Active', 'Inactive'))
) ENGINE=InnoDB;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    department_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'FACULTY', 'STAFF', 'STUDENT')),
    CONSTRAINT chk_user_status CHECK (status IN ('Active', 'Inactive', 'Suspended')),
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments(department_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE resource_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NULL
) ENGINE=InnoDB;

CREATE TABLE resources (
    resource_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    resource_name VARCHAR(120) NOT NULL,
    resource_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT NULL,
    capacity INT NOT NULL DEFAULT 1,
    location VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    purchase_date DATE NULL,
    CONSTRAINT chk_resource_capacity CHECK (capacity >= 1),
    CONSTRAINT chk_resource_status CHECK (status IN ('AVAILABLE', 'BOOKED', 'MAINTENANCE', 'INACTIVE')),
    CONSTRAINT fk_resources_category
        FOREIGN KEY (category_id) REFERENCES resource_categories(category_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(50) NOT NULL UNIQUE,
    building_name VARCHAR(100) NOT NULL,
    floor_number INT NOT NULL DEFAULT 0,
    capacity INT NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    facilities TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_room_capacity CHECK (capacity > 0),
    CONSTRAINT chk_room_status CHECK (status IN ('AVAILABLE', 'BOOKED', 'MAINTENANCE', 'INACTIVE')),
    CONSTRAINT chk_room_type CHECK (room_type IN ('Classroom', 'Computer Lab', 'Seminar Hall', 'Auditorium', 'Conference Room'))
) ENGINE=InnoDB;

CREATE TABLE time_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    slot_name VARCHAR(50) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    CONSTRAINT chk_slot_time CHECK (start_time < end_time),
    CONSTRAINT chk_slot_status CHECK (status IN ('Active', 'Inactive'))
) ENGINE=InnoDB;

CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    resource_id INT NOT NULL,
    room_id INT NULL,
    slot_id INT NOT NULL,
    booking_date DATE NOT NULL,
    purpose TEXT NOT NULL,
    booking_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_booking_status CHECK (booking_status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_bookings_resource
        FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_bookings_room
        FOREIGN KEY (room_id) REFERENCES rooms(room_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_bookings_slot
        FOREIGN KEY (slot_id) REFERENCES time_slots(slot_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_bookings_approver
        FOREIGN KEY (approved_by) REFERENCES users(user_id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE booking_participants (
    participant_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    user_id INT NOT NULL,
    participation_role VARCHAR(50) NOT NULL DEFAULT 'Attendee',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_booking_user UNIQUE (booking_id, user_id),
    CONSTRAINT fk_participants_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_participants_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE maintenance (
    maintenance_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id INT NULL,
    room_id INT NULL,
    reported_by INT NOT NULL,
    issue_title VARCHAR(150) NOT NULL,
    issue_description TEXT NOT NULL,
    maintenance_date DATE NOT NULL,
    completion_date DATE NULL,
    maintenance_status VARCHAR(20) NOT NULL DEFAULT 'Reported',
    cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT chk_maintenance_status CHECK (maintenance_status IN ('Reported', 'In Progress', 'Completed', 'Cancelled')),
    CONSTRAINT chk_maintenance_cost CHECK (cost >= 0.00),
    CONSTRAINT fk_maintenance_resource
        FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_maintenance_room
        FOREIGN KEY (room_id) REFERENCES rooms(room_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_maintenance_reporter
        FOREIGN KEY (reported_by) REFERENCES users(user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL,
    comments TEXT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rating_range CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_feedback_booking UNIQUE (booking_id),
    CONSTRAINT fk_feedback_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_feedback_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_bookings_date_slot ON bookings (booking_date, slot_id);
CREATE INDEX idx_bookings_user ON bookings (user_id);
CREATE INDEX idx_resources_category ON resources (category_id);
CREATE INDEX idx_resources_status ON resources (status);
CREATE INDEX idx_rooms_status ON rooms (status);
CREATE INDEX idx_maintenance_status ON maintenance (maintenance_status);
