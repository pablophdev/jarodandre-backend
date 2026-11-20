--liquibase formatted sql
--changeset ja:005-grillz-disenos
INSERT INTO catalogo.opciones_grillz (codigo,tipo,nombre,color,precio_clp) VALUES
 ('ventana','ESTILO','Marco abierto','',12000),
 ('solitario','ESTILO','Piedra central','',14000),
 ('colmillo','ESTILO','Colmillo','',16000),
 ('rayas','ESTILO','Rayas grabadas','',8000),
 ('corazon','ESTILO','Corazón','',10000);
