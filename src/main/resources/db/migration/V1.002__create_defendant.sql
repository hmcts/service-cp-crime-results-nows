CREATE TABLE cp_defendant (
    id UUID PRIMARY KEY NOT NULL,
    hearing_id UUID NOT NULL REFERENCES cp_hearing(id),
    master_defendant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_hearing_master UNIQUE (hearing_id, master_defendant_id)
);