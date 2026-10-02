# Task Tracker API

![CI](https://github.com/giorgimskh/task-tracker-api/actions/workflows/ci.yml/badge.svg)

A simple REST API for managing task lists and tasks. I built it with Spring Boot, Spring Data JPA and PostgreSQL.

## Features

- Create, read, update and delete task lists
- Create, read, update and delete tasks inside a task list
- Tasks have a priority (LOW, MEDIUM, HIGH) and a status (OPEN, CLOSED)
- A task list shows how many tasks it has and its progress (closed tasks / all tasks)
- Invalid requests return 400 and missing task lists or tasks return 404, both with an error message

## Tech used

- Java 23
- Spring Boot 3.5
- Spring Data JPA / Hibernate
- PostgreSQL
- H2 (only for tests)
- Maven
- JUnit 5, Mockito, MockMvc
- Postman

## How to run

You need JDK 23 and Docker.

1. Start the database:

```
docker compose up -d
```

2. Run the app:

```
./mvnw spring-boot:run
```

The app runs on http://localhost:8080. Tables are created automatically by Hibernate.

The database connection settings are in `src/main/resources/application.properties`. The password there matches the one in `docker-compose.yml`.

## Endpoints

Task lists:

```
GET    /api/task-lists
POST   /api/task-lists
GET    /api/task-lists/{task_list_id}
PUT    /api/task-lists/{task_list_id}
DELETE /api/task-lists/{task_list_id}
```

Tasks:

```
GET    /api/task-lists/{task_list_id}/tasks
POST   /api/task-lists/{task_list_id}/tasks
GET    /api/task-lists/{task_list_id}/tasks/{task_id}
PUT    /api/task-lists/{task_list_id}/tasks/{task_id}
DELETE /api/task-lists/{task_list_id}/tasks/{task_id}
```

Deleting a task list also deletes all of its tasks.

### Status codes

```
200 OK          - GET and PUT worked
201 Created     - POST created a task list or task
204 No Content  - DELETE worked
400 Bad Request - the request is invalid (for example a missing title)
404 Not Found   - the task list or task doesn't exist
```

### Example requests

All request bodies are JSON, so send the `Content-Type: application/json` header.

Create a task list:

```
POST /api/task-lists
{
  "title": "Groceries",
  "description": "weekly shopping"
}
```

Response:

```
{
  "id": "6f1c2d3e-...",
  "title": "Groceries",
  "description": "weekly shopping",
  "count": 0,
  "progress": null,
  "tasks": null
}
```

When a list has tasks, `count` is the number of tasks and `progress` is a number from 0 to 1. For example, 2 tasks with 1 closed gives 0.5.

Create a task (priority is optional, default is MEDIUM; status is always OPEN when you create a task):

```
POST /api/task-lists/{task_list_id}/tasks
{
  "title": "Buy milk",
  "description": "2 litres",
  "dueDate": "2026-12-31",
  "priority": "HIGH"
}
```

Update a task (id has to be the same as in the URL, and title, priority and status are required):

```
PUT /api/task-lists/{task_list_id}/tasks/{task_id}
{
  "id": "{task_id}",
  "title": "Buy milk",
  "priority": "HIGH",
  "status": "CLOSED"
}
```

Error response examples:

```
{
  "status": 400,
  "message": "Task list title is required!",
  "details": "uri=/api/task-lists"
}
```

```
{
  "status": 404,
  "message": "Task list not found",
  "details": "uri=/api/task-lists/00000000-0000-0000-0000-000000000000"
}
```

## Tests

Run the unit and integration tests:

```
./mvnw test
```

The tests use an H2 in-memory database, so Postgres doesn't need to be running.

- Service tests use Mockito
- Mapper tests are plain JUnit tests
- Controller tests use @SpringBootTest and MockMvc

The tests also run on GitHub Actions on every push and pull request.

## Postman

There is a Postman collection in the `postman/` folder that tests every endpoint, including error cases. There is also a `Local` environment with `base_url = http://localhost:8080`.

The collection creates its own test data and deletes it at the end. The requests need to run in order, because later requests use ids saved by earlier ones.

I also keep the collection in my Postman workspace ("Task Tracker API"). There I select the "Local" environment and click "Run collection".

To run it with the [Postman CLI](https://learning.postman.com/docs/postman-cli/postman-cli-installation/):

```
postman collection run "postman/collections/Task Tracker API" -e postman/environments/Local.environment.yaml
```

The database and the app need to be running for this.

## Project structure

```
controllers   - REST controllers and the exception handler
services      - business logic and validation
repositories  - JPA repositories
mappers       - convert between DTOs and entities
domain        - entities and DTOs
exceptions    - custom exceptions (ResourceNotFoundException)
```

## Things to improve

- Add JPA auditing for the created/updated timestamps instead of setting them by hand
- Use Bean Validation (`@Valid`, `@NotBlank`) instead of checking fields by hand in the services
- Return 404 when deleting a task list or task that doesn't exist (right now it returns 204)
- Validate the title when updating a task list
