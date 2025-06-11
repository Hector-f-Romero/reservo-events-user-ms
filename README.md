# Reservo - Event - User MS


# Table of contents


- [Reservo - Event - User MS](#reservo---event---user-ms)
- [Table of contents](#table-of-contents)
- [Reservo - Origin and context](#reservo---origin-and-context)
  - [🧠 About the project](#-about-the-project)
  - [Prerequisites](#prerequisites)
  - [License](#license)

# Reservo - Origin and context

Reservo is a web application that displays upcoming events hosted in an auditorium with limited seating, allowing users to create an event as long as the chosen time slot is available for the auditorium. This project was born from the desire and challenge to experiment with various technologies in a microservices architecture, experiencing firsthand the advantages and drawbacks of its implementation.

To function properly, this project is split into five repositories:
- [Reservo Front](https://github.com/Hector-f-Romero/reservo-front)
- [API Gateway](https://github.com/Hector-f-Romero/reservo-api-gateway)
- [Event & User MS](https://github.com/Hector-f-Romero/reservo-events-user-ms)
- [Hybrid Web Socket Server](https://github.com/Hector-f-Romero/reservo-ws-ms)
- [Auth MS](https://github.com/Hector-f-Romero/reservo-auth-ms)
  
> [!NOTE]
> I am aware that the broad separation of responsibilities in this project is not the most optimal and introduces operational complexity, but it was done intentionally to simulate what it’s like to work on a large-scale project with this architecture.


## 🧠 About the project

This web server provides CRUD operations for the User, Event, and Seat entities, enabling event creation, listing of upcoming events, seat reservation for specific events, and other related functions. It connects to a PostgreSQL 16 database.

To learn more about Spring Boot, this project initially implemented REST controllers as a first introduction to the framework, documented with Swagger. Later, controllers were added to listen for and emit domain events via NATS and by means of Aspect Oriented Programming (AOP), I was able to centralize error handling during NATS communication and learn to decouple this bussiness logic.

Due to my previous experience with NestJs and research in several forums and videos, this project uses a Feature-Based File Structure, which made it easier for me to group the different layers associated to a single entity.

## Prerequisites

- Java 21  
- Docker
- Configure and execute NestJs gateway
- Execute the endpoint `POST` `/seed` to generate an initial data.


## License

See `LICENSE` for more information.
