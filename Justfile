set dotenv-load := true

# ── Frontend ──────────────────────────────────────────────────────────────────

[group("frontend")]
[working-directory('src/frontend')]
frontend_install:
    npm install

[group("frontend")]
[working-directory('src/frontend')]
frontend_dev:
    npm run dev

[group("frontend")]
[working-directory('src/frontend')]
frontend_build:
    npm run build

[group("dev")]
dev:
  just db_init; \
  just minio_init; \
  sleep 3; \
  just backend_dev;


[group("backend")]
[working-directory('src/backend')]
@backend_install:
    ./mvnw clean install

[group("backend")]
[linux]
[macos]
[working-directory('src/backend')]
backend_dev:
    ./mvnw spring-boot:run

[group("backend")]
[windows]
[working-directory('src/backend')]
backend_dev:
    ./mvnw.cmd spring-boot:run

[group("db")]
db_init:
    docker run -d \
        --name vibes-postgres \
        -e POSTGRES_DB=${DB_NAME} \
        -e POSTGRES_USER=${DB_USERNAME} \
        -e POSTGRES_PASSWORD=${DB_PASSWORD} \
        -p ${DB_PORT}:${DB_PORT} \
        postgres:18

[group("db")]
db_close:
    docker stop vibes-postgres
    docker rm vibes-postgres

[group("db")]
db_logs:
    docker logs vibes-postgres

[group("minio")]
minio_init:
    docker run -d \
        --name vibes-minio \
        -e MINIO_ROOT_USER=${MINIO_ROOT_USER} \
        -e MINIO_ROOT_PASSWORD=${MINIO_ROOT_PASSWORD} \
        -p 9000:9000 \
        -p 9001:9001 \
        minio/minio:latest server /data --console-address ":9001"

[group("minio")]
minio_close:
    docker stop vibes-minio
    docker rm vibes-minio

[group("minio")]
minio_logs:
    docker logs vibes-minio
