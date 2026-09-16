-- Resolved defendant/hearing/offences content (design doc §5b), written once at ingestion time.
CREATE TABLE defendant_snapshot (
    id UUID PRIMARY KEY NOT NULL,
    defendant_row_id UUID NOT NULL REFERENCES defendant(id),
    content JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_defendant UNIQUE (defendant_row_id)
);