--liquibase formatted sql
--changeset ja:001-identidad
CREATE SCHEMA identidad;
CREATE TABLE identidad.regiones (
    id varchar(24) PRIMARY KEY,
    nombre varchar(120) NOT NULL UNIQUE
);
CREATE TABLE identidad.comunas (
    id bigint PRIMARY KEY,
    region_id varchar(24) NOT NULL REFERENCES identidad.regiones(id),
    nombre varchar(120) NOT NULL,
    UNIQUE (region_id, nombre)
);
CREATE TABLE identidad.usuarios (
    id uuid PRIMARY KEY,
    correo varchar(254) NOT NULL UNIQUE CHECK (correo = lower(trim(correo))),
    rut varchar(10) NOT NULL UNIQUE,
    nombre varchar(120) NOT NULL,
    telefono varchar(12),
    comuna_id bigint NOT NULL REFERENCES identidad.comunas(id),
    rol varchar(16) NOT NULL DEFAULT 'CLIENTE' CHECK (rol IN ('CLIENTE','EMPLEADO','ADMIN')),
    estado varchar(16) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('PENDIENTE','ACTIVO','BLOQUEADO')),
    creado_en timestamp with time zone NOT NULL,
    actualizado_en timestamp with time zone NOT NULL,
    version bigint NOT NULL DEFAULT 0
);
