-- cp_hearing.hearing_id was already NOT NULL + UNIQUE, so it satisfies every property a primary
-- key needs; the separate app-generated id column was redundant and its FK on cp_defendant
-- collided in name with cp_hearing.hearing_id despite storing a different value.
ALTER TABLE cp_defendant DROP CONSTRAINT cp_defendant_hearing_id_fkey;
ALTER TABLE cp_hearing DROP CONSTRAINT uq_hearing_id;
ALTER TABLE cp_hearing DROP CONSTRAINT cp_hearing_pkey;
ALTER TABLE cp_hearing DROP COLUMN id;
ALTER TABLE cp_hearing ADD PRIMARY KEY (hearing_id);
ALTER TABLE cp_defendant ADD CONSTRAINT cp_defendant_hearing_id_fkey FOREIGN KEY (hearing_id) REFERENCES cp_hearing(hearing_id);