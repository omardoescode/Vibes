set dotenv-load := true

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
        -e POSTGRES_DB=mydb \
        -e POSTGRES_USER=${DB_USERNAME} \
        -e POSTGRES_PASSWORD=${DB_PASSWORD} \
        -p 5432:5432 \
        postgres:18

[group("db")]
db_close:
    docker stop vibes-postgres
    docker rm vibes-postgres

[group("db")]
db_logs:
    docker logs vibes-postgres
