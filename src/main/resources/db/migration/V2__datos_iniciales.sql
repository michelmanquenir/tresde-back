-- Categorías y productos de ejemplo (los mismos que tenía el frontend)

INSERT INTO categories (slug, name, description, icon, hue, position) VALUES
('decoracion', 'Decoración', 'Macetas, lámparas y piezas para darle vida a tu espacio.', 'vase', 18, 1),
('figuras', 'Figuras', 'Coleccionables, miniaturas y bustos con gran detalle.', 'figure', 265, 2),
('hogar', 'Hogar y organización', 'Soportes, organizadores y soluciones prácticas.', 'home', 160, 3),
('gaming', 'Gaming', 'Soportes de control, dados, accesorios de escritorio.', 'gamepad', 210, 4),
('accesorios', 'Llaveros y accesorios', 'Pequeños regalos y detalles personalizables.', 'key', 340, 5),
('repuestos', 'Repuestos técnicos', 'Piezas funcionales, engranajes y reemplazos a medida.', 'gear', 45, 6);

-- created_at escalonado para conservar el orden original (más reciente primero)
INSERT INTO products (id, name, category_slug, price, old_price, description, material, height_cm, print_hours, rating, is_new, best_seller, hidden, created_at, updated_at) VALUES
('maceta-geometrica', 'Maceta geométrica Low-Poly', 'decoracion', 12990, NULL, 'Maceta facetada con drenaje y plato incluido. Ideal para suculentas y cactus.', 'PLA', NULL, 6, 4.8, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1' MINUTE, CURRENT_TIMESTAMP),
('lampara-luna', 'Lámpara Luna litofanía', 'decoracion', 24990, 29990, 'Lámpara con relieve lunar real impreso como litofanía. Incluye base LED USB.', 'PLA', NULL, 14, 4.9, FALSE, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2' MINUTE, CURRENT_TIMESTAMP),
('jarron-espiral', 'Jarrón espiral', 'decoracion', 15990, NULL, 'Jarrón decorativo con patrón en espiral. Para flores secas.', 'PETG', NULL, 9, 4.6, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3' MINUTE, CURRENT_TIMESTAMP),
('dragon-articulado', 'Dragón articulado', 'figuras', 18990, NULL, 'Dragón flexible impreso en una sola pieza, sin ensamblaje. 30 cm de largo.', 'PLA', NULL, 11, 5.0, TRUE, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4' MINUTE, CURRENT_TIMESTAMP),
('busto-resina', 'Busto de personaje en resina', 'figuras', 39990, NULL, 'Busto de alta resolución en resina, listo para pintar. 12 cm de alto.', 'Resina', 12, 8, 4.7, FALSE, FALSE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5' MINUTE, CURRENT_TIMESTAMP),
('miniaturas-rol', 'Set de miniaturas de rol (5)', 'figuras', 21990, NULL, 'Cinco miniaturas a escala 28 mm para tus partidas de rol.', 'Resina', NULL, 5, 4.8, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP - INTERVAL '6' MINUTE, CURRENT_TIMESTAMP),
('organizador-escritorio', 'Organizador modular de escritorio', 'hogar', 14990, NULL, 'Módulos que encajan entre sí para lápices, clips y cables.', 'PLA', NULL, 10, 4.7, FALSE, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '7' MINUTE, CURRENT_TIMESTAMP),
('soporte-audifonos', 'Soporte para audífonos', 'hogar', 9990, NULL, 'Soporte estable con base antideslizante y canal para cables.', 'PETG', NULL, 5, 4.5, FALSE, FALSE, FALSE, CURRENT_TIMESTAMP - INTERVAL '8' MINUTE, CURRENT_TIMESTAMP),
('gancho-pared', 'Ganchos de pared (pack x4)', 'hogar', 5990, NULL, 'Ganchos resistentes con tornillos incluidos. Soportan hasta 5 kg.', 'PETG', NULL, 3, 4.4, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP - INTERVAL '9' MINUTE, CURRENT_TIMESTAMP),
('soporte-control', 'Soporte doble para controles', 'gaming', 11990, 13990, 'Mantén tus controles ordenados. Compatible con los modelos más populares.', 'PLA', NULL, 7, 4.8, FALSE, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '10' MINUTE, CURRENT_TIMESTAMP),
('torre-dados', 'Torre de dados medieval', 'gaming', 19990, NULL, 'Torre de dados con forma de castillo y bandeja recolectora.', 'PLA', NULL, 16, 4.9, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP - INTERVAL '11' MINUTE, CURRENT_TIMESTAMP),
('llavero-nombre', 'Llavero con tu nombre', 'accesorios', 3990, NULL, 'Llavero personalizado en dos colores. Escríbenos el texto al comprar.', 'PLA', NULL, 1, 4.9, FALSE, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '12' MINUTE, CURRENT_TIMESTAMP),
('porta-celular', 'Porta celular plegable', 'accesorios', 6990, NULL, 'Soporte plegable de bolsillo con tres ángulos de inclinación.', 'PETG', NULL, 2, 4.6, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP - INTERVAL '13' MINUTE, CURRENT_TIMESTAMP),
('engranajes-repuesto', 'Engranaje de repuesto a medida', 'repuestos', 7990, NULL, 'Fabricamos el engranaje que necesitas a partir de tu pieza o medidas.', 'PETG', NULL, 3, 4.7, FALSE, FALSE, FALSE, CURRENT_TIMESTAMP - INTERVAL '14' MINUTE, CURRENT_TIMESTAMP),
('pies-goma', 'Patas antideslizantes TPU (x8)', 'repuestos', 4990, NULL, 'Patas flexibles para muebles y electrodomésticos.', 'TPU', NULL, 2, 4.5, FALSE, FALSE, FALSE, CURRENT_TIMESTAMP - INTERVAL '15' MINUTE, CURRENT_TIMESTAMP);

INSERT INTO product_colors (product_id, position, color) VALUES
('maceta-geometrica', 0, '#f4f1ea'), ('maceta-geometrica', 1, '#2b2b2b'), ('maceta-geometrica', 2, '#e86a3a'), ('maceta-geometrica', 3, '#6aa88f'),
('lampara-luna', 0, '#f4f1ea'),
('jarron-espiral', 0, '#c9b8f0'), ('jarron-espiral', 1, '#9fd8c9'), ('jarron-espiral', 2, '#f2c14e'),
('dragon-articulado', 0, '#7b4bd6'), ('dragon-articulado', 1, '#e8443a'), ('dragon-articulado', 2, '#1e9e6a'), ('dragon-articulado', 3, '#d4af37'),
('busto-resina', 0, '#bdbdbd'),
('miniaturas-rol', 0, '#bdbdbd'),
('organizador-escritorio', 0, '#2b2b2b'), ('organizador-escritorio', 1, '#f4f1ea'), ('organizador-escritorio', 2, '#3a7be8'),
('soporte-audifonos', 0, '#2b2b2b'), ('soporte-audifonos', 1, '#e86a3a'),
('gancho-pared', 0, '#f4f1ea'), ('gancho-pared', 1, '#2b2b2b'),
('soporte-control', 0, '#2b2b2b'), ('soporte-control', 1, '#ffffff'), ('soporte-control', 2, '#e8443a'),
('torre-dados', 0, '#8a8a8a'), ('torre-dados', 1, '#6b4f3a'),
('llavero-nombre', 0, '#e8443a'), ('llavero-nombre', 1, '#3a7be8'), ('llavero-nombre', 2, '#1e9e6a'), ('llavero-nombre', 3, '#f2c14e'), ('llavero-nombre', 4, '#7b4bd6'),
('porta-celular', 0, '#2b2b2b'), ('porta-celular', 1, '#9fd8c9'),
('engranajes-repuesto', 0, '#2b2b2b'), ('engranajes-repuesto', 1, '#f4f1ea'),
('pies-goma', 0, '#2b2b2b');
