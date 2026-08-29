UPDATE users_info
SET first_name = 'ミア'
WHERE user_id = 'ai_mina'
  AND account_type = 'AI';

UPDATE ai_character
SET character_name = 'ミア',
    system_prompt = REPLACE(system_prompt, 'ミナ', 'ミア')
WHERE user_id = 'ai_mina';
