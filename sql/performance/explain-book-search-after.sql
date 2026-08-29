-- Core query: admin catalogue page, first page, newest books first.
-- V5 adds idx_book_status_id(status, id), matching the filter and ordering.
-- USE INDEX makes the comparison reproducible after the migration is installed.
EXPLAIN ANALYZE
SELECT id, isbn, title, publisher_id, original_price, sale_price,
       stock, publish_date, edition, pages, cover_url, status,
       sales_count, create_time, update_time
FROM book USE INDEX (idx_book_status_id)
WHERE status = 'ON_SALE'
ORDER BY id DESC
LIMIT 20 OFFSET 0;
