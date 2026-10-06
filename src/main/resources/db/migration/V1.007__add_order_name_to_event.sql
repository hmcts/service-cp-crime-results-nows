-- Display name of the matched NOW definition, e.g. "Warrant for Custodial Sentence".
ALTER TABLE cp_event ADD COLUMN order_name VARCHAR(255) NOT NULL;
