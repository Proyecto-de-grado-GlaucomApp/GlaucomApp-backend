# GlauApp Backend

[English](#english) | [Español](#español)

## English

### Overview
GlauApp is a modular backend application built with Spring Modulith for glaucoma screening and patient management. The system provides a robust architecture for handling medical images, patient records, and secure authentication.

### Architecture
The application follows a modular monolith architecture using Spring Modulith, composed of the following modules:

![components](https://github.com/user-attachments/assets/4ebf3c16-6e19-48cc-85de-eeea2c75de61)

#### Core Modules
- **API Key User Authentication (`apikeyuserauth`)**: Handles user authentication and session management
- **API Key Management (`apikeymanagement`)**: Manages API key generation and validation
- **Security (`security`)**: Central security module for authentication and authorization
- **Glaucoma Screening (`glaucomascreening`)**: Core module for eye fundus image analysis
- **Clinical History (`clinical_history`)**: Manages patient medical records and examination data
- **S3 Integration (`s3`)**: Handles cloud storage for medical images using Amazon S3
- **Mobile Authentication (`mobileauth`)**: Manages mobile device authentication
- **Common (`common`)**: Shared utilities and configurations

### Technical Requirements
- Java 21
- Spring Boot 3.3.x
- Maven
- PostgreSQL
- AWS Account (for S3 storage)
- Docker (for image processing server)

### Setup Instructions

#### 1. Database Setup
The application requires PostgreSQL, which can be set up in three ways:

- **Local Installation**
- **Docker Container**
- **Cloud (Amazon RDS)**

Regardless of the chosen method, you must enable the uuid-ossp extension:

```bash
psql -U myuser -d mydatabase
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```
#### 2. Image Processing Server Setup
First, you need to start the glaucoma analysis server. This server contains the image processing algorithms.

```bash
# Build the Docker image
sudo docker build --no-cache -f Dockerfile_GlaucomaAnalyzerServer -t glaucomaanalyzerserver:latest .

# Run the container
sudo docker run -p 5000:5000 glaucomaanalyzerserver:latest
```

#### 3. Clone Repository
```bash
git clone https://github.com/yourusername/glauapp-backend.git
```

#### 4. Configure Environment Variables
```properties
# Database Configuration
DB_PORT=your_database_port
DB_HOST=your_database_host
DB_NAME=your_database_name
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

# Image Processing API
# Replace 'localhost' with the actual host where the Docker container is running
PYTHON_API_URL=http://localhost:5000/upload

# AWS Configuration
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_BUCKET_NAME=your_s3_bucket_name
AWS_REGION_NAME=your_aws_region

# Security
JWT_SECRET_KEY=your_jwt_secret_key
```

#### 5. Build the Project
```bash
mvn clean install
```

#### 6. Run the Application
```bash
mvn spring-boot:run
```


---

## Español

### Descripción General
GlauApp es una aplicación backend modular construida con Spring Modulith para el tamizaje de glaucoma y gestión de pacientes. El sistema proporciona una arquitectura robusta para el manejo de imágenes médicas, registros de pacientes y autenticación segura.

### Arquitectura
La aplicación sigue una arquitectura de monolito modular utilizando Spring Modulith, compuesta por los siguientes módulos:

![components](https://github.com/user-attachments/assets/4ebf3c16-6e19-48cc-85de-eeea2c75de61)


#### Módulos Principales
- **Autenticación de API Keys de Usuario (`apikeyuserauth`)**: Gestiona la autenticación de usuarios y sesiones
- **Gestión de API Keys (`apikeymanagement`)**: Administra la generación y validación de API keys
- **Seguridad (`security`)**: Módulo central de seguridad para autenticación y autorización
- **Tamizaje de Glaucoma (`glaucomascreening`)**: Módulo principal para análisis de imágenes de fondo de ojo
- **Historia Clínica (`clinical_history`)**: Gestiona registros médicos y datos de exámenes de pacientes
- **Integración S3 (`s3`)**: Maneja el almacenamiento en la nube de imágenes médicas usando Amazon S3
- **Autenticación Móvil (`mobileauth`)**: Gestiona la autenticación de dispositivos móviles
- **Común (`common`)**: Utilidades y configuraciones compartidas

### Requisitos Técnicos
- Java 21
- Spring Boot 3.3.x
- Maven
- PostgreSQL
- Cuenta AWS (para almacenamiento S3)
- Docker (para servidor de procesamiento de imágenes)

### Instrucciones de Configuración

#### 1. Configuración de Base de Datos

La aplicación requiere PostgreSQL. Tienes varias opciones para configurar la base de datos:

- **Instalación Local**
- **Contenedor Docker**
- **Nube (Amazon RDS)**

Después de configurar tu opción preferida, habilita la extensión requerida:

```bash
psql -U myuser -d mydatabase
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

#### 2. Configuración del Servidor de Procesamiento de Imágenes
Primero, debe iniciar el servidor de análisis de glaucoma. Este servidor contiene los algoritmos de procesamiento de imágenes.

```bash
# Construir la imagen Docker
sudo docker build --no-cache -f Dockerfile_GlaucomaAnalyzerServer -t glaucomaanalyzerserver:latest .

# Ejecutar el contenedor
sudo docker run -p 5000:5000 glaucomaanalyzerserver:latest
```

#### 3. Clonar el Repositorio
```bash
git clone https://github.com/yourusername/glauapp-backend.git
```

#### 4. Configurar Variables de Entorno
```properties
# Configuración de Base de Datos
DB_PORT=puerto_base_de_datos
DB_HOST=host_base_de_datos
DB_NAME=nombre_base_de_datos
DB_USERNAME=usuario_base_de_datos
DB_PASSWORD=contraseña_base_de_datos

# API de Procesamiento de Imágenes
# Reemplazar 'localhost' con el host donde se está ejecutando el contenedor Docker
PYTHON_API_URL=http://localhost:5000/upload

# Configuración AWS
AWS_ACCESS_KEY_ID=tu_aws_access_key
AWS_SECRET_ACCESS_KEY=tu_aws_secret_key
AWS_BUCKET_NAME=nombre_bucket_s3
AWS_REGION_NAME=region_aws

# Seguridad
JWT_SECRET_KEY=tu_clave_secreta_jwt
```

#### 5. Compilar el Proyecto
```bash
mvn clean install
```

#### 6. Ejecutar la Aplicación
```bash
mvn spring-boot:run
```
