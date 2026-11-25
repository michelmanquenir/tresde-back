-- Quién registró cada venta (vendedores, trabajadores o administrador). Las ventas antiguas quedan sin dato.
ALTER TABLE sales ADD COLUMN sold_by VARCHAR(255);
ALTER TABLE sales ADD COLUMN sold_by_name VARCHAR(255);
CREATE INDEX idx_sales_sold_by ON sales (sold_by);
