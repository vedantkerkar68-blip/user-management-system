# Workforce Management & Access Control System

A production-oriented full-stack workforce management application built with Spring Boot, React and MySQL, featuring JWT authentication, role-based access control, employee lifecycle management, audit logging, REST APIs and automated quality validation.

## Overview

This project transforms a basic CRUD system into a workforce management and access control platform for HR and administrative operations. It demonstrates application engineering, security hardening and a structured testing strategy covering unit, API, database and end-to-end validation.

## Problem Statement

Organizations need a secure, auditable way to manage employee records, enforce access boundaries by role, track status changes without losing history, and retain an immutable audit trail for compliance. This system addresses that with stateless authentication, server-side authorization, validated lifecycle transitions and comprehensive audit logging.

## Key Features

- Employee CRUD with Bean Validation and unique constraints (`employee_id`, `email`)
- Search / filter / sort / pagination and dashboard metrics
- Role-based access (ADMIN / HR / MANAGER / EMPLOYEE) enforced in backend
- JWT stateless authentication with BCrypt
- Employee lifecycle: ACTIVE / INACTIVE / ON_LEAVE / TERMINATED (TERMINATED irreversible, soft-delete)
- Audit logging for security-relevant operations
- Global exception handling with consistent error responses
- Dockerized stack: Frontend → Backend → MySQL

## Architecture

```
Frontend (React 18, React Router, Bootstrap 5, Axios)
        | REST / JWT (Authorization: Bearer <token>)
Backend (Spring Boot 3.2.5, Security 6, JPA, Validation, JJWT 0.12.5)
        | JDBC
Database (MySQL 8 in Docker; H2 file for local, H2 mem for test)
```

- Backend: `userMgmt/userMgmt`
- Frontend: `user-frontend` (Nginx production image)
- Automation: `automation` (Cucumber + Selenium + WebDriverManager, JUnit Platform Suite)

## Technology Stack

| Layer | Technologies |
|-------|--------------|
| Backend | Java 21, Spring Boot 3.2.5, Spring Security 6, Spring Data JPA, Bean Validation, JJWT 0.12.5 (HS256), BCrypt, MySQL, H2 (runtime), Lombok |
| Frontend | React 18, React Router v6, Bootstrap 5, Axios, CRA |
| Testing | JUnit 5, Mockito, Spring Security Test, REST Assured 5.4, H2, Cucumber 7.15, Selenium 4.18, WebDriverManager 5.8 |
| Infra | Docker, Docker Compose, Nginx |

## Authentication & Security

- `POST /api/auth/login` → `AuthenticationManager` → `JwtUtil.generateToken(username, ROLE_*)` with `role` claim, `sub`, `exp` (configurable via `jwt.expiration`, default 24h). Also `POST /api/auth/register` and `GET /api/auth/me`.
- `SecurityConfig`: stateless, `SessionCreationPolicy.STATELESS`, `Csrf.disable()`, `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`, 401 JSON entry point, `app.h2-console.enabled=false` by default (gated), `frameOptions(sameOrigin)` only when console enabled.
- Passwords: BCrypt, `WRITE_ONLY` JSON, never returned or logged, `toString()` excludes password.
- JWTs never logged; `getSigningKey()` derives 32-byte key via SHA-256 for short dev placeholders (production must use long random secret via `JWT_SECRET` env).

## Role-Based Access Control

Backend is the security boundary (React hiding is not authorization):

| Endpoint | Roles |
|----------|-------|
| `POST /api/employees` | ADMIN, HR |
| `GET /api/employees` | ADMIN, HR, MANAGER |
| `GET /api/employees/{id}` | ADMIN, HR, MANAGER, EMPLOYEE (EMPLOYEE only own id; enforced in controller) |
| `PUT /api/employees/{id}` | ADMIN, HR (role change only ADMIN, enforced in service) |
| `PATCH /api/employees/{id}/status` | ADMIN, HR |
| `DELETE /api/employees/{id}` → soft TERMINATE | ADMIN |
| `GET /api/employees/dashboard/stats`, `/departments` | ADMIN, HR, MANAGER |
| `GET /api/audit/**` | ADMIN |
| `POST /api/auth/login`, `/register`, `/h2-console/**` (when enabled) | permitAll (h2 gated by `app.h2-console.enabled`) |

Method-level `@PreAuthorize` + `SecurityFilterChain` URL rules.

## Employee Lifecycle

Statuses: `ACTIVE`, `INACTIVE`, `ON_LEAVE`, `TERMINATED`

- `EmployeeService.validateStatusTransition` rejects any transition **from** `TERMINATED` (`Cannot change status of a terminated employee` / `already terminated`). Other transitions are allowed; frontend reflects backend truth.
- `DELETE /api/employees/{id}` is soft — sets `employmentStatus=TERMINATED`, `updatedAt=now`, preserves row and audit history. Verified in service and tests. Frontend calls delete and shows terminated state.

## Audit Logging

`AuditLog(actorId nullable, actorEmail, action, targetEntity, targetId nullable, description, ipAddress, timestamp)` with `AuditAction` enum: `LOGIN`, `FAILED_LOGIN`, `LOGOUT`, `CREATE_EMPLOYEE`, `UPDATE_EMPLOYEE`, `ACTIVATE_EMPLOYEE`, `DEACTIVATE_EMPLOYEE`, `TERMINATE_EMPLOYEE`, `CHANGE_ROLE`. Failed logins log with `actorId=null` / `targetId=null` to avoid NOT NULL violations. No passwords/JWTs stored. Admin-only access with filtering and pagination.

## REST API

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/api/auth/login` | — | `{email,password}` → `{token, id, employeeId, email, fullName, role}`; logs LOGIN/FAILED_LOGIN |
| POST | `/api/auth/register` | — | creates Employee (defaults to EMPLOYEE if role null), hashes password |
| GET | `/api/auth/me` | JWT | current principal |
| POST | `/api/employees` | ADMIN,HR | `Employee` JSON (password write-only) → 201 |
| GET | `/api/employees` | ADMIN,HR,MANAGER | `?search=&department=&role=&status=&page=&size=&sortBy=&sortDir=` |
| GET | `/api/employees/{id}` | ALL | EMPLOYEE only own id |
| PUT | `/api/employees/{id}` | ADMIN,HR | full update, role change only ADMIN |
| PATCH | `/api/employees/{id}/status?status=` | ADMIN,HR | lifecycle validation |
| DELETE | `/api/employees/{id}` | ADMIN | soft TERMINATE → 200 with entity |
| GET | `/api/employees/dashboard/stats` | ADMIN,HR,MANAGER | totals by status, department distribution, recent hires |
| GET | `/api/employees/departments` | ADMIN,HR,MANAGER | list of department names |
| GET | `/api/audit` | ADMIN | `?actorId=&action=&startDate=&endDate=&page=&size=` |
| GET | `/api/audit/stats` | ADMIN | counts |

Error format (`GlobalExceptionHandler`): `{timestamp, status, error, message, path}` — 400 validation/bad request, 401 unauthenticated, 403 forbidden, 404 not found, 409 conflict (duplicate), 500 internal (no stack trace/SQL).

## Database

`employees`:
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, IDENTITY |
| employee_id | VARCHAR(20) | UK, NOT NULL, pattern `^[A-Z0-9-]+$` |
| full_name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(150) | UK, NOT NULL, `@Email` |
| password | VARCHAR(255) | NOT NULL, BCrypt, WRITE_ONLY |
| phone | VARCHAR(15) | `^[0-9]{10}$` |
| department | VARCHAR(100) | NOT NULL |
| designation | VARCHAR(100) | NOT NULL |
| role | VARCHAR(20) | NOT NULL, enum |
| joining_date | DATE | NOT NULL |
| employment_status | VARCHAR(20) | NOT NULL, enum |
| created_at | TIMESTAMP | NOT NULL, updatable=false |
| updated_at | TIMESTAMP | nullable |

`audit_logs`: `id PK, actor_id nullable, actor_email NOT NULL, action NOT NULL, target_entity, target_id nullable, description TEXT, timestamp NOT NULL, ip_address`.

MySQL 8 in Docker (`workforce_db`), H2 file (`local` profile, `./data/workforce_db`) and H2 mem (`test` profile, `create-drop`). `ddl-auto=update` (dev) / `create-drop` (test).

## Testing Strategy

### Unit Testing
`src/test/java/com/demo/service/EmployeeServiceTest` — 15 tests: create, duplicate employeeId/email, update, status transitions including terminated restrictions, role-change guard (only ADMIN), dashboard counts. `AuthControllerTest` — 4 tests (mocked). `UserMgmtApplicationTests` — 1 context load. Run: `cd userMgmt/userMgmt && ./mvnw test` (39 total with integration, see below) — `target/surefire-reports/`.

### API Testing
REST Assured (`io.rest-assured:rest-assured:5.4.0` in `userMgmt` and `automation`): `src/test/java/com/demo/api/EmployeeApiTest` — 13 tests: valid login, invalid login, create, retrieve, update, status change, validation failure, duplicate employee, duplicate email, authorization (admin/hr/manager/employee boundaries), dashboard. Executed as part of `mvn test`.

### BDD with Cucumber
4 features, 14 scenarios in `automation/src/test/resources/features`:
- `login.feature` (2): successful login, invalid login
- `employee-management.feature` (4): create, update, status change, terminate (soft)
- `authorization.feature` (4): ADMIN/HR/MANAGER/EMPLOYEE access boundaries
- `validation.feature` (4): invalid email, duplicate email, missing fields, short password

Runner: `automation/src/test/java/com/demo/automation/runners/RunCucumberTest.java` — JUnit Platform Suite, `glue=com.demo.automation.stepdefinitions`, `pretty, html:target/cucumber-report.html, json:target/cucumber-report.json` (separate property keys, verified). Reports in `automation/target/` (gitignored).

### UI Automation with Selenium
Selenium 4.18 + WebDriverManager 5.8, Chrome headless. Lightweight POM: `automation/src/test/java/com/demo/automation/pages/{LoginPage,EmployeePage,DashboardPage}`. Step definitions delegate to `CommonSteps` helpers with explicit `WebDriverWait(10s)`, no `Thread.sleep`. `hooks/WebHooks` manages driver lifecycle and screenshots on failure. Business-oriented scenarios, no ordering dependencies.

### Database Validation
`src/test/java/com/demo/repository/EmployeeRepositoryDataTest` — 6 tests: persistence with timestamps, unique `employee_id`, unique `email`, case-exact lookups, status queries, department distribution. Uses `@DataJpaTest @ActiveProfiles("test")` with H2 mem.

## Docker

Stack: Frontend (Nginx) → Backend (8082) → MySQL (3306)

- `userMgmt/userMgmt/Dockerfile`: multi-stage `maven:3.9-eclipse-temurin-21` build → `eclipse-temurin:21-jre`
- `user-frontend/Dockerfile`: `node:20-alpine` build → `nginx:alpine` with `nginx.conf` (SPA fallback + `/api` proxy to `http://backend:8082`)
- `docker-compose.yml`: env-driven (`${MYSQL_ROOT_PASSWORD:-change-me}`, `${JWT_SECRET:-dev-only-change-me}`, etc.), `127.0.0.1:3307:3306` host bind, `mysql-data` volume, healthcheck `mysqladmin ping` + backend `curl /api/auth/login`, `depends_on: condition: service_healthy`, `restart: unless-stopped`. H2 console disabled in production (`app.h2-console.enabled=false`).

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
│   ├── nginx.conf
│   └── Dockerfile
├── automation/                # Cucumber + Selenium
│   ├── src/test/java/com/demo/automation/{runners,stepdefinitions,pages,hooks,config}
│   ├── src/test/resources/features/{login,employee-management,authorization,validation}.feature
│   └── pom.xml
├── docker-compose.yml
├── .env.example
└── .gitignore
```

## Environment Configuration

Never commit `.env`. Template:

```bash
cp .env.example .env
# edit .env: set strong MYSQL_ROOT_PASSWORD and JWT_SECRET (min 32 chars random in production)
```

`.env.example`:
```
MYSQL_ROOT_PASSWORD=change-me
MYSQL_DATABASE=workforce_db
DB_USERNAME=root
DB_URL=jdbc:mysql://mysql-container:3306/workforce_db
JWT_SECRET=dev-only-change-me
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=http://localhost:3000
SPRING_PROFILES_ACTIVE=docker
```

Backend reads `DB_URL/DB_USERNAME/DB_PASSWORD/JWT_SECRET/JWT_EXPIRATION/CORS_ALLOWED_ORIGINS` via `${VAR:dev-only-change-me}` (dev placeholder, production must override). Frontend `user-frontend/.env.example`: `REACT_APP_API_URL=http://localhost:8082`.

## Running the Application

### Prerequisites
Java 21, Node 20+, Maven wrapper, Docker (optional)

### Local (H2 file, no Docker)

```bash
# Backend - local profile seeds dev users (profile-gated DevDataInitializer)
cd userMgmt/userMgmt
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# http://localhost:8082, H2 console http://localhost:8082/h2-console (local only)

# Frontend - new shell
cd user-frontend
npm install
npm start
# http://localhost:3000
```

Dev accounts (local profile only, BCrypt `password123`):
- ADMIN `admin@company.com`
- HR `hr@company.com`
- MANAGER `manager@company.com`
- EMPLOYEE `employee@company.com`

Documented as dev-only, not production.

### Docker (MySQL)

```bash
cp .env.example .env
# set real secrets in .env
docker compose up -d --build
# frontend http://localhost:3000 (Nginx 80 -> 3000:80)
# backend  http://localhost:8082
# mysql    127.0.0.1:3307 -> 3306
docker compose logs -f
docker compose down -v
```

## Running Tests

```bash
# Backend unit + API + DB (39 tests)
cd userMgmt/userMgmt
./mvnw test
# Reports: target/surefire-reports/

# Frontend
cd user-frontend
npm test -- --watchAll=false
npm run build
```

## Running Automation Tests

Requires backend (`local` profile, 8082) and frontend (3000) running:

```bash
# Terminal 1: backend
cd userMgmt/userMgmt && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# Terminal 2: frontend
cd user-frontend && npm start
# Terminal 3: automation
cd automation && ../userMgmt/userMgmt/mvnw test -Dtest=RunCucumberTest
# Reports: automation/target/cucumber-report.html, automation/target/cucumber-report.json
```

Automation uses explicit waits, no `Thread.sleep`, repeatable via generated data where possible (EMP-001 is demo seed, otherwise created per scenario).

## Screenshots

Screenshots are not committed; they can be added under `docs/screenshots/` for:

- Login
- Dashboard (totals, department distribution, recent hires)
- Employee list (search/filter/pagination)
- Employee details (status change, terminate)
- Audit logs (admin view)

WebDriver captures screenshots on failure via `WebHooks` (attached to Cucumber scenario).

## Technical Decisions

| Decision | Rationale | Trade-off |
|----------|-----------|-----------|
| JWT HS256 stateless | Simple, scalable, no session store | No revocation without blocklist; 24h expiry okay for demo (prod: short-lived + refresh) |
| BCrypt | Spring Security native | Sufficient vs Argon2 |
| Soft TERMINATE | Preserves audit history | Rows remain queryable (filtered by status) |
| H2 local/test, MySQL prod | Fast tests, persistent local file | `ddl-auto=update` dev convenience; prod should use migrations |
| H2 scope runtime, local profile | Keeps H2 out of prod jar semantics while allowing local | Still in jar; future: exclude via profile |
| POM in automation | Lightweight, explicit waits | More verbose than sleep |
| Context API vs Redux | Auth state only | Simpler |

## Known Limitations

- `getClientIp()` returns `127.0.0.1` (placeholder; production should use `X-Forwarded-For`).
- `JwtUtil` derives 32-byte key via SHA-256 for short dev placeholders; production must use long random secret.
- `DDL auto update` not migrations; `RecentEmployees` uses `Pageable.unpaged` then limit.
- Automation assumes Chrome available; Selenium tests require running frontend/backend (not self-contained).
- No rate limiting, no OpenAPI docs.

## Future Improvements

- Email notifications on status changes
- Bulk import (CSV)
- Organization hierarchy (departments → teams)
- Advanced audit search (date ranges, actor filters)
- API rate limiting
- OpenAPI/Swagger
- Kubernetes manifests (removed CI scope for now)
