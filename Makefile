ifneq (,$(wildcard .env))
	include .env
	export
endif

.PHONY: backend/install backend/dev backend/init_db backend/close_db

backend/install:
	cd src/backend; \
	./mvnw clean install

backend/dev:
	cd src/backend; \
	./mvnw spring-boot:run

db/init:
	docker run -d \
		--name vibes-postgres \
		-e POSTGRES_DB=mydb \
		-e POSTGRES_USER=$(DB_USERNAME) \
		-e POSTGRES_PASSWORD=$(DB_PASSWORD) \
		-p 5432:5432 \
		postgres:18

db/close:
	docker stop vibes-postgres; \
	docker rm vibes-postgres

db/logs:
	docker logs vibes-postgres
