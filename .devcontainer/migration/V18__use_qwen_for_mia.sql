UPDATE ai_character
SET model_name = 'qwen2.5:3b'
WHERE user_id = 'ai_mina'
  AND (model_name IS NULL OR model_name = '' OR model_name = 'mistral');

