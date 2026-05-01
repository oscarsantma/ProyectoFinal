CREATE DATABASE IF NOT EXISTS resiplus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE resiplus;

CREATE TABLE IF NOT EXISTS residencias (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS residentes (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    residencia_id INT UNSIGNED NOT NULL,
    nombre VARCHAR(160) NOT NULL,
    edad INT NOT NULL,
    habitacion VARCHAR(30) NOT NULL,
    planta VARCHAR(30) NOT NULL,
    fecha_nacimiento VARCHAR(40) NOT NULL,
    fecha_ingreso VARCHAR(40) NOT NULL,
    observaciones TEXT NOT NULL,
    necesidades TEXT NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_residentes_residencia
        FOREIGN KEY (residencia_id) REFERENCES residencias(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_residentes_residencia (residencia_id),
    INDEX idx_residentes_activo (activo)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    residencia_id INT UNSIGNED NULL,
    residente_id INT UNSIGNED NULL,
    nombre VARCHAR(160) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol ENUM('ADMIN', 'PERSONAL', 'FAMILIAR') NOT NULL,
    estado ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    activo TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_residencia
        FOREIGN KEY (residencia_id) REFERENCES residencias(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT fk_usuarios_residente
        FOREIGN KEY (residente_id) REFERENCES residentes(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    INDEX idx_usuarios_residencia (residencia_id),
    INDEX idx_usuarios_rol_estado (rol, estado),
    INDEX idx_usuarios_activo (activo)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS visitas (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_familiar INT UNSIGNED NOT NULL,
    id_residente INT UNSIGNED NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    estado ENUM('PENDIENTE', 'CONFIRMADA', 'RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',
    nota TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visitas_familiar
        FOREIGN KEY (id_familiar) REFERENCES usuarios(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_visitas_residente
        FOREIGN KEY (id_residente) REFERENCES residentes(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    INDEX idx_visitas_residente_fecha (id_residente, fecha, hora),
    INDEX idx_visitas_familiar (id_familiar),
    INDEX idx_visitas_estado (estado)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS mensajes (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_emisor INT UNSIGNED NOT NULL,
    id_receptor INT UNSIGNED NOT NULL,
    texto TEXT NOT NULL,
    hora VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mensajes_emisor
        FOREIGN KEY (id_emisor) REFERENCES usuarios(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_mensajes_receptor
        FOREIGN KEY (id_receptor) REFERENCES usuarios(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    INDEX idx_mensajes_chat (id_emisor, id_receptor)
) ENGINE=InnoDB;
