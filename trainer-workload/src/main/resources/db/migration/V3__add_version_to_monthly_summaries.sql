ALTER TABLE trainer_monthly_summaries
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
