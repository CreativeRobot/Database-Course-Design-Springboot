ALTER TABLE book
    ADD COLUMN pre_sale TINYINT(1) NOT NULL DEFAULT 0 AFTER stock,
    ADD COLUMN pre_sale_release_time DATETIME(6) NULL AFTER pre_sale,
    ADD CONSTRAINT chk_book_pre_sale_release
        CHECK (pre_sale = 0 OR pre_sale_release_time IS NOT NULL);

ALTER TABLE order_item
    ADD COLUMN pre_sale TINYINT(1) NOT NULL DEFAULT 0 AFTER refunded_quantity,
    ADD COLUMN pre_sale_release_time DATETIME(6) NULL AFTER pre_sale,
    ADD CONSTRAINT chk_order_item_pre_sale_release
        CHECK (pre_sale = 0 OR pre_sale_release_time IS NOT NULL);
