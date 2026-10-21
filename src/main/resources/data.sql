INSERT INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) VALUES(null, 'Juan',  'test@unlam.edu.ar',  'test', 'ADMIN', true, 300000);
INSERT INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) VALUES(null, 'Maria', 'maria@unlam.edu.ar', 'test', 'USER',  true, 600000);
INSERT INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) VALUES(null, 'Pedro', 'pedro@unlam.edu.ar', 'test', 'USER',  true, 900000);

INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha) VALUES(null, 'G-001', 'Supermercado', 30000, 'Comida',   2, 'OCASIONAL', NOW());
INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha) VALUES(null, 'G-002', 'Alquiler',     90000, 'Vivienda', 3, 'FIJO',      NOW());
