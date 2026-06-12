-- V4__simplify_rbac.sql
-- Simplify RBAC: replace ManyToMany roles with single role enum column

-- 1. Add new role column
ALTER TABLE users ADD COLUMN IF NOT EXISTS new_role VARCHAR(20);

-- 2. Migrate data from user_roles + roles to new_role
UPDATE users u SET new_role = COALESCE(
    (SELECT CASE r.name
        WHEN 'SUPER_ADMIN' THEN 'SUPER_ADMIN'
        WHEN 'ADMIN' THEN 'ADMIN'
        WHEN 'MERCHANT' THEN 'MERCHANT'
        ELSE 'USER'
    END
    FROM user_roles ur
    JOIN roles r ON ur.role_id = r.id
    WHERE ur.user_id = u.id
    ORDER BY CASE r.name
        WHEN 'SUPER_ADMIN' THEN 0
        WHEN 'ADMIN' THEN 1
        WHEN 'MERCHANT' THEN 2
        ELSE 3
    END
    LIMIT 1),
    CASE WHEN u.role = 'ADMIN' THEN 'SUPER_ADMIN' ELSE 'USER' END
);

-- 3. Set NOT NULL default
ALTER TABLE users ALTER COLUMN new_role SET NOT NULL;
ALTER TABLE users ALTER COLUMN new_role SET DEFAULT 'USER';

-- 4. Drop old role column and rename
ALTER TABLE users DROP COLUMN IF EXISTS role;
ALTER TABLE users RENAME COLUMN new_role TO role;

-- 5. Drop RBAC tables
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
