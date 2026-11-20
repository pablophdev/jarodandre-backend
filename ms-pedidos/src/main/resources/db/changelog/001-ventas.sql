--liquibase formatted sql
--changeset ja:001-ventas
CREATE TABLE IF NOT EXISTS ventas.carritos (
    id binary(16) PRIMARY KEY,
    usuario_id binary(16) UNIQUE,
    token_hash varchar(64) UNIQUE,
    creado_en datetime(6) NOT NULL,
    CHECK ((usuario_id IS NULL) <> (token_hash IS NULL))
);
CREATE TABLE IF NOT EXISTS ventas.carrito_items (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    carrito_id binary(16) NOT NULL,
    producto_id bigint NOT NULL,
    cantidad integer NOT NULL CHECK (cantidad BETWEEN 1 AND 99),
    UNIQUE (carrito_id, producto_id),
    FOREIGN KEY (carrito_id) REFERENCES ventas.carritos(id)
);
CREATE TABLE IF NOT EXISTS ventas.pedidos (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    carrito_id binary(16) NOT NULL,
    usuario_id binary(16),
    clave_checkout binary(16) NOT NULL,
    nombre_entrega varchar(120) NOT NULL,
    correo_entrega varchar(254) NOT NULL,
    direccion_entrega varchar(250) NOT NULL,
    ciudad_entrega varchar(100) NOT NULL,
    total_clp bigint NOT NULL CHECK (total_clp >= 0),
    estado_pago varchar(16) NOT NULL CHECK (estado_pago IN ('PENDIENTE','PAGADO','FALLIDO')),
    estado_entrega varchar(16) NOT NULL CHECK (estado_entrega IN ('PENDIENTE','ENVIADO','COMPLETADO')),
    creado_en datetime(6) NOT NULL,
    pagado_en datetime(6),
    CHECK ((estado_pago = 'PAGADO') = (pagado_en IS NOT NULL)),
    CHECK (estado_entrega = 'PENDIENTE' OR estado_pago = 'PAGADO'),
    UNIQUE (carrito_id, clave_checkout),
    FOREIGN KEY (carrito_id) REFERENCES ventas.carritos(id),
    INDEX idx_pedidos_carrito_pago (carrito_id, estado_pago),
    INDEX idx_pedidos_usuario (usuario_id, id)
);


CREATE TABLE IF NOT EXISTS ventas.pedido_items (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    pedido_id bigint NOT NULL,
    producto_id bigint NOT NULL,
    nombre_producto varchar(180) NOT NULL,
    precio_unitario_clp bigint NOT NULL CHECK (precio_unitario_clp >= 0),
    cantidad integer NOT NULL CHECK (cantidad BETWEEN 1 AND 99),
    subtotal_clp bigint NOT NULL,
    CHECK (subtotal_clp = precio_unitario_clp * cantidad),
    UNIQUE (pedido_id, producto_id),
    FOREIGN KEY (pedido_id) REFERENCES ventas.pedidos(id)
);
CREATE TABLE IF NOT EXISTS ventas.intentos_pago (
    id binary(16) PRIMARY KEY,
    pedido_id bigint NOT NULL,
    proveedor varchar(30) NOT NULL,
    referencia_externa varchar(100),
    monto_clp bigint NOT NULL CHECK (monto_clp >= 0),
    estado varchar(16) NOT NULL CHECK (estado IN ('PENDIENTE','PAGADO','FALLIDO')),
    creado_en datetime(6) NOT NULL,
    resuelto_en datetime(6),
    UNIQUE (proveedor, referencia_externa),
    FOREIGN KEY (pedido_id) REFERENCES ventas.pedidos(id),
    INDEX idx_intentos_pedido (pedido_id)
);

CREATE TABLE IF NOT EXISTS ventas.accesos_invitado (
    id binary(16) PRIMARY KEY,
    pedido_id bigint NOT NULL UNIQUE,
    token_hash varchar(64) NOT NULL UNIQUE,
    vence_en datetime(6) NOT NULL,
    revocado_en datetime(6),
    FOREIGN KEY (pedido_id) REFERENCES ventas.pedidos(id)
);
