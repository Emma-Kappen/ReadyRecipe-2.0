-- Seed data for readyrecipe_test
-- enable pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Recipes
INSERT INTO recipes (id, name, cuisine_type, cooking_time, rating, image_url) VALUES
  (gen_random_uuid(), 'Spaghetti Carbonara', 'Italian', 25, 4.5, ''),
  (gen_random_uuid(), 'Chana Masala', 'Indian', 40, 4.7, ''),
  (gen_random_uuid(), 'Chicken Stir Fry', 'Chinese', 20, 4.3, ''),
  (gen_random_uuid(), 'Shakshuka', 'Middle Eastern', 30, 4.4, '');

-- Pantry items for user1@example.com (replace USER_ID below if needed)
-- We'll find user IDs in the DB; using a placeholder SELECT to show mapping if needed

-- Replace the USER_UUID_TOKEN with an actual user id when running from scripts.
-- For scripted run below we'll insert using the known UUID for user1@example.com.

-- Pantry items assigned to fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef (user1@example.com)
INSERT INTO pantry_items (id, user_id, item_name, quantity, unit, category, expiry_date, date_added) VALUES
  (gen_random_uuid(), 'fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef', 'Large Eggs', 12.00, 'count', 'Dairy & Eggs', now()::date + interval '14 days', now()),
  (gen_random_uuid(), 'fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef', 'Whole Milk', 1.00, 'L', 'Dairy & Eggs', now()::date + interval '7 days', now()),
  (gen_random_uuid(), 'fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef', 'Brown Rice', 2.00, 'kg', 'Grains', now()::date + interval '365 days', now()),
  (gen_random_uuid(), 'fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef', 'Canned Tomatoes', 4.00, 'can', 'Canned Goods', now()::date + interval '730 days', now());

-- Pantry items for test1@example.com (64a0c288-...)
INSERT INTO pantry_items (id, user_id, item_name, quantity, unit, category, expiry_date, date_added) VALUES
  (gen_random_uuid(), '64a0c288-49e7-4931-a940-d3d0d0326be3', 'All-Purpose Flour', 1.50, 'kg', 'Baking', now()::date + interval '180 days', now()),
  (gen_random_uuid(), '64a0c288-49e7-4931-a940-d3d0d0326be3', 'Tomatoes', 6.00, 'count', 'Produce', now()::date + interval '5 days', now()),
  (gen_random_uuid(), '64a0c288-49e7-4931-a940-d3d0d0326be3', 'Chicken Breast', 1.00, 'kg', 'Meat', now()::date + interval '3 days', now());

-- Simple verification selects
SELECT count(*) AS recipes_count FROM recipes;
SELECT count(*) AS pantry_count FROM pantry_items;
SELECT id, item_name, user_id FROM pantry_items LIMIT 10;
