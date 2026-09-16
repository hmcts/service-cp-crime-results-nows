-- The defendantId -> masterDefendantId resolution table the read path needs (design doc §6).
CREATE TABLE defendant_case (
    id UUID PRIMARY KEY NOT NULL,
    defendant_row_id UUID NOT NULL REFERENCES defendant(id),
    case_urn varchar NOT NULL,
    defendant_id varchar NOT NULL,
    CONSTRAINT uq_case_urn_defendant UNIQUE (case_urn, defendant_id)
);