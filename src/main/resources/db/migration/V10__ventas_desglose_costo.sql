-- Desglose del costo unitario congelado al vender: materia prima (filamento + accesorios) y luz.
-- Las ventas anteriores quedan sin desglose (solo tienen unit_cost total).
ALTER TABLE sales ADD COLUMN unit_filament_cost DOUBLE PRECISION;
ALTER TABLE sales ADD COLUMN unit_accessories_cost DOUBLE PRECISION;
ALTER TABLE sales ADD COLUMN unit_electricity_cost DOUBLE PRECISION;
