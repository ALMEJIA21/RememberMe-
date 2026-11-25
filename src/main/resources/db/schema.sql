CREATE DATABASE IF NOT EXISTS rememberme_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE rememberme_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(180) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    rol ENUM('ROL_CUIDADOR', 'ROL_ADMIN', 'ROL_PACIENTE') NOT NULL,
    avatar_url VARCHAR(255) NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuarios_correo UNIQUE (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pacientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    fecha_nacimiento DATE NULL,
    alergias TEXT NULL,
    notas_clinicas TEXT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    avatar_url VARCHAR(255) NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_pacientes_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_pacientes_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cuidador_paciente (
    cuidador_id BIGINT NOT NULL,
    paciente_id BIGINT NOT NULL,
    es_principal TINYINT(1) NOT NULL DEFAULT 0,
    asignado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (cuidador_id, paciente_id),
    CONSTRAINT fk_cp_cuidador FOREIGN KEY (cuidador_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_cp_paciente FOREIGN KEY (paciente_id)
        REFERENCES pacientes(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS medicamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    principio_activo VARCHAR(150) NULL,
    cantidad_dosis DECIMAL(10, 2) NOT NULL,
    unidad_dosis VARCHAR(50) NOT NULL,
    via_administracion ENUM(
        'ORAL', 'TOPICA', 'INTRAVENOSA', 'INTRAMUSCULAR',
        'SUBCUTANEA', 'INHALATORIA', 'OFTALMICA', 'OTICA', 'OTRA'
    ) NOT NULL,
    frecuencia_horas INT NOT NULL,
    es_alto_riesgo TINYINT(1) NOT NULL DEFAULT 0,
    instrucciones TEXT NULL,
    fecha_hora_inicio DATETIME NOT NULL,
    fecha_fin DATE NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_med_dosis CHECK (cantidad_dosis > 0),
    CONSTRAINT chk_med_frecuencia CHECK (frecuencia_horas > 0),
    CONSTRAINT fk_med_paciente FOREIGN KEY (paciente_id)
        REFERENCES pacientes(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS registro_tomas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicamento_id BIGINT NOT NULL,
    administrado_por_usuario_id BIGINT NULL,
    hora_programada DATETIME NOT NULL,
    hora_real DATETIME NULL,
    estado ENUM('PENDIENTE', 'ADMINISTRADO', 'OMITIDO', 'RETRASADO') NOT NULL DEFAULT 'PENDIENTE',
    motivo_omision VARCHAR(500) NULL,
    verificacion_paciente TINYINT(1) NOT NULL DEFAULT 0,
    verificacion_medicamento TINYINT(1) NOT NULL DEFAULT 0,
    verificacion_dosis TINYINT(1) NOT NULL DEFAULT 0,
    verificacion_via TINYINT(1) NOT NULL DEFAULT 0,
    verificacion_hora TINYINT(1) NOT NULL DEFAULT 0,
    notas TEXT NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_toma_med_hora UNIQUE (medicamento_id, hora_programada),
    CONSTRAINT fk_toma_medicamento FOREIGN KEY (medicamento_id)
        REFERENCES medicamentos(id) ON DELETE RESTRICT,
    CONSTRAINT fk_toma_usuario FOREIGN KEY (administrado_por_usuario_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bienestar_cuidador (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha_registro DATE NOT NULL,
    nivel_estres INT NOT NULL,
    nivel_fatiga INT NOT NULL,
    horas_sueno DECIMAL(4, 2) NOT NULL,
    estado_emocional VARCHAR(100) NULL,
    nota TEXT NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_bc_estres CHECK (nivel_estres BETWEEN 1 AND 5),
    CONSTRAINT chk_bc_fatiga CHECK (nivel_fatiga BETWEEN 1 AND 5),
    CONSTRAINT chk_bc_sueno CHECK (horas_sueno BETWEEN 0 AND 24),
    CONSTRAINT uk_bc_usuario_fecha UNIQUE (usuario_id, fecha_registro),
    CONSTRAINT fk_bc_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categorias_guias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT uk_cat_guia_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS guias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    categoria_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    contenido TEXT NOT NULL,
    etiquetas VARCHAR(255) NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_guia_categoria FOREIGN KEY (categoria_id)
        REFERENCES categorias_guias(id) ON DELETE RESTRICT,
    FULLTEXT INDEX idx_guias_fulltext (titulo, contenido, etiquetas)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;