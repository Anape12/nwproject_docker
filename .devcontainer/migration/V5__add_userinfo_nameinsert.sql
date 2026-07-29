ALTER TABLE users_info
ADD COLUMN first_name VARCHAR(36),
    ADD COLUMN last_name VARCHAR(36);
UPDATE users_info
SET first_name = '太郎',
    last_name = '山田'
WHERE id = 1;
UPDATE users_info
SET first_name = '花子',
    last_name = '佐藤'
WHERE id = 2;
UPDATE users_info
SET first_name = '一郎',
    last_name = '鈴木'
WHERE id = 3;
ALTER TABLE users_info
MODIFY COLUMN first_name VARCHAR(36) NOT NULL,
    MODIFY COLUMN last_name VARCHAR(36) NOT NULL;