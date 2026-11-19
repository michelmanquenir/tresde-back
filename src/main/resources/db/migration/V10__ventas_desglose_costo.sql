-- Desde ahora el costo de una venta es solo la materia prima: filamento + accesorios.
-- (La luz se usa solo para calcular el precio sugerido del producto, no en las ventas.)
-- Las ventas anteriores conservan su unit_cost, que incluía la luz, y quedan sin desglose.
ALTER TABLE sales ADD COLUMN unit_filament_cost DOUBLE PRECISION;
ALTER TABLE sales ADD COLUMN unit_accessories_cost DOUBLE PRECISION;
