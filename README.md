# Workforce Management & Access Control System

A production-oriented full-stack Workforce Management & Access Control System built with Spring Boot and React, demonstrating secure authentication, RBAC, audited employee lifecycle management, and a structured quality strategy (unit, API, database, and end-to-end tests).

## Overview

Organizations need a secure, auditable system to manage employee lifecycles, enforce role-based access, and retain an immutable audit trail. This project covers:

- JWT authentication (stateless) + BCrypt hashing
- Role-based access (ADMIN / HR / MANAGER / EMPLOYEE)
- Employee lifecycle: ACTIVE / INACTIVE / ON_LEAVE / TERMINATED (TERMINATED is irreversible, implemented as soft-delete)
- Audit logging for security-relevant actions
- Search / filter / sort / pagination and dashboard metrics
- Global exception handling with consistent error responses

## Features

- **Employee CRUD** with Bean Validation (employeeId pattern, email, required fields) + unique constraints on `employee_id` and `email`
- **Lifecycle**: `updateEmployeeStatus` and `deleteEmployee` (soft-terminate) both enforce `TERMINATED` cannot transition; changes are audited
- **RBAC**: `@PreAuthorize` + `SecurityFilterChain` URL rules + service-level check (e.g. only ADMIN may change roles; EMPLOYEE may GET only own record)
- **Audit**: `LOGIN`, `FAILED_LOGIN` (null actor handled), `CREATE_EMPLOYEE`, `UPDATE_EMPLOYEE`, `ACTIVATE_EMPLOYEE`, `DEACTIVATE_EMPLOYEE`, `TERMINATE_EMPLOYEE` — description/IP stored, no passwords/tokens logged
- **Search**: `GET /api/employees?search=&department=&role=&status=&page=&size=&sortBy=&sortDir=` (server-side)
- **Dashboard**: `GET /api/employees/dashboard/stats` → totals by status, department distribution, recent hires

## Architecture

```
Frontend (React 18, React Router, Bootstrap 5, Axios) --REST/JWT--> Backend (Spring Boot 3.2, Security 6, JPA)
                                                                          |
                                                                    MySQL 8 / H2 (test,local)
                                                                          |
                                                               audit_logs + employees
```

- **Backend**: `userMgmt/userMgmt` — Spring Boot 3.2.5, Java 21, Spring Security 6 (stateless, JWT filter), JJWT 0.12.5 HS256, BCrypt
- **Frontend**: `user-frontend` — React 18, Context API auth, Axios interceptor (401 → logout), `REACT_APP_API_URL` env override, Nginx production image
- **DB**: MySQL 8 in Docker; H2 file (`local` profile) and H2 mem (`test` profile). `spring.jpa.hibernate.ddl-auto=update` (dev) / `create-drop` (test). No Flyway.
- **Automation**: `automation` — Cucumber 7.15 + Selenium 4.18 + WebDriverManager + JUnit Platform Suite

## Technology Stack

| Layer | Tech |
|-------|------|
| Backend | Spring Boot 3.2.5, Security 6, Data JPA, Validation, JJWT 0.12.5, MySQL, H2 (runtime), Lombok |
| Frontend | React 18, React Router v6, Bootstrap 5, Axios, CRA |
| Testing | JUnit 5, Mockito, Spring Security Test, REST Assured 5.4, H2, Cucumber + Selenium + WebDriverManager |
| Infra | Docker, Docker Compose, Nginx |

## Authentication & RBAC

- `POST /api/auth/login` → `AuthenticationManager` → `JwtUtil.generateToken(username, ROLE_*)` (claims: `role`, `sub`, `exp` from `jwt.expiration` env, default 24h). Also `POST /api/auth/register` and `GET /api/auth/me`.
- `SecurityConfig` (`app.h2-console.enabled=false` by default, gated; `app.cors.allowed-origins` env). JWT filter before `UsernamePasswordAuthenticationFilter`, stateless session, 401 JSON entry point, `frameOptions(sameOrigin)` only when H2 enabled. `@EnableMethodSecurity` for endpoint checks.
- CORS trims origins, allows `Authorization, Content-Type, X-Requested-With, Accept, Origin`, credentials true; avoids `*` with credentials.

Endpoint matrix (backend enforces; frontend hiding is not authorization):

| Endpoint | Roles |
|----------|-------|
| `POST /api/employees` | ADMIN, HR |
| `GET /api/employees` | ADMIN, HR, MANAGER |
| `GET /api/employees/{id}` | ADMIN, HR, MANAGER, EMPLOYEE (EMPLOYEE only own id) |
| `PUT /api/employees/{id}` | ADMIN, HR (role change only ADMIN) |
| `PATCH /api/employees/{id}/status` | ADMIN, HR |
| `DELETE /api/employees/{id}` → soft TERMINATE | ADMIN |
| `GET /api/employees/dashboard/stats`, `/departments` | ADMIN, HR, MANAGER |
| `GET /api/audit/**` | ADMIN |

## Employee Lifecycle

Statuses: `ACTIVE`, `INACTIVE`, `ON_LEAVE`, `TERMINATED`

- `validateStatusTransition` rejects any transition **from** `TERMINATED` (`Cannot change status of a terminated employee` / `already terminated`). All other transitions are allowed; frontend reflects backend truth.
- `DELETE /api/employees/{id}` is **soft** — sets `employmentStatus=TERMINATED`, `updatedAt=now`, preserves row for audit history (verified in service).

## Audit Logging

`AuditLog(actorId nullable, actorEmail, action, targetEntity, targetId nullable, description, ipAddress, timestamp)`. `action` = `AuditAction` enum. Failed logins use `actorId=null` / `targetId=null` to avoid NOT NULL violations. No passwords/JWTs in description.

## Validation & Error Handling

Bean Validation on `Employee` (employeeId `^[A-Z0-9-]+$`, email `@Email`, phone `^[0-9]{10}$`, sizes) + `@Size(min=8)` on password (raw before BCrypt). `@JsonProperty(WRITE_ONLY)` on password, `toString()` excludes it.

`GlobalExceptionHandler` → consistent `ErrorResponse{timestamp,status,error,message,path}`:

- 400 validation/constraint/illegal arg
- 401 `BadCredentialsException` → `Invalid email or password`
- 403 `AccessDeniedException`
- 409 `DataIntegrityViolationException` (duplicate employeeId/email)
- 500 generic, no stack trace/SQL leak

## Setup

### Prerequisites

Java 21, Node 20+, Maven wrapper included, Docker (optional), MySQL 8 if not using Docker.

### Environment Variables

Never commit `.env`. Use `.env.example` as template:

```bash
cp .env.example .env
# edit .env: set strong MYSQL_ROOT_PASSWORD and JWT_SECRET (min 32 chars)
```

`.env.example`:
```
MYSQL_ROOT_PASSWORD=change-me-strong-password
MYSQL_DATABASE=workforce_db
DB_USERNAME=root
JWT_SECRET=change-me-to-a-long-random-secret-min-32-chars-for-HS256
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=http://localhost:3000
SPRING_PROFILES_ACTIVE=docker
```

Backend also reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS` from env (see `application.properties` defaults marked `dev-only`). Production must override.

Frontend: `user-frontend/.env.example` → `REACT_APP_API_URL=http://localhost:8082` (or Docker Nginx proxy `/api` → `http://backend:8082`).

### Local Development (without Docker)

Uses H2 file DB (`local` profile) — demo accounts seeded via `DevDataInitializer` (`@Profile("local")`, password `password123`, BCrypt-hashed, development only).

```bash
# Backend (local profile, H2 file, H2 console at /h2-console)
cd userMgmt/userMgmt
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Frontend (new shell)
cd user-frontend
npm install
npm start
# app: http://localhost:3000  api: http://localhost:8082
```

Default **development** accounts (local profile only, not production):

| Role | Email | Password | Note |
|------|-------|----------|------|
| ADMIN | admin@company.com | password123 | dev-only |
| HR | hr@company.com | password123 | dev-only |
| MANAGER | manager@company.com | password123 | dev-only |
| EMPLOYEE | employee@company.com | password123 | dev-only |

### Docker (production-like)

```bash
cp .env.example .env
# set real secrets in .env
docker compose up -d --build
# frontend: http://localhost:3000 (Nginx :80 → mapped 3000:80)
# backend : http://localhost:8082
# mysql   : 127.0.0.1:3307 → container 3306 (host-only, not LAN)
# health: backend depends_on mysql (healthy), frontend depends_on backend (healthy)
docker compose logs -f
docker compose down -v   # remove volumes
```

Compose reads `.env`; backend receives `DB_URL/DB_PASSWORD/JWT_SECRET/CORS_ALLOWED_ORIGINS` from env. H2 console disabled (`app.h2-console.enabled=false`) in production.

## Testing

### Backend

39 tests (verified 2026-08-27, all passing):

- **Unit**: `EmployeeServiceTest` 15 — create/duplicate, update, status transitions (including terminated restrictions), role-change guard, dashboard counts
- **Controller**: `AuthControllerTest` 4 — login/failed login (mock)
- **Repository**: `EmployeeRepositoryDataTest` 6 — persistence, unique constraints (`employee_id`, `email`), status queries, department distribution
- **API (REST Assured)**: `EmployeeApiTest` 13 — valid/invalid login, expired/invalid token (where practical), CRUD, status, duplicate email/id, dashboard
- **App**: `UserMgmtApplicationTests` 1 — context loads

```bash
cd userMgmt/userMgmt
./mvnw test
# surefire reports: target/surefire-reports/
```

### Frontend

```bash
cd user-frontend
npm test -- --watchAll=false
# 1 suite (App.test.js), CRA default
npm run build
```

### Cucumber + Selenium (BDD/E2E)

4 features, 14 scenarios (business-oriented):

- `login.feature` (2) — success / invalid credentials
- `employee-management.feature` (4) — create, update, status change, terminate (soft)
- `authorization.feature` (4) — ADMIN/HR/MANAGER/EMPLOYEE boundaries
- `validation.feature` (4) — invalid email, duplicate email, missing fields, short password

Runner: `automation/src/test/java/com/demo/automation/runners/RunCucumberTest.java` — JUnit Platform Suite, glue=`com.demo.automation.stepdefinitions`, `pretty, html:target/cucumber-report.html, json:target/cucumber-report.json` (separate property keys, verified no duplicate `PLUGIN_PROPERTY_NAME`). Reports under `automation/target/` (gitignored).

Structure: `pages/` (LoginPage, EmployeePage, DashboardPage) lightweight POM; `stepdefinitions/` delegates to `CommonSteps` helpers with explicit `WebDriverWait`; `hooks/WebHooks` manages driver lifecycle + screenshots on failure. No `Thread.sleep`.

Avoids execution-order dependencies: test data is generated per scenario where possible; `EMP-001` references exist but are created via API/UI preconditions where needed.

Execution requires running backend (`local` profile, `DEFINED_PORT 8082`) and frontend (`3000`):

```bash
# Terminal 1: backend
cd userMgmt/userMgmt && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# Terminal 2: frontend
cd user-frontend && npm start
# Terminal 3: automation
cd automation && ../userMgmt/userMgmt/mvnw test -Dtest=RunCucumberTest
# Reports: automation/target/cucumber-report.html / .json
```

CI does not run Selenium (needs services); workflow validates compose config and runs backend/frontend builds.

## CI/CD

`.github/workflows/ci.yml` — on `push`/`pull_request`:

1. `backend` job: Java 21, `./mvnw -B clean verify` (+ uploads surefire reports)
2. `frontend` job: Node 20, `npm ci`, `npm test`, `npm run build`
3. `docker` job: `docker compose config` validation (depends on both)

No secrets in workflow; `maven`/`npm` caching enabled.

## API Reference

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | /api/auth/login | — | `{email,password}` → `{token, id, employeeId, email, fullName, role}`; also logs audit |
| POST | /api/auth/register | — | creates Employee (default EMPLOYEE if role null) |
| GET | /api/auth/me | JWT | current principal |
| POST | /api/employees | ADMIN,HR | `Employee` body (password write-only) → 201 |
| GET | /api/employees | ADMIN,HR,MANAGER | `?search=&department=&role=&status=&page=&size=&sortBy=&sortDir=` |
| GET | /api/employees/{id} | ALL | EMPLOYEE only own id |
| PUT | /api/employees/{id} | ADMIN,HR | full update, role change only ADMIN |
| PATCH | /api/employees/{id}/status?status= | ADMIN,HR | see lifecycle |
| DELETE | /api/employees/{id} | ADMIN | soft TERMINATE → 200 with updated entity |
| GET | /api/employees/dashboard/stats | ADMIN,HR,MANAGER | totals + distribution + recent |
| GET | /api/employees/departments | ADMIN,HR,MANAGER | string list |
| GET | /api/audit | ADMIN | Page `?actorId=&action=&startDate=&endDate=&page=&size=` |
| GET | /api/audit/stats | ADMIN | counts |

Error codes: 400 validation/bad request, 401 unauthenticated, 403 forbidden, 404 not found, 409 conflict (duplicate), 500 internal (no stack trace).

## Project Structure

```
user-management-system/
├── userMgmt/userMgmt/         # Spring Boot backend
│   ├── src/main/java/com/demo/{config,controller,model,repository,security,service,dto}
│   ├── src/main/resources/{application.properties, application-local.properties}
│   ├── src/test/{java/com/demo/{service,controller,api,repository}, resources/application-test.properties}
│   └── Dockerfile
├── user-frontend/             # React 18
│   ├── src/{pages,context,services,layout}
│   ├── nginx.conf             # SPA fallback + /api proxy → backend:8082
│   └── Dockerfile (multi-stage, nginx:alpine)
├── automation/                # Cucumber + Selenium
│   ├── src/test/java/com/demo/automation/{runners,stepdefinitions,pages,hooks,config}
│   ├── src/test/resources/features/{login,employee-management,authorization,validation}.feature
│   └── pom.xml                # inherits backend parent
├── .github/workflows/ci.yml
├── docker-compose.yml         # env-driven, healthchecks, 127.0.0.1 mysql bind, 3000:80 frontend
├── .env.example
└── .gitignore
```

## Technical Decisions

| Decision | Why | Trade-off |
|----------|-----|-----------|
| JWT stateless HS256 | Simple, scalable, no session store | No revocation without blocklist; 24h expiry acceptable for demo (prod should use short-lived + refresh) |
| BCrypt | Spring Security default | Sufficient; slower than Argon2 but native |
| Soft TERMINATE | Preserves audit history, meets spec | Employees remain queryable (filtered by status) |
| H2 local/test, MySQL prod | Fast tests, persistent local file DB | `ddl-auto=update` is dev convenience, prod should use migrations |
| POM in automation + runtime H2 | Keeps H2 out of production jar; tests still run | `runtime` H2 still in jar? Better `local` profile exclusion future |
| Explicit waits, no sleep | Reliable Selenium | Slightly more verbose |
| Context API vs Redux | Auth state only | Simpler for this scope |

## Troubleshooting

- `Duplicate step definitions` → ensure `CommonSteps` helpers are not annotated where `LoginSteps`/`AuthorizationSteps` own the phrase.
- Backend `401` on `/api/employees` → check `Authorization: Bearer <jwt>` and `JWT_SECRET` consistency.
- MySQL `not healthy` in compose → wait 30s; check `MYSQL_ROOT_PASSWORD` in `.env`.
- Cucumber not finding backend → run backend with `-Dspring-boot.run.profiles=local` and frontend on 3000 first.

## License

MIT
