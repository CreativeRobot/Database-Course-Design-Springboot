-- Demonstration data for the bookstore.
-- The script uses explicit IDs so the relationships are easy to inspect.

USE bookstore;

SET NAMES utf8mb4;

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
