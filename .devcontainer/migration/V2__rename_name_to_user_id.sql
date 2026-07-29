ALTER TABLE users_info
    RENAME COLUMN name TO user_id;
ALTER TABLE users_info
ADD UNIQUE (user_id);