CREATE TABLE cp_hearing (
    id UUID PRIMARY KEY NOT NULL,
    hearing_id UUID NOT NULL,
    hearing_day DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_hearing_id UNIQUE (hearing_id)
);