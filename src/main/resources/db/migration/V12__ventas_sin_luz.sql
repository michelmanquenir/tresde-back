-- En las ventas el costo es solo la materia prima (filamento + accesorios): la luz se usa únicamente
-- para el precio sugerido del producto. Las ventas que alcanzaron a guardar la luz (V10) se corrigen
-- para que su costo unitario no la incluya, y luego se quita esa columna.
UPDATE sales
SET unit_cost = COALESCE(unit_filament_cost, 0) + COALESCE(unit_accessories_cost, 0)
WHERE unit_electricity_cost IS NOT NULL;

ALTER TABLE sales DROP COLUMN unit_electricity_cost;
