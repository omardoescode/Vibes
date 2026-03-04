.PHONY: backend/dev
backend/dev:
	cd src/backend; \
	./mvnw spring-boot:run
