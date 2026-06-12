-- ============================================================
-- V2: Insert built-in accounts, RBAC seed data
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

-- ============================================================
-- Seed: Roles
-- ============================================================
INSERT INTO roles (id, name, description) VALUES
    ('r-00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN', '超级管理员 - 拥有所有权限，包括角色管理'),
    ('r-00000000-0000-0000-0000-000000000002', 'ADMIN', '管理员 - 管理商品和订单，无角色管理权限'),
    ('r-00000000-0000-0000-0000-000000000003', 'USER', '普通用户 - 可发布商品、购买、收藏和评价');

-- ============================================================
-- Seed: Permissions
-- ============================================================
-- Role management
INSERT INTO permissions (id, name, description, module) VALUES
    ('p-00000000-0000-0000-0000-000000000001', 'ROLE_READ',   '查看角色列表',       'ROLE_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000002', 'ROLE_CREATE', '创建角色',           'ROLE_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000003', 'ROLE_UPDATE', '修改角色及权限分配', 'ROLE_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000004', 'ROLE_DELETE', '删除角色',           'ROLE_MANAGEMENT');

-- User management
INSERT INTO permissions (id, name, description, module) VALUES
    ('p-00000000-0000-0000-0000-000000000010', 'USER_READ',        '查看用户列表',     'USER_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000011', 'USER_UPDATE',      '修改用户信息',     'USER_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000012', 'USER_ROLE_ASSIGN', '分配用户角色',     'USER_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000013', 'USER_DISABLE',     '禁用/启用用户',    'USER_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000014', 'USER_DELETE',      '删除用户',         'USER_MANAGEMENT');

-- Product management (admin-level)
INSERT INTO permissions (id, name, description, module) VALUES
    ('p-00000000-0000-0000-0000-000000000020', 'PRODUCT_READ_ALL',  '查看全站商品',       'PRODUCT_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000021', 'PRODUCT_CREATE',    '创建平台商品',       'PRODUCT_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000022', 'PRODUCT_UPDATE_ALL','编辑任意商品',       'PRODUCT_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000023', 'PRODUCT_DELETE_ALL','删除任意商品',       'PRODUCT_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000024', 'PRODUCT_APPROVE',   '审核商品（通过/驳回）','PRODUCT_MANAGEMENT');

-- Order management
INSERT INTO permissions (id, name, description, module) VALUES
    ('p-00000000-0000-0000-0000-000000000030', 'ORDER_READ_ALL',    '查看所有用户订单',   'ORDER_MANAGEMENT'),
    ('p-00000000-0000-0000-0000-000000000031', 'ORDER_UPDATE_STATUS','更新订单状态（发货等）','ORDER_MANAGEMENT');

-- Own product management (user-level)
INSERT INTO permissions (id, name, description, module) VALUES
    ('p-00000000-0000-0000-0000-000000000040', 'PRODUCT_CREATE_OWN','发布自己的商品',     'PRODUCT_OWN'),
    ('p-00000000-0000-0000-0000-000000000041', 'PRODUCT_UPDATE_OWN','编辑自己的商品',     'PRODUCT_OWN'),
    ('p-00000000-0000-0000-0000-000000000042', 'PRODUCT_DELETE_OWN','删除自己的商品',     'PRODUCT_OWN');

-- Shopping
INSERT INTO permissions (id, name, description, module) VALUES
    ('p-00000000-0000-0000-0000-000000000050', 'ORDER_CREATE',    '下单购买',           'SHOPPING'),
    ('p-00000000-0000-0000-0000-000000000051', 'FAVORITE_MANAGE', '管理收藏夹',         'SHOPPING'),
    ('p-00000000-0000-0000-0000-000000000052', 'REVIEW_CREATE',   '发表评价',           'SHOPPING');

-- ============================================================
-- Seed: Role-Permissions mapping
-- ============================================================
-- SUPER_ADMIN gets ALL permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'r-00000000-0000-0000-0000-000000000001', id FROM permissions;

-- ADMIN gets everything except ROLE_MANAGEMENT
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'r-00000000-0000-0000-0000-000000000002', id
FROM permissions
WHERE module != 'ROLE_MANAGEMENT';

-- USER gets own-product + shopping permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('r-00000000-0000-0000-0000-000000000003', 'p-00000000-0000-0000-0000-000000000040'),
    ('r-00000000-0000-0000-0000-000000000003', 'p-00000000-0000-0000-0000-000000000041'),
    ('r-00000000-0000-0000-0000-000000000003', 'p-00000000-0000-0000-0000-000000000042'),
    ('r-00000000-0000-0000-0000-000000000003', 'p-00000000-0000-0000-0000-000000000050'),
    ('r-00000000-0000-0000-0000-000000000003', 'p-00000000-0000-0000-0000-000000000051'),
    ('r-00000000-0000-0000-0000-000000000003', 'p-00000000-0000-0000-0000-000000000052');

-- ============================================================
-- Migrate: existing users.role -> user_roles
-- ============================================================
-- SuperAdmin -> SUPER_ADMIN role
INSERT INTO user_roles (user_id, role_id)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'r-00000000-0000-0000-0000-000000000001');

-- Other ADMIN users -> ADMIN role (non-protected admins)
INSERT INTO user_roles (user_id, role_id)
SELECT id, 'r-00000000-0000-0000-0000-000000000002'
FROM users WHERE role = 'ADMIN' AND is_protected = false;

-- All USER role users -> USER role
INSERT INTO user_roles (user_id, role_id)
SELECT id, 'r-00000000-0000-0000-0000-000000000003'
FROM users WHERE role = 'USER';
