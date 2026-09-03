ALTER TABLE book_bundle
    ADD COLUMN home_pinned TINYINT(1) NOT NULL DEFAULT 0 AFTER status,
    ADD COLUMN home_priority INT NOT NULL DEFAULT 0 AFTER home_pinned,
    ADD KEY idx_book_bundle_home_order (status, home_pinned, home_priority, create_time);
