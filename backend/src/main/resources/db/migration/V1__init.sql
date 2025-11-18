-- ==============================================
-- Appointment Scheduling System - Initial Schema
-- MySQL 8.0+
-- ==============================================

-- Users table - Core authentication
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('USER', 'STAFF', 'ADMIN') NOT NULL DEFAULT 'USER',
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Staff profiles - Extended information for staff members
CREATE TABLE staff_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    title VARCHAR(100),
    bio TEXT,
    specialization VARCHAR(255),
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    average_rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_available (is_available)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Services catalog
CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_active (is_active),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Staff services - Many-to-many relationship
CREATE TABLE staff_services (
    staff_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    PRIMARY KEY (staff_id, service_id),
    FOREIGN KEY (staff_id) REFERENCES staff_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Staff working hours - Weekly schedule
CREATE TABLE staff_working_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    day_of_week TINYINT NOT NULL COMMENT '0=Sunday, 6=Saturday',
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (staff_id) REFERENCES staff_profiles(id) ON DELETE CASCADE,
    INDEX idx_staff_day (staff_id, day_of_week),
    INDEX idx_active (is_active),
    CHECK (day_of_week BETWEEN 0 AND 6),
    CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Staff breaks - Scheduled breaks (recurring or one-time)
CREATE TABLE staff_breaks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    break_type ENUM('RECURRING', 'ONE_TIME') NOT NULL DEFAULT 'RECURRING',
    start_time DATETIME NOT NULL COMMENT 'UTC timestamp',
    end_time DATETIME NOT NULL COMMENT 'UTC timestamp',
    day_of_week TINYINT COMMENT 'For recurring breaks: 0=Sunday, 6=Saturday',
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (staff_id) REFERENCES staff_profiles(id) ON DELETE CASCADE,
    INDEX idx_staff_time (staff_id, start_time, end_time),
    INDEX idx_type (break_type),
    CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Appointments - Core booking table with optimistic locking
CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL COMMENT 'UTC timestamp',
    end_time DATETIME NOT NULL COMMENT 'UTC timestamp',
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW') NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    cancellation_reason VARCHAR(500),
    external_event_id VARCHAR(500) COMMENT 'ID from Google/MS Calendar',
    version INT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES staff_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    INDEX idx_user_time (user_id, start_time),
    INDEX idx_staff_time (staff_id, start_time),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_external_event (external_event_id(255)),
    CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OAuth tokens - Store calendar integration credentials
CREATE TABLE oauth_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider ENUM('GOOGLE', 'MICROSOFT') NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT,
    token_type VARCHAR(50) NOT NULL DEFAULT 'Bearer',
    expires_at TIMESTAMP NOT NULL,
    scope TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_provider (user_id, provider),
    INDEX idx_user_provider (user_id, provider),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Calendar events - Track synced events
CREATE TABLE calendar_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    provider ENUM('GOOGLE', 'MICROSOFT') NOT NULL,
    external_event_id VARCHAR(500) NOT NULL,
    calendar_id VARCHAR(255) NOT NULL,
    last_synced_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sync_status ENUM('SYNCED', 'PENDING', 'FAILED') NOT NULL DEFAULT 'SYNCED',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    UNIQUE KEY unique_provider_event (provider, external_event_id(255)),
    INDEX idx_appointment (appointment_id),
    INDEX idx_external_event (external_event_id(255)),
    INDEX idx_sync_status (sync_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Reminders - Scheduled notifications
CREATE TABLE reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    type ENUM('EMAIL', 'SMS', 'PUSH') NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    scheduled_time DATETIME NOT NULL COMMENT 'UTC timestamp',
    sent_at TIMESTAMP NULL,
    status ENUM('PENDING', 'SENT', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    INDEX idx_scheduled (scheduled_time, status),
    INDEX idx_appointment (appointment_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Webhook subscriptions - Track active webhooks
CREATE TABLE webhook_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider ENUM('GOOGLE', 'MICROSOFT') NOT NULL,
    resource_id VARCHAR(500) NOT NULL,
    channel_id VARCHAR(500) NOT NULL,
    expiration_time TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_provider (user_id, provider),
    INDEX idx_channel (channel_id(255)),
    INDEX idx_expiration (expiration_time, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Activity log - Audit trail
CREATE TABLE activity_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_value JSON,
    new_value JSON,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Refresh tokens - For JWT token refresh
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_token (token(255)),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================
-- Seed Data
-- ==============================================

-- Insert sample services
INSERT INTO services (name, description, duration_minutes, price, is_active) VALUES
('General Consultation', '30-minute general consultation', 30, 50.00, TRUE),
('Extended Consultation', '60-minute extended consultation', 60, 90.00, TRUE),
('Follow-up Appointment', '15-minute follow-up', 15, 25.00, TRUE),
('Initial Assessment', '45-minute initial assessment', 45, 75.00, TRUE);

-- Insert admin user (password: Admin@123 - hashed with BCrypt)
-- Note: In production, generate this hash securely
INSERT INTO users (email, password_hash, role, first_name, last_name, phone, timezone, is_active, email_verified) VALUES
('admin@appointments.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'System', 'Admin', '+1234567890', 'UTC', TRUE, TRUE);

-- ==============================================
-- Performance Optimization
-- ==============================================

-- Composite indexes for common queries
CREATE INDEX idx_appointments_staff_status_time ON appointments(staff_id, status, start_time);
CREATE INDEX idx_appointments_user_status_time ON appointments(user_id, status, start_time);

-- ==============================================
-- Views for Analytics
-- ==============================================

-- Daily appointment statistics
CREATE OR REPLACE VIEW daily_appointment_stats AS
SELECT 
    DATE(start_time) as appointment_date,
    COUNT(*) as total_appointments,
    COUNT(DISTINCT user_id) as unique_users,
    COUNT(DISTINCT staff_id) as active_staff,
    SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled,
    SUM(CASE WHEN status = 'NO_SHOW' THEN 1 ELSE 0 END) as no_shows
FROM appointments
GROUP BY DATE(start_time);

-- Staff workload view
CREATE OR REPLACE VIEW staff_workload AS
SELECT 
    s.id as staff_id,
    u.first_name,
    u.last_name,
    COUNT(a.id) as total_appointments,
    AVG(TIMESTAMPDIFF(MINUTE, a.start_time, a.end_time)) as avg_appointment_duration,
    SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_count,
    SUM(CASE WHEN a.status = 'NO_SHOW' THEN 1 ELSE 0 END) as no_show_count,
    s.average_rating
FROM staff_profiles s
JOIN users u ON s.user_id = u.id
LEFT JOIN appointments a ON s.id = a.staff_id
GROUP BY s.id, u.first_name, u.last_name, s.average_rating;
