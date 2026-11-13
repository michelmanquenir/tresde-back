-- Datos de costo de cada producto (solo visibles en el panel)
ALTER TABLE products ADD COLUMN grams DOUBLE PRECISION;          -- gramos de filamento por pieza
ALTER TABLE products ADD COLUMN batch_hours DOUBLE PRECISION;    -- horas de impresión del lote completo
ALTER TABLE products ADD COLUMN batch_pieces INTEGER;            -- piezas que salen en ese lote
ALTER TABLE products ADD COLUMN accessories_cost INTEGER;        -- argollas, imanes, etc. por pieza

-- Configuración global de costos (una sola fila, id = 1)
CREATE TABLE cost_settings (
    id                      INTEGER          PRIMARY KEY,
    machine_hour_cost       DOUBLE PRECISION NOT NULL,  -- pesos por hora de impresora (electricidad, etc.)
    machine_reserve_percent DOUBLE PRECISION NOT NULL,  -- % de cada venta que se aparta para la máquina
    price_multiplier        DOUBLE PRECISION NOT NULL,  -- precio sugerido = costo x multiplicador
    filament_pla            INTEGER          NOT NULL,  -- precio por kg
    filament_petg           INTEGER          NOT NULL,
    filament_resina         INTEGER          NOT NULL,
    filament_tpu            INTEGER          NOT NULL,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO cost_settings VALUES (1, 35, 10, 4, 15000, 15000, 15000, 15000, CURRENT_TIMESTAMP);
