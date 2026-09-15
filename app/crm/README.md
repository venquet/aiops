# AIOps CRM service

Java 21 Spring Boot CRM API designed as a stable, observable baseline for later AIOps exercises.

## Run locally

1. Start PostgreSQL: `docker compose up -d postgres`
2. Start the API: `./mvnw spring-boot:run`

The service listens on `http://localhost:8080`. Its database connection is configurable with `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD`.

Open `http://localhost:8080` for the CRM interface. It creates and lists customers and orders using the API below.

## API and observability

- `POST /api/customers`, `GET /api/customers`, `GET /api/customers/{id}`, `PUT /api/customers/{id}`
- `POST /api/orders`, `GET /api/orders`, `GET /api/orders/{id}`, `GET /api/customers/{id}/orders`
- `GET /actuator/health`, `GET /actuator/metrics`, `GET /actuator/prometheus`

Build an image only after producing the application JAR: `./mvnw package -DskipTests && docker build -t aiops-crm .`
