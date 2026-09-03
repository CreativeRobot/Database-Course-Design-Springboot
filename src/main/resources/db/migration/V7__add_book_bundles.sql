CREATE TABLE book_bundle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    bundle_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    create_time DATETIME(6) NOT NULL,
    update_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_book_bundle_status_id (status, id),
    CONSTRAINT chk_book_bundle_price CHECK (bundle_price >= 0),
    CONSTRAINT chk_book_bundle_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE = InnoDB;

CREATE TABLE book_bundle_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    bundle_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    create_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_book_bundle_item_bundle_book UNIQUE (bundle_id, book_id),
    KEY idx_book_bundle_item_book (book_id),
    CONSTRAINT fk_book_bundle_item_bundle FOREIGN KEY (bundle_id) REFERENCES book_bundle (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_book_bundle_item_book FOREIGN KEY (book_id) REFERENCES book (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE order_bundle_application (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    bundle_id BIGINT NOT NULL,
    bundle_name VARCHAR(100) NOT NULL,
    bundle_price DECIMAL(10, 2) NOT NULL,
    regular_amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_order_bundle_application_order_bundle UNIQUE (order_id, bundle_id),
    KEY idx_order_bundle_application_order (order_id),
    CONSTRAINT fk_order_bundle_application_order FOREIGN KEY (order_id) REFERENCES book_order (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_order_bundle_application_amounts CHECK (
        bundle_price >= 0 AND regular_amount >= 0 AND discount_amount >= 0
    )
) ENGINE = InnoDB;

CREATE TABLE order_bundle_application_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    book_title VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    allocated_discount DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT uk_order_bundle_application_item UNIQUE (application_id, order_item_id),
    KEY idx_order_bundle_application_item_order_item (order_item_id),
    CONSTRAINT fk_order_bundle_application_item_application FOREIGN KEY (application_id)
        REFERENCES order_bundle_application (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_order_bundle_application_item_order_item FOREIGN KEY (order_item_id)
        REFERENCES order_item (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_order_bundle_application_item_book FOREIGN KEY (book_id)
        REFERENCES book (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_order_bundle_application_item_amounts CHECK (
        sale_price >= 0 AND allocated_discount >= 0 AND quantity > 0
    )
) ENGINE = InnoDB;

ALTER TABLE order_item
    ADD COLUMN discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0 AFTER subtotal,
    ADD COLUMN paid_subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0 AFTER discount_amount,
    ADD CONSTRAINT chk_order_item_discount_amount CHECK (discount_amount >= 0),
    ADD CONSTRAINT chk_order_item_paid_subtotal CHECK (paid_subtotal >= 0);
