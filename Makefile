.PHONY: api web infra clean

GRADLE_CMD=$(if $(wildcard api/gradlew),./gradlew,gradle)

api:
	cd api && $(GRADLE_CMD) bootRun

web:
	cd web && npm run dev

infra:
	docker compose -f infra/docker-compose.yml up --build

clean:
	rm -rf api/build web/.next
