--liquibase formatted sql

--changeset ja:003-credenciales
ALTER TABLE identidad.usuarios
    ADD COLUMN password_hash varchar(100);