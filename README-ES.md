<p align="center">
    <img src="public/Reservo-combined-mark.svg" alt="Reservo combined mark" />
</p>

# Tabla de contenidos

- [Tabla de contenidos](#tabla-de-contenidos)
- [Contexto de Reservo y finalidad](#contexto-de-reservo-y-finalidad)
- [Reservo Events User MS](#reservo-events-user-ms-)
  - [1. NATS (Principal)](#1-nats-principal)
  - [2. API REST (Con fines educativos)](#2-api-rest-con-fines-educativos)
  - [Paradigmas y herramientas aprendidas](#paradigmas-y-herramientas-aprendidas-️)
- [Configuración del entorno ⚙](#configuración-del-entorno-)
  - [Requisitos previos](#requisitos-previos)
  - [Inicialización del proyecto](#inicialización-del-proyecto)
- [Licencia](#licencia)

# Contexto de Reservo y finalidad

Reservo nace como un proyecto personal con **el objetivo de aprender y experimentar de primera mano los principios de la arquitectura de microservicios**, comprendiendo tanto sus ventajas como sus desafíos mientras desarrollaba una solución para gestionar las reservas de un auditorio.

Además, quería explorar una tecnología diferente al ecosistema de JavaScript para construir servidores web, por lo que decidí desarrollar el núcleo del proyecto en Java 17 con Spring Boot. Este camino me permitió adquirir nuevos conocimientos, afianzar buenas prácticas y diseñar un repositorio orientado a facilitar una experiencia de desarrollo limpia y mantenible.

Reconozco que la distribución de responsabilidades entre los distintos microservicios pudo haber sido más simple. Sin embargo, quise retarme simulando un entorno más complejo, donde fuera necesario orquestar múltiples servicios al mismo tiempo. Esta decisión, aunque desafiante, me permitió entender mejor las implicaciones reales de trabajar con este tipo de arquitectura en contextos más amplios.

Reservo está compuesto por cinco repositorios:

- [Fronted desarrollado en Astro y React 🚀](https://github.com/Hector-f-Romero/reservo-front)
- [Gateway de NestJS encargado de enrutar las peticiones al microservicio correspondiente 🧠](https://github.com/Hector-f-Romero/reservo-api-gateway)
- [Microservicio en Spring Boot que gestiona el CRUD de las entidades involucradas 🎨](https://github.com/Hector-f-Romero/reservo-events-user-ms)
- [App híbrida de NestJS que utiliza WebSockets y se comunica entre microservicios ⌚](https://github.com/Hector-f-Romero/reservo-ws-ms)
- [Microservicio de NestJS dedicado a la autenticación 🔐](https://github.com/Hector-f-Romero/reservo-auth-ms)

<p align="center">
    <img src="public/Reservo-architecture-diagram.svg" alt="Diagrama de arquitectura de Reservo" />
</p>

En cada repositorio he documentado los principales retos enfrentados y los aprendizajes obtenidos durante el desarrollo. Mirando hacia atrás, solo puedo sentirme orgulloso del esfuerzo invertido y del resultado alcanzado con este proyecto.

# Reservo Events User MS 👤

Este servidor web proporciona operaciones CRUD para las entidades Usuario, Evento y Asiento, permitiendo la creación de eventos, listado de eventos próximos, reservación de asientos para eventos específicos y otras funciones relacionadas. El servicio utiliza una base de datos PostgreSQL 16 gestionada mediante Docker a través del archivo `docker-compose.yaml`.

Este proyecto admite dos modos de comunicación:

## 1. NATS (Principal)

Este microservicio está diseñado principalmente para operar a través de [NATS](https://nats.io), un sistema de mensajería ligero. Se utiliza un patrón de publicación/suscripción para eventos del sistema. La lógica relacionada con esta implementación está organizada en subpaquetes llamados `nats` dentro de cada paquete de entidad.

Cada uno de estos subpaquetes típicamente incluye:
- Una clase `Publisher` para emitir eventos.
- Un `NatsController` para escuchar y responder a mensajes de NATS.
- Una lista de subjects (patrones de eventos) a los que el microservicio se suscribe.
- Una clase que extiende `NatsMessage`, lo cual ayuda a Spring a identificar qué servicios deben manejar mensajes específicos.

## 2. API REST (Con fines educativos)

Para familiarizarme con Spring Boot, la implementación inicial incluyó controladores REST tradicionales. Estos están completamente documentados usando Swagger y sirven como ejemplos del desarrollo de APIs RESTful en Spring Boot. También fueron útiles para realizar pruebas durante las primeras etapas del desarrollo.

## Paradigmas y herramientas aprendidas 🛠️

Durante el desarrollo de este microservicio, adquirí experiencia práctica con varias herramientas potentes y paradigmas arquitectónicos del ecosistema Java:

- **JPA & Hibernate**: Permitieron definir relaciones complejas entre entidades y estrategias de persistencia de datos. Además, exploré el uso de consultas SQL nativas para operaciones personalizadas y optimizadas en la base de datos.
- **Lombok**: Ayudó a reducir código repetitivo al generar automáticamente getters, setters, constructores y más, mejorando significativamente la legibilidad.
- **MapStruct**: Simplificó el mapeo entre DTOs y entidades, promoviendo una separación clara entre modelos de API y lógica interna del dominio.
- **Programación Orientada a Aspectos (AOP)**: Gracias al aprendizaje de esta técnica, implementé un manejo de errores centralizado durante la comunicación con NATS, lo que permitió una separación más limpia de la lógica de negocio.
- Permitió la gestión centralizada de aspectos transversales como el manejo de errores y el registro de logs, especialmente durante la comunicación basada en eventos.
- **Spring Security (spring-boot-starter-security)**: Proporcionó una base sólida para comprender cómo asegurar endpoints REST e integrar capas de autorización de forma efectiva.

Debido a mi experiencia previa con NestJS y a la investigación en varios foros y videos, este proyecto utiliza una estructura de archivos basada en características (*Feature-Based File Structure*), lo cual facilitó agrupar las distintas capas asociadas a una única entidad.

# Configuración del entorno ⚙

## Requisitos previos
- Java 27
- Maven
- Docker Desktop

## Inicialización del proyecto

1. Clona este repositorio y navega al directorio del proyecto.
2. Crea el archivo de variables de entorno con: `cp .env.example .env`.
3. Rellena los valores requeridos en el archivo `.env`.
4. Inicia el servidor NATS con: `docker-compose up -d`.
5. Compila el proyecto con: `./mvnw clean install`.
6. Ejecuta la aplicación: `./mvnw spring-boot:run`.
7. Asegúrate de que los siguientes servicios estén en ejecución:
   - Reservo API Gateway
   - Reservo Auth MS
   - Reservo WebSocket MS.
8. Ejecuta el gateway y envía una solicitud `HTTP POST` al endpoint `/V1/seed` para poblar la base de datos con datos de prueba. Para más información, consulta el [repositorio del gateway](https://github.com/Hector-f-Romero/reservo-api-gateway).

> [!NOTA]
> Alternativamente, puedes enviar una solicitud POST a `http://localhost:8080/api/v1/seed` directamente desde este servicio para poblar la base de datos sin depender del gateway.

Por defecto, la documentación de la API está disponible en: `http://localhost:8080/api/v1/swagger-ui/index.html`.

# Licencia

Consulta el archivo `LICENSE` para más información.

