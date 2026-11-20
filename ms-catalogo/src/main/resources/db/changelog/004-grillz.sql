--liquibase formatted sql
--changeset ja:004-grillz
CREATE TABLE catalogo.opciones_grillz (
 codigo varchar(40) PRIMARY KEY, tipo varchar(20) NOT NULL, nombre varchar(100) NOT NULL,
 color varchar(20) NOT NULL, precio_clp bigint NOT NULL
);
INSERT INTO catalogo.opciones_grillz VALUES
 ('dorado','MATERIAL','Dorado','#d9ac52',35000),
 ('plata','MATERIAL','Plateado','#cbd4df',25000),
 ('rosa','MATERIAL','Oro rosado','#d99882',38000),
 ('liso','ESTILO','Liso','',0),
 ('piedras','ESTILO','Con piedras','',18000);
CREATE TABLE catalogo.disenos_grillz (
 id binary(16) PRIMARY KEY, usuario_id binary(16) NOT NULL, autor varchar(120) NOT NULL,
 nombre varchar(100) NOT NULL, configuracion text NOT NULL, estimado_clp bigint NOT NULL,
 estado varchar(20) NOT NULL, creado_en datetime(6) NOT NULL,
 INDEX idx_grillz_usuario(usuario_id,creado_en), INDEX idx_grillz_estado(estado,creado_en)
);
