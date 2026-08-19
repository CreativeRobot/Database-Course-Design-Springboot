-- Apply once to databases created before avatar upload was introduced.
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500) NULL;
