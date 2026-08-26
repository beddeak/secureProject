UPDATE documents
SET status = 'PENDING_REVIEW'
WHERE status = 'REVIEWED';
