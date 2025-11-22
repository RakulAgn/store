-- Clear existing stores table
-- This is needed because we added user_id column which is NOT NULL
-- but existing stores don't have a user

DELETE FROM stores;

-- Optional: You can also drop and recreate the table
-- DROP TABLE IF EXISTS stores;
-- DROP TABLE IF EXISTS users;
