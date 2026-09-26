# Student Management System

A production-style, beginner-friendly **Spring Boot 3 + MySQL** REST API for
managing students, built with a clean layered architecture and a complete
DevOps toolchain (Docker, Docker Compose, Jenkins, Prometheus, Grafana) —
designed to be easy to run, explain, and demo in a DevOps viva.

> No authentication, no frontend — pure backend REST API, by design.

---

## JDK 25 upgrade notes

This project now targets **JDK 25** (moved from JDK 21). Summary of what
changed and why — see [Explanation of every DevOps file](#explanation-of-every-devops-file)
for the full per-file rationale:

- `pom.xml`: parent bumped `spring-boot-starter-parent` 3.2.5 → **3.5.16**
  (the first Boot 3.x line documented as compatible "up to and including
  Java 25"), `java.version` property 21 → **25**, an explicit
  `lombok.version` (**1.18.48**, the newest release - 1.18.40 was merely the
  *first* JDK 25-aware release and had rough edges later patches fixed) and
  Lombok explicitly registered as a `maven-compiler-plugin`
  `annotationProcessorPath` (see [Known issue](#known-issue-lombok-not-generating-code-on-first-build)
  below) rather than relying on implicit classpath discovery.
- `Dockerfile`: base images bumped from `...-temurin-21` to
  **`...-temurin-25`** in both build and runtime stages.
- `Jenkinsfile`: the `jdk 'JDK21'` tool reference renamed to **`jdk
  'JDK25'`** — you'll need a JDK 25 tool installation registered in
  Jenkins under that exact name (Manage Jenkins → Tools).
- **No Java source files changed** — the codebase already used `jakarta.*`
  imports and no deprecated/removed APIs, so it needed no code changes for
  JDK 25.
- **MySQL configuration and all CRUD functionality are unchanged.**
- **IntelliJ**: this project ships with no `.idea`/`.iml` files (plain
  Maven project), so there's nothing to patch — set the Project SDK and
  language level to 25 yourself once, as below.

### Setting IntelliJ to JDK 25

1. Install JDK 25 if IntelliJ doesn't already list it: **File → Project
   Structure → SDKs → + → Download JDK...** (or point it at your Oracle
   JDK 25.0.4 install directory).
2. **File → Project Structure → Project** → set **SDK** to `25` and
   **Language level** to `25 (...)`.
3. **File → Project Structure → Modules** → the `student-management-system`
   module should inherit the project SDK; if it shows its own SDK/language
   level, set both to `25` there too.
4. **Settings → Build, Execution, Deployment → Build Tools → Maven →
   Runner** → set **JRE** to `25`.
5. **Settings → Build, Execution, Deployment → Compiler → Annotation
   Processors** → confirm "Enable annotation processing" is checked (needed
   for Lombok — this was presumably already on).
6. Reload the Maven project (the circular-arrows "Reload" icon in the Maven
   tool window) so IntelliJ re-reads the updated `pom.xml`.

### Known issue: Lombok not generating code on first build

On the very first pass at this upgrade, `mvn clean package` under JDK 25
failed with dozens of `cannot find symbol: method getXxx()` /
`method builder()` errors, even though `mvn dependency:tree` confirmed
Lombok was on the classpath at the right version. In other words, Lombok's
annotation processor loaded but silently generated nothing — every
`@Data`/`@Builder`/`@RequiredArgsConstructor`/`@NoArgsConstructor` method
was simply missing.

This is a known category of problem: each new JDK release changes internal
`javac` structures Lombok depends on via reflection, and Lombok needs its
own release to catch up — the first release supporting a new JDK (in this
case 1.18.40) isn't always fully solid for every case. Two changes fixed it:

1. **Bumped `lombok.version` 1.18.40 → 1.18.48** (the newest release as of
   this writing) — several 1.18.4x patches after 1.18.40 specifically fixed
   JDK 25 javac/annotation-processing bugs.
2. **Explicitly registered Lombok as a `maven-compiler-plugin`
   `annotationProcessorPath`** in `pom.xml`, instead of relying on Maven's
   implicit "scan the compile classpath for processors" discovery. This is
   Lombok's own officially documented Maven setup
   ([projectlombok.org/setup/maven](https://projectlombok.org/setup/maven))
   and is the standard, most reliable way to wire it in.

If `mvn clean package` still fails the same way after both of those
changes, the next things to try, in order:
- Bump to whatever the current newest Lombok release is (check
  [projectlombok.org/changelog](https://projectlombok.org/changelog)).
- Add `-XX:+EnableDynamicAgentLoading` to `MAVEN_OPTS` before building —
  newer JDKs warn on (and are trending toward restricting) tools that
  dynamically attach a Java agent at runtime, which is part of how Lombok
  patches `javac`.
- As a last resort, add `<fork>true</fork>` to the `maven-compiler-plugin`
  configuration in `pom.xml`, forcing it to run `javac` in its own JVM
  process instead of in-process inside Maven's JVM.

---

## Table of contents

1. [Tech stack](#tech-stack)
2. [Folder structure](#folder-structure)
3. [Architecture diagram](#architecture-diagram)
4. [API flow diagram](#api-flow-diagram)
5. [Student entity & validation rules](#student-entity--validation-rules)
6. [REST API reference](#rest-api-reference)
7. [Running the project](#running-the-project)
8. [Docker architecture diagram](#docker-architecture-diagram)
9. [CI/CD pipeline diagram](#cicd-pipeline-diagram)
10. [Monitoring: Actuator, Prometheus & Grafana](#monitoring-actuator-prometheus--grafana)
11. [Explanation of every DevOps file](#explanation-of-every-devops-file)
12. [Explanation of every DevOps tool](#explanation-of-every-devops-tool)
13. [Testing](#testing)
14. [Troubleshooting](#troubleshooting)

---

## Tech stack

| Concern            | Choice                                   |
|--------------------|-------------------------------------------|
| Language           | Java 25                                   |
| Framework          | Spring Boot 3.5.16                        |
| Build tool         | Maven                                     |
| Database           | MySQL 8 (H2 in-memory for tests)          |
| Persistence        | Spring Data JPA / Hibernate               |
| Validation         | Jakarta Bean Validation (spring-boot-starter-validation) |
| Boilerplate        | Lombok (optional, used for getters/setters/builders) |
| Monitoring         | Spring Boot Actuator + Micrometer + Prometheus + Grafana |
| Containerization   | Docker, Docker Compose                    |
| CI/CD              | Jenkins (declarative pipeline)            |

---

## Folder structure

```
student-management-system/
├── pom.xml                     # Maven build file: dependencies, plugins, Java version
├── Dockerfile                  # Multi-stage build -> small runtime image
├── docker-compose.yml          # app + mysql + prometheus + grafana, wired together
├── Jenkinsfile                 # Declarative CI/CD pipeline
├── prometheus.yml              # Prometheus scrape configuration
├── .dockerignore
├── .gitignore
├── README.md                   # You are here
└── src/
    ├── main/
    │   ├── java/com/dispatchtrack/studentmanagement/
    │   │   ├── StudentManagementSystemApplication.java   # main() entry point
    │   │   ├── controller/
    │   │   │   └── StudentController.java                # REST endpoints (HTTP layer)
    │   │   ├── service/
    │   │   │   ├── StudentService.java                    # business-logic contract
    │   │   │   └── impl/
    │   │   │       └── StudentServiceImpl.java             # business-logic implementation
    │   │   ├── repository/
    │   │   │   └── StudentRepository.java                  # Spring Data JPA repository
    │   │   ├── entity/
    │   │   │   └── Student.java                             # JPA entity (maps to "students" table)
    │   │   ├── dto/
    │   │   │   └── StudentDTO.java                          # API request/response shape + validation
    │   │   ├── exception/
    │   │   │   ├── ResourceNotFoundException.java           # 404 trigger
    │   │   │   ├── DuplicateResourceException.java          # 409 trigger
    │   │   │   ├── ErrorResponse.java                        # uniform error JSON body
    │   │   │   └── GlobalExceptionHandler.java               # @RestControllerAdvice, maps exceptions -> HTTP status
    │   │   └── config/
    │   │       └── OpenApiAndAppConfig.java                  # cross-cutting bean configuration (metrics tags, etc.)
    │   └── resources/
    │       └── application.properties                        # MySQL, JPA, Actuator/Prometheus config
    └── test/
        ├── java/com/dispatchtrack/studentmanagement/
        │   ├── StudentManagementSystemApplicationTests.java  # Spring context smoke test
        │   ├── service/StudentServiceImplTest.java            # unit tests (Mockito, no Spring context)
        │   └── controller/StudentControllerIntegrationTest.java # full-stack MockMvc tests (H2)
        └── resources/
            └── application.properties                          # H2 datasource for tests
```

This is exactly the "layered architecture" requested: each concern
(**controller → service → repository → entity**) lives in its own package,
with **dto**, **exception**, and **config** as supporting layers. Every
layer only talks to the layer directly below it — the controller never
touches the repository or JPA directly, and the entity never leaves the
service layer.

---

## Architecture diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                         Client (curl / Postman)                      │
└───────────────────────────────────┬────────────────────────────────-─┘
                                     │ HTTP JSON  (POST/GET/PUT/DELETE)
                                     ▼
┌────────────────────────────────────────────────────────────────────┐
│  CONTROLLER LAYER        StudentController                         │
│  - Maps HTTP routes (/students, /students/{id})                    │
│  - Validates request body (@Valid StudentDTO)                      │
│  - Returns proper HTTP status codes (200/201/204/404/400/409)      │
└───────────────────────────────────┬──────────────────────────────--─┘
                                     │ StudentDTO
                                     ▼
┌────────────────────────────────────────────────────────────────────┐
│  SERVICE LAYER           StudentService / StudentServiceImpl       │
│  - Business rules (e.g. unique email check)                        │
│  - Maps StudentDTO <-> Student entity                              │
│  - Throws ResourceNotFoundException / DuplicateResourceException   │
└───────────────────────────────────┬──────────────────────────────--─┘
                                     │ Student entity
                                     ▼
┌────────────────────────────────────────────────────────────────────┐
│  REPOSITORY LAYER        StudentRepository (extends JpaRepository) │
│  - CRUD + derived queries (findByEmail, existsByEmail)             │
│  - Spring Data JPA generates the implementation at runtime         │
└───────────────────────────────────┬──────────────────────────────--─┘
                                     │ SQL (via Hibernate)
                                     ▼
┌────────────────────────────────────────────────────────────────────┐
│                         MySQL Database                             │
│                    table: students (id, name, email,               │
│                            department, cgpa)                       │
└──────────────────────────────────────────────────────────────────--─┘

  Cross-cutting concerns (apply across every layer above):
  ┌───────────────────────────┐  ┌────────────────────────────────┐
  │ exception / GlobalException│  │ config / OpenApiAndAppConfig    │
  │ Handler -> uniform JSON    │  │ -> app-wide beans (metric tags) │
  │ error responses            │  │                                  │
  └───────────────────────────┘  └────────────────────────────────┘
```

---

## API flow diagram

Example: `POST /students` (create a student)

```
Client                Controller             Service                Repository            MySQL
  │  POST /students       │                      │                       │                  │
  │  {name, email, ...}   │                      │                       │                  │
  ├──────────────────────▶│                      │                       │                  │
  │                       │ @Valid checks fields │                       │                  │
  │                       │ (400 if invalid) ────┼──────────────┐        │                  │
  │                       │                      │              │        │                  │
  │                       │ createStudent(dto)   │◀─────────────┘        │                  │
  │                       ├─────────────────────▶│                       │                  │
  │                       │                      │ existsByEmail(email)  │                  │
  │                       │                      ├──────────────────────▶│                  │
  │                       │                      │                       │ SELECT ... ─────▶│
  │                       │                      │                       │◀──── result ─────┤
  │                       │                      │◀──────────────────────┤                  │
  │                       │                      │ (409 if duplicate)    │                  │
  │                       │                      │ dto -> entity         │                  │
  │                       │                      │ save(entity)          │                  │
  │                       │                      ├──────────────────────▶│                  │
  │                       │                      │                       │ INSERT ... ─────▶│
  │                       │                      │                       │◀──── saved row ──┤
  │                       │                      │◀──────────────────────┤                  │
  │                       │                      │ entity -> dto         │                  │
  │                       │◀─────────────────────┤                       │                  │
  │◀── 201 Created ───────┤                      │                       │                  │
  │   {id, name, ...}     │                      │                       │                  │
```

Every other endpoint (`GET`, `PUT`, `DELETE`) follows the same shape:
Controller validates/routes → Service applies business rules → Repository
talks to MySQL → response bubbles back up, and any thrown exception is
intercepted by `GlobalExceptionHandler` before it reaches the client.

---

## Student entity & validation rules

| Field        | Type    | Rules                                               |
|--------------|---------|------------------------------------------------------|
| `id`         | Long    | Auto-generated by MySQL (identity column)             |
| `name`       | String  | Required, not blank                                   |
| `email`      | String  | Required, must be a valid email, unique               |
| `department` | String  | Required, not blank                                   |
| `cgpa`       | Double  | Required, must be between `0.0` and `10.0`            |

Validation is enforced with Jakarta Bean Validation annotations on
`StudentDTO` (`@NotBlank`, `@Email`, `@NotNull`, `@DecimalMin/@DecimalMax`)
and triggered by `@Valid` in the controller. Failures return **HTTP 400**
with a field-by-field error map.

---

## REST API reference

Base URL: `http://localhost:8080`

| Method   | Endpoint          | Description                  | Success status |
|----------|-------------------|-------------------------------|-----------------|
| `POST`   | `/students`       | Create a new student           | `201 Created`   |
| `GET`    | `/students`       | List all students              | `200 OK`        |
| `GET`    | `/students/{id}`  | Get a student by id            | `200 OK`        |
| `PUT`    | `/students/{id}`  | Update a student by id         | `200 OK`        |
| `DELETE` | `/students/{id}`  | Delete a student by id         | `204 No Content`|

Error responses (uniform shape from `GlobalExceptionHandler`):

| Situation                          | Status |
|-------------------------------------|--------|
| Validation failure (e.g. blank name)| `400 Bad Request` |
| Student id does not exist           | `404 Not Found`    |
| Email already in use                | `409 Conflict`     |
| Unexpected server error             | `500 Internal Server Error` |

### Example requests

Create a student:

```bash
curl -X POST http://localhost:8080/students \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Asha Rao",
        "email": "asha.rao@example.com",
        "department": "Computer Science",
        "cgpa": 8.7
      }'
```

List all students:

```bash
curl http://localhost:8080/students
```

Get one student:

```bash
curl http://localhost:8080/students/1
```

Update a student:

```bash
curl -X PUT http://localhost:8080/students/1 \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Asha Rao",
        "email": "asha.rao@example.com",
        "department": "Data Science",
        "cgpa": 9.1
      }'
```

Delete a student:

```bash
curl -X DELETE http://localhost:8080/students/1 -i
```

---

## Running the project

### Option A — Run locally with Maven + a local MySQL server

1. **Prerequisites**: JDK 25, Maven 3.9+, a running MySQL 8 instance.
2. Create the database:
   ```sql
   CREATE DATABASE student_management_db;
   ```
3. Edit `src/main/resources/application.properties` if your MySQL
   username/password/port differ from the defaults (`root` / `root` /
   `3306`).
4. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
5. The API is now available at `http://localhost:8080/students`.

### Option B — Run everything with Docker Compose (recommended)

No local MySQL, JDK, or Maven installation required — only Docker.

```bash
docker compose up --build
```

This starts, on one shared Docker network:

- **MySQL** on `localhost:3306`
- **the Spring Boot app** on `localhost:8080`
- **Prometheus** on `localhost:9090`
- **Grafana** on `localhost:3000` (login: `admin` / `admin`)

Stop everything (and remove volumes) with:

```bash
docker compose down -v
```

### Option C — Build and run the Docker image directly

```bash
docker build -t student-management-system .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://<your-mysql-host>:3306/student_management_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  student-management-system
```

---

## Docker architecture diagram

```
                         Docker Host
 ┌───────────────────────────────────────────────────────────────────┐
 │                         Network: sms-network                      │
 │                                                                     │
 │   ┌───────────────┐   scrapes    ┌───────────────┐                │
 │   │   Prometheus   │─────────────▶│  Grafana       │◀── you (:3000)│
 │   │   (:9090)      │  metrics     │  (:3000)       │   view dashboards
 │   └───────▲────────┘  data source └───────────────┘                │
 │           │ scrapes /actuator/prometheus                            │
 │           │                                                        │
 │   ┌───────┴────────┐   JDBC        ┌───────────────┐               │
 │   │  Spring Boot    │──────────────▶│    MySQL      │               │
 │   │  app (:8080)    │  reads/writes │   (:3306)     │               │
 │   └───────▲────────┘   student data └───────────────┘               │
 │           │                                                        │
 └───────────┼────────────────────────────────────────────────────────┘
             │ HTTP :8080 (published to host)
             │
        ┌────┴─────┐
        │  Client   │   (curl / Postman / browser)
        └──────────┘
```

Each service runs in its own container, built from its own image
(`mysql:8.0`, our custom app image, `prom/prometheus`, `grafana/grafana`),
and they all communicate over the private `sms-network` bridge network
created by Docker Compose — container names (`mysql`, `app`) work as
hostnames for inter-container DNS resolution.

---

## CI/CD pipeline diagram

```
 ┌────────────┐   git push    ┌───────────────────────────────────────────┐
 │  Developer  │──────────────▶│               Jenkins Server              │
 └────────────┘                │                                           │
                                │  Jenkinsfile pipeline:                   │
                                │                                           │
                                │  1) Checkout   -> git clone repo         │
                                │        │                                 │
                                │        ▼                                 │
                                │  2) Build      -> mvn clean compile      │
                                │        │                                 │
                                │        ▼                                 │
                                │  3) Test       -> mvn test (JUnit)       │
                                │        │            surefire report      │
                                │        ▼                                 │
                                │  4) Package    -> mvn package (jar)      │
                                │        │                                 │
                                │        ▼                                 │
                                │  5) Docker build -> docker build -t ...  │
                                │        │                                 │
                                │        ▼                                 │
                                │  6) Docker run   -> docker run -d ...    │
                                │        │                                 │
                                └────────┼─────────────────────────────────┘
                                         ▼
                              ┌────────────────────┐
                              │  Running container  │
                              │  student-management- │
                              │  system:latest       │
                              │  serving :8080        │
                              └────────────────────┘
```

Each stage fails fast: if `Test` fails, `Package`/`Docker build`/`Docker
run` never execute, so a broken build never reaches a running container.

---

## Monitoring: Actuator, Prometheus & Grafana

1. **Spring Boot Actuator** exposes operational endpoints out of the box:
   - `GET /actuator/health` — liveness/readiness (`{"status":"UP"}`)
   - `GET /actuator/info` — static app metadata (name, version)
   - `GET /actuator/metrics` — browsable list of all collected metrics
   - `GET /actuator/prometheus` — all metrics in Prometheus' scrape format

2. **Micrometer** (via the `micrometer-registry-prometheus` dependency)
   is the bridge library that converts Actuator's internal metrics into
   the Prometheus text format at `/actuator/prometheus`.

3. **Prometheus** (see `prometheus.yml`) polls (scrapes)
   `http://app:8080/actuator/prometheus` every 5 seconds and stores the
   time-series data in its own database.

4. **Grafana** connects to Prometheus as a data source and renders that
   time-series data as dashboards.

### Setting up a Grafana dashboard (step by step)

1. Open Grafana at `http://localhost:3000` and log in (`admin` / `admin`
   from `docker-compose.yml`; you'll be prompted to change it).
2. Go to **Connections → Data sources → Add data source → Prometheus**.
3. Set the URL to `http://prometheus:9090` (the container name on the
   shared Docker network) and click **Save & test**.
4. Go to **Dashboards → New → New Dashboard → Add visualization**, pick
   the Prometheus data source, and either:
   - build your own panel (e.g. query `http_server_requests_seconds_count`
     for request throughput, `jvm_memory_used_bytes` for JVM memory, or
     `process_cpu_usage` for CPU), or
   - import a ready-made community dashboard for Spring Boot / Micrometer
     (Dashboards → Import, e.g. dashboard ID `4701` "JVM (Micrometer)" or
     `12900` "Spring Boot Statistics") and select your Prometheus data
     source when prompted.
5. Save the dashboard. It now auto-refreshes as Prometheus keeps scraping
   the running app.

---

## Explanation of every DevOps file

### `Dockerfile`
A **multi-stage build**. Stage 1 uses a full `maven:3.9-eclipse-temurin-25`
image to compile the source and produce `student-management-system.jar`.
Stage 2 starts from a minimal `eclipse-temurin:25-jre-alpine` image and
copies in *only* that jar — the final image ships no source code, no
Maven, and no build cache, which keeps it small and reduces the attack
surface. It also runs as a non-root user and defines a `HEALTHCHECK` that
polls Actuator's `/actuator/health` endpoint.

### `docker-compose.yml`
Declares and wires together every container the system needs — the app,
MySQL, Prometheus, and Grafana — as one unit. It handles: building the
app image from the `Dockerfile`, passing MySQL connection details to the
app via environment variables, waiting for MySQL to report healthy before
starting the app (`depends_on: condition: service_healthy`), mounting
`prometheus.yml` into the Prometheus container, exposing each service's
port to the host, and persisting data (MySQL tables, Prometheus history,
Grafana dashboards) in named volumes so it survives container restarts.

### `Jenkinsfile`
A **declarative Jenkins pipeline** describing the CI/CD process as code
(so the pipeline itself is version-controlled alongside the app). It
defines six stages — Checkout, Build, Test, Package, Build Docker Image,
Run Docker Container — each of which must succeed before the next one
runs. Test results are published via `junit`, and the built jar is
archived as a build artifact.

### `application.properties`
Central Spring Boot configuration file. It configures the MySQL JDBC
connection string/credentials, tells Hibernate to auto-create/update the
`students` table (`ddl-auto=update`) and which SQL dialect to use, and
configures which Actuator endpoints are exposed over HTTP
(`health,info,metrics,prometheus`) so Prometheus can reach the metrics
endpoint.

### `prometheus.yml`
Prometheus' own configuration file. `scrape_interval` sets how often it
polls targets; `scrape_configs` lists what to scrape — here, itself (for
a sanity check) and the Spring Boot app's `/actuator/prometheus`
endpoint, addressed by its Docker Compose service name `app`.

---

## Explanation of every DevOps tool

- **Git** — the version control system used to track every change to the
  source code over time, enabling branching, history, and collaboration.
  This project's `.gitignore` keeps build output (`target/`) out of
  version control.

- **GitHub** (or any Git host) — where the Git repository is hosted
  remotely, so the team (and Jenkins) can pull the latest code. The
  Jenkins pipeline's "Checkout" stage clones from here.

- **Maven** — the build automation and dependency management tool for
  Java. `pom.xml` declares every library the project needs (Spring Web,
  JPA, Validation, Actuator, MySQL driver, Lombok, test libraries) and
  Maven downloads them, compiles the code, runs tests, and packages
  everything into a single runnable jar.

- **Jenkins** — the CI/CD automation server that runs the pipeline
  defined in `Jenkinsfile` automatically (e.g. on every Git push):
  building, testing, packaging, containerizing, and deploying the app
  without manual steps, catching failures early.

- **Docker** — packages the application and everything it needs (JRE,
  libraries) into a single portable, isolated container image, so it
  runs identically on a laptop, a Jenkins agent, or a production server.

- **Docker Compose** — orchestrates *multiple* containers (app + MySQL +
  Prometheus + Grafana) as one coordinated stack, defined declaratively
  in `docker-compose.yml`, instead of starting each container by hand.

- **Spring Boot Actuator** — adds production-ready operational endpoints
  (health checks, metrics, app info) to the Spring Boot app with almost
  no code, forming the foundation that monitoring tools plug into.

- **Prometheus** — an open-source monitoring system that periodically
  scrapes (pulls) metrics from targets like our app's
  `/actuator/prometheus` endpoint and stores them as time-series data
  that can be queried and alerted on.

- **Grafana** — a visualization layer on top of Prometheus (and other
  data sources) that turns raw time-series metrics into readable,
  shareable dashboards and graphs.

---

## Testing

Two kinds of tests are included, both running against an in-memory H2
database (`src/test/resources/application.properties`) so no real MySQL
server is needed to run the test suite:

- **`StudentServiceImplTest`** — pure unit tests of the service layer
  with the repository mocked via Mockito (no Spring context, fast).
- **`StudentControllerIntegrationTest`** — full-stack tests that boot the
  real Spring context and drive the actual HTTP endpoints via `MockMvc`.
- **`StudentManagementSystemApplicationTests`** — a smoke test that just
  verifies the Spring application context starts successfully.

Run all tests with:

```bash
mvn test
```

> **Note on this delivery**: this project was generated and reviewed in a
> sandboxed environment without access to Maven Central, so `mvn test`
> could not be executed here to produce a live pass/fail report. The code
> was written and reviewed carefully against standard Spring Boot 3 / Java
> 25 conventions, but please run `mvn clean test` yourself (or let the
> Jenkins pipeline do it) as the first thing after unzipping the project,
> before relying on it.

---

## Troubleshooting

- **`Communications link failure` / can't connect to MySQL** — make sure
  MySQL is running and reachable at the host/port in
  `application.properties` (or the `SPRING_DATASOURCE_*` environment
  variables when using Docker), and that the `student_management_db`
  database exists.
- **Port already in use** — another process is using `8080` (app),
  `3306` (MySQL), `9090` (Prometheus), or `3000` (Grafana). Either stop
  that process or change the port mapping in `docker-compose.yml`.
- **Prometheus target shows "DOWN"** — check that the app container is
  healthy (`docker ps`, `docker logs sms-app`) and that
  `http://app:8080/actuator/prometheus` is reachable *from inside* the
  Prometheus container (`docker exec sms-prometheus wget -qO- http://app:8080/actuator/prometheus`).
- **Jenkins can't run `docker` commands** — the Jenkins agent's user
  needs to be in the `docker` group (or Docker-in-Docker / a Docker
  agent needs to be configured) so the pipeline can invoke the Docker CLI.
