# Tasks: Automatización REST del AuthService con Screenplay + Serenity Rest

**Feature Branch**: `001-screenplay-authservice-rest`  
**Date**: 2026-04-08  
**Input**: Design documents from `specs/001-screenplay-authservice-rest/`  
**Prerequisites**: plan.md ✅ · spec.md ✅ · research.md ✅ · data-model.md ✅ · contracts/ ✅

---

## Phase 1: Setup (Infraestructura compartida)

**Purpose**: Clases utilitarias sin dependencias de user story — bloqueantes para todo lo demás.

- [x] T001 [P] Crear constantes de URL en `src/test/java/com/ticketing/util/Endpoints.java` (`AUTH_BASE`, `REGISTER`, `LOGIN`, `EVENTS_BASE`)
- [x] T002 [P] Crear builders de payload en `src/test/java/com/ticketing/util/AuthPayloadBuilder.java` — `registerPayload(firstName, lastName, email, password)` con 5 campos (`confirmPassword = password`) y `loginPayload(email, password)` con 2 campos

---

## Phase 2: Foundational (Prerequisitos bloqueantes)

**Purpose**: Infraestructura Screenplay compartida por los 4 user stories — **DEBE completarse antes de implementar cualquier user story**.

**⚠️ CRÍTICO**: Ningún user story puede implementarse hasta completar esta fase.

- [x] T003 Crear Question compartida `src/test/java/com/ticketing/questions/ElCodigoDeRespuesta.java` — implementa `Question<Integer>`, retorna `SerenityRest.lastResponse().statusCode()`
- [x] T004 Crear hook de setup en `src/test/java/com/ticketing/hooks/AutenticacionHooks.java` — `@Before(order=0)`: inicializa `OnStage` con `Cast.whereEveryoneCan(CallAnApi.at(Endpoints.AUTH_BASE))`, genera `email = "user-" + UUID.randomUUID() + "@test.com"`, almacena email y password con `actor.remember`; `@After`: `OnStage.drawTheCurtainOn()`
- [x] T005 Crear skeleton del feature Gherkin en `src/test/resources/features/autenticacion_api.feature` con el Scenario completo de 9 pasos (Given/When/Then × flujo de 4 pasos) sin modificar `crud_eventos.feature`
- [x] T006 Crear skeleton de Step Definitions en `src/test/java/com/ticketing/stepdefinitions/AutenticacionApiStepDefinitions.java` — todos los `@Given`/`@When`/`@Then` de `autenticacion_api.feature` declarados con cuerpos vacíos (compilable, no funcional)

**Checkpoint**: Proyecto compila sin errores (`./gradlew compileTestJava`) — runner descubre la nueva feature pero los steps no tienen implementación aún.

---

## Phase 3: User Story 1 — Registro exitoso de nuevo usuario (Priority: P1) 🎯 MVP

**Goal**: Automatizar el Paso 1 — `POST /api/auth/register` con datos válidos resulta en 201 Created.

**Independent Test**: Ejecutar `./gradlew test --tests "*autenticacion*"` con solo los pasos del Paso 1 implementados (los demás marcados `@Pending`) — debe pasar con 201 confirmado.

- [x] T007 [US1] Crear Task `src/test/java/com/ticketing/tasks/RegistrarUsuario.java` — método de fábrica estático `RegistrarUsuario.con(nombre, apellido, email, password)`, `performAs` ejecuta `SerenityRest.given().contentType(JSON).body(AuthPayloadBuilder.registerPayload(...)).post(Endpoints.REGISTER)` y almacena el email en la memoria del actor con `actor.remember("email", email)` (ver [contracts/auth-register.md](contracts/auth-register.md))
- [x] T008 [US1] Implementar step `when("un nuevo usuario se registra con datos validos")` en `AutenticacionApiStepDefinitions.java` — instancia actor con `OnStage.theActorCalled(...)` y llama `actor.attemptsTo(RegistrarUsuario.con(...))`
- [x] T009 [US1] Implementar step `then("el sistema confirma la creacion del usuario con codigo 201")` en `AutenticacionApiStepDefinitions.java` — `actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(201)))`

**Checkpoint**: Paso 1 del escenario pasa de forma aislada — 201 confirmado en reporte Serenity.

---

## Phase 4: User Story 2 — Inicio de sesión exitoso y obtención de token JWT (Priority: P1)

**Goal**: Automatizar el Paso 2 — `POST /api/auth/login` con credenciales válidas resulta en 200 OK con token JWT capturado en la memoria del actor.

**Independent Test**: Los pasos 1+2 pasan; el token `actor.recall("token")` no es null tras el Paso 2.

- [x] T010 Crear Question `src/test/java/com/ticketing/questions/ElTokenJwt.java` — implementa `Question<String>`, retorna `SerenityRest.lastResponse().body().jsonPath().getString("token")` (campo `token` de `LoginUserResponse` — ver [contracts/auth-login.md](contracts/auth-login.md))
- [x] T011 [US2] Crear Task `src/test/java/com/ticketing/tasks/IniciarSesionApi.java` — método `IniciarSesionApi.con(email, password)`, `performAs` ejecuta POST a `Endpoints.LOGIN` con `AuthPayloadBuilder.loginPayload(email, password)`, extrae el token directamente con `SerenityRest.lastResponse().body().jsonPath().getString("token")` y lo persiste con `actor.remember("token", token)` (las Tasks no invocan Questions — constitution §II)
- [x] T012 [US2] Implementar step `when("el usuario inicia sesion con sus credenciales correctas")` en `AutenticacionApiStepDefinitions.java` — recupera email/password con `actor.recall(...)` y llama `actor.attemptsTo(IniciarSesionApi.con(email, password))`
- [x] T013 [US2] Implementar steps `then("el sistema devuelve un token de acceso valido con codigo 200")` en `AutenticacionApiStepDefinitions.java` — afirma `ElCodigoDeRespuesta` → `equalTo(200)` y `ElTokenJwt` → `not(emptyOrNullString())`

**Checkpoint**: Pasos 1 y 2 pasan; token JWT visible en la evidencia del reporte Serenity.

---

## Phase 5: User Story 3 — Rechazo de registro con email duplicado (Priority: P2)

**Goal**: Automatizar el Paso 3 — `POST /api/auth/register` con el mismo email del Paso 1 resulta en 409 Conflict.

**Independent Test**: Ejecutar el escenario completo hasta el Paso 3 — 409 confirmado; pasos 1 y 2 siguen verdes.

- [x] T014 [US3] Crear Task `src/test/java/com/ticketing/tasks/IntentarRegistrarEmailDuplicado.java` — método `IntentarRegistrarEmailDuplicado.con(email, password)`, `performAs` ejecuta POST a `Endpoints.REGISTER` con el mismo payload que `RegistrarUsuario` pero sin `actor.remember` (solo provoca el 409 — ver [contracts/auth-register.md](contracts/auth-register.md))
- [x] T015 [US3] Implementar step `when("el mismo usuario intenta registrarse nuevamente con el mismo email")` en `AutenticacionApiStepDefinitions.java` — recupera email con `actor.recall("email")` y llama `actor.attemptsTo(IntentarRegistrarEmailDuplicado.con(email, password))`
- [x] T016 [US3] Implementar step `then("el sistema rechaza el registro por conflicto con codigo 409")` en `AutenticacionApiStepDefinitions.java` — `actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(409)))`

**Checkpoint**: Pasos 1, 2 y 3 pasan; 409 confirmado en reporte Serenity.

---

## Phase 6: User Story 4 — Rechazo de login con contraseña incorrecta (Priority: P2)

**Goal**: Automatizar el Paso 4 — `POST /api/auth/login` con contraseña incorrecta resulta en 401 Unauthorized sin emitir token.

**Independent Test**: El escenario completo de 4 pasos pasa — 401 confirmado en Paso 4; los pasos 1–3 siguen verdes.

- [x] T017 [US4] Crear Task `src/test/java/com/ticketing/tasks/IntentarLoginConPasswordIncorrecto.java` — método `IntentarLoginConPasswordIncorrecto.conEmail(String email)`, `performAs` ejecuta POST a `Endpoints.LOGIN` con `AuthPayloadBuilder.loginPayload(email, "wrong-password")` — contraseña fija diferente a la registrada (ver [contracts/auth-login.md](contracts/auth-login.md))
- [x] T018 [US4] Implementar step `when("el usuario intenta iniciar sesion con una contrasena incorrecta")` en `AutenticacionApiStepDefinitions.java` — recupera email con `actor.recall("email")` y llama `actor.attemptsTo(IntentarLoginConPasswordIncorrecto.conEmail(email))`
- [x] T019 [US4] Implementar step `then("el sistema deniega el acceso con codigo 401")` en `AutenticacionApiStepDefinitions.java` — `actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(401)))`

**Checkpoint**: Escenario completo de 4 pasos pasa. Los 4 códigos HTTP (201, 200, 409, 401) verificados en el reporte Serenity.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Calidad, trazabilidad y completitud del reporte.

- [x] T020 [P] Crear Question auxiliar `src/test/java/com/ticketing/questions/ElMensajeDeRespuesta.java` — implementa `Question<String>`, retorna `SerenityRest.lastResponse().body().jsonPath().getString("message")` (disponible para aserciones sobre body de error en 409 y 401 si se decide ampliar cobertura)
- [x] T021 Implementar step `given("que el servicio de autenticacion esta disponible")` en `AutenticacionApiStepDefinitions.java` — cuerpo vacío anotado con `@Step("el servicio de autenticacion esta disponible")`: `public void elServicioEstaDisponible() { }` — documenta la precondición en el reporte Serenity sin lógica adicional
- [x] T022 Ejecutar `./gradlew clean test aggregate` contra el AuthService disponible en `localhost:8003` y validar que el reporte `target/site/serenity/index.html` refleja los 4 pasos del escenario con evidencia HTTP (request/response) de cada llamada REST (ver [quickstart.md](quickstart.md))
- [x] T023 Configurar timeout de conexión ≤ 5 segundos en `src/test/java/com/ticketing/hooks/AutenticacionHooks.java` para que la suite falle rápido cuando el AuthService no está disponible — añadir en `@Before`: `RestAssured.config = RestAssuredConfig.config().connectionConfig(ConnectionConfig.connectionConfig().closeIdleConnectionsAfterEachResponse())` y usar `RestAssured.given().config(RestAssured.config().socketConfig(SocketConfig.socketConfig().socketTimeout(5000)))`, o alternativamente definir `restassured.connection.timeout=5000` en `src/test/resources/serenity.conf` (cubre SC-006 — fallo descriptivo ante no disponibilidad del AuthService)

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup)         → sin dependencias, inicio inmediato
Phase 2 (Foundational)  → depende de Phase 1 · bloquea Phase 3, 4, 5, 6
Phase 3 (US1)           → depende de Phase 2 · bloquea Phase 4 (por cadena del escenario único)
Phase 4 (US2)           → depende de Phase 3 · bloquea Phase 5
Phase 5 (US3)           → depende de Phase 4 · bloquea Phase 6
Phase 6 (US4)           → depende de Phase 5
Phase 7 (Polish)        → depende de Phase 6
```

> **Nota sobre secuencialidad**: A diferencia de proyectos con historias independientes, este flujo es un **único Scenario causal**. Cada paso de implementación depende del paso anterior para que el escenario compile y ejecute correctamente de extremo a extremo. La ejecución secuencial (US1 → US2 → US3 → US4) es la estrategia correcta para este diseño.

### User Story Dependencies

- **US1 (P1)**: Requiere solo Phase 2 completa. Primer punto de valor verificable (201).
- **US2 (P1)**: Requiere US1 completa (el email del Paso 1 se usa en el login). Segundo punto de valor (200 + token).
- **US3 (P2)**: Requiere US2 completa (email debe existir en BD para provocar 409).
- **US4 (P2)**: Requiere US1 completa (email debe existir en BD para provocar 401). Puede implementarse en paralelo con US3 si hay dos implementadores.

### Parallel Opportunities

- **T001 + T002** (Phase 1): Archivos distintos, sin dependencias entre sí → paralelizables.
- **T003 + T004** (Phase 2): Archivos distintos, sin dependencias entre sí → paralelizables.
- **T005 + T006** (Phase 2): Dependen de T001/T002 pero son archivos distintos → paralelizables entre sí.
- **T010** (ElTokenJwt.java): No depende de ninguna Task → paralelizable con T011 inicio.
- **T020** (ElMensajeDeRespuesta.java, Phase 7): Independiente, puede crearse en cualquier momento.
- **US3 y US4** (Phases 5 y 6): T014/T015/T016 y T017/T018/T019 son archivos distintos → paralelizables si hay dos implementadores (ambos están listos tras US2).

---

## Parallel Execution Example

### Phase 1 (Setup) — completar en paralelo

```
Implementador A: T001 → Endpoints.java
Implementador B: T002 → AuthPayloadBuilder.java
```

### Phase 2 (Foundational) — tras Phase 1

```
Paso 2a (paralelo):
  Implementador A: T003 → ElCodigoDeRespuesta.java
  Implementador B: T004 → AutenticacionHooks.java

Paso 2b (paralelo, tras 2a):
  Implementador A: T005 → autenticacion_api.feature
  Implementador B: T006 → AutenticacionApiStepDefinitions.java (skeleton)
```

### Phases 3–6 (User Stories) — secuencial para un implementador, parcialmente paralelo para dos

```
Implementador único:
  T007 → T008 → T009 | T010 → T011 → T012 → T013 | T014 → T015 → T016 | T017 → T018 → T019

Dos implementadores (tras US2):
  Implementador A: T014 → T015 → T016  (US3)
  Implementador B: T017 → T018 → T019  (US4)
```

---

## Implementation Strategy

**MVP Scope** (valor verificable mínimo): Completar solo **Phase 1 + Phase 2 + Phase 3 (US1)** — el escenario ejecuta y confirma el 201 del registro.

**Full Scope**: Phases 1–6 — los 4 pasos del flujo de autenticación verificados de extremo a extremo.

**Orden recomendado para un solo implementador**: T001 → T002 → T003 → T004 → T005 → T006 → T007 → T008 → T009 → T010 → T011 → T012 → T013 → T014 → T015 → T016 → T017 → T018 → T019 → T020 → T021 → T022 → T023

---

## Task Count Summary

| Fase | User Story | Tareas | Archivos nuevos |
|------|-----------|--------|-----------------|
| Phase 1 — Setup | — | 2 (T001–T002) | `Endpoints.java`, `AuthPayloadBuilder.java` |
| Phase 2 — Foundational | — | 4 (T003–T006) | `ElCodigoDeRespuesta.java`, `AutenticacionHooks.java`, `autenticacion_api.feature`, `AutenticacionApiStepDefinitions.java` |
| Phase 3 | US1 | 3 (T007–T009) | `RegistrarUsuario.java` + impl en StepDefs |
| Phase 4 | US2 | 4 (T010–T013) | `IniciarSesionApi.java`, `ElTokenJwt.java` + impl en StepDefs |
| Phase 5 | US3 | 3 (T014–T016) | `IntentarRegistrarEmailDuplicado.java` + impl en StepDefs |
| Phase 6 | US4 | 3 (T017–T019) | `IntentarLoginConPasswordIncorrecto.java` + impl en StepDefs |
| Phase 7 — Polish | — | 4 (T020–T023) | `ElMensajeDeRespuesta.java` + validación + timeout SC-006 |
| **Total** | **4 US** | **23 tareas** | **12 archivos nuevos** |
