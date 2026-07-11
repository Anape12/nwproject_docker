CREATE TABLE permission_mst (
    permission_id   VARCHAR(2) PRIMARY KEY,
    permission_name VARCHAR(50) NOT NULL,
    display_order   INT NOT NULL,
    delete_flg      CHAR(1) DEFAULT '0'
);

INSERT INTO permission_mst
(permission_id, permission_name, display_order)
VALUES
('1', '管理者', 1),
('2', '一般ユーザー', 2);