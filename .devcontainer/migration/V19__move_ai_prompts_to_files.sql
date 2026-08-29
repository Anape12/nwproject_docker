ALTER TABLE ai_character
    ADD COLUMN prompt_key VARCHAR(64) NOT NULL DEFAULT 'mia' AFTER user_id;

UPDATE ai_character
SET prompt_key = 'mia'
WHERE user_id = 'ai_mina';

ALTER TABLE ai_character
    DROP COLUMN character_name,
    DROP COLUMN system_prompt,
    DROP COLUMN personality,
    DROP COLUMN interests;
