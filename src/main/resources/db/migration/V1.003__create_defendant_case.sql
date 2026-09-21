-- The defendantId -> masterDefendantId resolution table the read path needs (design doc §6).
CREATE TABLE cp_defendant_case (
    id UUID PRIMARY KEY NOT NULL,
    defendant_row_id UUID NOT NULL REFERENCES cp_defendant(id),
    case_urn VARCHAR(64) NOT NULL,
    defendant_id VARCHAR(64) NOT NULL,
    CONSTRAINT uq_case_urn_defendant UNIQUE (case_urn, defendant_id)
);