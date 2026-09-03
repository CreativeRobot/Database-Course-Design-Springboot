-- Demonstration data for the bookstore.
-- The script uses explicit IDs so the relationships are easy to inspect.

USE bookstore;

SET NAMES utf8mb4;

-- Active idempotent fixtures. All demo passwords are: password
-- The BCrypt hash was verified with Spring Security BCryptPasswordEncoder.
INSERT IGNORE INTO users
    (username, password, status, role, nickname, email, phone, create_time, update_time)
VALUES
    ('admin', '$2a$10$d49eAi8plVBXZWkYl1DfUuHCgRC.jQoL1KRasLhqzGHJwfsJkuZn2',
     1, 'ADMIN', 'System Admin', 'admin@bookstore.local', '13800000001',
     '2026-08-01 09:00:00', '2026-08-01 09:00:00'),
    ('reader', '$2a$10$d49eAi8plVBXZWkYl1DfUuHCgRC.jQoL1KRasLhqzGHJwfsJkuZn2',
     1, 'CUSTOMER', 'Book Reader', 'reader@bookstore.local', '13800000002',
     '2026-08-01 09:05:00', '2026-08-01 09:05:00'),
    ('alice', '$2a$10$d49eAi8plVBXZWkYl1DfUuHCgRC.jQoL1KRasLhqzGHJwfsJkuZn2',
     1, 'CUSTOMER', 'Alice Wang', 'alice@bookstore.local', '13800000003',
     '2026-08-01 09:06:00', '2026-08-01 09:06:00'),
    ('bob', '$2a$10$d49eAi8plVBXZWkYl1DfUuHCgRC.jQoL1KRasLhqzGHJwfsJkuZn2',
     1, 'CUSTOMER', 'Bob Li', 'bob@bookstore.local', '13800000004',
     '2026-08-01 09:07:00', '2026-08-01 09:07:00'),
    ('carol', '$2a$10$d49eAi8plVBXZWkYl1DfUuHCgRC.jQoL1KRasLhqzGHJwfsJkuZn2',
     1, 'CUSTOMER', 'Carol Zhang', 'carol@bookstore.local', '13800000005',
     '2026-08-01 09:08:00', '2026-08-01 09:08:00'),
    ('david', '$2a$10$d49eAi8plVBXZWkYl1DfUuHCgRC.jQoL1KRasLhqzGHJwfsJkuZn2',
     1, 'CUSTOMER', 'David Chen', 'david@bookstore.local', '13800000006',
     '2026-08-01 09:09:00', '2026-08-01 09:09:00')
;

UPDATE users
SET password = '$2a$10$d49eAi8plVBXZWkYl1DfUuHCgRC.jQoL1KRasLhqzGHJwfsJkuZn2',
    status = 1,
    role = CASE WHEN username = 'admin' THEN 'ADMIN' ELSE 'CUSTOMER' END,
    update_time = '2026-08-01 09:09:00'
WHERE username IN ('admin', 'reader', 'alice', 'bob', 'carol', 'david');

INSERT IGNORE INTO publisher (name, phone, address, introduction, create_time, update_time)
VALUES
    ('Higher Education Press', '010-58581118', 'Beijing',
     'Academic and educational publishing.', '2026-08-01 09:10:00', '2026-08-01 09:10:00'),
    ('People''s Literature Publishing House', '010-65252930', 'Beijing',
     'Literature and humanities publishing.', '2026-08-01 09:11:00', '2026-08-01 09:11:00'),
    ('Tsinghua University Press', '010-62770175', 'Beijing',
     'Technology, science, and university textbooks.', '2026-08-01 09:12:00', '2026-08-01 09:12:00'),
    ('Science Press', '010-64034563', 'Beijing',
     'Popular science and research publishing.', '2026-08-01 09:13:00', '2026-08-01 09:13:00')
;

-- `author.name` is not unique, so avoid duplicates with an anti-join.
INSERT INTO author (name, country, introduction, create_time, update_time)
SELECT seed.name, seed.country, seed.introduction, '2026-08-01 09:15:00', '2026-08-01 09:15:00'
FROM (
    SELECT 'Database Systems Team' AS name, 'China' AS country, 'Database course material authors.' AS introduction
    UNION ALL SELECT 'William Shakespeare', 'United Kingdom', 'English playwright and poet.'
    UNION ALL SELECT 'Jane Austen', 'United Kingdom', 'English novelist.'
    UNION ALL SELECT 'Robert C. Martin', 'United States', 'Software engineer and author.'
    UNION ALL SELECT 'Andrew Hunt', 'United States', 'Software developer and technical author.'
    UNION ALL SELECT 'David Thomas', 'United States', 'Software developer and technical author.'
    UNION ALL SELECT 'Stephen Hawking', 'United Kingdom', 'Theoretical physicist and science communicator.'
    UNION ALL SELECT 'Ian Goodfellow', 'United States', 'Machine learning researcher.'
    UNION ALL SELECT 'Yoshua Bengio', 'Canada', 'Machine learning researcher.'
    UNION ALL SELECT 'Aaron Courville', 'Canada', 'Machine learning researcher.'
    UNION ALL SELECT 'Donald E. Knuth', 'United States', 'Computer scientist and author.'
) AS seed
LEFT JOIN author existing ON existing.name = seed.name
WHERE existing.id IS NULL;

INSERT IGNORE INTO category (name, parent_id, sort_order, status, create_time, update_time)
VALUES
    ('Computer Science', NULL, 1, 1, '2026-08-01 09:30:00', '2026-08-01 09:30:00'),
    ('Literature', NULL, 2, 1, '2026-08-01 09:31:00', '2026-08-01 09:31:00'),
    ('Science', NULL, 3, 1, '2026-08-01 09:32:00', '2026-08-01 09:32:00')
;

INSERT IGNORE INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT 'Database', id, 1, 1, '2026-08-01 09:33:00', '2026-08-01 09:33:00'
FROM category WHERE name = 'Computer Science';

INSERT IGNORE INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT 'Programming', id, 2, 1, '2026-08-01 09:34:00', '2026-08-01 09:34:00'
FROM category WHERE name = 'Computer Science';

INSERT IGNORE INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT 'Artificial Intelligence', id, 3, 1, '2026-08-01 09:35:00', '2026-08-01 09:35:00'
FROM category WHERE name = 'Computer Science';

INSERT IGNORE INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT 'Classic Literature', id, 1, 1, '2026-08-01 09:36:00', '2026-08-01 09:36:00'
FROM category WHERE name = 'Literature';

INSERT IGNORE INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT 'Popular Science', id, 1, 1, '2026-08-01 09:37:00', '2026-08-01 09:37:00'
FROM category WHERE name = 'Science';

-- Additional second-level categories for browsing and community-filter testing.
INSERT IGNORE INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT 'Web Development', id, 4, 1, '2026-09-03 10:00:00', '2026-09-03 10:00:00'
FROM category WHERE name = 'Computer Science';

INSERT IGNORE INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT 'Essays', id, 2, 1, '2026-09-03 10:01:00', '2026-09-03 10:01:00'
FROM category WHERE name = 'Literature';

INSERT IGNORE INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT 'Astronomy', id, 2, 1, '2026-09-03 10:02:00', '2026-09-03 10:02:00'
FROM category WHERE name = 'Science';

INSERT IGNORE INTO book
    (isbn, title, publisher_id, original_price, sale_price, stock, publish_date,
     edition, pages, description, cover_url, status, create_time, update_time)
SELECT seed.isbn, seed.title, publisher.id, seed.original_price, seed.sale_price,
       seed.stock, seed.publish_date, seed.edition, seed.pages, seed.description,
       seed.cover_url, 'ON_SALE', '2026-08-01 09:40:00', '2026-08-01 09:40:00'
FROM (
    SELECT '9780000000001' AS isbn, 'Database Systems Concepts' AS title, 'Higher Education Press' AS publisher_name, 69.90 AS original_price, 59.90 AS sale_price, 19 AS stock, '2024-09-01' AS publish_date, '7th' AS edition, 720 AS pages, 'Relational database systems course material.' AS description, 'https://example.com/covers/database.jpg' AS cover_url
    UNION ALL SELECT '9780000000002', 'Relational Database Practice', 'Higher Education Press', 59.90, 49.90, 9, '2025-03-01', '2nd', 420, 'SQL, indexing and transaction exercises.', 'https://example.com/covers/sql.jpg'
    UNION ALL SELECT '9780000000003', 'Hamlet', 'People''s Literature Publishing House', 49.90, 39.90, 12, '2023-06-01', '1st', 280, 'A classic tragedy by William Shakespeare.', 'https://example.com/covers/hamlet.jpg'
    UNION ALL SELECT '9780000000004', 'Pride and Prejudice', 'People''s Literature Publishing House', 56.00, 45.00, 15, '2022-09-01', '1st', 432, 'Jane Austen classic novel.', 'https://example.com/covers/pride-and-prejudice.jpg'
    UNION ALL SELECT '9780000000005', 'Clean Code', 'Tsinghua University Press', 79.00, 65.00, 18, '2024-01-01', '2nd', 464, 'A handbook of agile software craftsmanship.', 'https://example.com/covers/clean-code.jpg'
    UNION ALL SELECT '9780000000006', 'The Pragmatic Programmer', 'Tsinghua University Press', 88.00, 72.00, 14, '2024-04-01', '2nd', 352, 'Practical advice for software developers.', 'https://example.com/covers/pragmatic-programmer.jpg'
    UNION ALL SELECT '9780000000007', 'A Brief History of Time', 'Science Press', 68.00, 55.00, 20, '2023-03-01', '1st', 240, 'An introduction to cosmology.', 'https://example.com/covers/brief-history-of-time.jpg'
    UNION ALL SELECT '9780000000008', 'Deep Learning', 'Tsinghua University Press', 128.00, 108.00, 8, '2025-02-01', '1st', 800, 'A comprehensive introduction to deep learning.', 'https://example.com/covers/deep-learning.jpg'
    UNION ALL SELECT '9780000000009', 'The Art of Computer Programming', 'Tsinghua University Press', 168.00, 138.00, 6, '2024-06-01', '1st', 672, 'Foundational algorithms and programming techniques.', 'https://example.com/covers/taocp.jpg'
    UNION ALL SELECT '9780000000010', 'Database Design for Beginners', 'Higher Education Press', 58.00, 46.00, 25, '2025-07-01', '1st', 360, 'An accessible introduction to database design.', 'https://example.com/covers/database-design.jpg'
) AS seed
JOIN publisher ON publisher.name = seed.publisher_name;

INSERT IGNORE INTO book_author (book_id, author_id, author_order)
SELECT book.id, author.id, seed.author_order
FROM (
    SELECT '9780000000001' AS isbn, 'Database Systems Team' AS author_name, 1 AS author_order
    UNION ALL SELECT '9780000000002', 'Database Systems Team', 1
    UNION ALL SELECT '9780000000003', 'William Shakespeare', 1
    UNION ALL SELECT '9780000000004', 'Jane Austen', 1
    UNION ALL SELECT '9780000000005', 'Robert C. Martin', 1
    UNION ALL SELECT '9780000000006', 'Andrew Hunt', 1
    UNION ALL SELECT '9780000000006', 'David Thomas', 2
    UNION ALL SELECT '9780000000007', 'Stephen Hawking', 1
    UNION ALL SELECT '9780000000008', 'Ian Goodfellow', 1
    UNION ALL SELECT '9780000000008', 'Yoshua Bengio', 2
    UNION ALL SELECT '9780000000008', 'Aaron Courville', 3
    UNION ALL SELECT '9780000000009', 'Donald E. Knuth', 1
    UNION ALL SELECT '9780000000010', 'Database Systems Team', 1
) AS seed
JOIN book ON book.isbn = seed.isbn
JOIN (SELECT name, MIN(id) AS id FROM author GROUP BY name) AS author ON author.name = seed.author_name;

INSERT IGNORE INTO book_category (book_id, category_id)
SELECT book.id, category.id
FROM (
    SELECT '9780000000001' AS isbn, 'Database' AS category_name
    UNION ALL SELECT '9780000000002', 'Database'
    UNION ALL SELECT '9780000000003', 'Classic Literature'
    UNION ALL SELECT '9780000000004', 'Classic Literature'
    UNION ALL SELECT '9780000000005', 'Programming'
    UNION ALL SELECT '9780000000006', 'Programming'
    UNION ALL SELECT '9780000000007', 'Popular Science'
    UNION ALL SELECT '9780000000008', 'Artificial Intelligence'
    UNION ALL SELECT '9780000000009', 'Programming'
    UNION ALL SELECT '9780000000010', 'Database'
) AS seed
JOIN book ON book.isbn = seed.isbn
JOIN category ON category.name = seed.category_name;

/* Legacy fixed-ID fixture retained for reference only. It is intentionally not executed:
   a pre-existing local user can occupy ID 2, so these relationships are unsafe.
-- BCrypt hash for the local demonstration password: password
INSERT IGNORE INTO users
    (id, username, password, status, role, nickname, email, phone,
     create_time, update_time)
VALUES
    (1, 'admin',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     1, 'ADMIN', 'System Admin', 'admin@bookstore.local', '13800000001',
     '2026-08-01 09:00:00', '2026-08-01 09:00:00'),
    (2, 'reader',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     1, 'CUSTOMER', 'Book Reader', 'reader@bookstore.local', '13800000002',
     '2026-08-01 09:05:00', '2026-08-01 09:05:00');

INSERT IGNORE INTO publisher
    (id, name, phone, address, introduction, create_time, update_time)
VALUES
    (1, 'Higher Education Press', '010-58581118',
     'Beijing', 'Academic and educational publishing.', '2026-08-01 09:10:00',
     '2026-08-01 09:10:00'),
    (2, 'People''s Literature Publishing House', '010-65252930',
     'Beijing', 'Literature and humanities publishing.', '2026-08-01 09:11:00',
     '2026-08-01 09:11:00');

INSERT IGNORE INTO author
    (id, name, country, introduction, create_time, update_time)
VALUES
    (1, 'Database Systems Team', 'China',
     'A demonstration author for database course materials.',
     '2026-08-01 09:15:00', '2026-08-01 09:15:00'),
    (2, 'William Shakespeare', 'United Kingdom',
     'English playwright and poet.',
     '2026-08-01 09:16:00', '2026-08-01 09:16:00'),
    (3, 'Jane Austen', 'United Kingdom',
     'English novelist known for social commentary.',
     '2026-08-01 09:17:00', '2026-08-01 09:17:00');

INSERT IGNORE INTO category
    (id, name, parent_id, sort_order, status, create_time, update_time)
VALUES
    (1, 'Computer Science', NULL, 1, 1,
     '2026-08-01 09:20:00', '2026-08-01 09:20:00'),
    (2, 'Database', 1, 1, 1,
     '2026-08-01 09:21:00', '2026-08-01 09:21:00'),
    (3, 'Literature', NULL, 2, 1,
     '2026-08-01 09:22:00', '2026-08-01 09:22:00');

INSERT IGNORE INTO book
    (id, isbn, title, publisher_id, original_price, sale_price, stock,
     publish_date, edition, pages, description, cover_url, status,
     create_time, update_time)
VALUES
    (1, '9780000000001', 'Database Systems Concepts', 1,
     69.90, 59.90, 19, '2024-09-01', '7th', 720,
     'A course demonstration book about relational database systems.',
     'https://example.com/covers/database.jpg', 'ON_SALE',
     '2026-08-01 09:30:00', '2026-08-05 10:00:00'),
    (2, '9780000000002', 'Relational Database Practice', 1,
     59.90, 49.90, 9, '2025-03-01', '2nd', 420,
     'Hands-on SQL, indexing and transaction exercises.',
     'https://example.com/covers/sql.jpg', 'ON_SALE',
     '2026-08-01 09:31:00', '2026-08-05 10:00:00'),
    (3, '9780000000003', 'Selected English Literature', 2,
     89.90, 79.90, 3, '2023-06-01', '1st', 560,
     'A literature collection used for order and review demonstrations.',
     'https://example.com/covers/literature.jpg', 'ON_SALE',
     '2026-08-01 09:32:00', '2026-08-05 10:00:00');

INSERT IGNORE INTO book_author (book_id, author_id, author_order)
VALUES
    (1, 1, 1),
    (2, 1, 1),
    (3, 2, 1),
    (3, 3, 2);

INSERT IGNORE INTO book_category (book_id, category_id)
VALUES
    (1, 2),
    (2, 2),
    (3, 3);

INSERT IGNORE INTO user_address
    (id, user_id, receiver_name, receiver_phone, province, city, district,
     detail_address, postal_code, default_address, create_time, update_time)
VALUES
    (1, 2, 'Book Reader', '13800000002', 'Guangdong', 'Shenzhen',
     'Nanshan', 'Science Park Road 1', '518000', TRUE,
     '2026-08-01 09:40:00', '2026-08-01 09:40:00');

INSERT IGNORE INTO cart_item
    (id, user_id, book_id, quantity, selected, create_time, update_time)
VALUES
    (1, 2, 3, 1, TRUE, '2026-08-05 10:05:00', '2026-08-05 10:05:00');

INSERT IGNORE INTO book_order
    (id, order_no, user_id, status, total_amount, discount_amount,
     shipping_fee, payable_amount, expire_time, receiver_name, receiver_phone,
     receiver_address, remark, create_time, update_time, paid_time,
     shipped_time, completed_time, cancelled_time)
VALUES
    (1, 'BS202608010001', 2, 'COMPLETED', 109.80, 0.00, 0.00, 109.80,
     NULL, 'Book Reader', '13800000002',
     'Guangdong Shenzhen Nanshan Science Park Road 1 Postal: 518000',
     'Completed sample order',
     '2026-08-02 10:00:00', '2026-08-04 15:00:00',
     '2026-08-02 10:01:00', '2026-08-03 09:00:00',
     '2026-08-04 15:00:00', NULL),
    (2, 'BS202608050001', 2, 'PENDING_PAYMENT', 79.90, 0.00, 0.00, 79.90,
     '2026-08-05 10:40:00', 'Book Reader', '13800000002',
     'Guangdong Shenzhen Nanshan Science Park Road 1 Postal: 518000',
     'Pending payment sample order',
     '2026-08-05 10:10:00', '2026-08-05 10:10:00',
     NULL, NULL, NULL, NULL);

INSERT IGNORE INTO order_item
    (id, order_id, book_id, book_title, isbn, unit_price, quantity, subtotal)
VALUES
    (1, 1, 1, 'Database Systems Concepts', '9780000000001', 59.90, 1, 59.90),
    (2, 1, 2, 'Relational Database Practice', '9780000000002', 49.90, 1, 49.90),
    (3, 2, 3, 'Selected English Literature', '9780000000003', 79.90, 1, 79.90);

INSERT IGNORE INTO payment
    (id, payment_no, order_id, payment_method, amount, status, paid_time,
     create_time, update_time)
VALUES
    (1, 'PAY202608020001', 1, 'MOCK', 109.80, 'SUCCESS',
     '2026-08-02 10:01:00', '2026-08-02 10:00:30', '2026-08-02 10:01:00');

INSERT IGNORE INTO book_review
    (id, user_id, book_id, order_item_id, rating, content, status,
     create_time, update_time)
VALUES
    (1, 2, 1, 1, 5, 'Clear structure and useful examples.', 1,
     '2026-08-04 16:00:00', '2026-08-04 16:00:00');

INSERT IGNORE INTO inventory_log
    (id, book_id, change_quantity, before_stock, after_stock, change_type,
     order_id, remark, create_time)
VALUES
    (1, 1, 20, 0, 20, 'PURCHASE_IN', NULL,
     'Initial stock', '2026-08-01 09:35:00'),
    (2, 1, -1, 20, 19, 'ORDER_OUT', 1,
     'Order BS202608010001', '2026-08-02 10:00:00'),
    (3, 2, 10, 0, 10, 'PURCHASE_IN', NULL,
     'Initial stock', '2026-08-01 09:36:00'),
    (4, 2, -1, 10, 9, 'ORDER_OUT', 1,
     'Order BS202608010001', '2026-08-02 10:00:00'),
    (5, 3, 5, 0, 5, 'PURCHASE_IN', NULL,
     'Initial stock', '2026-08-01 09:37:00'),
    (6, 3, -2, 5, 3, 'ORDER_OUT', 2,
     'Order BS202608050001', '2026-08-05 10:10:00');
*/
