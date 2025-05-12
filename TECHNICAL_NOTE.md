# Technical Report

## Overview

The goal is to have a backend service designed to monitor financial transactions and automatically flag those that match predefined suspicious patterns. Also, to provide an extensible and efficient system capable of identifying potentially fraudulent or anomalous behavior based on a configurable set of rules.

I built using Java 19 with Spring Boot and MySQL as the primary datastore. It exposes two core RESTful endpoints: one for logging a new transaction and another for retrieving all suspicious transactions for a given user.

---

## System Architecture

The architecture I employed is a layered architecture that promotes modularity, separation of concerns, and ease of maintenance. Below are the primary layers and their responsibilities:

### 1. Controller Layer
- Exposes RESTful endpoints using Spring’s `@RestController`.
- Handles incoming HTTP requests and delegates processing to the service layer.
- Validates inputs and returns appropriate HTTP status codes.

### 2. Service Layer
- Coordinates the main business logic of the application.
- Responsible for saving transactions and triggering the suspicious activity detection logic.
- Implemented as interfaces to allow for future extension or alternative detection strategies.

### 3. Suspicious Activity Detection Service
- Encapsulates all rule-based logic used to evaluate whether a transaction is suspicious.
- Defined via an interface (`SuspiciousActivityService`) with a rule-based implementation (`RuleBasedSuspiciousActivityService`).
- Each rule (e.g., high-value, frequent small transactions, rapid transfers) is implemented as an independent method for clarity and testability.

### 4. Repository Layer
- Uses Spring Data JPA to interact with the MySQL database.
- Custom query methods support rule evaluation based on time windows and transaction types.
- No custom SQL is written; I used Spring’s method naming conventions for maintainability.

### 5. DTO Layer
- Data Transfer Objects (`TransactionRequest`, `SuspiciousTransactionResponse`) are used to separate the internal data model from external API contracts.
- Prevents overexposing domain entities and enables better control over input/output formatting.

### 6. Persistence Layer
- A MySQL database is used for data persistence.
- A single `Transaction` table holds all transaction records with a `suspicious` boolean flag for simplicity.
- JPA/Hibernate is used for ORM mapping.

In summary, I ensure the system has high cohesion within components and low coupling between layers, making the system maintainable, testable, and extensible.

---
## Suspicious Activity Detection Logic

Suspicious activity detection is handled by a dedicated service layer that evaluates each transaction against a fixed set of business rules. This logic is encapsulated in the `RuleBasedSuspiciousActivityService` class, which implements the `SuspiciousActivityService` interface. The service is invoked immediately before persisting any new transaction.

### Detection Rules

The system supports the following suspicious activity detection rules:

#### 1. High Volume Transaction
- **Condition**: Any transaction with an amount greater than $10,000.
- **Implementation**: A direct comparison of the transaction amount.

#### 2. Frequent Small Transactions
- **Condition**: More than 5 transactions under $100 within a 1-hour window for the same user.
- **Implementation**: A timestamp-based range query to count recent small transactions, excluding the current one.

#### 3. Rapid Transfers
- **Condition**: 3 or more transfer transactions within a 5-minute window.
- **Implementation**: A query that retrieves recent `TRANSFER` transactions by the same user in the past 5 minutes. If 2 or more are found, the current transaction would be the third and is therefore flagged.

### Evaluation Strategy

Each rule is evaluated independently. If any rule evaluates to true, the transaction is flagged as suspicious. The logical operator used is OR.

The detector is designed to be modular, making it easy to add, remove, or refactor rules without affecting the rest of the application.

### Performance Consideration

Each detection rule involves at least one database read. While this is okay for the current scale, more advanced implementations could utilize in-memory caching or a rule engine to improve performance under high throughput.

---

## Testing Strategy

The project includes both unit and integration tests to ensure correctness, robustness, and confidence in future refactoring. Tests are written using JUnit 5, Mockito, and Spring’s testing support.

### Unit Tests

Since the scope of the task is simple, I focused on the `RuleBasedSuspiciousActivityService`, where the core detection logic resides. These tests use mocked repository responses to isolate and validate each rule individually:

- **High Value Detection**: Ensures transactions over $10,000 are flagged.
- **Frequent Small Transactions**: Simulates more than 5 transactions under $100 in 1 hour.
- **Rapid Transfers**: Validates that 3 transfers within 5 minutes trigger a flag.
- **Negative Scenarios**: Confirms that clean transactions are not falsely flagged.

Mockito is used to mock the repository layer.

### Integration Tests

Using integration tests, I validate the full behavior of the application, from HTTP layer to database.
Tests include:

- Logging transactions via the `POST /transactions` endpoint.
- Verifying suspicious activity is detected end-to-end for each rule.
- Fetching flagged transactions via `GET /users/{userId}/suspicious`.
- Validating HTTP status codes and response payloads.
- Handling invalid input and verifying correct error responses.

### Test Execution

All tests can be executed using:

```bash
./mvnw test
```
---

## Key Design Decisions

### 1. Interface-Based Service Design

Both the `TransactionService` and `SuspiciousActivityService` are defined as interfaces. This promotes loose coupling and allows for easy substitution of components in the future, such as switching from a rule-based engine to a machine learning model without changing the application logic.

### 2. Rule Encapsulation in a Dedicated Service

All suspicious activity detection rules are encapsulated in a dedicated class (`RuleBasedSuspiciousActivityService`). This keeps the business logic isolated from other concerns and allows each rule to be tested and maintained independently.

### 3. Use of DTOs for API Input and Output

To decouple the domain model from external API contracts, the application uses DTOs:
- `TransactionRequest` handles inbound data for logging transactions.
- `SuspiciousTransactionResponse` shapes the outbound data returned from the suspicious transaction query.
This separation prevents overexposing internal structures and allows for future changes to the entity model without affecting API clients.

### 4. Dockerized Environment

The application and database are containerized using Docker and Docker Compose. This simplifies local setup and makes it easy to ensure that the development, testing, and production environments behave consistently.

### 5. Minimal External Dependencies

For now, my focus is on the core logic, the application avoids additional layers such as caching, security, or asynchronous messaging.

---
## Trade-offs and Limitations and Future Improvements

### 1. Hardcoded Detection Rules

All thresholds for suspicious activity (e.g., transaction amount > $10,000, 5 small transactions in 1 hour, 3 rapid transfers) are currently hardcoded in the rule implementation. A more robust solution would externalize these values to a configuration file or database for runtime control.

### 2. Synchronous Evaluation

Suspicious activity is evaluated synchronously before saving each transaction. This guarantees real-time detection but introduces some performance overhead on write operations. In high-throughput systems, offloading detection to an asynchronous worker or event-driven pipeline (e.g., Kafka + background service) could improve throughput.

### 3. Tight Coupling to MySQL and JPA

The system is tightly integrated with Spring Data JPA and MySQL. This is suitable for the scope of the tasj, but future scalability might require database abstraction layers, NoSQL support, or event-sourced architectures.

### 4. Single Table Design

All transaction data is stored in a single `Transaction` table with a boolean flag (`suspicious`) to indicate status. This may become inefficient if the volume of data grows significantly. Archiving strategies might be needed in the future.

### 5. No User Authentication or Authorization

For simplicity, the application does not implement any authentication or authorization. In a production system, endpoint access should be secured using token-based auth (e.g., JWT) and user identity should be validated on each request.

### 6. Basic Error Handling

The application provides minimal validation and error handling. More detailed error responses, input validation feedback, and structured error models (e.g., RFC 7807) would be expected in production environments.

### 7. No Pagination for Query Results

The `GET /users/{userId}/suspicious` endpoint returns all suspicious transactions without pagination. This would need to be enhanced with limit/offset or cursor-based pagination for scalability.

### 8. Monitoring and Alerting

Integrate monitoring tools (e.g., Prometheus, Grafana) and set up alerting when suspicious transactions exceed certain thresholds. This would provide operational visibility and real-time fraud monitoring capabilities.

### 9. Performance Optimization

Introduce caching (e.g., Redis) for frequently accessed data and use query optimizations or indexing strategies for time-based transaction queries.

### 10. Audit Logging and Compliance

Log all rule evaluations and detection decisions with reasons for auditability. This can support compliance requirements and help in incident investigations.
