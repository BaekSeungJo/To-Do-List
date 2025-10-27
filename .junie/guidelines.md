### To‑Do‑List — Development Guidelines (Project‑specific)

This document captures build, configuration, testing, and development notes that are specific to this repository. It assumes familiarity with Java/Spring, Next.js, Docker, and Playwright.

---

#### Repository layout
- api — Spring Boot service (Gradle build). Java 21 runtime.
- web — Next.js 14 app with Playwright tests.
- infra — Docker Compose for local orchestration (Postgres + API + Web).
- docs — PRD and agent notes.
- Makefile — convenience targets for common workflows.

---

### Build and configuration

#### Prerequisites
- Node.js 18+ and npm 9+ (web)
- Java 21 JDK (api)
- Docker Desktop (Compose v2) for containerized workflows
- Git Bash or PowerShell on Windows

Optional, for browser e2e tests locally:
- Playwright browsers: `npx playwright install` (done once per machine)


#### Environment variables (web)
Web uses public Firebase and API endpoint variables via `web/.env`:
- `NEXT_PUBLIC_API_BASE_URL` — REST base URL, defaults to `http://localhost:8080/api` for local dev
- `NEXT_PUBLIC_FIREBASE_*` — Firebase client config (public by design). Do not place secrets without the `NEXT_PUBLIC_` prefix in the web `.env`.

Adjust these for your environment. When running via Docker Compose, the `web` service sets `NEXT_PUBLIC_API_BASE_URL` accordingly (see `infra/docker-compose.yml`).


#### Back-end build (Gradle)
The repository includes `api/gradlew` and `api/gradlew.bat` scripts but is missing the `gradle/wrapper/gradle-wrapper.jar`. Symptoms:
- `./gradlew test` or `gradlew.bat test` fails with “Unable to access jarfile ... gradle-wrapper.jar”.

To proceed locally you have two options:
1) Use a locally installed Gradle 8.10.2
   - Verify: `gradle -v` (should be 8.10.x)
   - In `api/`, run:
     - `gradle test` — runs unit/integration tests (uses H2 in-memory via `application-test.yml`; Postgres is not required for tests).
     - `gradle bootRun` — starts the API on `:8080`.
     - `gradle bootJar` — produces `build/libs/*.jar`.
   - Recommended: regenerate the wrapper so CI and Makefile targets work reliably:
     - `gradle wrapper --gradle-version 8.10.2`
     - Commit the generated `gradle/wrapper/gradle-wrapper.jar` and updated files.

2) Use Docker only for runtime
   - The `api/Dockerfile` expects a prebuilt JAR at `api/build/libs/*.jar` and copies it to `/app/app.jar`.
   - Build steps:
     - `cd api && gradle bootJar`
     - `cd .. && docker compose -f infra/docker-compose.yml build api`
   - Note: There is no multi-stage Dockerfile that builds the app; you must build the JAR before `docker compose up`.

Windows note: The repository’s `Makefile` uses `./gradlew ...` which works in Git Bash. In PowerShell/CMD, prefer `gradlew.bat ...`.


#### Front-end build
- `cd web && npm ci`
- `npm run dev` — starts Next.js dev server on `http://localhost:3000`
- `npm run build && npm start` — production build and run

If Playwright tests will be run locally, install browsers once:
- `cd web && npx playwright install`


#### Orchestrated local environment (Docker Compose)
File: `infra/docker-compose.yml`
- `db` — Postgres 14 Alpine
- `api` — builds from `api/` Dockerfile
  - Depends on `db`
  - Uses `SPRING_PROFILES_ACTIVE=docker` and connects to `jdbc:postgresql://db:5432/todo`
- `web` — builds from `web/` Dockerfile and points `NEXT_PUBLIC_API_BASE_URL` at `http://localhost:8080/api`

Important:
- Ensure the API JAR exists before composing up (see Back-end build). Otherwise the `api` image build will fail at `COPY build/libs/*.jar app.jar`.

Commands:
- `make up` — `docker compose -f infra/docker-compose.yml up --build`
- `make down` — `docker compose -f infra/docker-compose.yml down -v`

Ports:
- Postgres: 5432
- API: 8080
- Web: 3000

---

### Testing

#### Overview
- Front-end: Playwright tests live in `web/tests`. They assume a running web server (dev or prod). Tests are e2e and hit `http://localhost:3000` by default.
- Back-end: Gradle/JUnit 5 tests (API module). Tests default to H2 in-memory DB per `api/src/test/resources/application-test.yml` and do not require Postgres.


#### Front-end tests: configuring and running
One-time per machine:
- `cd web && npm ci`
- `npx playwright install` (downloads Chromium/Firefox/WebKit)

Run tests locally (validated):
- Start the dev server in a terminal: `npm run dev`
- In another terminal: `npx playwright test` (or `npm test` since `package.json` maps it to Playwright)

Observed baseline:
- The existing `web/tests/smoke.spec.ts` passed locally with the dev server running.

Useful flags:
- `npx playwright test --headed` — run with browser UI
- `npx playwright test -g "home page"` — run matching tests only
- `npx playwright show-report` — open the last HTML report

Add a new test (example):
Create `web/tests/landing.spec.ts`:
```ts
import { test, expect } from '@playwright/test';

test('landing shows title', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Firebase Todo Service' })).toBeVisible();
});
```
Run: `npx playwright test web/tests/landing.spec.ts`

CI note: If adding tests that rely on API, ensure `NEXT_PUBLIC_API_BASE_URL` points at a reachable endpoint in CI, or mock network calls.


#### Back-end tests: configuring and running
Given the current wrapper state, prefer a local Gradle installation:
- `cd api`
- `gradle test`

Details:
- Test profile uses H2 in-memory configured in `application-test.yml`:
  - `spring.datasource.url=jdbc:h2:mem:todo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`
  - Flyway disabled for tests (`spring.flyway.enabled=false`)
- No running Postgres is needed for unit/most integration tests.

Add a new test (example skeleton):
Create `api/src/test/java/com/example/todo/ExampleTest.java`:
```java
package com.example.todo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExampleTest {
  @Test void sample() { assertTrue(true); }
}
```
Run: `gradle test`

If you prefer wrapper scripts once restored:
- Unix: `./gradlew test`
- Windows: `gradlew.bat test`

Regenerating the wrapper (recommended):
- `gradle wrapper --gradle-version 8.10.2`
- Commit the generated files so CI (`.github/workflows/codex-ci.yml`) can run `./gradlew ...` reliably.

---

### Makefile targets
- `make up` — Compose up with build (requires API JAR prebuilt)
- `make down` — Compose down with volumes pruned
- `make api` — `./gradlew bootRun` (use Git Bash or adapt to `gradlew.bat` on Windows)
- `make web` — `npm run dev`
- `make test-api` — `./gradlew test` (consider `gradlew.bat` on Windows or `gradle test`)
- `make test-web` — `npm test` (Playwright)

On Windows PowerShell, prefer explicit commands instead of `make`, or install Make via MSYS2/Git for Windows and run in Git Bash.

---

### Code style and conventions

#### API (Java/Spring)
- Java 21, Gradle 8.10.x
- Testing: JUnit 5, conventional layout under `api/src/test/java/...`
- DB in tests: H2 in Postgres mode; Flyway disabled for faster tests
- Profiles: use `docker` profile for containerized runtime
- Naming: package under `com.example.todo` (adjust as needed); test classes suffixed with `Test`

#### Web (Next.js/TypeScript)
- Next.js 14, React 18, TypeScript 5
- ESLint: `npm run lint`; follow Next.js defaults
- Tests: Playwright in `web/tests`; prefer user-centric assertions (`getByRole`, labels)
- Public config via `NEXT_PUBLIC_*` only; avoid embedding secrets client-side

---

### Troubleshooting
- Gradle wrapper missing JAR
  - Symptom: `Unable to access jarfile ... gradle-wrapper.jar`
  - Fix: use local Gradle and run `gradle wrapper --gradle-version 8.10.2`; commit wrapper files.
- Docker Compose fails to build `api`
  - Cause: missing JAR in `api/build/libs` prior to `COPY`
  - Fix: run `gradle bootJar` first
- Port conflicts
  - 3000 (web), 8080 (api), 5432 (db); stop existing services or remap
- Playwright tests fail locally
  - Ensure dev server is running at `http://localhost:3000`
  - Run `npx playwright install` if browsers are missing
  - Use `--headed` for debugging; `show-report` to inspect failures
- Windows path/shell quirks
  - Prefer `gradlew.bat` in PowerShell, or run Make/`./gradlew` from Git Bash

---

### Verified example commands (on Windows, PowerShell)
- Front-end tests (validated):
  - `cd web; npm ci`
  - `npx playwright install`
  - Start server: `npm run dev` (in background/another terminal)
  - Run: `npx playwright test` → observed 1 passing test (`web/tests/smoke.spec.ts`)

- Back-end tests:
  - Wrapper currently not runnable due to missing `gradle-wrapper.jar`.
  - With local Gradle installed: `cd api; gradle test` (uses H2 and should pass once sources/tests compile).

---

### Notes for CI owners
- `.github/workflows/codex-ci.yml` invokes `./gradlew`. Ensure wrapper JAR is committed or regenerate it to avoid CI breakage.
- Consider a multi-stage `api` Dockerfile to build the JAR inside the image for reproducible builds, e.g., a `gradle:jdk21` build stage producing the artifact, then copy into a slim `eclipse-temurin:21-jre` runtime stage.
