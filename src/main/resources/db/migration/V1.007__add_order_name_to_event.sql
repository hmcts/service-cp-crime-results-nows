-- Display name of the matched NOW definition (e.g. "Warrant for Custodial Sentence"), captured at
-- match time alongside event_type so the Query API can return it without re-fetching nows-metadata.
-- Nullable: rows recorded before this column existed have no value.
ALTER TABLE cp_event ADD COLUMN order_name VARCHAR(255);
