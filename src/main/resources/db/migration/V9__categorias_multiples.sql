-- Un producto puede estar en varias categorías. La de posición 0 es la principal
-- (se sigue guardando también en products.category_slug).
CREATE TABLE product_categories (
    product_id    VARCHAR(140) NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    position      INTEGER      NOT NULL,
    category_slug VARCHAR(40)  NOT NULL REFERENCES categories (slug),
    PRIMARY KEY (product_id, position)
);

CREATE INDEX idx_product_categories_slug ON product_categories (category_slug);

INSERT INTO product_categories (product_id, position, category_slug)
SELECT id, 0, category_slug FROM products;
