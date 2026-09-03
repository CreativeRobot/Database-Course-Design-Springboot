-- Demo catalog data for manual testing.
-- The fixed ISBNs and names make this migration idempotent on reruns.

INSERT INTO publisher (name, phone, address, introduction, create_time, update_time)
SELECT '演示数据出版社', '010-55550001', '北京市海淀区演示路1号', '用于预售、折扣和组合包功能测试的出版社。', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM publisher WHERE name = '演示数据出版社');

SET @demo_publisher_id = (SELECT id FROM publisher WHERE name = '演示数据出版社' LIMIT 1);

INSERT INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT '演示文学', NULL, 10, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '演示文学');
INSERT INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT '演示计算机', NULL, 20, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '演示计算机');
INSERT INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT '演示教育', NULL, 30, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '演示教育');
INSERT INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT '演示经济', NULL, 40, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '演示经济');
INSERT INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT '演示生活', NULL, 50, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '演示生活');
INSERT INTO category (name, parent_id, sort_order, status, create_time, update_time)
SELECT '演示儿童', NULL, 60, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '演示儿童');

SET @cat_literature_id = (SELECT id FROM category WHERE name = '演示文学' LIMIT 1);
SET @cat_computer_id = (SELECT id FROM category WHERE name = '演示计算机' LIMIT 1);
SET @cat_education_id = (SELECT id FROM category WHERE name = '演示教育' LIMIT 1);
SET @cat_economics_id = (SELECT id FROM category WHERE name = '演示经济' LIMIT 1);
SET @cat_life_id = (SELECT id FROM category WHERE name = '演示生活' LIMIT 1);
SET @cat_children_id = (SELECT id FROM category WHERE name = '演示儿童' LIMIT 1);

INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008001', '演示文学：远方来信', @demo_publisher_id, 59.00, 49.00, 80, 128,
       0, NULL, '2025-05-01', '第1版', 280, '用于首页热门分类和折扣展示的测试图书。',
       'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008001');
INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008002', '演示文学：月光书店', @demo_publisher_id, 68.00, 54.00, 60, 96,
       0, NULL, '2025-08-15', '第1版', 320, '折扣测试图书，与其他文学图书组成组合包。',
       'https://images.unsplash.com/photo-1511108690759-009cbe9f8377?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008002');
INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008003', '演示计算机：从零开始写代码', @demo_publisher_id, 99.00, 79.00, 120, 210,
       0, NULL, '2025-10-01', '第2版', 460, '用于热门分类、折扣和组合包联动测试的计算机图书。',
       'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008003');
INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008004', '演示计算机：数据库实践指南', @demo_publisher_id, 109.00, 89.00, 90, 175,
       0, NULL, '2026-01-20', '第1版', 520, '数据库课程和组合包测试用书。',
       'https://images.unsplash.com/photo-1555062734-4c4d9d3e6f1f?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008004');
INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008005', '演示教育：学习方法论', @demo_publisher_id, 79.00, 65.00, 70, 84,
       1, '2026-12-15 10:00:00', NULL, '第1版', 300, '预售测试图书，发售时间在未来。',
       'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008005');
INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008006', '演示经济：看懂商业世界', @demo_publisher_id, 88.00, 66.00, 50, 142,
       1, '2026-11-20 10:00:00', NULL, '第1版', 360, '预售并带有折扣价格的测试图书。',
       'https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008006');
INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008007', '演示生活：一日三餐', @demo_publisher_id, 72.00, 58.00, 45, 61,
       0, NULL, '2026-03-10', '第1版', 240, '生活分类折扣测试图书。',
       'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008007');
INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008008', '演示儿童：小小探险家', @demo_publisher_id, 49.00, 39.00, 100, 118,
       1, '2026-10-30 10:00:00', NULL, '第1版', 180, '预售测试图书，方便验证首页预售日历。',
       'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008008');

SET @book_literature_1 = (SELECT id FROM book WHERE isbn = '9787300008001' LIMIT 1);
SET @book_literature_2 = (SELECT id FROM book WHERE isbn = '9787300008002' LIMIT 1);
SET @book_computer_1 = (SELECT id FROM book WHERE isbn = '9787300008003' LIMIT 1);
SET @book_computer_2 = (SELECT id FROM book WHERE isbn = '9787300008004' LIMIT 1);
SET @book_education = (SELECT id FROM book WHERE isbn = '9787300008005' LIMIT 1);
SET @book_economics = (SELECT id FROM book WHERE isbn = '9787300008006' LIMIT 1);
SET @book_life = (SELECT id FROM book WHERE isbn = '9787300008007' LIMIT 1);
SET @book_children = (SELECT id FROM book WHERE isbn = '9787300008008' LIMIT 1);

INSERT INTO book_category (book_id, category_id)
SELECT @book_literature_1, @cat_literature_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_literature_1 AND category_id = @cat_literature_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_literature_2, @cat_literature_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_literature_2 AND category_id = @cat_literature_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_computer_1, @cat_computer_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_computer_1 AND category_id = @cat_computer_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_computer_2, @cat_computer_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_computer_2 AND category_id = @cat_computer_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_education, @cat_education_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_education AND category_id = @cat_education_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_economics, @cat_economics_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_economics AND category_id = @cat_economics_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_life, @cat_life_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_life AND category_id = @cat_life_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_children, @cat_children_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_children AND category_id = @cat_children_id);

INSERT INTO book_bundle (name, description, bundle_price, status, version, create_time, update_time)
SELECT '演示组合包：文学入门套装', '包含两本演示文学图书，组合购买更优惠。', 89.00, 'ACTIVE', 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle WHERE name = '演示组合包：文学入门套装');
INSERT INTO book_bundle (name, description, bundle_price, status, version, create_time, update_time)
SELECT '演示组合包：编程与数据库套装', '包含编程基础和数据库实践两本图书。', 149.00, 'ACTIVE', 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle WHERE name = '演示组合包：编程与数据库套装');
INSERT INTO book_bundle (name, description, bundle_price, status, version, create_time, update_time)
SELECT '演示组合包：全栈学习套装', '与编程和数据库套装部分重叠，用于测试重叠组合选择规则。', 205.00, 'ACTIVE', 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle WHERE name = '演示组合包：全栈学习套装');

SET @bundle_literature = (SELECT id FROM book_bundle WHERE name = '演示组合包：文学入门套装' LIMIT 1);
SET @bundle_computer = (SELECT id FROM book_bundle WHERE name = '演示组合包：编程与数据库套装' LIMIT 1);
SET @bundle_full = (SELECT id FROM book_bundle WHERE name = '演示组合包：全栈学习套装' LIMIT 1);

INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_literature, @book_literature_1, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_literature AND book_id = @book_literature_1);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_literature, @book_literature_2, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_literature AND book_id = @book_literature_2);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_computer, @book_computer_1, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_computer AND book_id = @book_computer_1);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_computer, @book_computer_2, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_computer AND book_id = @book_computer_2);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_full, @book_computer_1, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_full AND book_id = @book_computer_1);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_full, @book_computer_2, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_full AND book_id = @book_computer_2);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_full, @book_education, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_full AND book_id = @book_education);
