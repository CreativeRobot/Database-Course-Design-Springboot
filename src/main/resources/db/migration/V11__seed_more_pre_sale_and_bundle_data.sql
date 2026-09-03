-- Additional demo data for testing the release calendar and bundle offers.
-- Fixed ISBNs and names keep this migration idempotent on reruns.

SET @demo_publisher_id = (
    SELECT id FROM publisher WHERE name = '演示数据出版社' LIMIT 1
);
SET @cat_literature_id = (
    SELECT id FROM category WHERE name = '演示文学' LIMIT 1
);
SET @cat_computer_id = (
    SELECT id FROM category WHERE name = '演示计算机' LIMIT 1
);
SET @cat_education_id = (
    SELECT id FROM category WHERE name = '演示教育' LIMIT 1
);
SET @cat_economics_id = (
    SELECT id FROM category WHERE name = '演示经济' LIMIT 1
);
SET @cat_life_id = (
    SELECT id FROM category WHERE name = '演示生活' LIMIT 1
);
SET @cat_children_id = (
    SELECT id FROM category WHERE name = '演示儿童' LIMIT 1
);

INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008009', '演示预售：星河边的图书馆', @demo_publisher_id, 76.00, 62.00, 120, 0,
       1, '2026-10-15 10:00:00', NULL, '第1版', 336, '用于测试十月预售日历和限时折扣。',
       'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008009');

INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008010', '演示预售：产品设计入门', @demo_publisher_id, 92.00, 78.00, 100, 0,
       1, '2026-10-15 14:00:00', NULL, '第1版', 410, '与另一部图书同日发售，用于测试日历横向卡片。',
       'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008010');

INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008011', '演示预售：数据库系统进阶', @demo_publisher_id, 128.00, 99.00, 90, 0,
       1, '2026-10-15 18:00:00', NULL, '第2版', 560, '与前两本图书同日发售，用于测试同日多书。',
       'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008011');

INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008012', '演示预售：儿童科学实验室', @demo_publisher_id, 58.00, 49.00, 150, 0,
       1, '2026-11-05 09:30:00', NULL, '第1版', 192, '用于测试十一月预售图书。',
       'https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008012');

INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008013', '演示预售：商业增长案例集', @demo_publisher_id, 105.00, 86.00, 75, 0,
       1, '2026-11-20 10:00:00', NULL, '第1版', 428, '用于测试经济类预售图书和组合包。',
       'https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008013');

INSERT INTO book (
    isbn, title, publisher_id, original_price, sale_price, stock, sales_count,
    pre_sale, pre_sale_release_time, publish_date, edition, pages, description,
    cover_url, status, create_time, update_time
)
SELECT '9787300008014', '演示预售：写给成年人的生活美学', @demo_publisher_id, 84.00, 69.00, 80, 0,
       1, '2026-12-15 15:00:00', NULL, '第1版', 288, '用于测试十二月预售图书。',
       'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600', 'ON_SALE', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book WHERE isbn = '9787300008014');

SET @book_presale_1 = (SELECT id FROM book WHERE isbn = '9787300008009' LIMIT 1);
SET @book_presale_2 = (SELECT id FROM book WHERE isbn = '9787300008010' LIMIT 1);
SET @book_presale_3 = (SELECT id FROM book WHERE isbn = '9787300008011' LIMIT 1);
SET @book_presale_4 = (SELECT id FROM book WHERE isbn = '9787300008012' LIMIT 1);
SET @book_presale_5 = (SELECT id FROM book WHERE isbn = '9787300008013' LIMIT 1);
SET @book_presale_6 = (SELECT id FROM book WHERE isbn = '9787300008014' LIMIT 1);

INSERT INTO book_category (book_id, category_id)
SELECT @book_presale_1, @cat_literature_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_presale_1 AND category_id = @cat_literature_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_presale_2, @cat_computer_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_presale_2 AND category_id = @cat_computer_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_presale_3, @cat_computer_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_presale_3 AND category_id = @cat_computer_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_presale_4, @cat_children_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_presale_4 AND category_id = @cat_children_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_presale_5, @cat_economics_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_presale_5 AND category_id = @cat_economics_id);
INSERT INTO book_category (book_id, category_id)
SELECT @book_presale_6, @cat_life_id
WHERE NOT EXISTS (SELECT 1 FROM book_category WHERE book_id = @book_presale_6 AND category_id = @cat_life_id);

INSERT INTO book_bundle (
    name, description, bundle_price, status, home_pinned, home_priority,
    version, create_time, update_time
)
SELECT '测试组合包：预售编程三件套', '三本预售图书组合购买，适合测试组合包首页展示。', 219.00,
       'ACTIVE', 1, 10, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle WHERE name = '测试组合包：预售编程三件套');

INSERT INTO book_bundle (
    name, description, bundle_price, status, home_pinned, home_priority,
    version, create_time, update_time
)
SELECT '测试组合包：亲子阅读套装', '儿童预售图书与文学图书组合，测试跨类别组合。', 99.00,
       'ACTIVE', 1, 20, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle WHERE name = '测试组合包：亲子阅读套装');

INSERT INTO book_bundle (
    name, description, bundle_price, status, home_pinned, home_priority,
    version, create_time, update_time
)
SELECT '测试组合包：商业与生活精选', '经济类和生活类图书组合，测试普通组合包排序。', 139.00,
       'ACTIVE', 0, 0, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle WHERE name = '测试组合包：商业与生活精选');

INSERT INTO book_bundle (
    name, description, bundle_price, status, home_pinned, home_priority,
    version, create_time, update_time
)
SELECT '测试组合包：全站预售挑战包', '四本不同类别预售图书组合，测试多本组合包。', 269.00,
       'ACTIVE', 0, 0, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle WHERE name = '测试组合包：全站预售挑战包');

SET @bundle_presale = (SELECT id FROM book_bundle WHERE name = '测试组合包：预售编程三件套' LIMIT 1);
SET @bundle_family = (SELECT id FROM book_bundle WHERE name = '测试组合包：亲子阅读套装' LIMIT 1);
SET @bundle_business = (SELECT id FROM book_bundle WHERE name = '测试组合包：商业与生活精选' LIMIT 1);
SET @bundle_all = (SELECT id FROM book_bundle WHERE name = '测试组合包：全站预售挑战包' LIMIT 1);

INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_presale, @book_presale_1, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_presale AND book_id = @book_presale_1);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_presale, @book_presale_2, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_presale AND book_id = @book_presale_2);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_presale, @book_presale_3, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_presale AND book_id = @book_presale_3);

INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_family, @book_presale_1, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_family AND book_id = @book_presale_1);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_family, @book_presale_4, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_family AND book_id = @book_presale_4);

INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_business, @book_presale_5, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_business AND book_id = @book_presale_5);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_business, @book_presale_6, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_business AND book_id = @book_presale_6);

INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_all, @book_presale_2, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_all AND book_id = @book_presale_2);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_all, @book_presale_3, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_all AND book_id = @book_presale_3);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_all, @book_presale_4, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_all AND book_id = @book_presale_4);
INSERT INTO book_bundle_item (bundle_id, book_id, create_time)
SELECT @bundle_all, @book_presale_5, CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM book_bundle_item WHERE bundle_id = @bundle_all AND book_id = @book_presale_5);
