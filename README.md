# Spring Boot Expert P1 - Block 14

Contracts and invoices REST API secured with Spring Security and JWT. Users live in an H2 database managed with JPA.

## Stack

- Java 17, Spring Boot 3.2.5
- Spring Web, Spring Data JPA, Spring Security, Bean Validation
- JJWT 0.12.6
- H2 in-memory database
- Lombok 1.18.24
- JUnit 5, Mockito, MockMvc

## Running it

```bash
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080`. The schema is dropped and recreated on every start, and `InitializeData` loads the users, two contracts and three invoices.

H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:billing`, user `sa`, empty password).

A Postman collection is in `src/main/resources/postman`. Each login request stores the token in the `token` collection variable, so the rest of the requests run as the last user you logged in with.

## Users

All of them use the password `Nter.2024`.

| Username | Stored roles | passwordExpired |
|----------|--------------|-----------------|
| `admin` | ADMIN | false |
| `contract.reader` | READ_CONTRACT | false |
| `contract.writer` | WRITE_CONTRACT | false |
| `invoice.reader` | READ_INVOICE | false |
| `invoice.writer` | WRITE_INVOICE | false |
| `expired.user` | READ_CONTRACT, READ_INVOICE | true |

`DatabaseUserDetailsService` adds `ROLE_USER` to everyone and the `PASSWORD_EXPIRED` authority when the flag is true. Both are built in memory each time the user is authenticated and are never written to the database.

## Token

The token is signed with HS384 and carries:

| Claim | Value |
|-------|-------|
| `typ` (header) | `JWT` |
| `iss` | `Exam_adrian_16/11/2026` |
| `jti` | random UUID |
| `userId` | user id |
| `sub` | username |
| `roles` | user roles, `USER` included |
| `iat` / `nbf` | issue time, so it is valid right away |
| `exp` | issue time plus 3 hours |

Issuer, key and expiration come from `jwt.issuer`, `jwt.secret` and `jwt.expiration` in `application.properties`. The key in that file is for local use only. Set `JWT_SECRET` (base64, at least 256 bits) to override it.

On every request the filter validates the signature, issuer and dates, then reloads the user from the database. That way a password change or a role change applies straight away without waiting for the token to expire.

## Endpoints

| Method | Path | Who can call it |
|--------|------|-----------------|
| POST | `/auth/login` | anyone |
| POST | `/auth/refresh` | any authenticated user |
| POST | `/auth/user/changepass` | `PASSWORD_EXPIRED` or `ADMIN` |
| GET | `/api/contracts`, `/api/contracts/{id}` | `READ_CONTRACT` or `ADMIN` |
| POST | `/api/contracts` | `WRITE_CONTRACT` or `ADMIN` |
| GET | `/api/invoices`, `/api/invoices/{id}`, `/api/invoices?contractId=1` | `READ_INVOICE` or `ADMIN` |
| POST | `/api/invoices` | `WRITE_INVOICE` or `ADMIN` |

URL rules are in `SecurityConfig`. Anything not listed there falls back to `ADMIN`. The password change rule is declared on the controller with `@PreAuthorize`, as the exam asks.

Invoices of a contract are filtered on `/api/invoices` and not exposed under `/api/contracts/{id}/invoices`. Otherwise a user with only `READ_CONTRACT` could read invoices.

## Examples

```bash
TOKEN=$(curl -s localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"expired.user","password":"Nter.2024"}' | jq -r .token)

curl -s localhost:8080/api/contracts -H "Authorization: Bearer $TOKEN"

curl -i -X POST localhost:8080/auth/user/changepass -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"newPassword":"Changed.2024"}'
```

After the change `expired.user` no longer has `PASSWORD_EXPIRED`, so a second call with the same token returns 403.

Errors share one format:

```json
{"timestamp": "2026-11-16T10:00:00", "status": 403, "error": "Forbidden", "message": "You are not allowed to access this resource"}
```

401 means a missing, invalid or expired token, or wrong credentials on login. 403 means the user is authenticated but lacks the role.

## Tests

```bash
./mvnw clean verify
```

- `JwtServiceTest` checks the exact claim set, the 3 hour window with a fixed clock, and that expired tokens or tokens from another issuer are rejected.
- `DatabaseUserDetailsServiceTest` checks the runtime authorities and that nothing is saved.
- `AuthControllerTest` covers login, refresh, forged tokens and the password change flow against H2.
- `AuthorizationTest` goes through the access matrix above with real tokens for each user.
