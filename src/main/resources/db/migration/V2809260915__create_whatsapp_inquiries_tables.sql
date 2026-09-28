CREATE TABLE contact_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_contact_categories_user_name ON contact_categories(user_id, lower(name));

CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    do_not_contact BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_contacts_user_phone UNIQUE (user_id, phone)
);

CREATE TABLE contact_category_assignments (
    contact_id UUID NOT NULL REFERENCES contacts(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES contact_categories(id) ON DELETE CASCADE,
    PRIMARY KEY (contact_id, category_id)
);

CREATE INDEX idx_contact_category_assignments_category_id ON contact_category_assignments(category_id);

CREATE TABLE inquiry_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    storage_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    size_bytes BIGINT NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_inquiry_photos_user_id ON inquiry_photos(user_id);

CREATE TABLE inquiries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    pause_reason VARCHAR(50),
    simulated BOOLEAN NOT NULL DEFAULT false,
    next_send_at TIMESTAMP,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_inquiries_user_id ON inquiries(user_id);
CREATE INDEX idx_inquiries_status_next_send_at ON inquiries(status, next_send_at);

CREATE TABLE inquiry_categories (
    inquiry_id UUID NOT NULL REFERENCES inquiries(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES contact_categories(id) ON DELETE CASCADE,
    PRIMARY KEY (inquiry_id, category_id)
);

CREATE TABLE inquiry_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_id UUID NOT NULL REFERENCES inquiries(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    photo_id UUID REFERENCES inquiry_photos(id)
);

CREATE INDEX idx_inquiry_items_inquiry_id ON inquiry_items(inquiry_id);
CREATE INDEX idx_inquiry_items_photo_id ON inquiry_items(photo_id);

-- nome e telefone são copiados do contato: o histórico continua legível se o contato mudar ou for removido
CREATE TABLE inquiry_recipients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inquiry_id UUID NOT NULL REFERENCES inquiries(id) ON DELETE CASCADE,
    contact_id UUID REFERENCES contacts(id) ON DELETE SET NULL,
    position INTEGER NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    message_text TEXT,
    sent_parts INTEGER NOT NULL DEFAULT 0,
    attempts INTEGER NOT NULL DEFAULT 0,
    failure_reason TEXT,
    sent_at TIMESTAMP
);

CREATE INDEX idx_inquiry_recipients_inquiry_id ON inquiry_recipients(inquiry_id);
CREATE INDEX idx_inquiry_recipients_contact_id_sent_at ON inquiry_recipients(contact_id, sent_at);
