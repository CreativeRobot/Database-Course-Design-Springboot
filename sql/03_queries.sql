-- Typical queries for demonstration and database-course defense.

USE bookstore;

-- 1. Public catalog with publisher and category names.
SELECT
    b.id,
    b.isbn,
    b.title,
    p.name AS publisher_name,
    GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ', ') AS categories,
    b.sale_price,
    b.stock,
    b.status
FROM book b
JOIN publisher p ON p.id = b.publisher_id
LEFT JOIN book_category bc ON bc.book_id = b.id
LEFT JOIN category c ON c.id = bc.category_id
WHERE b.status = 'ON_SALE'
GROUP BY b.id, b.isbn, b.title, p.name, b.sale_price, b.stock, b.status
ORDER BY b.id DESC;

-- 2. A user's order list.
SELECT
    o.id,
    o.order_no,
    o.status,
    o.total_amount,
    o.payable_amount,
    o.expire_time,
    o.create_time,
    COUNT(oi.id) AS item_count
FROM book_order o
LEFT JOIN order_item oi ON oi.order_id = o.id
WHERE o.user_id = 2
GROUP BY o.id, o.order_no, o.status, o.total_amount, o.payable_amount,
         o.create_time
ORDER BY o.create_time DESC, o.id DESC;

-- 3. Complete order detail with historical item snapshots.
SELECT
    o.order_no,
    o.status,
    o.receiver_name,
    o.receiver_phone,
    o.receiver_address,
    oi.book_title,
    oi.isbn,
    oi.unit_price,
    oi.quantity,
    oi.subtotal
FROM book_order o
JOIN order_item oi ON oi.order_id = o.id
WHERE o.order_no = 'BS202608010001'
ORDER BY oi.id;

-- 4. Monthly sales report.
SELECT
    DATE_FORMAT(o.completed_time, '%Y-%m') AS sale_month,
    COUNT(DISTINCT o.id) AS completed_orders,
    SUM(oi.quantity) AS sold_quantity,
    SUM(oi.subtotal) AS merchandise_amount
FROM book_order o
JOIN order_item oi ON oi.order_id = o.id
WHERE o.status = 'COMPLETED'
GROUP BY DATE_FORMAT(o.completed_time, '%Y-%m')
ORDER BY sale_month;

-- 5. Best-selling books.
SELECT
    oi.book_id,
    oi.book_title,
    SUM(oi.quantity) AS sold_quantity,
    SUM(oi.subtotal) AS sales_amount
FROM order_item oi
JOIN book_order o ON o.id = oi.order_id
WHERE o.status IN ('PENDING_SHIPMENT', 'SHIPPED', 'COMPLETED')
GROUP BY oi.book_id, oi.book_title
ORDER BY sold_quantity DESC, sales_amount DESC
LIMIT 10;

-- 6. Low-stock warning.
SELECT
    id,
    isbn,
    title,
    stock,
    status
FROM book
WHERE status = 'ON_SALE'
  AND stock <= 5
ORDER BY stock ASC, id ASC;

-- 7. Review summary by book.
SELECT
    b.id,
    b.title,
    COUNT(r.id) AS review_count,
    ROUND(AVG(r.rating), 2) AS average_rating
FROM book b
LEFT JOIN book_review r
    ON r.book_id = b.id
   AND r.status = 1
GROUP BY b.id, b.title
ORDER BY average_rating DESC, review_count DESC;

-- 8. Inventory audit: the latest after_stock should match book.stock.
WITH latest_inventory AS (
    SELECT
        il.book_id,
        il.after_stock,
        ROW_NUMBER() OVER (
            PARTITION BY il.book_id
            ORDER BY il.create_time DESC, il.id DESC
        ) AS row_no
    FROM inventory_log il
)
SELECT
    b.id,
    b.title,
    b.stock AS current_stock,
    li.after_stock AS latest_logged_stock,
    b.stock - li.after_stock AS difference
FROM book b
JOIN latest_inventory li
    ON li.book_id = b.id
   AND li.row_no = 1
WHERE b.stock <> li.after_stock;
