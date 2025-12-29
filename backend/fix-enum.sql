-- Fix ENUM types in users table
ALTER TABLE users MODIFY COLUMN role ENUM('STUDENT','TEACHER','ADMIN') NOT NULL DEFAULT 'STUDENT';
ALTER TABLE users MODIFY COLUMN status ENUM('ACTIVE','BANNED') NOT NULL DEFAULT 'ACTIVE';

-- Clear the users table (since old enum values won't be compatible)
DELETE FROM users;
