CREATE TABLE users_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20),
    password VARCHAR(200),
    birthday DATE,
    permission VARCHAR(5),
    password_expiration VARCHAR(8),
    delete_flg VARCHAR(5)
);
INSERT INTO users_info (
        name,
        password,
        birthday,
        permission,
        password_expiration,
        delete_flg
    )
VALUES (
        'a0001',
        '$2a$10$45HSUdWr4xrIYVymHlDmL.v0sc6xpENpHAszdaiSUG8bVWKuUs5LK',
        '1993-12-26',
        '1',
        '99999999',
        '0'
    ),
    (
        'a0002',
        '$2a$10$Fi4YRJ0ONJB3/8S8pWhmdOkZLDwipULqyd5k7BXRnoXbDT.10/aAe',
        '1956-05-26',
        '1',
        '99999999',
        '0'
    ),
    (
        'a0003',
        '$2a$10$Fi4YRJ0ONJB3/8S8pWhmdOkZLDwipULqyd5k7BXRnoXbDT.10/aAe',
        '1961-04-11',
        '2',
        '99999999',
        '0'
    );
CREATE TABLE thread_info (
    thread_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author_id VARCHAR(20) NOT NULL,
    thread_content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
INSERT INTO thread_info (
        title,
        author_id,
        thread_content
    )
VALUES ('スレッドタイトル1', 'a0001', 'スレッド内容1'),
    ('スレッドタイトル2', 'a0002', 'スレッド内容2');
CREATE TABLE thread_comment (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    thread_id INT NOT NULL,
    author_id VARCHAR(20) NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO thread_comment (
        thread_id,
        author_id,
        comment_text
    )
VALUES (1, 'a0002', 'コメント内容1'),
    (1, 'a0001', 'コメント内容2'),
    (2, 'a0001', 'コメント内容3');