-- TABLE
CREATE TABLE categorias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE,
    descripcion TEXT,
    activo BOOLEAN DEFAULT 1,
    fechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE clientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    empresa TEXT,
    telefono TEXT,
    email TEXT,
    direccion TEXT,
    cedula TEXT NOT NULL UNIQUE,
    fechaRegistro DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT 1
);
CREATE TABLE demo (ID integer primary key, Name varchar(20), Hint text );
CREATE TABLE detalle_facturas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    facturaId INTEGER NOT NULL,
    productoId INTEGER NOT NULL,
    cantidad INTEGER NOT NULL,
    precioUnitario DECIMAL(10,2) NOT NULL,
    descuento DECIMAL(10,2) NOT NULL DEFAULT 0,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (facturaId) REFERENCES facturas(id) ON DELETE CASCADE,
    FOREIGN KEY (productoId) REFERENCES productos(id)
);
CREATE TABLE facturas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numeroFactura TEXT NOT NULL UNIQUE,
    clienteId INTEGER NOT NULL,
    usuarioId INTEGER NOT NULL,
    fechaEmision DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    impuestos DECIMAL(10,2) NOT NULL DEFAULT 0,
    descuentos DECIMAL(10,2) NOT NULL DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    estado TEXT NOT NULL DEFAULT 'COMPLETADA' CHECK (estado IN ('COMPLETADA', 'ANULADA', 'PENDIENTE')),
    observaciones TEXT,
    FOREIGN KEY (clienteId) REFERENCES clientes(id),
    FOREIGN KEY (usuarioId) REFERENCES usuarios(id)
);
CREATE TABLE inventario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    productoId INTEGER NOT NULL,
    tipoMovimiento TEXT NOT NULL CHECK (tipoMovimiento IN ('ENTRADA', 'SALIDA', 'AJUSTE')),
    cantidad INTEGER NOT NULL,
    fechaMovimiento DATETIME DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT NOT NULL,
    usuarioId INTEGER NOT NULL,
    facturaId INTEGER, -- Referencia opcional para movimientos por venta
    FOREIGN KEY (productoId) REFERENCES productos(id),
    FOREIGN KEY (usuarioId) REFERENCES usuarios(id),
    FOREIGN KEY (facturaId) REFERENCES facturas(id)
);
CREATE TABLE productos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    categoriaId INTEGER,
    precio DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    stockMinimo INTEGER NOT NULL DEFAULT 0,
    unidadMedida TEXT NOT NULL DEFAULT 'UNIDAD',
    activo BOOLEAN DEFAULT 1,
    fechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (categoriaId) REFERENCES categorias(id),
    UNIQUE(nombre, categoriaId)
);
CREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombreUsuario TEXT NOT NULL UNIQUE,
    contraseña TEXT NOT NULL,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    rol TEXT NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'CAJERO')),
    fechaCreacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT 1
);
 
-- INDEX
CREATE INDEX idx_clientes_cedula ON clientes(cedula);
CREATE INDEX idx_clientes_nombre ON clientes(nombre);
CREATE INDEX idx_detalle_factura ON detalle_facturas(facturaId);
CREATE INDEX idx_facturas_cliente ON facturas(clienteId);
CREATE INDEX idx_facturas_fecha ON facturas(fechaEmision);
CREATE INDEX idx_facturas_numero ON facturas(numeroFactura);
CREATE INDEX idx_inventario_fecha ON inventario(fechaMovimiento);
CREATE INDEX idx_inventario_producto ON inventario(productoId);
CREATE INDEX idx_productos_categoria ON productos(categoriaId);
CREATE INDEX idx_productos_codigo ON productos(codigo);
CREATE INDEX idx_productos_nombre ON productos(nombre);
 
-- TRIGGER
CREATE TRIGGER actualizar_stock_inventario
    AFTER INSERT ON inventario
    BEGIN
        UPDATE productos 
        SET stock = CASE 
            WHEN NEW.tipoMovimiento = 'ENTRADA' THEN stock + NEW.cantidad
            WHEN NEW.tipoMovimiento = 'SALIDA' THEN stock - NEW.cantidad
            WHEN NEW.tipoMovimiento = 'AJUSTE' THEN NEW.cantidad
        END
        WHERE id = NEW.productoId;
    END;
CREATE TRIGGER registrar_movimiento_venta
    AFTER INSERT ON detalle_facturas
    BEGIN
        INSERT INTO inventario (productoId, tipoMovimiento, cantidad, motivo, usuarioId, facturaId)
        SELECT NEW.productoId, 'SALIDA', NEW.cantidad, 
               'Venta - Factura: ' || f.numeroFactura, f.usuarioId, NEW.facturaId
        FROM facturas f WHERE f.id = NEW.facturaId;
    END;
 
-- VIEW
CREATE VIEW vista_facturas AS
SELECT 
    f.id,
    f.numeroFactura,
    f.fechaEmision,
    CONCAT(c.nombre, ' ', c.apellido) as cliente,
    c.empresa,
    CONCAT(u.nombre, ' ', u.apellido) as usuario,
    f.subtotal,
    f.impuestos,
    f.descuentos,
    f.total,
    f.estado,
    f.observaciones
FROM facturas f
JOIN clientes c ON f.clienteId = c.id
JOIN usuarios u ON f.usuarioId = u.id;
CREATE VIEW vista_productos AS
SELECT 
    p.id,
    p.codigo,
    p.nombre,
    p.descripcion,
    c.nombre as categoria,
    p.precio,
    p.stock,
    p.stockMinimo,
    p.unidadMedida,
    p.activo,
    p.fechaCreacion,
    CASE WHEN p.stock <= p.stockMinimo THEN 1 ELSE 0 END as stock_bajo
FROM productos p
LEFT JOIN categorias c ON p.categoriaId = c.id;
CREATE VIEW vista_ventas_diarias AS
SELECT 
    DATE(f.fechaEmision) as fecha,
    COUNT(*) as total_facturas,
    SUM(f.total) as total_ventas,
    AVG(f.total) as promedio_venta
FROM facturas f
WHERE f.estado = 'COMPLETADA'
GROUP BY DATE(f.fechaEmision);
 
