# Entitlement Access Viewer — Backend

Spring Boot REST API for the runtime portion of the entitlement-access POC.

## Role in the architecture

The backend is deliberately conventional: it serves users and their access from PostgreSQL.

It does **not** call an LLM.

Generated entitlement descriptions are created separately by `llm-utility` and stored in `entitlement_descriptions`. The backend performs an ordinary database join and returns those stored values with the user's access.

## Stack

- Java 25
- Spring Boot 4.1
- Spring Data JPA
- PostgreSQL
- Maven

## Run with the application stack

From the repository root:

```bash
docker compose up -d --build
```

The API is available at:

```text
http://localhost:8080
```

Useful endpoints:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/users
curl http://localhost:8080/api/users/1/access
```

## Description behavior

The access query uses a `LEFT JOIN` to `entitlement_descriptions`.

This is important to the POC: an entitlement is still returned even when offline enrichment has not yet produced a description.

Before enrichment, `description` and `riskNote` can therefore be `null`.

After `llm-utility` runs, the same API returns the stored description and optional entitlement-level risk hint without making any model call.

## Local development

With PostgreSQL already running:

```bash
cd backend
mvn spring-boot:run
```

The default database configuration expects the POC database on `localhost:5432`. When the backend runs in the root Docker Compose stack, `DB_HOST` is set to the `db` service.

## Tests

Run:

```bash
cd backend
mvn test
```

The test suite covers:

- Spring application startup;
- the real JPA access query against PostgreSQL;
- preservation of entitlements that do not yet have generated descriptions;
- controller routing and response serialization.

Testcontainers is used for database-backed tests.

## Data ownership

The backend does not create or evolve the database schema through Hibernate.

`db/init.sql` is the source of truth for the POC schema and seed data.

This keeps the data model visible and reproducible for demonstration purposes.

## POC limitations

The backend intentionally omits production concerns such as:

- authentication and authorization;
- API security hardening;
- entitlement lifecycle operations;
- certification workflow;
- policy-driven SoD analysis;
- production observability and deployment controls.

Those are outside the scope of the architectural idea being demonstrated here.
