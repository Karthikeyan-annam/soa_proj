# From Cinema Booking Problem to a Discoverable Microservice Platform

## Building CinePass with DTI principles

A cinema booking platform looks simple until several users try to reserve the same seat, services need to evolve independently, and the frontend must remain stable while the backend grows. CinePass addresses that problem with a focused microservice architecture built around Digital Transformation and Innovation (DTI) principles.

### Start with the problem, not the technology

The core requirement is reliable, convenient cinema booking. Users need to register, browse movies, choose a showtime, select available seats, complete a booking, and present a verifiable ticket. Staff need a separate check-in workflow.

That requirement exposes the real engineering risks: duplicated responsibilities, unsafe concurrent seat selection, scattered authentication logic, and a frontend coupled to internal service addresses. The solution begins by defining clear business capabilities and a single public API boundary.

### Turn business capabilities into services

CinePass separates the system into User, Movie, Show, and Booking services. Eureka provides service discovery, while the API Gateway provides the public entry point. This is more than splitting code into folders: each service owns a meaningful business area and can be developed, tested, and deployed with less coordination.

The gateway routes `/api/users`, `/api/movies`, `/api/shows`, and `/api/bookings` to logical service names. The React frontend does not need to know where those services run. That creates a stable customer experience while the platform can change behind the contract.

### Make security part of the architecture

Authentication is implemented as a workflow, not a checkbox. The User Service hashes passwords and issues a signed JWT after registration or login. The gateway validates the bearer token before forwarding protected requests and passes trusted user identity headers to downstream services.

Public catalogue browsing remains accessible, while booking mutations and profile operations require authentication. Production secrets and CORS origins are externalized through environment variables. This keeps security decisions close to the boundary where requests enter the platform.

### Protect the business invariant

The most important booking rule is simple: one seat must not be sold twice. CinePass combines pessimistic locking with a unique `(showId, seatNumber)` database constraint. The design therefore addresses both application-level coordination and database-level enforcement when concurrent requests arrive.

### Deliver a usable transformation

The frontend and backend are separate concerns with one clear contract. React handles the customer and staff experiences. Nginx serves the production build and proxies `/api` to the gateway. Docker Compose supplies PostgreSQL, service discovery, health checks, production configuration, and repeatable startup.

This is the practical side of DTI: the result is not only a new architecture diagram. It is a working path from customer interaction to protected service call, persistent state, and ticket verification.

### What comes next

A production evolution would add managed TLS and DNS, centralized secrets, database migrations, rate limiting, observability, automated security scanning, backups with restore tests, and CI/CD promotion through staging. The current design establishes the right foundations while making those improvements possible without redesigning the customer workflow.

CinePass demonstrates that successful transformation is a sequence of deliberate decisions: understand the problem, isolate capabilities, secure the boundary, protect critical invariants, and give users a reliable end-to-end experience.

## Review checklist

- Problem and requirements are explicit before architecture choices.
- Microservices map to business capabilities.
- Eureka removes hard-coded service locations.
- JWT authentication protects the gateway boundary.
- API Gateway centralizes routes and CORS.
- Frontend and backend communicate through a stable `/api` contract.
- Concurrent seat booking is protected by both locking and a database constraint.
- Deployment and production follow-up risks are stated honestly.
