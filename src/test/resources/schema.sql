-- Extensión para UUID en PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla para usuarios oftalmólogos
CREATE TABLE ophthal_user (
    ophtal_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255)
);

-- Tabla para pacientes
CREATE TABLE pacient (
    pacient_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cedula VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    ophtal_id UUID NOT NULL,
    UNIQUE (ophtal_id, cedula), -- Constraint para asegurar que la combinación sea única
    FOREIGN KEY (ophtal_id) REFERENCES ophthal_user(ophtal_id) ON DELETE CASCADE -- Relación con OphthalUser
);

-- Tabla para exámenes
CREATE TABLE exam (
    exam_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    date VARCHAR(255) NOT NULL,
    image_id VARCHAR(255) NOT NULL,
    distance_ratio DOUBLE PRECISION NOT NULL,
    perimeter_ratio DOUBLE PRECISION NOT NULL,
    area_ratio DOUBLE PRECISION NOT NULL,
    neuroretinal_rim_perimeter DOUBLE PRECISION NOT NULL,
    neuroretinal_rim_area DOUBLE PRECISION NOT NULL,
    cup_perimeter DOUBLE PRECISION NOT NULL,
    cup_area DOUBLE PRECISION NOT NULL,
    state VARCHAR(255) NOT NULL,
    ddl_stage INTEGER NOT NULL,
    pacient_id UUID NOT NULL,
    FOREIGN KEY (pacient_id) REFERENCES pacient(pacient_id) ON DELETE CASCADE -- Relación con Pacient
);
