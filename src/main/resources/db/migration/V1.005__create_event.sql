-- matched_result_type_ids lets the Query API filter defendant_snapshot.content per event type at
-- read time (design doc §5c), without re-fetching nows-metadata or re-running the requirement-tree
-- match.
CREATE TABLE event (
    id UUID PRIMARY KEY NOT NULL,
    defendant_row_id UUID NOT NULL REFERENCES defendant(id),
    event_type VARCHAR(128) NOT NULL,
    matched_result_type_ids JSONB NOT NULL,
    matched_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_defendant_event_type UNIQUE (defendant_row_id, event_type)
);