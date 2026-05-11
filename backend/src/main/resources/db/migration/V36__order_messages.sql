CREATE TABLE order_messages (
    id         UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id   UUID                     NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    sender_id  UUID                     NOT NULL REFERENCES users(id),
    content    TEXT                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_messages_order_id ON order_messages(order_id);
