-- Preserve existing Flyway migrations and migrate their applied catalog entries.
-- Authentication: AUTH-001 -> ERR00000001.
-- Application: APP-000 -> ERR00010000, APP-139 -> ERR00010139.
UPDATE error_message_mst
SET error_code = CASE
    WHEN error_code REGEXP '^AUTH-[0-9]{3}$'
        THEN CONCAT('ERR', LPAD(CAST(SUBSTRING(error_code, 6) AS UNSIGNED), 8, '0'))
    WHEN error_code REGEXP '^APP-[0-9]{3}$'
        THEN CONCAT('ERR', LPAD(10000 + CAST(SUBSTRING(error_code, 5) AS UNSIGNED), 8, '0'))
    ELSE error_code
END
WHERE error_code REGEXP '^(AUTH|APP)-[0-9]{3}$';

ALTER TABLE error_message_mst
    ADD CONSTRAINT chk_error_message_code_format
    CHECK (error_code REGEXP '^(ERR|WAR)[0-9]{8}$');
