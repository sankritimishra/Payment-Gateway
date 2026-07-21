Payment Gateway

A Spring Boot backend for a small banking system — user accounts, loans, and
account-to-account payments. Built as a learning project focused on getting
the transactional details right: idempotent payments, row-level locking to
prevent race conditions under concurrent transfers, automatic retry of
transient failures, and consistent error handling across the whole API.

Features


User account management (create, read, update, delete)
Loan origination with EMI, total-payable, and outstanding-balance tracking
Account-to-account transfers protected by an Idempotency-Key, so retried
requests (client timeout, network blip, double-click) never double-charge
Row-level locking (SELECT ... FOR UPDATE), acquired in a fixed order,
so two concurrent transfers between the same pair of accounts can't
deadlock each other
Automatic retry of transient database failures, deliberately excluding
business-rule failures (retrying "insufficient funds" would never help)
One centralized error handler (GlobalExceptionHandler) so every endpoint
returns the same JSON error shape instead of leaking stack traces


Tech stack


Java 11, Spring Boot 2.7
Spring JDBC (NamedParameterJdbcTemplate) — raw SQL, no ORM
MySQL 8
Spring Retry
JUnit 5, Mockito, AssertJ — unit tests
Testcontainers (MySQL) — repository-level integration tests that exercise
real locking behavior H2 wouldn't faithfully emulate


Getting started

Prerequisites

JDK 11
MySQL 8 (local install or Docker)
Maven (or the bundled ./mvnw)
Docker, only if you want to run the Testcontainers-backed repository tests
Configuration

Database credentials are read from environment variables, not committed to
the repo:

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/paymentdb}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

For local development, put real values in
src/main/resources/application-local.properties (git-ignored) and run
with the local profile:

export SPRING_PROFILES_ACTIVE=local

or just export DB_USERNAME/DB_PASSWORD directly.


Database schema

No formal migration tool is set up yet; tables are created manually. Expected
schema:


users — account_number, username, first_name, middle_name, last_name, age, location
user_account_details — account_number, balance
loan — account_number, loan_amount, interst_rate, tenure, total_loan_payable, start_date, emi, loan_paid, total_outstanding
payment_history — sourceAccountNumber, sourceAccountName, destAccountNumber, destAccountName, amount, isSuccessful, messageSentToSourceAccount, messageSentToDestAccount
idempotency_record — idempotency_key (primary key), status (IN_PROGRESS/COMPLETED), response_body


Project status

This project is being actively hardened — see github-issues-backlog.md (one
directory up) for the running list of known gaps (concurrency tests under
real load, request validation, and a few open bugs) tracked as day-sized
GitHub issues.


