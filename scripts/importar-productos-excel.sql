-- ════════════════════════════════════════════════════════════════════════════
-- Importa los productos del Excel de costos (noviembre 2026).
-- Requiere el backend desplegado con la migración V7 (catálogo de accesorios).
-- Ejecutar en Supabase → SQL Editor. Es seguro repetirlo: no duplica nada.
--
--  • Productos nuevos: se crean OCULTOS y con stock 0, en PLA.
--    Agrégales fotos y stock desde el panel y luego márcalos como visibles.
--  • Si ya existe un producto con el mismo id, SOLO se actualizan gramos, horas y piezas
--    (no se tocan nombre, precio, fotos, stock, visibilidad ni sus accesorios).
--
-- Conversión del Excel:
--   costo Excel  = gramos × 15 + horas × valor hora + accesorio
--   costo panel  = gramos × precio kg/1000 + horas lote × 35 ÷ piezas lote + accesorios
--   → piezas del lote = 35 ÷ valor hora (redondeado); horas del lote = horas del Excel.
--   Precio = columna VENTA; si está vacía, "venta ideal" redondeada a la decena.
--   El accesorio de $30 se asigna como "Argolla de llavero" (llaveros y adornos)
--   o "Mecanismo de clicker" (clickers). Puedes renombrarlos o cambiar su costo en Accesorios.
-- ════════════════════════════════════════════════════════════════════════════

BEGIN;

INSERT INTO products
    (id, name, category_slug, price, description, material, is_new, best_seller, hidden, stock,
     grams, batch_hours, batch_pieces, created_at, updated_at)
VALUES
--   id                      nombre                    categoría     precio descripción                                              mat.   nuevo best  oculto stock gramos horas piezas
    ('mini-cubos',           'Mini cubos',             'accesorios',  1000, 'Mini cubos impresos en 3D.',                             'PLA', TRUE, FALSE, TRUE, 0,    7,   6,    4,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('cubo',                 'Cubo',                   'accesorios',  3000, 'Cubo impreso en 3D.',                                    'PLA', TRUE, FALSE, TRUE, 0,   28,   4,    1,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('harry-potter-navidad', 'Harry Potter navideño',  'decoracion',   750, 'Adorno navideño de Harry Potter impreso en 3D.',         'PLA', TRUE, FALSE, TRUE, 0,    5,   3,    9,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('grinch',               'Grinch',                 'decoracion',  1000, 'Adorno del Grinch impreso en 3D.',                       'PLA', TRUE, FALSE, TRUE, 0,   11,   7,    7,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('llavero-naruto',       'Llavero Naruto',         'accesorios',  1000, 'Llavero de Naruto impreso en 3D.',                       'PLA', TRUE, FALSE, TRUE, 0,    8,  11,    9,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('llavero-marvel',       'Llavero Marvel',         'accesorios',  1000, 'Llavero de Marvel impreso en 3D.',                       'PLA', TRUE, FALSE, TRUE, 0,    3, 1.5,    5,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('clicker-pikachu',      'Clicker Pikachu',        'accesorios',   120, 'Clicker de Pikachu impreso en 3D.',                      'PLA', TRUE, FALSE, TRUE, 0, NULL, NULL,   1,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('casa-navidad',         'Casa navideña',          'decoracion',  3480, 'Casa navideña impresa en 3D.',                           'PLA', TRUE, FALSE, TRUE, 0,   51,   3,    1,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('clicker-letras',       'Clicker de letras',      'accesorios',   300, 'Clicker de letras personalizado impreso en 3D.',         'PLA', TRUE, FALSE, TRUE, 0,    2, 3.5,    9,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('oso-navidad',          'Oso navideño',           'decoracion',  2000, 'Oso navideño impreso en 3D.',                            'PLA', TRUE, FALSE, TRUE, 0,   25,   2,    1,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('llavero-akatsuki',     'Llavero Akatsuki',       'accesorios',   530, 'Llavero de Akatsuki impreso en 3D.',                     'PLA', TRUE, FALSE, TRUE, 0,    6, 0.3,    1,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('perro-navidad',        'Perro navideño',         'decoracion',  2280, 'Perro navideño impreso en 3D.',                          'PLA', TRUE, FALSE, TRUE, 0,   38, NULL,   1,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('arbol-verde-blanco',   'Árbol verde y blanco',   'decoracion',  2560, 'Árbol de Navidad verde y blanco impreso en 3D.',         'PLA', TRUE, FALSE, TRUE, 0,   38,   2,    1,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    grams        = EXCLUDED.grams,
    batch_hours  = EXCLUDED.batch_hours,
    batch_pieces = EXCLUDED.batch_pieces,
    updated_at   = CURRENT_TIMESTAMP;

-- Accesorios del catálogo (si ya existen con ese nombre, se respetan su costo actual)
INSERT INTO accessories (name, cost, created_at) VALUES
    ('Argolla de llavero', 30, CURRENT_TIMESTAMP),
    ('Mecanismo de clicker', 30, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- 1 accesorio por pieza, solo a los productos que todavía no tienen ninguno
INSERT INTO product_accessories (product_id, accessory_id, quantity)
SELECT v.product_id, a.id, 1
FROM (VALUES
    ('harry-potter-navidad', 'Argolla de llavero'), ('grinch', 'Argolla de llavero'),
    ('llavero-naruto', 'Argolla de llavero'), ('llavero-marvel', 'Argolla de llavero'),
    ('llavero-akatsuki', 'Argolla de llavero'),
    ('clicker-pikachu', 'Mecanismo de clicker'), ('clicker-letras', 'Mecanismo de clicker')
) AS v (product_id, accessory_name)
JOIN accessories a ON a.name = v.accessory_name
WHERE NOT EXISTS (SELECT 1 FROM product_accessories pa WHERE pa.product_id = v.product_id);

-- Color por defecto (solo a los que no tienen ninguno). El árbol lleva verde y blanco.
INSERT INTO product_colors (product_id, position, color)
SELECT v.product_id, v.position, v.color
FROM (VALUES
    ('mini-cubos', 0, '#bdbdbd'), ('cubo', 0, '#bdbdbd'), ('harry-potter-navidad', 0, '#bdbdbd'),
    ('grinch', 0, '#4caf50'), ('llavero-naruto', 0, '#ff7a00'), ('llavero-marvel', 0, '#c62828'),
    ('clicker-pikachu', 0, '#fdd835'), ('casa-navidad', 0, '#bdbdbd'), ('clicker-letras', 0, '#bdbdbd'),
    ('oso-navidad', 0, '#8d6e63'), ('llavero-akatsuki', 0, '#212121'), ('perro-navidad', 0, '#bdbdbd'),
    ('arbol-verde-blanco', 0, '#2e7d32'), ('arbol-verde-blanco', 1, '#f5f5f5')
) AS v (product_id, position, color)
WHERE NOT EXISTS (SELECT 1 FROM product_colors c WHERE c.product_id = v.product_id);

COMMIT;

-- Verificación: costo por unidad con la configuración actual del panel (compáralo con tu Excel)
SELECT p.name,
       p.price AS precio,
       ROUND((COALESCE(p.grams, 0) * s.filament_pla / 1000
            + COALESCE(p.batch_hours, 0) * s.machine_hour_cost / GREATEST(COALESCE(p.batch_pieces, 1), 1)
            + COALESCE((SELECT SUM(a.cost * pa.quantity) FROM product_accessories pa
                        JOIN accessories a ON a.id = pa.accessory_id WHERE pa.product_id = p.id), 0))::numeric, 1) AS costo_unidad,
       p.stock, p.hidden AS oculto
FROM products p CROSS JOIN cost_settings s
WHERE p.id IN ('mini-cubos', 'cubo', 'harry-potter-navidad', 'grinch', 'llavero-naruto', 'llavero-marvel',
               'clicker-pikachu', 'casa-navidad', 'clicker-letras', 'oso-navidad', 'llavero-akatsuki',
               'perro-navidad', 'arbol-verde-blanco')
ORDER BY p.name;
