--liquibase formatted sql
--changeset ja:001-comunicaciones
--validCheckSum: 9:9a9dd949e9efe0d17b4a95847454f9bb
CREATE TABLE comunicaciones.mensajes_contacto (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    nombre varchar(120) NOT NULL CHECK (length(trim(nombre)) > 0),
    correo varchar(254) NOT NULL CHECK (correo = lower(trim(correo))),
    mensaje varchar(2000) NOT NULL CHECK (length(trim(mensaje)) > 0),
    creado_en datetime(6) NOT NULL,
    leido_en datetime(6)
);
CREATE INDEX idx_contacto_bandeja ON comunicaciones.mensajes_contacto(creado_en DESC, id DESC);
CREATE TABLE comunicaciones.suscripciones (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    correo varchar(254) NOT NULL UNIQUE CHECK (correo = lower(trim(correo))),
    alta_en datetime(6) NOT NULL,
    baja_en datetime(6)
);
CREATE INDEX idx_suscripciones_activas ON comunicaciones.suscripciones(baja_en, alta_en DESC);
