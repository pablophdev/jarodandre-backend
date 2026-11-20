--liquibase formatted sql
--changeset ja:003-resenas
CREATE TABLE catalogo.resenas (
    id bigint AUTO_INCREMENT PRIMARY KEY,
    producto_id bigint NOT NULL,
    usuario_id binary(16) NOT NULL,
    autor varchar(120) NOT NULL,
    estrellas integer NOT NULL CHECK (estrellas BETWEEN 1 AND 5),
    comentario varchar(2000) NOT NULL,
    creado_en datetime(6) NOT NULL,
    actualizado_en datetime(6) NOT NULL,
    UNIQUE (producto_id, usuario_id),
    FOREIGN KEY (producto_id) REFERENCES catalogo.productos(id)
);
-- Los contadores previos eran valores de demostración o edición manual.
UPDATE catalogo.productos SET valoracion=0, cantidad_resenas=0;
