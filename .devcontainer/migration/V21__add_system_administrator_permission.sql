INSERT INTO permission_mst (permission_id, permission_name, display_order, delete_flg)
VALUES ('0', 'システム権限者', 0, '0')
ON DUPLICATE KEY UPDATE permission_name = VALUES(permission_name), delete_flg = '0';
