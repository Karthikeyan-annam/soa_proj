# CinePass Entertainment

High-concurrency cinema booking platform: Movie, Show, Booking, and User microservices behind an API Gateway, registered in Eureka, secured with JWT.

## Project showcase

The workflow is documented as two separate layers plus a LinkedIn-ready narrative:

- [Frontend workflow](FRONTEND_WORKFLOW.md): React user journey, API contract, session handling, and Nginx delivery.
- [Backend workflow](BACKEND_WORKFLOW.md): requirements, microservices, Eureka discovery, JWT authentication, gateway routing, and booking consistency.
.\tools\apache-maven-3.9.9\bin\mvn.cmd -f backend\pom.xml -pl api-gateway,user-service,movie-service,show-service,booking-service spring-boot:run

## Run

1. Java 17+ (Java 25 works with the ByteBuddy experimental flag already set in `start-cinepass.bat`)
2. Node.js 18+
3. Double-click `start-cinepass.bat` or:

.\tools\apache-maven-3.9.9\bin\mvn.cmd -f backend\pom.xml -pl movie-service,booking-service test
$env:JAVA_TOOL_OPTIONS="-Dnet.bytebuddy.experimental=true"
.\tools\apache-maven-3.9.9\bin\mvn.cmd -f backend\pom.xml -pl eureka-server spring-boot:run
.\tools\apache-maven-3.9.9\bin\mvn.cmd -pl api-gateway,user-service,movie-service,show-service,booking-service spring-boot:run
cd frontend; npm install; npm run dev
```

- Web: http://localhost:5173
- Gateway: http://localhost:8080
- Eureka: http://localhost:8761
- Staff QR check-in: http://localhost:5173/check-in

The local launcher uses PostgreSQL on `localhost:5433` with username `postgres` and
the password in the ignored `.env` file. It uses separate databases named `users`,
`movies`, `shows`, and `bookings`. In pgAdmin, register a server with host
`localhost`, port `5433`, and the same credentials, then refresh the database list.

## Booking flow

Register/login → Interstellar → 15 Sep 2026 6:00 PM → seats → pay → QR ticket.

Concurrent seat holds use pessimistic locks plus a unique `(showId, seatNumber)` constraint so two customers cannot book the same seat.

## Tests

```powershell
$env:JAVA_TOOL_OPTIONS="-Dnet.bytebuddy.experimental=true"
.\tools\apache-maven-3.9.9\bin\mvn.cmd -pl movie-service,booking-service test
```

## Container deployment

The repository includes a Docker Compose deployment for a persistent staging or single-host production baseline:

1. Install Docker Desktop with Compose support.
2. Use the local `.env` file or copy `.env.example` to `.env` and replace the placeholder JWT secret.
3. Start the stack:

```powershell
docker compose up --build -d
```

4. Open http://localhost:3000. The frontend serves the built React application and proxies `/api` to the gateway.
5. Check service health:

```powershell
docker compose ps
docker compose logs --tail=100 api-gateway
Invoke-RestMethod http://localhost:8080/actuator/health
```

The Compose stack uses PostgreSQL with separate `users`, `movies`, `shows`, and `bookings` databases. PostgreSQL is exposed on host port `55432` for pgAdmin because this machine already has another PostgreSQL instance on port `5432`. The backend containers activate the `prod` Spring profile, use the service name `postgres` for database access, and use `eureka-server` for service discovery. The initial baseline defaults `JPA_DDL_AUTO` to `update` so a new database can start; after adding and applying Flyway or Liquibase migrations, set `JPA_DDL_AUTO=validate` and remove schema auto-management. The local H2 configuration remains available for the existing test suite.

Stop the stack with `docker compose down`. Add `-v` only when intentionally deleting the PostgreSQL volume and all stored data.

## Production follow-up

Before exposing this stack to the public internet, add managed TLS and DNS, a secret manager, database backups and restore tests, database migrations, rate limiting, centralized logs and metrics, dependency/image scanning, CI/CD promotion through staging, and a payment provider integration. The current Compose file is a deployable baseline, not a complete highly available cloud architecture.
