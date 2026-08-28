-- Refund/return transaction support.
ALTER TABLE book_order
    ADD COLUMN refunded_amount DECIMAL(10, 2) NOT NULL DEFAULT 0 AFTER payable_amount,
    ADD CONSTRAINT chk_order_refunded_amount CHECK (refunded_amount >= 0 AND refunded_amount <= payable_amount);

ALTER TABLE order_item
    ADD COLUMN refunded_quantity INT NOT NULL DEFAULT 0 AFTER quantity,
    ADD CONSTRAINT chk_order_item_refunded_quantity CHECK (refunded_quantity >= 0 AND refunded_quantity <= quantity);

ALTER TABLE inventory_log
    MODIFY change_type ENUM('PURCHASE_IN','ORDER_OUT','ORDER_CANCEL_RETURN','REFUND_RETURN','MANUAL_ADJUSTMENT') NOT NULL,
    DROP CHECK chk_inventory_type_business_rules,
    ADD CONSTRAINT chk_inventory_type_business_rules CHECK (
        (change_type = 'ORDER_OUT' AND change_quantity < 0 AND order_id IS NOT NULL)
        OR (change_type IN ('ORDER_CANCEL_RETURN', 'REFUND_RETURN') AND change_quantity > 0 AND order_id IS NOT NULL)
        OR (change_type = 'PURCHASE_IN' AND change_quantity > 0 AND order_id IS NULL)
        OR (change_type = 'MANUAL_ADJUSTMENT' AND order_id IS NULL)
    );

CREATE TABLE refund_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    refund_no VARCHAR(40) NOT NULL,
    order_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type ENUM('REFUND_ONLY','RETURN_REFUND') NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    quantity INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    review_remark VARCHAR(500) NULL,
    reviewer_id BIGINT NULL,
    reviewed_time DATETIME(6) NULL,
    create_time DATETIME(6) NULL,
    update_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refund_no UNIQUE (refund_no),
    KEY idx_refund_order (order_id),
    KEY idx_refund_status (status),
    KEY idx_refund_create_time (create_time),
    CONSTRAINT fk_refund_order FOREIGN KEY (order_id) REFERENCES book_order(id) ON DELETE RESTRICT,
    CONSTRAINT fk_refund_item FOREIGN KEY (order_item_id) REFERENCES order_item(id) ON DELETE RESTRICT,
    CONSTRAINT fk_refund_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_refund_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_refund_quantity CHECK (quantity > 0),
    CONSTRAINT chk_refund_amount CHECK (amount > 0)
) ENGINE = InnoDB;
