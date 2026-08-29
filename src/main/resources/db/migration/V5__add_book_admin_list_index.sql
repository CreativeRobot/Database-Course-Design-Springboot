-- The admin catalogue query filters by book.status and orders by book.id DESC.
-- The previous schema had no index starting with status, so MySQL could scan
-- the table and perform a filesort for this high-frequency page query.
CREATE INDEX idx_book_status_id ON book (status, id);
