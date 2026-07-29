CREATE TABLE chat_room (
    room_id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    room_name VARCHAR(100),
    room_type CHAR(1) NOT NULL,
    -- 1:1対1 2:グループ
    created_by_id VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delete_flg CHAR(1) DEFAULT '0'
);
INSERT INTO chat_room (
        room_name,
        room_type,
        created_by_id
    )
VALUES (NULL, '1', 'a0001'),
    ('開発チーム', '2', 'a0001');
CREATE TABLE chat_room_member (
    room_id CHAR(36) NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (room_id, user_id),
    CONSTRAINT fk_chat_room_member_room FOREIGN KEY (room_id) REFERENCES chat_room(room_id),
    CONSTRAINT fk_chat_room_member_user FOREIGN KEY (user_id) REFERENCES users_info(user_id)
);
INSERT INTO chat_room_member (room_id, user_id)
VALUES (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_type = '1'
            LIMIT 1
        ), 'a0001'
    ), (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_type = '1'
            LIMIT 1
        ), 'a0002'
    ), (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_name = '開発チーム'
            LIMIT 1
        ), 'a0001'
    ), (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_name = '開発チーム'
            LIMIT 1
        ), 'a0002'
    ), (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_name = '開発チーム'
            LIMIT 1
        ), 'a0003'
    );
CREATE TABLE chat_message (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    room_id CHAR(36) NOT NULL,
    posted_by_id VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    delete_flg CHAR(1) DEFAULT '0',
    CONSTRAINT fk_chat_message_room FOREIGN KEY (room_id) REFERENCES chat_room(room_id),
    CONSTRAINT fk_chat_message_user FOREIGN KEY (posted_by_id) REFERENCES users_info(user_id)
);
INSERT INTO chat_message (room_id, posted_by_id, message)
VALUES (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_type = '1'
            LIMIT 1
        ), 'a0001', 'こんにちは！'
    ), (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_type = '1'
            LIMIT 1
        ), 'a0002', 'こんにちは！'
    ), (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_type = '1'
            LIMIT 1
        ), 'a0001', '今日空いてる？'
    ), (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_name = '開発チーム'
            LIMIT 1
        ), 'a0001', 'グループを作成しました。'
    ), (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_name = '開発チーム'
            LIMIT 1
        ), 'a0002', 'よろしくお願いします。'
    ), (
        (
            SELECT room_id
            FROM chat_room
            WHERE room_name = '開発チーム'
            LIMIT 1
        ), 'a0003', 'よろしくお願いします！'
    );