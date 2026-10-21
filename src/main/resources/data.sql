INSERT INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) VALUES(null, 'Juan',  'test@unlam.edu.ar',  'test', 'ADMIN', true, 300000);
INSERT INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) VALUES(null, 'Maria', 'maria@unlam.edu.ar', 'test', 'USER',  true, 600000);
INSERT INTO Usuario(id, nombre, email, password, rol, activo, ingresoMensual) VALUES(null, 'Pedro', 'pedro@unlam.edu.ar', 'test', 'USER',  true, 900000);

INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento) VALUES(null, 'G-001', 'Supermercado', 30000, 'ALIMENTACION', 2, 'OCASIONAL', NOW(), true, NULL);
INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento) VALUES(null, 'G-002', 'Alquiler', 90000, 'ALQUILER', 3, 'FIJO', NOW(), true, '2026-11-01 12:00:00');
INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento) VALUES(null, 'G-003', 'Internet', 15000, 'SERVICIOS', null, 'RECURRENTE', NOW(), false, '2026-11-10 18:00:00');

INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento) VALUES(null, 'G-004', 'Luz', 18000, 'SERVICIOS', null, 'RECURRENTE', NOW(), false, DATE_ADD(NOW(), INTERVAL 5 DAY));
INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento) VALUES(null, 'G-005', 'Gas', 9500, 'SERVICIOS', null, 'RECURRENTE', NOW(), false, DATE_ADD(NOW(), INTERVAL 20 DAY));
INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento) VALUES(null, 'G-006', 'Expensas', 45000, 'ALQUILER', null, 'FIJO', NOW(), false, DATE_ADD(NOW(), INTERVAL 45 DAY));
INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento) VALUES(null, 'G-007', 'Agua', 7000, 'SERVICIOS', null, 'RECURRENTE', NOW(), false, DATE_SUB(NOW(), INTERVAL 3 DAY));
INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento) VALUES(null, 'G-008', 'Cable', 12000, 'SERVICIOS', null, 'RECURRENTE', NOW(), false, DATE_SUB(NOW(), INTERVAL 10 DAY));
INSERT INTO Gasto(id, codigo, descripcion, monto, categoria, usuario_pagador_id, tipoGasto, fecha, pagado, fechaVencimiento) VALUES(null, 'G-009', 'Monotributo', 25000, 'SERVICIOS', 2, 'FIJO', NOW(), true, DATE_ADD(NOW(), INTERVAL 3 DAY));