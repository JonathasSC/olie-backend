CREATE TABLE wear_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    category_id UUID REFERENCES categories(id),
    expected_lifespan INTEGER NOT NULL,
    expected_lifespan_unit VARCHAR(50) NOT NULL,
    replacement_alert_sent BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_wear_items_user_id ON wear_items(user_id);
CREATE INDEX idx_wear_items_category_id ON wear_items(category_id);

-- cada linha é uma unidade do item; a unidade em uso (ciclo atual) é a que tem removal_date nulo
CREATE TABLE wear_item_cycles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wear_item_id UUID NOT NULL REFERENCES wear_items(id) ON DELETE CASCADE,
    purchase_date DATE NOT NULL,
    installation_date DATE,
    removal_date DATE,
    purchase_value NUMERIC,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_wear_item_cycles_wear_item_id ON wear_item_cycles(wear_item_id);
CREATE UNIQUE INDEX uq_wear_item_cycles_current ON wear_item_cycles(wear_item_id) WHERE removal_date IS NULL;

ALTER TABLE notifications ADD COLUMN wear_item_id UUID REFERENCES wear_items(id) ON DELETE SET NULL;

CREATE INDEX idx_notifications_wear_item_id ON notifications(wear_item_id);
