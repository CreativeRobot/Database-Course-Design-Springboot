# Order Expiration Design

`book_order.expire_time` stores the payment deadline for a pending-payment
order. New orders use a 30-minute payment window.

The scheduler scans every 60 seconds. It only processes rows whose status is
`PENDING_PAYMENT` and whose `expire_time` is earlier than or equal to the
current time. The status update is conditional, so a payment or manual
cancellation that wins the race prevents duplicate stock restoration.

After a successful status update, each order item is restored to inventory and
an `ORDER_CANCEL_RETURN` record is written to `inventory_log` in the same
transaction.
