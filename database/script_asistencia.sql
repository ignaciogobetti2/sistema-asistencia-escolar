-- 1. CREACIÓN DE LA BASE DE DATOS
CREATE DATABASE IF NOT EXISTS control_asistencia_db;
USE control_asistencia_db;

-- 2. CREACIÓN DE LAS TABLAS (Estructura DDL)
CREATE TABLE cursos (
    id_curso INT PRIMARY KEY AUTO_INCREMENT,
    nombre_curso VARCHAR(50) NOT NULL,
    division VARCHAR(10) NOT NULL
);

-- SE QUITO EL AUTO_INCREMENT PARA PERMITIR ASIGNACIÓN MANUAL DE ID
CREATE TABLE alumnos (
    id_alumno INT PRIMARY KEY, 
    id_curso INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    FOREIGN KEY (id_curso) REFERENCES cursos(id_curso) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE materias (
    id_materia INT PRIMARY KEY AUTO_INCREMENT,
    nombre_materia VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL
);

CREATE TABLE registros_asistencia (
    id_registro INT PRIMARY KEY AUTO_INCREMENT,
    id_alumno INT NOT NULL,
    id_materia INT NOT NULL,
    id_usuario INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    estado_asistencia VARCHAR(20) NOT NULL,
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id_alumno) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 3. CARGA DE DATOS INICIALES DEL SISTEMA
INSERT INTO usuarios (username, password, rol) VALUES ('pablo', 'pablo1', 'DOCENTE');
