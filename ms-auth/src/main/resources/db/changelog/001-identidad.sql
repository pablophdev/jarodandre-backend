--liquibase formatted sql
--changeset ja:001-identidad
--validCheckSum: 9:cbcfb25202073ff295c896bc04c0e6cb
CREATE TABLE identidad.regiones (
    id varchar(24) PRIMARY KEY,
    nombre varchar(120) NOT NULL UNIQUE
);
CREATE TABLE identidad.comunas (
    id bigint PRIMARY KEY,
    region_id varchar(24) NOT NULL,
    nombre varchar(120) NOT NULL,
    UNIQUE (region_id, nombre),
    FOREIGN KEY (region_id) REFERENCES identidad.regiones(id)
);
CREATE TABLE identidad.usuarios (
    id binary(16) PRIMARY KEY,
    correo varchar(254) NOT NULL UNIQUE CHECK (correo = lower(trim(correo))),
    rut varchar(10) NOT NULL UNIQUE,
    nombre varchar(120) NOT NULL,
    telefono varchar(12),
    comuna_id bigint NOT NULL,
    rol varchar(16) NOT NULL DEFAULT 'CLIENTE' CHECK (rol IN ('CLIENTE','EMPLEADO','ADMIN')),
    estado varchar(16) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('PENDIENTE','ACTIVO','BLOQUEADO')),
    creado_en datetime(6) NOT NULL,
    actualizado_en datetime(6) NOT NULL,
    version bigint NOT NULL DEFAULT 0,
    FOREIGN KEY (comuna_id) REFERENCES identidad.comunas(id)
);
