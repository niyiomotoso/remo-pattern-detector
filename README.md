
# Suspicious Activity Pattern Detector

This project is a backend service that detects suspicious activity patterns in financial transactions. It provides RESTful APIs to log user transactions and retrieve any that are flagged as suspicious based on predefined business rules.

I built it with **Spring Boot**, backed by a **MySQL database**, and I packaged with **Docker Compose** for setup and deployment. Unit tests and integration tests are included to ensure robustness. The integration test is powered by **Testcontainers** to replicate production-like behavior.


## Project Description

The Suspicious Activity Pattern Detector is designed to analyze a stream of transactions in real-time and flag those that match one or more suspicious patterns. These rules are implemented in a modular service layer that can be extended or swapped easily.

### Core Features
- **Store Transactions**: Accept and persist new transactions with details like `userId`, `amount`, `timestamp`, and `type` (deposit, withdrawal, transfer).
- **Suspicious Activity Detection**:
  - **High Volume Transaction**: Flags any transaction over `$10,000`.
  - **Frequent Small Transactions**: Flags users who perform more than **5 transactions under $100** within a 1-hour window.
  - **Rapid Transfers**: Flags users who execute **3 or more transfer transactions** within a 5-minute window.
- **Fetch Suspicious Transactions**: Retrieve a user's suspicious transactions ordered by timestamp.

### Technology Stack
- Java 19
- Spring Boot 3.4.5
- MySQL 8
- Docker & Docker Compose
- JPA (Hibernate)
- Testcontainers + JUnit 5
- MockMvc for HTTP-level testing
- Lombok for boilerplate-free models

This service is modular and designed with extensibility in mind. Core services are defined by interfaces, allowing for future implementations (e.g., rule-based vs ML-based detection) without impacting existing layers.


## ️ Setup Instructions
### Prerequisites

Make sure you have the following installed:

- Java 19
- Maven
- Docker 
- Docker Compose

### Run the Application with Docker Compose

NOTE: MAKE SURE DOCKER IS RUNNING BEFORE RUNNING THE COMMANDS BELOW.

1. **Build the JAR file**:

   ```bash
   ./mvnw clean package
   ```

2. **Start the services**:

   ```bash
   docker-compose up --build
   ```

   This command will:
    - Start a MySQL 8 container
    - Build and run the Spring Boot application
    - Expose API at `http://localhost:8100`

---

###  Run Tests

To run all unit and integration tests:

```bash
./mvnw test
```
Perfect — here's the **API Documentation** section in clean GitHub markdown format:


##  API Documentation

###  `POST /api/v1/transactions`

Logs a new transaction and automatically evaluates it for suspicious activity.

####  Request Body

```json
{
  "userId": "john123",
  "amount": 1500.0,
  "timestamp": "2025-05-09T14:30:00",
  "type": "DEPOSIT"
}
```

- `userId` (String) – ID of the user performing the transaction
- `amount` (Double) – Amount involved
- `timestamp` (ISO 8601) – Time of transaction (e.g., `2025-05-09T14:30:00`)
- `type` (Enum) – One of: `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`

####  Response

- `201 Created` – Transaction saved (whether suspicious or not)
- `400 Bad Request` – If required fields are missing or invalid

---

###  `GET /api/v1/users/{userId}/suspicious`

Fetches all suspicious transactions for a given user, ordered by timestamp.

#### Path Parameter

- `userId` – The user identifier

####  Response Body

```json
[
  {
    "id": 12,
    "amount": 12000.0,
    "timestamp": "2025-05-09T14:30:00",
    "type": "DEPOSIT"
  },
  {
    "id": 13,
    "amount": 50.0,
    "timestamp": "2025-05-09T14:45:00",
    "type": "WITHDRAWAL"
  }
]
```

- Each item includes the suspicious transaction’s `id`, `amount`, `timestamp`, and `type`.

---

### Useful Notes

- All timestamps are expected in `ISO_LOCAL_DATE_TIME` format (e.g., `2025-05-09T14:30:00`)
- Transactions are flagged automatically at the time of saving based on business rules.

## Assumptions and Trade-offs

###  Assumptions

- The `timestamp` provided in the request body is assumed to be accurate and in UTC/ISO format.
- Transaction detection rules are checked **before** saving the transaction to the database.
- A transaction is marked suspicious if it satisfies **any** of the defined rules (logical OR).
- All suspicious transactions are stored in the same table with a `suspicious` boolean flag for simplicity.
- Rule thresholds are fixed and hardcoded (e.g., `$10,000`, 5 small transactions, 3 rapid transfers) for this implementation.
- No authentication or rate-limiting is implemented since this is a scoped take-home exercise.

---

### Trade-offs

- **Rule Flexibility**: My detection figures is hardcoded rather than configurable; a real-world solution would externalize thresholds (say using DB or config server).
- **Database Coupling**: The service is tightly coupled to MySQL and JPA. For higher scalability, a more decoupled architecture (event-based, CQRS) could be considered.
- **Performance**: My logic executes DB queries for each transaction submission. This is acceptable for small to medium scale, but a rule engine or caching layer could improve efficiency at scale.
- **Error Handling**: Very basic validation and error messaging are included. In production, a standardized error model (e.g., RFC 7807) would be implemented.
- **Testcontainers Overhead**: Integration tests use Testcontainers with real MySQL which ensures some level of real-world scenario but adds to test execution time. This is a deliberate choice to simulate production behavior.

