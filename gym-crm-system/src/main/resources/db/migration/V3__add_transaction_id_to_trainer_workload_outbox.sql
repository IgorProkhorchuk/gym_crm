ALTER TABLE trainer_workload_outbox
    ADD COLUMN transaction_id VARCHAR(100);

UPDATE trainer_workload_outbox
SET transaction_id = 'legacy-outbox-event-' || id
WHERE transaction_id IS NULL;

ALTER TABLE trainer_workload_outbox
    ALTER COLUMN transaction_id SET NOT NULL;

CREATE INDEX idx_trainer_workload_outbox_transaction_id
    ON trainer_workload_outbox (transaction_id);
