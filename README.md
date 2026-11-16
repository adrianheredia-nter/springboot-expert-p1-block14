# Spring Boot Expert P1 - Bloque 14

API REST de contratos y facturas protegida con Spring Security y JWT. Los usuarios se guardan en una base de datos H2 y se gestionan con JPA.

## Stack

- Java 17 y Spring Boot 3.2.5
- Spring Web, Spring Data JPA, Spring Security y Bean Validation
- JJWT 0.12.6
- H2 en memoria
- Lombok 1.18.24
- JUnit 5, Mockito y MockMvc

## Arranque

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`. El esquema se borra y se vuelve a crear en cada arranque, y `InitializeData` carga los usuarios, dos contratos y tres facturas.

La consola de H2 está en `http://localhost:8080/h2-console`, con JDBC URL `jdbc:h2:mem:billing`, usuario `sa` y contraseña vacía.

En `src/main/resources/postman` hay una colección de Postman. Cada petición de login guarda el token en la variable de colección `token`, así que el resto de peticiones se lanzan con el último usuario con el que hayas hecho login.

## Usuarios de prueba

Todos tienen la contraseña `Nter.2024`.

| Usuario | Roles guardados | passwordExpired |
|---------|-----------------|-----------------|
| `admin` | ADMIN | false |
| `contract.reader` | READ_CONTRACT | false |
| `contract.writer` | WRITE_CONTRACT | false |
| `invoice.reader` | READ_INVOICE | false |
| `invoice.writer` | WRITE_INVOICE | false |
| `expired.user` | READ_CONTRACT, READ_INVOICE | true |

`DatabaseUserDetailsService` añade `ROLE_USER` a todos los usuarios y la authority `PASSWORD_EXPIRED` a los que tienen el flag a `true`. Las dos se construyen en memoria cada vez que se autentica el usuario y nunca se escriben en la base de datos.

## Token JWT

El token se firma con HS384 y lleva estos campos:

| Claim | Valor |
|-------|-------|
| `typ` (cabecera) | `JWT` |
| `iss` | `Exam_adrian_16/11/2026` |
| `jti` | UUID aleatorio |
| `userId` | id del usuario |
| `sub` | nombre de usuario |
| `roles` | roles del usuario, incluido `USER` |
| `iat` / `nbf` | momento de emisión, así que es válido desde ese instante |
| `exp` | momento de emisión más 3 horas |

El issuer, la clave y la caducidad se leen de `jwt.issuer`, `jwt.secret` y `jwt.expiration` en `application.properties`. La clave de ese fichero es solo para desarrollo local. Para usar otra, define `JWT_SECRET` con una clave en Base64 de al menos 256 bits.

En cada petición, `JwtAuthenticationFilter` valida la firma, el issuer y las fechas, y después vuelve a cargar el usuario desde la base de datos. Por eso un cambio de contraseña o de roles se aplica en la siguiente petición, sin esperar a que caduque el token. El esquema `Bearer` de la cabecera `Authorization` se acepta sin distinguir mayúsculas.

## Endpoints

| Método | Ruta | Quién puede llamarlo |
|--------|------|----------------------|
| POST | `/auth/login` | cualquiera |
| POST | `/auth/refresh` | cualquier usuario autenticado |
| POST | `/auth/user/changepass` | `PASSWORD_EXPIRED` o `ADMIN` |
| GET | `/api/contracts`, `/api/contracts/{id}` | `READ_CONTRACT` o `ADMIN` |
| POST | `/api/contracts` | `WRITE_CONTRACT` o `ADMIN` |
| GET | `/api/invoices`, `/api/invoices/{id}`, `/api/invoices?contractId=1` | `READ_INVOICE` o `ADMIN` |
| POST | `/api/invoices` | `WRITE_INVOICE` o `ADMIN` |

Las reglas por URL están en `SecurityConfig`, y cualquier ruta que no aparezca ahí exige `ADMIN`. La regla del cambio de contraseña va en el controller con `@PreAuthorize`, como pide el enunciado.

Las facturas de un contrato se filtran en `/api/invoices` y no cuelgan de `/api/contracts/{id}/invoices`. Si colgaran de esa ruta, un usuario con solo `READ_CONTRACT` podría leer facturas.

Login y refresh devuelven el token con este formato:

```json
{"token": "eyJ...", "tokenType": "Bearer", "expiresIn": 10800}
```

Para crear contratos y facturas se envían estos cuerpos. `endDate` es opcional y `amount` tiene que ser positivo, con dos decimales como máximo:

```json
{"contractNumber": "CT-2024-003", "customerName": "Acme", "startDate": "2026-01-01", "endDate": "2026-12-31"}
```

```json
{"invoiceNumber": "INV-2024-0004", "issueDate": "2026-02-01", "amount": 150.00, "contractId": 1}
```

## Ejemplos

```bash
TOKEN=$(curl -s localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"expired.user","password":"Nter.2024"}' | jq -r .token)

curl -s localhost:8080/api/contracts -H "Authorization: Bearer $TOKEN"

curl -i -X POST localhost:8080/auth/user/changepass -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"newPassword":"Changed.2024"}'
```

La nueva contraseña debe tener entre 8 y 64 caracteres y ser distinta de la actual. Al cambiarla se guarda en la base de datos y `passwordExpired` pasa a `false`. A partir de ese momento `expired.user` ya no tiene `PASSWORD_EXPIRED`, así que una segunda llamada con el mismo token devuelve 403.

## Errores

Todos los errores tienen el mismo formato:

```json
{"timestamp": "2026-11-16T10:00:00", "status": 403, "error": "Forbidden", "message": "You are not allowed to access this resource"}
```

| Código | Cuándo |
|--------|--------|
| 400 | cuerpo mal formado, validación fallida o nueva contraseña igual a la actual |
| 401 | falta el token, es inválido o ha caducado, o las credenciales del login son incorrectas |
| 403 | el usuario está autenticado pero no tiene el rol necesario |
| 404 | el contrato o la factura no existe |
| 409 | número de contrato o de factura duplicado |

## Tests

```bash
./mvnw clean verify
```

- `JwtServiceTest` comprueba que el token lleva exactamente los claims de la tabla, la ventana de 3 horas con un reloj fijo y que se rechazan los tokens caducados o con otro issuer.
- `DatabaseUserDetailsServiceTest` comprueba las authorities que se añaden en tiempo de ejecución y que no se guarda nada.
- `AuthControllerTest` cubre contra H2 el login, el refresh, los tokens manipulados, el esquema `bearer` en minúsculas y el flujo de cambio de contraseña.
- `AuthorizationTest` recorre la matriz de permisos anterior con tokens reales de cada usuario, y también los casos 401, 404 y 409.
