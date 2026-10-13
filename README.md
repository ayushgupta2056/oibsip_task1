# PayCheck API

[![CI](https://github.com/ayushgupta2056/oibsip_task1/actions/workflows/ci.yml/badge.svg?branch=ccr-d2605e1c-5x9wtx)](https://github.com/ayushgupta2056/oibsip_task1/actions/workflows/ci.yml)

A small payment validation service. You send a payment, it runs a few fraud checks,
and returns **APPROVED** or **DECLINED**. Every payment is saved in PostgreSQL.

**Tech:** Java 17 · Spring Boot 3 · PostgreSQL · Docker · JUnit 5 · GitHub Actions

## Endpoints

| Method | URL              | What it does            |
|--------|------------------|-------------------------|
| POST   | `/payments`      | Submit a payment        |
| GET    | `/payments`      | List all payments       |
| GET    | `/payments/{id}` | View one payment        |

There is also a simple web page at `http://localhost:8080/` with a form and a table of payments.

## Fraud rules (Strategy pattern)

Each rule is its own small class that implements one interface:

```java
public interface FraudRule {
    Optional<String> check(PaymentRequest request);   // a reason if it fails, empty if OK
}
```

| Rule                 | Declines when                                          |
|----------------------|--------------------------------------------------------|
| `HighAmountRule`     | amount is over ₹1,00,000                               |
| `BlockedCountryRule` | country is on the blocked list (`KP`, `IR`)            |
| `VelocityRule`       | the same card makes more than 3 payments in 1 minute   |

`PaymentService` receives **all** `FraudRule` beans from Spring and runs them in turn.
The first rule that fails declines the payment. **To add a new rule, add one new class
with `@Component`. No other code changes.** Limits are set in `application.yml`.

Every attempt counts towards the velocity rule, including declined ones, so a card that
keeps retrying is still stopped.

Card numbers are never stored in full: the database keeps a masked number (`************1111`)
for display and a SHA-256 hash to recognise repeat use of the same card.

## How to run

You need **Java 17+** and **Docker**.

```bash
# 1. Start PostgreSQL (one command)
docker compose up -d

# 2. Start the API (use "mvn" instead if you have Maven installed)
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080`. The table is created automatically.

### Run the tests

```bash
./mvnw test
```

Tests use an in-memory H2 database, so they don't need Docker or PostgreSQL.
GitHub Actions runs them on every push.

## Sample request and response

**Approved payment**

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{"cardNumber": "4111111111111111", "amount": 2500, "country": "IN"}'
```

```json
{
  "id": 1,
  "cardNumber": "************1111",
  "amount": 2500,
  "country": "IN",
  "status": "APPROVED",
  "reason": null,
  "createdAt": "2026-10-13T09:22:49.584801249Z"
}
```

**Declined payment (amount over the limit)**

```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -d '{"cardNumber": "4111111111111111", "amount": 150000, "country": "IN"}'
```

```json
{
  "id": 2,
  "cardNumber": "************1111",
  "amount": 150000,
  "country": "IN",
  "status": "DECLINED",
  "reason": "Amount is over the limit of 100000",
  "createdAt": "2026-10-13T09:22:49.709490793Z"
}
```

**Invalid input** returns `400 Bad Request`:

```json
{
  "cardNumber": "must be 12 to 19 digits",
  "amount": "must be greater than 0",
  "country": "must be a 2-letter country code, e.g. IN"
}
```

**Unknown id** (`GET /payments/99`) returns `404 Not Found`:

```json
{ "error": "Payment 99 not found" }
```

## Tests

13 JUnit 5 tests:

- **Unit tests** for each fraud rule (`HighAmountRuleTest`, `BlockedCountryRuleTest`, `VelocityRuleTest`).
  The velocity rule uses a fixed `Clock` and a Mockito mock, so it doesn't depend on real time.
- **API tests** (`PaymentControllerTest`) that start the whole app with MockMvc: approve,
  decline by amount, decline by velocity, list, get by id, 404 and validation errors.

## Project structure

```
src/main/java/com/paycheck
├── PayCheckApplication.java
├── payment/        controller, service, entity, repository, request/response
├── fraud/          FraudRule interface + the three rules
└── common/         error handling and card masking/hashing
```
