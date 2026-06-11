-- ============================================================
-- V2: Insert built-in accounts
-- Passwords are placeholders; DataInitializer will replace them
-- with properly encoded hashes on application startup.
-- ============================================================

-- Super Admin (cannot be renamed, re-passworded, or deleted)
INSERT INTO users (id, email, username, password, role, balance, is_protected) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'admin@merchandise.com', 'SuperAdmin', '{noop}placeholder', 'ADMIN', 0.0, true);

-- Test Merchant (seller of all built-in test products)
INSERT INTO users (id, email, username, password, role, balance, is_protected) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'merchant@merchandise.com', 'OfficialMerchant', '{noop}placeholder', 'USER', 0.0, true);

-- Test User (with $10,000,000 balance for testing)
INSERT INTO users (id, email, username, password, role, balance, is_protected) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'testuser@merchandise.com', 'TestUser', '{noop}placeholder', 'USER', 10000000.0, true);
