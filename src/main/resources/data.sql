-- ========================================================
-- 0. CREACIÓN DE LA TABLA INTERMEDIA (Por si acaso)
-- ========================================================
CREATE TABLE IF NOT EXISTS Hogar_Usuario (
    Hogar_id BIGINT NOT NULL,
    integrantes_id BIGINT NOT NULL,
    PRIMARY KEY (Hogar_id, integrantes_id)
);


-- ========================================================
-- 1. CREACIÓN DE HOGARES
-- ========================================================
INSERT IGNORE INTO Hogar(id, nombreHogar, pisoMinimoHogar, precioHoraTarea, pin) 
VALUES(1, 'Hogar Principal', 200000.0, 5000.0, "hogar1");

INSERT IGNORE INTO Hogar(id, nombreHogar, pisoMinimoHogar, precioHoraTarea, pin) 
VALUES(2, 'Hogar Quinta / Depto 2', 150000.0, 4000.0, "hogar2");


-- ========================================================
-- 2. USUARIOS
-- ========================================================
-- Usuarios para el Hogar 1 (IDs 1, 2, 3)
INSERT IGNORE INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) 
VALUES(1, 'Juan',  'test@unlam.edu.ar',  'test', 'ADMIN', true, 300000);

INSERT IGNORE INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) 
VALUES(2, 'Maria', 'maria@unlam.edu.ar', 'test', 'USER',  true, 600000);

INSERT IGNORE INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) 
VALUES(3, 'Pedro', 'pedro@unlam.edu.ar', 'test', 'USER',  true, 900000);

-- Usuarios para el Hogar 2 (IDs 4, 5, 6 - Juan se repite en ambos hogares)
INSERT IGNORE INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) 
VALUES(4, 'Juan',     'juan.quinta@unlam.edu.ar', 'test', 'ADMIN', true, 300000);

INSERT IGNORE INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) 
VALUES(5, 'Sofia',    'sofia@unlam.edu.ar',       'test', 'USER',  true, 450000);

INSERT IGNORE INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) 
VALUES(6, 'Lucas',    'lucas@unlam.edu.ar',       'test', 'USER',  true, 500000);


-- ========================================================
-- 3. ASOCIACIÓN EN LA TABLA INTERMEDIA (Usando Hogar_Usuario)
-- ========================================================
-- Asociar usuarios al Hogar 1
INSERT IGNORE INTO Hogar_Usuario(Hogar_id, integrantes_id) VALUES(1, 1);
INSERT IGNORE INTO Hogar_Usuario(Hogar_id, integrantes_id) VALUES(1, 2);
INSERT IGNORE INTO Hogar_Usuario(Hogar_id, integrantes_id) VALUES(1, 3);

-- Asociar usuarios al Hogar 2
INSERT IGNORE INTO Hogar_Usuario(Hogar_id, integrantes_id) VALUES(2, 4);
INSERT IGNORE INTO Hogar_Usuario(Hogar_id, integrantes_id) VALUES(2, 5);
INSERT IGNORE INTO Hogar_Usuario(Hogar_id, integrantes_id) VALUES(2, 6);
INSERT IGNORE INTO Hogar_Usuario(Hogar_id, integrantes_id) VALUES(2, 1);

-- ========================================================
-- 4. GASTOS - HOGAR 1 (hogar_id = 1)
-- ========================================================
INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(1, 'G-001', 'Supermercado', 30000, 'ALIMENTACION', 2, 'OCASIONAL', NOW(), true, NULL, 1);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(2, 'G-002', 'Alquiler', 90000, 'ALQUILER', 3, 'FIJO', NOW(), true, '2026-11-01 12:00:00', 1);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(3, 'G-003', 'Internet', 15000, 'SERVICIOS', null, 'RECURRENTE', NOW(), false, '2026-11-10 18:00:00', 1);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(4, 'G-004', 'Luz', 18000, 'SERVICIOS', null, 'RECURRENTE', NOW(), false, DATE_ADD(NOW(), INTERVAL 5 DAY), 1);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(5, 'G-005', 'Gas', 9500, 'SERVICIOS', null, 'RECURRENTE', NOW(), false, DATE_ADD(NOW(), INTERVAL 20 DAY), 1);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(6, 'G-006', 'Expensas', 45000, 'ALQUILER', null, 'FIJO', NOW(), false, DATE_ADD(NOW(), INTERVAL 45 DAY), 1);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(7, 'G-007', 'Agua', 7000, 'SERVICIOS', null, 'RECURRENTE', NOW(), false, DATE_SUB(NOW(), INTERVAL 3 DAY), 1);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(8, 'G-008', 'Cable', 12000, 'SERVICIOS', null, 'RECURRENTE', NOW(), false, DATE_SUB(NOW(), INTERVAL 10 DAY), 1);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(9, 'G-009', 'Monotributo', 25000, 'SERVICIOS', 2, 'FIJO', NOW(), true, DATE_ADD(NOW(), INTERVAL 3 DAY), 1);


-- ========================================================
-- 5. GASTOS - HOGAR 2 (hogar_id = 2)
-- ========================================================
INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(10, 'G-101', 'Mantenimiento Pileta', 40000, 'SERVICIOS', 5, 'FIJO', NOW(), true, NULL, 2);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(11, 'G-102', 'Jardineria y Cortado de Pasto', 22000, 'SERVICIOS', 6, 'OCASIONAL', NOW(), true, '2026-11-05 10:00:00', 2);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(12, 'G-103', 'Alarma Quinta', 18000, 'SERVICIOS', 4, 'RECURRENTE', NOW(), true, '2026-11-12 15:00:00', 2);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(13, 'G-104', 'Asado Bienvenida', 35000, 'ALIMENTACION', 5, 'OCASIONAL', NOW(), true, DATE_SUB(NOW(), INTERVAL 2 DAY), 2);

INSERT IGNORE INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento, hogar_id) 
VALUES(14, 'G-105', 'Fumigacion', 16000, 'SERVICIOS', null, 'FIJO', NOW(), false, DATE_ADD(NOW(), INTERVAL 8 DAY), 2);