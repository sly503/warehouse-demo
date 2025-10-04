-- Insert sample users with BCrypt encoded password "password123"
-- The BCrypt hash for "password123" is: $2a$10$N9qo8uLOickgx2ZMRZoMye.IjQ6v3jRgDHqFw6LqJaPgSPxFfLvLm

-- System Admin user
INSERT IGNORE INTO users (username, password, email, first_name, last_name, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
VALUES ('admin1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjQ6v3jRgDHqFw6LqJaPgSPxFfLvLm', 'admin1@warehouse.com', 'Admin', 'User', 'SYSTEM_ADMIN', true, true, true, true, NOW(), NOW());

-- Warehouse Manager user
INSERT IGNORE INTO users (username, password, email, first_name, last_name, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
VALUES ('manager1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjQ6v3jRgDHqFw6LqJaPgSPxFfLvLm', 'manager1@warehouse.com', 'Manager', 'User', 'WAREHOUSE_MANAGER', true, true, true, true, NOW(), NOW());

-- Client user
INSERT IGNORE INTO users (username, password, email, first_name, last_name, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
VALUES ('client1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjQ6v3jRgDHqFw6LqJaPgSPxFfLvLm', 'client1@warehouse.com', 'Client', 'User', 'CLIENT', true, true, true, true, NOW(), NOW());

-- Insert sample items with different volumes and prices
INSERT IGNORE INTO items (item_name, quantity, unit_price, package_volume, description, sku, created_at, updated_at)
VALUES
    ('Laptop Computer', 50, 899.99, 0.015, 'High-performance business laptop', 'SKU-LAPTOP-001', NOW(), NOW()),
    ('Office Chair', 120, 299.99, 0.25, 'Ergonomic office chair with lumbar support', 'SKU-CHAIR-001', NOW(), NOW()),
    ('Standing Desk', 30, 599.99, 0.35, 'Electric height-adjustable standing desk', 'SKU-DESK-001', NOW(), NOW()),
    ('Monitor 27"', 75, 399.99, 0.08, '27-inch 4K LED monitor', 'SKU-MONITOR-001', NOW(), NOW()),
    ('Wireless Mouse', 200, 29.99, 0.002, 'Bluetooth wireless optical mouse', 'SKU-MOUSE-001', NOW(), NOW()),
    ('Mechanical Keyboard', 150, 129.99, 0.006, 'RGB mechanical gaming keyboard', 'SKU-KEYBOARD-001', NOW(), NOW()),
    ('Printer All-in-One', 25, 499.99, 0.15, 'Multifunction color laser printer', 'SKU-PRINTER-001', NOW(), NOW()),
    ('External SSD 1TB', 100, 159.99, 0.001, 'Portable solid-state drive 1TB', 'SKU-SSD-001', NOW(), NOW()),
    ('Webcam HD', 80, 79.99, 0.003, 'Full HD 1080p webcam with microphone', 'SKU-WEBCAM-001', NOW(), NOW()),
    ('Office Phone', 40, 199.99, 0.005, 'VoIP business desk phone', 'SKU-PHONE-001', NOW(), NOW());

-- Insert sample trucks with different container volumes
INSERT IGNORE INTO trucks (chassis_number, license_plate, container_volume, available, driver_name, notes, created_at, updated_at)
VALUES
    ('CH-2024-001', 'LH-TR-001', 50.0, true, 'John Smith', 'Large capacity truck for bulk deliveries', NOW(), NOW()),
    ('CH-2024-002', 'LH-TR-002', 30.0, true, 'Maria Garcia', 'Medium capacity truck for regular deliveries', NOW(), NOW()),
    ('CH-2024-003', 'LH-TR-003', 20.0, true, 'David Johnson', 'Small capacity truck for express deliveries', NOW(), NOW());