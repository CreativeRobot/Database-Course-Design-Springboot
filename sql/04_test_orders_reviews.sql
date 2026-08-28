-- Repeatable test orders and book reviews for the bookstore.
-- Prerequisite: run sql/02_data.sql first so the five customer accounts
-- and books with ISBN 9780000000001 through 9780000000008 exist.
--
-- The script inserts 30 orders prefixed with TEST-ORD-202608- and 15 reviews.
-- Re-running it is safe: existing test orders, order items, reviews, and sales
-- counts are left unchanged.
USE bookstore;
SET NAMES utf8mb4;
DROP TEMPORARY TABLE IF EXISTS tmp_order_review_seed;
CREATE TEMPORARY TABLE tmp_order_review_seed (
    order_no VARCHAR(32) NOT NULL,
    username VARCHAR(50) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    shipping_fee DECIMAL(10, 2) NOT NULL,
    remark VARCHAR(255) NULL,
    create_time DATETIME NOT NULL,
    rating INT NULL,
    review_content VARCHAR(1000) NULL,
    review_status TINYINT NULL,
    PRIMARY KEY (order_no)
) ENGINE = MEMORY DEFAULT CHARSET = utf8mb4;
INSERT INTO tmp_order_review_seed (
        order_no,
        username,
        isbn,
        quantity,
        status,
        shipping_fee,
        remark,
        create_time,
        rating,
        review_content,
        review_status
    )
VALUES (
        'TEST-ORD-202608-001',
        'reader',
        '9780000000001',
        1,
        'COMPLETED',
        0.00,
        '课程学习用书',
        '2026-08-02 09:15:00',
        5,
        '内容系统全面，适合入门和复习。',
        1
    ),
    (
        'TEST-ORD-202608-002',
        'alice',
        '9780000000002',
        2,
        'COMPLETED',
        6.00,
        '和同学一起购买',
        '2026-08-03 10:20:00',
        4,
        '练习题很实用，物流也很快。',
        1
    ),
    (
        'TEST-ORD-202608-003',
        'bob',
        '9780000000003',
        1,
        'COMPLETED',
        0.00,
        NULL,
        '2026-08-04 14:05:00',
        5,
        '装帧精美，经典作品值得收藏。',
        1
    ),
    (
        'TEST-ORD-202608-004',
        'carol',
        '9780000000004',
        1,
        'COMPLETED',
        6.00,
        '送给朋友',
        '2026-08-05 16:40:00',
        3,
        '故事不错，但纸张比预期薄一些。',
        1
    ),
    (
        'TEST-ORD-202608-005',
        'david',
        '9780000000005',
        1,
        'COMPLETED',
        0.00,
        '开发参考书',
        '2026-08-06 11:30:00',
        5,
        '案例清晰，对日常编码很有帮助。',
        1
    ),
    (
        'TEST-ORD-202608-006',
        'reader',
        '9780000000006',
        1,
        'COMPLETED',
        6.00,
        NULL,
        '2026-08-07 09:50:00',
        4,
        '建议很实在，阅读体验很好。',
        1
    ),
    (
        'TEST-ORD-202608-007',
        'alice',
        '9780000000007',
        2,
        'COMPLETED',
        0.00,
        '科普阅读',
        '2026-08-08 13:10:00',
        5,
        '语言通俗，读起来很有趣。',
        1
    ),
    (
        'TEST-ORD-202608-008',
        'bob',
        '9780000000008',
        1,
        'COMPLETED',
        8.00,
        NULL,
        '2026-08-09 18:25:00',
        4,
        '内容扎实，适合作为进阶读物。',
        1
    ),
    (
        'TEST-ORD-202608-009',
        'carol',
        '9780000000001',
        1,
        'COMPLETED',
        0.00,
        '数据库课程作业',
        '2026-08-10 10:05:00',
        5,
        '知识点覆盖完整，例子容易理解。',
        1
    ),
    (
        'TEST-ORD-202608-010',
        'david',
        '9780000000002',
        1,
        'COMPLETED',
        6.00,
        NULL,
        '2026-08-11 15:35:00',
        3,
        '整体不错，部分题目解析可以更详细。',
        0
    ),
    (
        'TEST-ORD-202608-011',
        'reader',
        '9780000000003',
        2,
        'COMPLETED',
        0.00,
        '家庭阅读',
        '2026-08-12 12:15:00',
        4,
        '发货及时，书籍没有破损。',
        1
    ),
    (
        'TEST-ORD-202608-012',
        'alice',
        '9780000000004',
        1,
        'COMPLETED',
        6.00,
        NULL,
        '2026-08-13 17:45:00',
        5,
        '很喜欢译文，阅读过程很愉快。',
        1
    ),
    (
        'TEST-ORD-202608-013',
        'bob',
        '9780000000005',
        1,
        'COMPLETED',
        0.00,
        '团队学习',
        '2026-08-14 09:35:00',
        2,
        '内容有价值，但这本书与我的需求不太匹配。',
        0
    ),
    (
        'TEST-ORD-202608-014',
        'carol',
        '9780000000006',
        2,
        'COMPLETED',
        8.00,
        NULL,
        '2026-08-15 14:20:00',
        4,
        '干货很多，值得反复阅读。',
        1
    ),
    (
        'TEST-ORD-202608-015',
        'david',
        '9780000000007',
        1,
        'COMPLETED',
        0.00,
        '周末阅读',
        '2026-08-16 19:10:00',
        5,
        '包装认真，图书质量很好。',
        0
    ),
    (
        'TEST-ORD-202608-016',
        'reader',
        '9780000000008',
        1,
        'PENDING_SHIPMENT',
        6.00,
        '请尽快发货',
        '2026-08-18 10:00:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-017',
        'alice',
        '9780000000001',
        2,
        'PENDING_SHIPMENT',
        0.00,
        NULL,
        '2026-08-19 11:25:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-018',
        'bob',
        '9780000000002',
        1,
        'PENDING_SHIPMENT',
        6.00,
        '工作日送达',
        '2026-08-20 15:10:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-019',
        'carol',
        '9780000000003',
        1,
        'PENDING_SHIPMENT',
        0.00,
        NULL,
        '2026-08-21 09:40:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-020',
        'david',
        '9780000000004',
        1,
        'SHIPPED',
        6.00,
        '请放在前台',
        '2026-08-20 13:30:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-021',
        'reader',
        '9780000000005',
        1,
        'SHIPPED',
        0.00,
        NULL,
        '2026-08-21 16:05:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-022',
        'alice',
        '9780000000006',
        2,
        'SHIPPED',
        8.00,
        '周末可收货',
        '2026-08-22 10:50:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-023',
        'bob',
        '9780000000007',
        1,
        'SHIPPED',
        0.00,
        NULL,
        '2026-08-23 14:15:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-024',
        'carol',
        '9780000000008',
        1,
        'PENDING_PAYMENT',
        6.00,
        '测试待支付订单',
        '2026-08-24 09:20:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-025',
        'david',
        '9780000000001',
        1,
        'PENDING_PAYMENT',
        0.00,
        NULL,
        '2026-08-24 12:35:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-026',
        'reader',
        '9780000000002',
        2,
        'PENDING_PAYMENT',
        8.00,
        '保留库存测试',
        '2026-08-25 10:10:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-027',
        'alice',
        '9780000000003',
        1,
        'PENDING_PAYMENT',
        6.00,
        NULL,
        '2026-08-25 17:55:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-028',
        'bob',
        '9780000000004',
        1,
        'CANCELLED',
        0.00,
        '测试已取消订单',
        '2026-08-18 08:45:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-029',
        'carol',
        '9780000000005',
        1,
        'CANCELLED',
        6.00,
        NULL,
        '2026-08-19 18:20:00',
        NULL,
        NULL,
        NULL
    ),
    (
        'TEST-ORD-202608-030',
        'david',
        '9780000000006',
        1,
        'CANCELLED',
        0.00,
        '用户取消测试',
        '2026-08-22 11:45:00',
        NULL,
        NULL,
        NULL
    );
-- Save which rows are new so sales_count is only incremented once.
DROP TEMPORARY TABLE IF EXISTS tmp_new_order_no;
CREATE TEMPORARY TABLE tmp_new_order_no (
    order_no VARCHAR(32) NOT NULL,
    PRIMARY KEY (order_no)
) ENGINE = MEMORY DEFAULT CHARSET = utf8mb4;
INSERT INTO tmp_new_order_no (order_no)
SELECT seed.order_no
FROM tmp_order_review_seed AS seed
    JOIN users AS user_account ON user_account.username = seed.username
    AND user_account.status = 1
    AND user_account.role = 'CUSTOMER'
    JOIN book AS book_info ON book_info.isbn = seed.isbn
    LEFT JOIN book_order AS existing_order ON existing_order.order_no = seed.order_no
WHERE existing_order.id IS NULL;
START TRANSACTION;
INSERT INTO book_order (
        order_no,
        user_id,
        status,
        total_amount,
        discount_amount,
        shipping_fee,
        payable_amount,
        expire_time,
        receiver_name,
        receiver_phone,
        receiver_address,
        remark,
        create_time,
        update_time,
        paid_time,
        shipped_time,
        completed_time,
        cancelled_time
    )
SELECT seed.order_no,
    user_account.id,
    seed.status,
    ROUND(book_info.sale_price * seed.quantity, 2),
    0.00,
    seed.shipping_fee,
    ROUND(
        book_info.sale_price * seed.quantity + seed.shipping_fee,
        2
    ),
    CASE
        WHEN seed.status = 'PENDING_PAYMENT' THEN DATE_ADD(NOW(), INTERVAL 30 MINUTE)
    END,
    CONCAT('测试收货人', RIGHT(seed.order_no, 3)),
    CONCAT('1390000', RIGHT(seed.order_no, 4)),
    CONCAT(
        '测试市书香路',
        CAST(RIGHT(seed.order_no, 3) AS UNSIGNED),
        '号'
    ),
    seed.remark,
    seed.create_time,
    CASE
        WHEN seed.status = 'COMPLETED' THEN DATE_ADD(seed.create_time, INTERVAL 4 DAY)
        WHEN seed.status = 'SHIPPED' THEN DATE_ADD(seed.create_time, INTERVAL 1 DAY)
        WHEN seed.status = 'PENDING_SHIPMENT' THEN DATE_ADD(seed.create_time, INTERVAL 10 MINUTE)
        WHEN seed.status = 'CANCELLED' THEN DATE_ADD(seed.create_time, INTERVAL 30 MINUTE)
        ELSE seed.create_time
    END,
    CASE
        WHEN seed.status IN ('PENDING_SHIPMENT', 'SHIPPED', 'COMPLETED') THEN DATE_ADD(seed.create_time, INTERVAL 10 MINUTE)
    END,
    CASE
        WHEN seed.status IN ('SHIPPED', 'COMPLETED') THEN DATE_ADD(seed.create_time, INTERVAL 1 DAY)
    END,
    CASE
        WHEN seed.status = 'COMPLETED' THEN DATE_ADD(seed.create_time, INTERVAL 4 DAY)
    END,
    CASE
        WHEN seed.status = 'CANCELLED' THEN DATE_ADD(seed.create_time, INTERVAL 30 MINUTE)
    END
FROM tmp_order_review_seed AS seed
    JOIN tmp_new_order_no AS new_order ON new_order.order_no = seed.order_no
    JOIN users AS user_account ON user_account.username = seed.username
    JOIN book AS book_info ON book_info.isbn = seed.isbn;
INSERT INTO order_item (
        order_id,
        book_id,
        book_title,
        isbn,
        unit_price,
        quantity,
        subtotal
    )
SELECT order_info.id,
    book_info.id,
    book_info.title,
    book_info.isbn,
    book_info.sale_price,
    seed.quantity,
    ROUND(book_info.sale_price * seed.quantity, 2)
FROM tmp_order_review_seed AS seed
    JOIN book_order AS order_info ON order_info.order_no = seed.order_no
    JOIN book AS book_info ON book_info.isbn = seed.isbn
    LEFT JOIN order_item AS existing_item ON existing_item.order_id = order_info.id
    AND existing_item.book_id = book_info.id
WHERE existing_item.id IS NULL;
UPDATE book AS book_info
    JOIN (
        SELECT item.book_id,
            SUM(item.quantity) AS completed_quantity
        FROM order_item AS item
            JOIN book_order AS order_info ON order_info.id = item.order_id
            JOIN tmp_new_order_no AS new_order ON new_order.order_no = order_info.order_no
        WHERE order_info.status = 'COMPLETED'
        GROUP BY item.book_id
    ) AS added_sales ON added_sales.book_id = book_info.id
SET book_info.sales_count = book_info.sales_count + added_sales.completed_quantity;
INSERT INTO book_review (
        user_id,
        book_id,
        order_item_id,
        rating,
        content,
        status,
        create_time,
        update_time
    )
SELECT order_info.user_id,
    item.book_id,
    item.id,
    seed.rating,
    seed.review_content,
    seed.review_status,
    DATE_ADD(order_info.completed_time, INTERVAL 1 HOUR),
    DATE_ADD(order_info.completed_time, INTERVAL 1 HOUR)
FROM tmp_order_review_seed AS seed
    JOIN book_order AS order_info ON order_info.order_no = seed.order_no
    JOIN order_item AS item ON item.order_id = order_info.id
    LEFT JOIN book_review AS existing_review ON existing_review.order_item_id = item.id
WHERE order_info.status = 'COMPLETED'
    AND seed.rating BETWEEN 1 AND 5
    AND existing_review.id IS NULL;
COMMIT;
-- Verification queries: expected counts are 30 orders, 30 order items, and 15 reviews.
SELECT status,
    COUNT(*) AS order_count
FROM book_order
WHERE order_no LIKE 'TEST-ORD-202608-%'
GROUP BY status
ORDER BY status;
SELECT COUNT(*) AS test_order_item_count
FROM order_item AS item
    JOIN book_order AS order_info ON order_info.id = item.order_id
WHERE order_info.order_no LIKE 'TEST-ORD-202608-%';
SELECT COUNT(*) AS test_review_count,
    SUM(review.status = 1) AS visible_review_count,
    SUM(review.status = 0) AS hidden_review_count
FROM book_review AS review
    JOIN order_item AS item ON item.id = review.order_item_id
    JOIN book_order AS order_info ON order_info.id = item.order_id
WHERE order_info.order_no LIKE 'TEST-ORD-202608-%';
