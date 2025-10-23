.PHONY: help up down api web test-api test-web

help:
@echo "Usage: make [target]"
@echo "Targets: up, down, api, web, test-api, test-web"

up:
docker compose -f infra/docker-compose.yml up --build

down:
docker compose -f infra/docker-compose.yml down -v

api:
cd api && ./gradlew bootRun

web:
cd web && npm run dev

test-api:
cd api && ./gradlew test

test-web:
cd web && npm test
