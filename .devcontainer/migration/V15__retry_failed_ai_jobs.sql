UPDATE ai_response_job
SET status = 'PENDING',
    retry_count = 0,
    error_message = NULL,
    started_at = NULL,
    completed_at = NULL
WHERE status = 'FAILED';
