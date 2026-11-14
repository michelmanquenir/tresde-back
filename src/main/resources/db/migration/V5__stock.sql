-- Unidades disponibles para la venta. Los productos existentes parten en 0 (agotados).
ALTER TABLE products ADD COLUMN stock INTEGER NOT NULL DEFAULT 0;
