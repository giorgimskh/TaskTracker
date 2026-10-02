# 📌 Task Tracker

A **REST API** for managing task lists and their tasks, built with **Spring Boot** and **Spring Data JPA** on **PostgreSQL**.

> The repository is named *Task-Tracker-CLI* for historical reasons. There is no command-line interface. The app is a web API.

---

## ✨ Features
- 📂 Create, list, update and delete **task lists**
- ✅ Create, list, update and delete **tasks** inside a list
- 🚦 Task **priority** (`LOW` · `MEDIUM` · `HIGH`) and **status** (`OPEN` · `CLOSED`)
- 📊 Every task list reports its task **count** and **progress** (closed / total)
- 🛡️ Input validation with clear `400 Bad Request` error responses
- 🧪 Unit + integration tests (JUnit, Mockito, MockMvc on H2) and a **Postman** end-to-end collection

---

## 🧰 Tech Stack
| | |
|---|---|
| Language | Java 23 |
| Framework | Spring Boot 3.5 (Web, Data JPA) |
| Database | PostgreSQL (runtime), H2 (tests) |
| Build | Maven (wrapper included) |
| API testing | Postman collection + Postman CLI |

---

## 🚀 Getting Started

### Prerequisites
- JDK 23+
- Docker (for PostgreSQL)

### 1. Start PostgreSQL
```bash
docker compose up -d
```
Postgres starts on `localhost:5432`. The credentials match `src/main/resources/application.properties`.
Hibernate creates and updates the schema automatically (`ddl-auto=update`).

### 2. Run the API
```bash
./mvnw spring-boot:run
```
The API is available at `http://localhost:8080`.

---

## 📡 API Endpoints

### Task lists: `/api/task-lists`
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/task-lists` | List all task lists |
| `POST` | `/api/task-lists` | Create a task list |
| `GET` | `/api/task-lists/{task_list_id}` | Get one task list (with its tasks) |
| `PUT` | `/api/task-lists/{task_list_id}` | Update a task list |
| `DELETE` | `/api/task-lists/{task_list_id}` | Delete a task list (and all its tasks) |

### Tasks: `/task_list/{task_list_id}/tasks`
| Method | Path | Description |
|---|---|---|
| `GET` | `/task_list/{task_list_id}/tasks` | List tasks in a list |
| `POST` | `/task_list/{task_list_id}/tasks` | Create a task |
| `GET` | `/task_list/{task_list_id}/tasks/{task_id}` | Get one task |
| `PUT` | `/task_list/{task_list_id}/tasks/{task_id}` | Update a task |
| `DELETE` | `/task_list/{task_list_id}/tasks/{task_id}` | Delete a task |

### Examples
Create a task list:
```http
POST /api/task-lists
Content-Type: application/json

{ "title": "Groceries", "description": "weekly shopping" }
```
```json
{ "id": "6f1c…", "title": "Groceries", "description": "weekly shopping", "count": 0, "progress": null, "tasks": null }
```

Create a task (`priority` is optional and defaults to `MEDIUM`; `status` always starts as `OPEN`):
```http
POST /task_list/6f1c…/tasks
Content-Type: application/json

{ "title": "Buy milk", "description": "2 litres", "dueDate": "2026-12-31", "priority": "HIGH" }
```

Update a task (the body `id` must match the URL, and `title`, `priority` and `status` are required):
```http
PUT /task_list/6f1c…/tasks/9a2b…
Content-Type: application/json

{ "id": "9a2b…", "title": "Buy milk", "priority": "HIGH", "status": "CLOSED" }
```

### Rules & behaviour
- The server assigns `id`. Sending an `id` on create returns `400`.
- On update, the `id` in the body must match the one in the URL.
- Validation errors return `400` with an error body:
  ```json
  { "status": 400, "message": "Task list title is required!", "details": "uri=/api/task-lists" }
  ```
- `GET` on a list or task that doesn't exist returns `200` with an empty (`null`) body.

---

## 🧪 Testing

### Unit & integration tests
```bash
./mvnw test
```
These use an in-memory H2 database, so you don't need Postgres.
- **Service tests**: Mockito, with the repositories mocked
- **Mapper tests**: plain JUnit
- **Controller tests**: `@SpringBootTest` + MockMvc against real endpoints

### Postman (end-to-end)
The `postman/` folder has a collection that covers every endpoint: 28 requests and 63 assertions, including error cases. It also has a `Local` environment.

| Path | Contents |
|---|---|
| `postman/collections/Task Tracker API/` | Requests and test scripts (Postman v3 YAML format) |
| `postman/environments/Local.environment.yaml` | `base_url = http://localhost:8080` |
| `.postman/resources.yaml` | Links this repo to the **Task Tracker API** Postman workspace |

The collection creates its own data, chains the generated ids between requests, and deletes everything at the end.
**Run it in order**, with Postgres and the API running.

**Postman app:** open the *Task Tracker API* workspace, choose the **Local** environment, then use **Run collection**.

**Postman CLI:**
```bash
postman collection run "postman/collections/Task Tracker API" -e postman/environments/Local.environment.yaml
```

**Keeping the workspace in sync:**
```bash
postman workspace push   # repo → Postman cloud
postman workspace pull   # Postman cloud → repo
```

---

## 🗂️ Project Structure
```text
src/main/java/com/george/task_tracker_cli/
├── Controllers/      # REST controllers + GlobalExceptionHandler
├── Services/         # service interfaces, Impl/ holds the implementations (validation lives here)
├── Repositories/     # Spring Data JPA repositories
├── mappers/          # DTO ↔ entity mappers, impl/ holds the implementations
└── domain/
    ├── entities/     # TaskList, Task, TaskStatus, TaskPriority
    └── dto/          # TaskListDto, TaskDto, ErrorResponse (records)
```
Request flow: **Controller → Mapper → Service → Repository**
