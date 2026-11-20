--liquibase formatted sql
--changeset ja:001-catalogo
CREATE TABLE IF NOT EXISTS catalogo.categorias (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    nombre varchar(100) NOT NULL UNIQUE,
    slug varchar(120) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS catalogo.productos (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    categoria_id bigint NOT NULL,
    nombre varchar(180) NOT NULL,
    descripcion text NOT NULL,
    precio_clp bigint NOT NULL CHECK (precio_clp >= 0),
    imagen_key varchar(500) NOT NULL,
    etiquetas json NOT NULL,
    grupo varchar(20) NOT NULL CHECK (grupo IN ('new','popular')),
    insignia varchar(80) NOT NULL,
    destacado boolean NOT NULL,
    valoracion integer NOT NULL CHECK (valoracion BETWEEN 0 AND 5),
    cantidad_resenas integer NOT NULL CHECK (cantidad_resenas >= 0),
    disponibilidad varchar(20) NOT NULL CHECK (disponibilidad IN ('DISPONIBLE','AGOTADO','CONSULTAR')),
    archivado_en datetime(6),
    creado_en datetime(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    actualizado_en datetime(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    FOREIGN KEY (categoria_id) REFERENCES catalogo.categorias(id),
    INDEX idx_productos_categoria (categoria_id, archivado_en)
);

CREATE TABLE IF NOT EXISTS catalogo.publicaciones (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    slug varchar(180) NOT NULL UNIQUE,
    titulo varchar(250) NOT NULL,
    autor_visible varchar(120) NOT NULL,
    parrafos json NOT NULL,
    portada_key varchar(500) NOT NULL,
    estado varchar(20) NOT NULL CHECK (estado IN ('BORRADOR','PUBLICADO')),
    publicado_en datetime(6),
    creado_en datetime(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    actualizado_en datetime(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CHECK (estado <> 'PUBLICADO' OR (publicado_en IS NOT NULL AND json_length(parrafos) > 0)),
    INDEX idx_publicaciones_estado_fecha (estado, publicado_en)
);

