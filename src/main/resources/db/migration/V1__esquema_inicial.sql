-- Esquema inicial de la tienda TresD

CREATE TABLE categories (
    slug        VARCHAR(40)  PRIMARY KEY,
    name        VARCHAR(80)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    icon        VARCHAR(30)  NOT NULL,
    hue         INTEGER      NOT NULL,
    position    INTEGER      NOT NULL
);

CREATE TABLE products (
    id            VARCHAR(140)  PRIMARY KEY,           -- slug legible, ej: dragon-articulado
    name          VARCHAR(120)  NOT NULL,
    category_slug VARCHAR(40)   NOT NULL REFERENCES categories (slug),
    price         INTEGER       NOT NULL,
    old_price     INTEGER,
    description   VARCHAR(2000) NOT NULL,
    material      VARCHAR(10)   NOT NULL,
    height_cm     DOUBLE PRECISION,
    print_hours   DOUBLE PRECISION,
    rating        DOUBLE PRECISION,
    is_new        BOOLEAN       NOT NULL DEFAULT FALSE,
    best_seller   BOOLEAN       NOT NULL DEFAULT FALSE,
    hidden        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_products_category ON products (category_slug);

CREATE TABLE product_colors (
    product_id VARCHAR(140) NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    position   INTEGER      NOT NULL,
    color      VARCHAR(7)   NOT NULL,
    PRIMARY KEY (product_id, position)
);

CREATE TABLE product_images (
    product_id VARCHAR(140) NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    position   INTEGER      NOT NULL,
    url        VARCHAR(1000) NOT NULL,                 -- /api/images/{uuid} o URL externa
    PRIMARY KEY (product_id, position)
);

-- Fotos subidas desde el panel (se guardan en la base para no depender del disco del servidor)
CREATE TABLE stored_images (
    id           UUID         PRIMARY KEY,
    content_type VARCHAR(50)  NOT NULL,
    data         BYTEA        NOT NULL,
    size_bytes   INTEGER      NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(60)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    name          VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL
);
