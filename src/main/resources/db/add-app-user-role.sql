-- Role for app_users (ADMIN = full access including user management, USER = standard access).

ALTER TABLE app_users
    ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'USER';

-- Promote the default bootstrap account if it exists (adjust username if needed).
UPDATE app_users SET role = 'ADMIN' WHERE username = 'admin';
