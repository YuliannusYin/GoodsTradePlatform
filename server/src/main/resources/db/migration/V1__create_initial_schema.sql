-- ============================================================
-- V1: Create initial database schema
-- ============================================================

CREATE TABLE users (
    id              VARCHAR(36)    PRIMARY KEY,
    email           VARCHAR(255)   NOT NULL UNIQUE,
    username        VARCHAR(255)   NOT NULL UNIQUE,
    password        VARCHAR(255)   NOT NULL,
    role            VARCHAR(10)    NOT NULL DEFAULT 'USER',
    avatar_url      VARCHAR(500),
    bio             VARCHAR(500),
    balance         DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    is_protected    BOOLEAN        NOT NULL DEFAULT false
);

CREATE TABLE products (
    id              VARCHAR(36)    PRIMARY KEY,
    name            VARCHAR(255)   NOT NULL,
    description     TEXT,
    image_urls      JSONB,
    price           DOUBLE PRECISION NOT NULL,
    quantity        INTEGER        NOT NULL DEFAULT 0,
    category        VARCHAR(30)    NOT NULL,
    condition       VARCHAR(15),
    source          VARCHAR(20),
    seller_id       VARCHAR(36)    REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE orders (
    id                  VARCHAR(36)    PRIMARY KEY,
    status              VARCHAR(15)    NOT NULL DEFAULT 'PENDING',
    price               DOUBLE PRECISION NOT NULL,
    payment_method      VARCHAR(25)    NOT NULL,
    address             TEXT           NOT NULL,
    delivery_method     VARCHAR(30)    NOT NULL,
    received            TIMESTAMP,
    expected_delivery   TIMESTAMP,
    user_id             VARCHAR(36)    NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE order_items (
    id              VARCHAR(36)    PRIMARY KEY,
    amount          INTEGER        NOT NULL,
    price           DOUBLE PRECISION NOT NULL,
    product_id      VARCHAR(36)    NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    order_id        VARCHAR(36)    NOT NULL REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE favorites (
    id              VARCHAR(36)    PRIMARY KEY,
    created_at      TIMESTAMP      NOT NULL,
    user_id         VARCHAR(36)    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id      VARCHAR(36)    NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)
);

CREATE TABLE reviews (
    id              VARCHAR(36)    PRIMARY KEY,
    rating          INTEGER        NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment         TEXT,
    created_at      TIMESTAMP      NOT NULL,
    user_id         VARCHAR(36)    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id      VARCHAR(36)    NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)
);

-- Indexes
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_name_lower ON products(LOWER(name));

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

CREATE INDEX idx_favorites_user_id ON favorites(user_id);
CREATE INDEX idx_favorites_product_id ON favorites(product_id);

CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
