# Entitlements POC

## Step 2: Spring Boot backend

**Stack:** Java 25 (LTS) · Spring Boot 4.1.0 (Spring Framework 7) · Maven · Spring Data JPA

### Prerequisites
- JDK 25 installed and on your PATH (`java -version`)
- Step 1's Postgres running: `docker compose up -d` from the repo root

### Run it
```bash
cd backend
mvn spring-boot:run
```
The API comes up on `http://localhost:8080`. Try:
```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/users
curl http://localhost:8080/api/users/1/access
```
`entitlement_descriptions` is still empty at this point (that's step 4), so `description`
and `risk_note` will come back `null` for every row - that's expected, not a bug.

### Notes
- `spring.jpa.hibernate.ddl-auto=none` - Hibernate never touches the schema. `db/init.sql`
  from step 1 is the single source of truth for table structure.
- CORS is currently locked to `http://localhost:5173` (Vite's default port) for the React
  UI landing in step 3.
- **Spring Boot 4 fully modularized its dependencies** - the old `spring-boot-starter-web`
  is now `spring-boot-starter-webmvc` (with the embedded server, Tomcat here, as its own
  separate starter), and the old monolithic `spring-boot-autoconfigure`/
  `spring-boot-test-autoconfigure` jars were split into dozens of per-technology modules.
  We're using Spring's own **Classic Starters** (`spring-boot-starter-classic` and
  `spring-boot-starter-test-classic`) as an intentional shortcut - they restore the full
  pre-4.0 auto-configuration surface in one dependency instead of us hand-picking every
  module (`spring-boot-jdbc`, `spring-boot-data-jpa-test`, `spring-boot-webmvc-test`,
  etc.). Spring's own 4.0 migration guide recommends exactly this as a first step for
  existing apps: get it compiling, then modularize properly later if it's worth it for a
  real (non-prototype) app.
- **This project was scaffolded and code-reviewed in a sandboxed environment without
  access to Maven Central**, so none of this has actually been compiled end-to-end here -
  only reviewed against Spring's official 4.0 migration guide. Please run `mvn package`
  next and report back the exact error if anything's still off; I'd rather fix a real
  error than keep guessing.

### Testing (step 2.1)
Three test classes, all runnable via plain `mvn test` (no separate `mvn verify` needed):

- **`EntitlementsBackendApplicationTests`** - smoke test, boots the full Spring context
  against a real Postgres.
- **`UserEntitlementRepositoryTest`** - the important one. Runs the actual JPQL join
  query against a real Postgres seeded from `db/init.sql`, and specifically checks that
  the `LEFT JOIN` to `entitlement_descriptions` returns `null` (not a dropped row) when
  no description exists yet - since that's the exact state the whole prototype starts in
  before step 4's LLM utility runs.
- **`AccessControllerTest`** - fast, DB-free `@WebMvcTest` with mocked repositories,
  checking the controller serializes fields and routes correctly.

**Requirements:** Docker must be running (Testcontainers spins up a throwaway Postgres
per test class) - you already have Docker Desktop working via WSL2, so this should just
work with `mvn test`.

**One thing to watch for:** Testcontainers had a major version bump (1.x → 2.x) with
breaking API changes (fewer generics, explicit Docker image handling, renamed artifacts)
not long before this was written. `pom.xml` pins `testcontainers.version` to `1.21.3`
(the last 1.x release) deliberately, since the test code here
(`PostgreSQLContainer<?>`, `@Testcontainers`/`@Container` from
`org.testcontainers.junit.jupiter`, artifact id `postgresql`) is written against the 1.x
API/naming.

