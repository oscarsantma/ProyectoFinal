-- Base de datos ResiPlus
-- Ejecutar en phpMyAdmin o desde la terminal MySQL

CREATE DATABASE IF NOT EXISTS resiplus
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE resiplus;

CREATE TABLE IF NOT EXISTS residencias (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS residentes (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    edad             INT DEFAULT 0,
    habitacion       VARCHAR(10),
    planta           VARCHAR(20),
    residencia       VARCHAR(100),
    fecha_nacimiento DATE,
    fecha_ingreso    DATE,
    observaciones    TEXT,
    necesidades      TEXT,
    activo           TINYINT(1) DEFAULT 1
);

CREATE TABLE IF NOT EXISTS usuarios (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    rol         ENUM('FAMILIAR','PERSONAL','ADMIN') NOT NULL,
    residencia  VARCHAR(100),
    estado      ENUM('PENDIENTE','APROBADO','RECHAZADO') DEFAULT 'PENDIENTE',
    id_residente INT DEFAULT NULL,
    FOREIGN KEY (id_residente) REFERENCES residentes(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS visitas (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    id_familiar INT NOT NULL,
    fecha       DATE NOT NULL,
    hora        VARCHAR(10) NOT NULL,
    estado      ENUM('PENDIENTE','APROBADA','RECHAZADA') DEFAULT 'PENDIENTE',
    nota        TEXT,
    FOREIGN KEY (id_familiar) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS mensajes (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    emisor   INT NOT NULL,
    receptor INT NOT NULL,
    texto    TEXT NOT NULL,
    hora     VARCHAR(20) NOT NULL,
    FOREIGN KEY (emisor)   REFERENCES usuarios(id),
    FOREIGN KEY (receptor) REFERENCES usuarios(id)
);

-- Datos de prueba para empezar
INSERT IGNORE INTO residencias (nombre) VALUES
    ('Residencia Los Pinos'),
    ('Residencia El Olivar');

INSERT IGNORE INTO usuarios (nombre, email, password, rol, residencia, estado) VALUES
    ('Administrador', 'admin@resiplus.es', 'admin123', 'ADMIN', '', 'APROBADO');
