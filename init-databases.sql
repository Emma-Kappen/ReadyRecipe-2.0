-- Create database user if not exists
CREATE USER "user" WITH PASSWORD 'pass';

-- Create readyrecipe database owned by user
CREATE DATABASE readyrecipe OWNER "user";

-- Create readyrecipe_test database owned by user
CREATE DATABASE readyrecipe_test OWNER "user";

-- Grant all privileges on databases to user
GRANT ALL PRIVILEGES ON DATABASE readyrecipe TO "user";
GRANT ALL PRIVILEGES ON DATABASE readyrecipe_test TO "user";

-- Grant schema privileges
GRANT USAGE ON SCHEMA public TO "user";
GRANT CREATE ON SCHEMA public TO "user";
