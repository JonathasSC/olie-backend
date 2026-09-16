CREATE TABLE planned_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    estimated_value NUMERIC NOT NULL,
    estimated_date DATE NOT NULL,
    category_id UUID REFERENCES categories(id),
    date_alert_sent BOOLEAN NOT NULL DEFAULT false,
    balance_alert_sent BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_planned_items_user_id ON planned_items(user_id);
CREATE INDEX idx_planned_items_category_id ON planned_items(category_id);
