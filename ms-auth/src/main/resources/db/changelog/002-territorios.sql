--liquibase formatted sql
--changeset ja:002-territorios
INSERT INTO identidad.regiones (id,nombre) VALUES
('RM','Región Metropolitana de Santiago'), ('Araucania','Región de la Araucanía'), ('Nuble','Región de Ñuble');
INSERT INTO identidad.comunas (id,region_id,nombre) VALUES
(1,'RM','Santiago'), (2,'RM','Puente Alto'), (3,'RM','Maipú'), (4,'RM','La Florida'), (5,'RM','Las Condes'),
(6,'Araucania','Temuco'), (7,'Araucania','Villarrica'), (8,'Araucania','Pucón'),
(9,'Nuble','Chillán'), (10,'Nuble','San Carlos'), (11,'Nuble','Bulnes');
