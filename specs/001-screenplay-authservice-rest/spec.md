# Feature Specification: Automatización REST del AuthService con Screenplay + Serenity Rest

**Feature Branch**: `001-screenplay-authservice-rest`  
**Created**: 2026-04-08  
**Status**: Draft  
**Input**: User description: "Automatizar con Screenplay + Serenity Rest los endpoints REST del AuthService en localhost:8003. Flujo de 4 pasos: Paso 1 POST /api/auth/register → 201 Created. Paso 2 POST /api/auth/login → 200 con token JWT. Paso 3 POST /api/auth/register mismo email → 409 Conflict. Paso 4 POST /api/auth/login contraseña incorrecta → 401 Unauthorized. Sin Selenium, sin browser. El token del paso 2 se propaga entre pasos con actor.remember/recall. Email único por ejecución usando UUID."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registro exitoso de nuevo usuario (Priority: P1)

Un nuevo usuario se registra en el sistema de autenticación proporcionando un email único generado dinámicamente (UUID) y una contraseña válida. El sistema acepta la solicitud y confirma la creación del recurso.

**Why this priority**: Es el paso fundacional del flujo. Sin un registro exitoso no es posible ejecutar ninguno de los pasos posteriores. Valida que el endpoint de registro responde correctamente ante credenciales nuevas y produce el recurso.

**Independent Test**: Puede testarse de forma aislada enviando un POST a `/api/auth/register` con un email generado dinámicamente (UUID) y verificando que la respuesta tiene código 201 Created.

**Acceptance Scenarios**:

1. **Given** que el AuthService está disponible en `localhost:8003`, **When** se envía POST `/api/auth/register` con el payload `{ "firstName", "lastName", "email" (UUID-based), "password", "confirmPassword" }` donde `password` y `confirmPassword` son iguales, **Then** el sistema responde con HTTP 201 Created.
2. **Given** que el sistema respondió 201 Created, **When** se inspecciona el cuerpo de la respuesta, **Then** contiene al menos el email registrado del usuario creado.

---

### User Story 2 - Inicio de sesión exitoso y obtención de token JWT (Priority: P1)

Un usuario previamente registrado inicia sesión con sus credenciales correctas. El sistema valida las credenciales y devuelve un token JWT que el actor almacena para utilizarlo en pasos posteriores del flujo.

**Why this priority**: El token JWT obtenido en este paso es el insumo crítico que se propaga al resto del flujo mediante `actor.remember/recall`. Valida autenticación funcional y generación de token.

**Independent Test**: Puede testarse enviando POST `/api/auth/login` con credenciales de un usuario existente y verificando que la respuesta tiene código 200 y contiene un token JWT.

**Acceptance Scenarios**:

1. **Given** que existe un usuario registrado con email y contraseña conocidos, **When** se envía POST `/api/auth/login` con esas credenciales correctas, **Then** el sistema responde con HTTP 200 OK.
2. **Given** que la respuesta es 200 OK, **When** se inspecciona el cuerpo de la respuesta, **Then** contiene un token JWT válido (cadena no vacía con formato de tres segmentos separados por puntos).
3. **Given** que el token JWT fue recibido, **When** el actor ejecuta `remember` para almacenar el token, **Then** el token está disponible para pasos posteriores mediante `recall`.

---

### User Story 3 - Rechazo de registro con email duplicado (Priority: P2)

El sistema rechaza el intento de registrar un usuario con un email que ya fue registrado, devolviendo un error de conflicto que protege la unicidad de identidad.

**Why this priority**: Valida la integridad del negocio: el sistema no debe permitir duplicidad de cuentas. Reutiliza el email del Paso 1 para provocar el conflicto de forma determinista.

**Independent Test**: Puede testarse enviando dos veces POST `/api/auth/register` con el mismo email y verificando que la segunda solicitud recibe HTTP 409 Conflict.

**Acceptance Scenarios**:

1. **Given** que un usuario con el email del Paso 1 ya está registrado, **When** se envía nuevamente POST `/api/auth/register` con ese mismo email, **Then** el sistema responde con HTTP 409 Conflict.
2. **Given** que la respuesta es 409 Conflict, **When** se inspecciona el cuerpo de la respuesta, **Then** contiene un mensaje de error indicando que el email ya está en uso.

---

### User Story 4 - Rechazo de login con contraseña incorrecta (Priority: P2)

El sistema rechaza el intento de inicio de sesión cuando la contraseña proporcionada no coincide con la del usuario, protegiendo el acceso y garantizando que ningún token sea emitido ante credenciales inválidas.

**Why this priority**: Valida la seguridad del endpoint de autenticación. Las credenciales incorrectas no deben producir un token de acceso bajo ninguna circunstancia.

**Independent Test**: Puede testarse enviando POST `/api/auth/login` con el email del Paso 1 y una contraseña deliberadamente incorrecta, verificando que la respuesta tiene código 401 Unauthorized.

**Acceptance Scenarios**:

1. **Given** que existe un usuario registrado con el email del Paso 1, **When** se envía POST `/api/auth/login` con ese email y una contraseña incorrecta, **Then** el sistema responde con HTTP 401 Unauthorized.
2. **Given** que la respuesta es 401 Unauthorized, **When** se inspecciona el cuerpo de la respuesta, **Then** no contiene ningún token JWT y sí contiene un mensaje de error de credenciales inválidas.

---

### Edge Cases

- ¿Qué ocurre si el AuthService no está disponible en `localhost:8003` cuando se inicia la suite? La prueba debe fallar en el Paso 1 con un error descriptivo de conexión rechazada, no con timeout silencioso.
- ¿Qué ocurre si el UUID generado produce un formato de email inválido? El email debe formatearse correctamente (ej. `uuid@test.com`) para que el servidor no rechace el formato antes de validar el duplicado.
- ¿Qué ocurre si el token JWT no fue almacenado correctamente en el Paso 2? Los pasos que dependan del `recall` deben fallar de forma explícita indicando que el token no fue encontrado en la memoria del actor.
- ¿Qué ocurre si los pasos se ejecutan fuera de orden? Al estar modelados como un único Scenario Cucumber, Cucumber garantiza la ejecución del orden declarado (Paso 1 → 2 → 3 → 4). El `actor.remember/recall` opera dentro del mismo actor de sesión del escenario, sin necesidad de mecanismos de estado externos.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: La suite DEBE cubrir los cuatro pasos del flujo de autenticación dentro de **un único Scenario Cucumber** con pasos secuenciales: registro exitoso → login exitoso → registro duplicado → login con contraseña incorrecta. No se modelan como escenarios independientes, dado que la propagación del token JWT mediante `actor.remember/recall` requiere que todos los pasos se ejecuten bajo el mismo actor de sesión.
- **FR-002**: Cada paso DEBE verificar el código de estado HTTP de la respuesta como condición principal de aceptación.
- **FR-003**: El Paso 1 DEBE generar un email único por ejecución utilizando un identificador UUID para garantizar ausencia de colisiones entre ejecuciones. El payload de registro DEBE incluir los cinco campos requeridos por `RegisterUserRequest`: `firstName`, `lastName`, `email`, `password` y `confirmPassword` (los dos últimos con valor idéntico para superar la validación `[Compare]` del backend).
- **FR-004**: El Paso 2 DEBE extraer el token JWT del campo `token` de la respuesta JSON (`{ "token": "eyJ...", "expiresIn": 3600 }`) y almacenarlo en la memoria del actor mediante `remember`, quedando accesible para pasos posteriores mediante `recall`.
- **FR-005**: El Paso 3 DEBE reutilizar el mismo email registrado en el Paso 1 (recuperado con `recall`) para provocar el conflicto de registro duplicado.
- **FR-006**: El Paso 4 DEBE usar el email registrado en el Paso 1 con una contraseña deliberadamente diferente a la original para provocar el rechazo de autenticación.
- **FR-007**: La suite DEBE ejecutarse sin navegador web ni controlador Selenium; todas las interacciones son exclusivamente a través del protocolo HTTP (API REST).
- **FR-008**: La suite DEBE estructurarse con el patrón Screenplay (Actores, Tareas y Preguntas).
- **FR-009**: La suite DEBE producir reportes Serenity con evidencia de cada paso: petición enviada, respuesta recibida y resultado de la aserción.
- **FR-010**: El AuthService DEBE estar accesible en `localhost:8003` como prerequisito de ejecución; la suite no gestiona el arranque del servicio.

### Key Entities

- **Actor**: Entidad Screenplay que representa al usuario bajo prueba. Almacena el email único y el token JWT entre pasos mediante `remember/recall`.
- **Credenciales de Registro** (`RegisterUserRequest`): Cinco campos — `firstName`, `lastName`, `email` (UUID-based), `password`, `confirmPassword`. Los campos `password` y `confirmPassword` deben ser idénticos (validación `[Compare]` del backend).
- **Credenciales de Login** (`LoginUserRequest`): Dos campos — `email` y `password`.
- **Token JWT**: Cadena de autenticación obtenida en el Paso 2, extraída del campo `token` de la respuesta JSON del login. Persistida en la memoria del actor para uso posterior. La respuesta incluye también el campo `expiresIn` (segundos de validez).
- **Respuesta HTTP**: Objeto capturado de cada llamada REST, conteniendo código de estado y cuerpo. Es el insumo de las Preguntas Screenplay para las aserciones.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Los cuatro pasos del flujo de autenticación se ejecutan y validan en menos de 10 segundos totales bajo condiciones normales de red local.
- **SC-002**: El 100% de los cuatro pasos del Scenario único pasan satisfactoriamente cuando el AuthService está disponible y en estado inicial limpio.
- **SC-003**: El email único generado por UUID garantiza cero colisiones entre ejecuciones consecutivas de la suite en el mismo entorno.
- **SC-004**: El token JWT obtenido en el Paso 2 es recuperado correctamente en pasos posteriores sin requerir una nueva llamada al endpoint de login.
- **SC-005**: El reporte Serenity generado al finalizar la ejecución muestra el resultado individual de cada uno de los cuatro pasos con el código HTTP específico verificado.
- **SC-006**: Ante la no disponibilidad del AuthService, la suite falla en el Paso 1 con un mensaje de error descriptivo en un tiempo máximo configurable (por defecto ≤ 5 segundos).

## Clarifications

### Session 2026-04-08

- Q: ¿Cuál es el nombre exacto del campo del token JWT en la respuesta de POST /api/auth/login? → A: `token` — contrato `LoginUserResponse`: `{ "token": "eyJ...", "expiresIn": 3600 }`
- Q: ¿Los 4 pasos se modelan como un único Scenario o como escenarios independientes? → A: Un único Scenario Cucumber con 4 pasos secuenciales — el `actor.remember/recall` opera dentro del mismo actor de sesión sin mecanismos adicionales.
- Q: ¿Cuál es el schema exacto del request body de `/api/auth/register` y `/api/auth/login`? → A: Register → `{ "firstName", "lastName", "email", "password", "confirmPassword" }` (5 campos, `password`==`confirmPassword` requerido por `[Compare]`); Login → `{ "email", "password" }`. Contratos: `RegisterUserRequest` y `LoginUserRequest` del AuthService.

## Assumptions

- El AuthService está desplegado y accesible en `localhost:8003` antes de ejecutar la suite; no se gestiona el arranque automático del servicio.
- El AuthService acepta y devuelve datos en formato JSON (`Content-Type: application/json`).
- El endpoint `/api/auth/login` devuelve el token JWT en el campo `token` del cuerpo JSON: `{ "token": "eyJ...", "expiresIn": 3600 }`. Contrato definido en `LoginUserResponse` del AuthService.
- Un email con formato `<uuid>@test.com` es aceptado como email válido por la capa de validación del AuthService.
- La misma contraseña se usa en los Pasos 1 y 2; la contraseña del Paso 4 es cualquier cadena diferente a la original.
- No se requiere autenticación adicional (ej. API Key de servicio) para acceder a los endpoints del AuthService más allá de las credenciales de usuario.
- El proyecto ya cuenta con Serenity BDD y soporte REST (serenity-rest-assured) configurado en `build.gradle`.
- No se contempla limpieza automática de datos al finalizar la suite; cada ejecución crea un usuario único y no lo borra.
