

# 🩺 RememberMe — Plataforma de Adherencia Terapéutica y Bienestar del Cuidador

**RememberMe** es una aplicación web clínica desarrollada bajo arquitectura multicapa con **Spring Boot 3.4** y **Java 21**. Su propósito es garantizar la seguridad farmacológica en pacientes con enfermedades crónicas o polimedicación mediante la verificación estructurada de los **5 correctos**, al mismo tiempo que monitorea y protege la salud física y emocional del cuidador.

---

## 📋 Tabla de Contenidos
1. [Características Principales](#-características-principales)
2. [Stack Tecnológico](#-stack-tecnológico)
3. [Arquitectura del Proyecto](#-arquitectura-del-proyecto)
4. [Modelo de Base de Datos (MER)](#-modelo-de-base-de-datos-mer)
5. [Requisitos Previos](#-requisitos-previos)
6. [Instalación y Ejecución Local](#-instalación-y-ejecución-local)
7. [Solución de Problemas en Entornos Restringidos](#-solución-de-problemas-en-entornos-restringidos)
8. [Autor](#-autor)

---

## ✨ Características Principales

* **Gestión Multi-Paciente:** Administración centralizada de perfiles clínicos, alergias, fechas de nacimiento y notas médicas, permitiendo asignar cuidadores principales y de apoyo (`cuidador_paciente`).
* **Protocolo de los 5 Correctos:** Registro trazable de cada toma (`registro_tomas`) validando obligatoriamente:
  1. **Paciente correcto**
  2. **Medicamento correcto**
  3. **Dosis correcta**
  4. **Vía de administración correcta** (`ORAL`, `TOPICA`, `INTRAVENOSA`, `INTRAMUSCULAR`, `SUBCUTANEA`, `INHALATORIA`, `OFTALMICA`, `OTICA`, `OTRA`)
  5. **Hora correcta** (control de estados: `PENDIENTE`, `ADMINISTRADO`, `OMITIDO`, `RETRASADO`).
* **Alertas de Alto Riesgo:** Identificación visual prioritaria para fármacos críticos (`es_alto_riesgo`) y documentación obligatoria de motivos de omisión.
* **Bienestar y Autocuidado del Cuidador:** Bitácora diaria (`bienestar_cuidador`) para medir nivel de estrés, fatiga, horas de sueño y estado emocional, previniendo el síndrome del cuidador quemado.
* **Guías Clínicas Categorizadas:** Biblioteca de apoyo (`categorias_guias` y `guias`) con protocolos prácticos y búsqueda por etiquetas.
* **Seguridad y Roles:** Autenticación con **Spring Security 6**, cifrado de contraseñas con **BCrypt** y control de acceso basado en roles (`ROL_CUIDADOR`, `ROL_ADMIN`).

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología | Versión |
| :--- | :--- | :--- |
| **Lenguaje** | Java (OpenJDK / Temurin) | `21` |
| **Framework Backend** | Spring Boot (Web, Data JPA, Security, Validation) | `3.4.13` |
| **Motor de Plantillas y UI** | Thymeleaf + Thymeleaf Extras Spring Security 6 + Tailwind CSS | `3.x` |
| **Base de Datos** | MariaDB / MySQL | `10.x / 11.x` |
| **Documentación API** | Springdoc OpenAPI (Swagger UI) | `2.8.8` |
| **Gestor de Dependencias** | Maven Wrapper (`mvnw`) | `3.9.x` |

---

## 🏗️ Arquitectura del Proyecto

El proyecto sigue el patrón **MVC multicapa** con separación estricta de responsabilidades:

```text
RememberMe-/
├── .mvn/                         # Archivos del Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/rememberme/app/
│   │   │   ├── config/           # Configuraciones generales y beans de infraestructura
│   │   │   ├── controller/       # Controladores Web (Thymeleaf) y endpoints REST
│   │   │   ├── dto/              # Objetos de transferencia y validación de datos
│   │   │   ├── entity/           # Entidades JPA mapeadas a MariaDB
│   │   │   ├── exception/        # Manejo global de excepciones personalizadas
│   │   │   ├── repository/       # Interfaces Spring Data JPA
│   │   │   ├── security/         # Configuración Spring Security 6 y UserDetailsService
│   │   │   ├── service/          # Lógica de negocio y reglas clínicas
│   │   │   └── RememberMeApplication.java
│   │   └── resources/
│   │       ├── db/               # Scripts SQL (schema.sql)
│   │       ├── static/           # Recursos estáticos (CSS, JS, imágenes)
│   │       ├── templates/        # Vistas dinámicas HTML con Thymeleaf
│   │       └── application.yml   # Configuración de servidor, JPA y conexión a BD
│   └── test/                     # Pruebas unitarias y de integración
├── pom.xml                       # Dependencias y plugins de Maven
└── README.md
```

---

## 🗄️ Modelo de Base de Datos (MER)

El esquema relacional `rememberme_db` está compuesto por **7 entidades principales**:

* **`usuarios`**: Cuentas de acceso al sistema (`ROL_CUIDADOR`, `ROL_ADMIN`).
* **`pacientes`**: Información clínica, alergias y estado activo de cada paciente.
* **`cuidador_paciente`**: Tabla intermedia con clave primaria compuesta (`cuidador_id`, `paciente_id`) que gestiona la asignación de cuidadores principales y secundarios.
* **`medicamentos`**: Prescripciones activas por paciente, dosis, unidad, vía de administración, frecuencia en horas y bandera de alto riesgo.
* **`registro_tomas`**: Auditoría histórica de tomas programadas vs. reales, estados y verificación booleana de los 5 correctos.
* **`bienestar_cuidador`**: Seguimiento diario de estrés, fatiga, horas de sueño y estado emocional del cuidador.
* **`categorias_guias` y `guias`**: Contenido educativo clínico organizado por categorías y etiquetas.

---

## ⚙️ Requisitos Previos

Antes de ejecutar el proyecto en un entorno local, asegúrate de contar con:

1. **JDK 21** instalado y configurado en la variable de entorno `JAVA_HOME`.
2. **MariaDB** (o MySQL) en ejecución en el puerto `3306`.
3. **Git** instalado para clonar el repositorio.

---

## 🚀 Instalación y Ejecución Local

1. **Clonar el repositorio:**
   ```bash
   git clone -b main https://github.com/ALMEJIA21/RememberMe-.git
   cd RememberMe-
   ```

2. **Crear la base de datos en MariaDB:**
   ```sql
   CREATE DATABASE IF NOT EXISTS rememberme_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **Verificar credenciales de base de datos en `src/main/resources/application.yml`:**
   Asegúrate de que `spring.datasource.url`, `username` y `password` coincidan con tu instancia local de MariaDB.

4. **Compilar y ejecutar la aplicación con Maven Wrapper:**
   * **Windows (PowerShell / CMD):**
     ```powershell
     .\mvnw spring-boot:run
     ```
   * **Linux / macOS:**
     ```bash
     ./mvnw spring-boot:run
     ```

5. **Acceder a la plataforma desde el navegador:**
   * **Aplicación Web:** `http://localhost:8080`
   * **Documentación Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

---

## 🔧 Solución de Problemas en Entornos Restringidos

Si ejecutas el proyecto en equipos compartidos o institucionales (por ejemplo, laboratorios con Java 8 predeterminado o sin permisos de administrador):

* **Configurar JDK 21 portable en la sesión actual de PowerShell:**
  ```powershell
  $env:JAVA_HOME = "C:\Ruta\A\Tu\jdk-21"
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"
  java -version
  ```
* **Ejecutar en modo sin conexión (Offline) una vez descargadas las dependencias en `.m2`:**
  ```powershell
  .\mvnw spring-boot:run -o
  ```

---
