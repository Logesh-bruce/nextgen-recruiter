# HireFlow AI — Backend

> AI-powered SaaS recruitment platform backend built with **Java 21 · Spring Boot 3.3 · PostgreSQL · JWT/OAuth2 · OpenAI/Gemini · Docker · GitHub Actions · AWS**.

---

## Architecture

```
com.hireflow
├── config/          # SecurityConfig, CorsConfig, AsyncConfig, RedisConfig
├── controller/      # Thin HTTP entry points — delegates to services
├── service/         # Business logic (interface + impl)
├── repository/      # Spring Data JPA repositories
├── domain/          # JPA entities
├── dto/             # Request / Response DTOs (no entity leakage)
├── mapper/          # MapStruct entity ↔ DTO mappers
├── exception/       # GlobalExceptionHandler + domain exceptions
├── event/           # Spring ApplicationEvents
├── security/        # JwtFilter, JwtProvider, UserDetailsService
├── client/          # AI API adapters (OpenAI / Gemini)
└── util/            # File, slug, pagination utilities
```

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 21 |
| Maven | 3.9+ |
| Docker & Docker Compose | 24+ |
| PostgreSQL | 16+ (or via Docker) |

---

## Local Development Setup

### 1. Clone & configure

```bash
git clone https://github.com/Logesh-bruce/nextgen-recruiter.git
cd nextgen-recruiter
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# Edit application-local.yml and fill in your secrets (DB password, API keys, etc.)
```

### 2. Start infrastructure with Docker Compose

```bash
docker compose up -d db redis
```

### 3. Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

App starts at: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
Health check: `http://localhost:8080/actuator/health`

---

## Generate RSA Keys (JWT)

```bash
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem

# Base64 encode for application-local.yml
# On Linux/Mac:
base64 -w 0 private.pem   # → paste as hireflow.jwt.private-key
base64 -w 0 public.pem    # → paste as hireflow.jwt.public-key

# On Windows (PowerShell):
[Convert]::ToBase64String([IO.File]::ReadAllBytes("private.pem"))
[Convert]::ToBase64String([IO.File]::ReadAllBytes("public.pem"))
```

---

## Running Tests

```bash
# Unit tests only
./mvnw test

# Integration tests (requires Docker for Testcontainers)
./mvnw verify
```

---

## API Documentation

See [`docs/api_contract.md`](docs/api_contract.md) for the full REST API contract.

Base URL: `http://localhost:8080/api/v1`

---

## Database

Schema is managed by **Flyway**. Migrations live in `src/main/resources/db/migration/`.  
See [`docs/schema.sql`](docs/schema.sql) for the full DDL.

---

## Module Build Order

| # | Module | Branch Tag |
|---|---|---|
| 1 | Project scaffold | `v0.1-scaffold` |
| 2 | DB migration (Flyway) | `v0.2-db-migration` |
| 3 | Domain entities | `v0.3-entities` |
| 4 | Auth (JWT + Register/Login) | `v0.4-auth` |
| 5 | Google OAuth2 | `v0.5-oauth2` |
| 6 | Jobs module | `v0.6-jobs` |
| 7 | Applications module | `v0.7-applications` |
| 8 | Resume parsing | `v0.8-resume` |
| 9 | AI scoring module | `v0.9-ai` |
| 10 | Interviews module | `v0.10-interviews` |
| 11 | Notifications module | `v0.11-notifications` |
| 12 | Docker + CI/CD | `v0.12-devops` |

---

## Tech Stack

- **Runtime**: Java 21, Spring Boot 3.3.4
- **Database**: PostgreSQL 16, Flyway, Spring Data JPA, HikariCP
- **Auth**: JWT (JJWT 0.12, RS256), Spring Security, OAuth2 (Google)
- **AI**: OpenAI GPT-4o-mini / Google Gemini 1.5 Flash via Spring AI
- **Parsing**: Apache PDFBox 3.x (PDF), Apache POI 5.x (DOCX), Apache Tika (MIME)
- **Cache**: Redis via Spring Cache
- **Notifications**: SendGrid (email), Twilio (SMS)
- **API Docs**: SpringDoc OpenAPI 3 / Swagger UI
- **DevOps**: Docker, GitHub Actions, AWS ECS Fargate + RDS + S3
