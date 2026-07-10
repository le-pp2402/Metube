CREATE TABLE outbox (
    id BIGINT PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,                       
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PROCESSING, SENT, FAILED
    retry_count INT DEFAULT 0 NOT NULL,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_outbox_polling
ON outbox(created_at ASC, next_retry_at ASC) 
WHERE status = 'PENDING';

ALTER TABLE outbox SET (
    autovacuum_vacuum_scale_factor = 0.05,
    autovacuum_vacuum_threshold = 100,     
    autovacuum_analyze_scale_factor = 0.02,
    autovacuum_analyze_threshold = 50
);
