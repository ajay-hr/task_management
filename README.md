# Task Management API

Spring Boot REST API for task and project management with JWT authentication, collaborative project membership, assignment tracking, due dates, and scheduled deadline reminders.

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Security with JWT access and refresh tokens
- Spring Data JPA with PostgreSQL
- Spring Mail for reminder notifications
- Maven

## Features

- User registration and login with BCrypt password hashing.
- Stateless JWT authentication with refresh-token endpoint.
- Project CRUD with owner and member-based access control.
- Can add tasks with status workflow: `TODO`, `IN_PROGRESS`, `DONE`.
- Task assignment to project owner or members only.
- Scheduled reminder job for assigned tasks due in the next 24 hours.

## Run Locally

For a quick local demo without PostgreSQL:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The dev profile uses an in-memory H2 database and disables reminder emails. By default the API runs on `http://localhost:7001`.

The browser UI is available at:

```text
http://localhost:7001/
```

Swagger UI is available at:

```text
http://localhost:7001/swagger-ui.html
```

If you override the port, use that port instead, for example `http://localhost:7002/swagger-ui.html`.

Registration validation:

- Email must be a valid address such as `exam@gmail.com`.
- Password must be at least 8 characters and include a letter, a number, and a special character.
- Example password: `Password@123`.

Start PostgreSQL and create a database named `task_management`, then run:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/task_management
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
export JWT_SECRET=change-this-secret-to-at-least-32-characters
./mvnw spring-boot:run
```

Optional admin user:

```bash
export ADMIN_EMAIL=admin@example.com
export ADMIN_PASSWORD=Admin@123
```

## API Overview

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/projects`
- `POST /api/projects`
- `GET /api/projects/{id}`
- `PUT /api/projects/{id}`
- `DELETE /api/projects/{id}`
- `GET /api/projects/{projectId}/tasks`
- `POST /api/projects/{projectId}/tasks`
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `DELETE /api/tasks/{id}`
- `POST /api/admin/users/{userId}/reset-password`

Send authenticated requests with:

```text
Authorization: Bearer <accessToken>
```
