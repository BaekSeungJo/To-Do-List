# Firebase Todo Service

Monorepo scaffolding for a Firebase-authenticated to-do list service. The repository hosts a Spring Boot backend (`api`) and a Next.js frontend (`web`) supported by Docker-based infrastructure.

## Structure

- `api/` — Java 21 + Spring Boot 3.3 hexagonal/DDD backend
- `web/` — Next.js 14 + TypeScript frontend
- `infra/` — Docker Compose definitions for local orchestration
- `docs/` — Product and agent specifications

## Getting Started

```bash
# Launch postgres, api, and web containers
make up

# Run backend locally
make api

# Run frontend locally
make web

# Execute tests
make test-api
make test-web
```

Refer to `docs/PRD.md` and `docs/Agent.md` for detailed functional and architectural requirements.
