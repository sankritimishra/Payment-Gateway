# Payment Gateway

A Spring Boot backend for a small banking system — user accounts, loans, and
account-to-account payments. Built as a learning project focused on getting
the transactional details right: idempotent payments, row-level locking to
prevent race conditions under concurrent transfers, automatic retry of
transient failures, and consistent error handling across the whole API.

## Features

- User account management (create, read, update, delete)
- Loan origination with EMI, total-payable, and outstanding-balance tracking
- Account-to-account transfers protected by an `Idempotency-Key`, so retried
  requests (client timeout, network blip, double-click) never double-charge
- Row-level locking (`SELECT ... FOR UPDATE`), acquired in a fixed order,
  so two concurrent transfers between the same pair of accounts can't
  deadlock each other
- Automatic retry of transient database failures, deliberately excluding
  business-rule failures (retrying "insufficient funds" would never help)
- One centralized error handler (`GlobalExceptionHandler`) so every endpoint
  returns the same JSON error shape instead of leaking stack traces

## Design notes

**Idempotency.** `POST /payments` requires an `Idempotency-Key` header.
`PaymentHistoryService` reserves the key with an atomic insert
(`INSERT IGNORE` against a table where `idempotency_key` is the primary
key) before doing any work. If the reservation fails because the key
already exists, the request is either replayed from the cached response
(key already `COMPLETED`) or rejected with 409 (key still `IN_PROGRESS`,
meaning a concurrent duplicate is mid-flight). If processing fails, the
reservation is released so a legitimate retry with the same key isn't
blocked forever.

**Concurrency.** `PaymentHistoryRepository.makePayment()` locks both the
source and destination account rows with `SELECT ... FOR UPDATE` before
reading their balances, closing the read-then-write race a plain
`SELECT` followed by `UPDATE` would allow. The two rows are always locked
in a fixed order (lower account number first), so two transfers moving
money in opposite directions between the same two accounts can't each
hold one lock while waiting on the other.

**Retry vs. recover.** `makePayment()` is `@Retryable` on
`DataAccessException` only — transient issues like a dropped connection —
and explicitly excludes `InsufficientFundsException` and
`IllegalArgumentException`, since retrying a business-rule failure would
just waste attempts and delay the user's real error. Once retries are
exhausted, `@Recover` records the transaction as failed instead of
throwing.

**Error handling.** Every exception is mapped centrally instead of per
controller — see [Error responses](#error-responses) below.

## Tech stack

- Java 11, Spring Boot 2.7
- Spring JDBC (`NamedParameterJdbcTemplate`) — raw SQL, no ORM
- MySQL 8
- Spring Retry
- JUnit 5, Mockito, AssertJ — unit tests
- Testcontainers (MySQL) — repository-level integration tests that exercise
  real locking behavior H2 wouldn't faithfully emulate

## Getting started

### Prerequisites

- JDK 11
- MySQL 8 (local install or Docker)
- Maven (or the bundled `./mvnw`)
- Docker, only if you want to run the Testcontainers-backed repository tests

### Configuration

Database credentials are read from environment variables, not committed to
the repo:

```
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/paymentdb}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

For local development, put real values in
`src/main/resources/application-local.properties` (git-ignored) and run
with the `local` profile:

```bash
export SPRING_PROFILES_ACTIVE=local
```

or just export `DB_USERNAME`/`DB_PASSWORD` directly.

### Running

```bash
./mvnw spring-boot:run
```

The API is available on `http://localhost:8080`.

### Running tests

```bash
./mvnw test
```

Unit tests (`PaymentHistoryServiceTest`, `LoanServiceTest`) run with no
external dependencies. 

## API reference

| Method | Path                                       | Description                              |
|--------|---------------------------------------------|-------------------------------------------|
| GET    | `/users/info/{account_number}`              | Fetch a user's details                    |
| POST   | `/users/add_new_user`                       | Create a new user                         |
| POST   | `/users/update_existing_user/{account_number}` | Update an existing user                |
| DELETE | `/users/delete_existing_user/{account_number}` | Delete a user                          |
| GET    | `/account/info/{account_number}`            | Get account balance                       |
| POST   | `/account/details/{account_number}`         | Create account details                    |
| POST   | `/account/updatebalance/{account_number}`   | Update account balance                    |
| GET    | `/loan/details/{account_number}`            | Fetch loan details                        |
| POST   | `/add/loan/details/{account_number}`        | Add a new loan                            |
| POST   | `/update/loan/details/{account_number}`     | Apply one EMI payment to a loan           |
| POST   | `/payments`                                 | Transfer funds between accounts (requires `Idempotency-Key` header) |

Example payment request:

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 5f2e6a9e-2b3d-4c9a-9f21-1a2b3c4d5e6f" \
  -d '{
        "sourceAccountNumber": "1000000001",
        "sourceAccountName": "Alice",
        "destAccountNumber": "2000000002",
        "destAccountName": "Bob",
        "amount": 500.00
      }'
```

Retrying the exact same request with the same `Idempotency-Key` returns the
original response instead of transferring the money again.

## Error responses

Every error returns the same shape:

```json
{
  "timestamp": "2026-07-21T10:15:30Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Account 1000000001 has insufficient funds for this transaction",
  "path": "/payments"
}
```

| Exception                             | Status | Meaning                                      |
|----------------------------------------|--------|-----------------------------------------------|
| `InsufficientFundsException`           | 422    | Source account doesn't have enough balance    |
| `DuplicateRequestInProgressException`  | 409    | Same `Idempotency-Key` is already being processed |
| `IllegalArgumentException`             | 400    | Invalid input (bad account number, non-positive amount) |
| `IllegalStateException`                | 409    | Unexpected but non-corrupt state (e.g. a lost idempotency reservation) |
| `IndexOutOfBoundsException`            | 404    | Requested account/user/loan doesn't exist     |
| `DateTimeParseException`               | 400    | Malformed date (expected `yyyy-MM-dd`)        |
| `DataAccessException`                  | 503    | Database error (message is generic; details are server-logged, not exposed) |
| Anything else                          | 500    | Unexpected error (generic message; full exception server-logged) |

## Database schema

No formal migration tool is set up yet; tables are created manually. Expected
schema:

- **users** — `account_number`, `username`, `first_name`, `middle_name`, `last_name`, `age`, `location`
- **user_account_details** — `account_number`, `balance`
- **loan** — `account_number`, `loan_amount`, `interst_rate`, `tenure`, `total_loan_payable`, `start_date`, `emi`, `loan_paid`, `total_outstanding`
- **payment_history** — `sourceAccountNumber`, `sourceAccountName`, `destAccountNumber`, `destAccountName`, `amount`, `isSuccessful`, `messageSentToSourceAccount`, `messageSentToDestAccount`
- **idempotency_record** — `idempotency_key` (primary key), `status` (`IN_PROGRESS`/`COMPLETED`), `response_body`

## Project status

This project is being actively hardened — see `github-issues-backlog.md` (one
directory up) for the running list of known gaps (concurrency tests under
real load, request validation, and a few open bugs) tracked as day-sized
GitHub issues.

