ALTER TABLE refund_request
    ADD COLUMN bundle_aware BOOLEAN NOT NULL DEFAULT FALSE AFTER order_item_id;

CREATE TABLE bundle_refund_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    refund_no VARCHAR(40) NOT NULL,
    order_id BIGINT NOT NULL,
    bundle_application_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type ENUM('REFUND_ONLY','RETURN_REFUND') NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    review_remark VARCHAR(500) NULL,
    reviewer_id BIGINT NULL,
    reviewed_time DATETIME(6) NULL,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_bundle_refund_no UNIQUE (refund_no),
    KEY idx_bundle_refund_order (order_id),
    KEY idx_bundle_refund_application (bundle_application_id),
    KEY idx_bundle_refund_user (user_id),
    KEY idx_bundle_refund_status (status),
    KEY idx_bundle_refund_create_time (create_time),
    CONSTRAINT fk_bundle_refund_order FOREIGN KEY (order_id) REFERENCES book_order(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bundle_refund_application FOREIGN KEY (bundle_application_id) REFERENCES order_bundle_application(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bundle_refund_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bundle_refund_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_bundle_refund_amount CHECK (amount > 0)
) ENGINE = InnoDB;

CREATE TABLE bundle_refund_request_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    book_title VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    allocated_discount DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_bundle_refund_request_item UNIQUE (request_id, order_item_id),
    KEY idx_bundle_refund_item_order_item (order_item_id),
    KEY idx_bundle_refund_item_book (book_id),
    CONSTRAINT fk_bundle_refund_item_request FOREIGN KEY (request_id) REFERENCES bundle_refund_request(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bundle_refund_item_order_item FOREIGN KEY (order_item_id) REFERENCES order_item(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bundle_refund_item_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE RESTRICT,
    CONSTRAINT chk_bundle_refund_item_amounts CHECK (sale_price >= 0 AND allocated_discount >= 0 AND quantity > 0 AND amount > 0)
) ENGINE = InnoDB;
