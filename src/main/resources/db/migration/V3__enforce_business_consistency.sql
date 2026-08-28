-- Enforce address, money, and inventory invariants on existing Flyway-managed schemas.

-- Keep the oldest default address when legacy data contains duplicates.
UPDATE user_address address
JOIN (
    SELECT user_id, MIN(id) AS keep_id
    FROM user_address
    WHERE default_address = TRUE
    GROUP BY user_id
) keep_address
    ON keep_address.user_id = address.user_id
SET address.default_address = (address.id = keep_address.keep_id)
WHERE address.default_address = TRUE
  AND address.id <> keep_address.keep_id;

-- MySQL unique indexes allow multiple NULL values, so this generated key
-- enforces at most one TRUE default address for each user.
ALTER TABLE user_address
    ADD COLUMN default_user_id BIGINT
        GENERATED ALWAYS AS (IF(default_address, user_id, NULL)) STORED,
    ADD CONSTRAINT uk_user_default_address UNIQUE (default_user_id);

ALTER TABLE book_order
    ADD CONSTRAINT chk_order_discount_not_over_total
        CHECK (discount_amount <= total_amount),
    ADD CONSTRAINT chk_order_payable_formula
        CHECK (payable_amount = total_amount - discount_amount + shipping_fee);

ALTER TABLE order_item
    ADD CONSTRAINT chk_order_item_subtotal_formula
        CHECK (subtotal = unit_price * quantity);

ALTER TABLE inventory_log
    ADD CONSTRAINT chk_inventory_stock_transition
        CHECK (after_stock = before_stock + change_quantity),
    ADD CONSTRAINT chk_inventory_non_zero_change
        CHECK (change_quantity <> 0),
    ADD CONSTRAINT chk_inventory_type_business_rules
        CHECK (
            (change_type = 'ORDER_OUT'
                AND change_quantity < 0
                AND order_id IS NOT NULL)
            OR
            (change_type = 'ORDER_CANCEL_RETURN'
                AND change_quantity > 0
                AND order_id IS NOT NULL)
            OR
            (change_type = 'PURCHASE_IN'
                AND change_quantity > 0
                AND order_id IS NULL)
            OR
            (change_type = 'MANUAL_ADJUSTMENT'
                AND order_id IS NULL)
        );
