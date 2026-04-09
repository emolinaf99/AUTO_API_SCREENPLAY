# Implementation Plan: Automatización REST del AuthService con Screenplay + Serenity Rest

**Branch**: `001-screenplay-authservice-rest` | **Date**: 2026-04-08 | **Spec**: [spec.md](spec.md)

## Summary

Implementar un escenario Cucumber con cuatro pasos secuenciales que automatizan el ciclo completo de autenticación del AuthService (`localhost:8003`) utilizando el patrón Screenplay con Serenity Rest. Los pasos validan: registro exitoso (201), login exitoso con captura de token JWT (200), registro duplicado (409) y login con contraseña incorrecta (401). El token JWT se propaga entre pasos mediante `actor.remember/recall`. Cada ejecución genera un email único por UUID para garantizar independencia.

---

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Serenity BDD 4.2.0, serenity-screenplay 4.2.0, serenity-rest-assured 4.2.0 (RestAssured), Cucumber 7.18.0, JUnit 4.13.2 — todos ya presentes en `build.gradle`  
**Storage**: N/A — suite de pruebas API sin persistencia local  
**Testing**: Gradle (`./gradlew clean test aggregate`)  
**Target Platform**: JVM / Linux — servicio en `http://localhost:8003`  
**Project Type**: API test automation suite (extensión de proyecto existente)  
**Performance Goals**: 4 pasos completados en < 10 segundos totales en red local  
**Constraints**: Sin Selenium, sin WebDriver, sin browser. Token JWT via `actor.remember/recall` únicamente. `localhost:8003` debe estar disponible antes de ejecutar.  
**Scale/Scope**: 1 Feature, 1 Scenario, ~10 clases Java nuevas

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Regla | Estado | Justificación |
|---|---|---|
| Sin Selenium ni WebDriver | ✅ PASS | No se importa `org.openqa.selenium` en ninguna clase. Solo `SerenityRest`. |
| Actor tiene `CallAnApi`, no `BrowseTheWeb` | ✅ PASS | Todos los Actores usan `actor.can(CallAnApi.at(Endpoints.AUTH_BASE))` |
| Token JWT via `actor.remember/recall` | ✅ PASS | `IniciarSesionApi` almacena el token; ninguna variable estática ni campo de clase |
| SRP en Tasks (una Task = un endpoint + caso) | ✅ PASS | 4 Tasks independientes: `RegistrarUsuario`, `IniciarSesionApi`, `IntentarRegistrarEmailDuplicado`, `IntentarLoginConPasswordIncorrecto` |
| Email único por ejecución con UUID | ✅ PASS | `UUID.randomUUID()` en `@Before` hook, almacenado en actor |
| Sin código comentado | ✅ PASS | Verificar en code review |
| Nomenclatura semántica (negocio) | ✅ PASS | Nombres expresan intención, no detalles HTTP |
| Feature existente `crud_eventos.feature` intacta | ✅ PASS | Solo se añade `autenticacion_api.feature`; no se toca ningún archivo existente |
| `serenity.conf` sin WebDriver | ✅ PASS | Confirmado — solo `project.name`, encoding, screenshots |
| Definition of Done alcanzable | ✅ PASS | Todos los criterios son verificables con `./gradlew clean test aggregate` |

**RESULTADO: GATE PASS — proceder a fases de diseño**

---

## Project Structure

### Documentation (this feature)

```text
specs/001-screenplay-authservice-rest/
├── plan.md              ← este archivo (Phase 0+1 output)
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── auth-register.md ← Phase 1 output
│   └── auth-login.md    ← Phase 1 output
└── tasks.md             ← Phase 2 output (/speckit.tasks — NO creado aquí)
```

### Source Code (repository root)

```text
src/test/
├── java/com/ticketing/
│   ├── util/
│   │   ├── Endpoints.java                          ← NUEVO: constantes de URL
│   │   └── AuthPayloadBuilder.java                 ← NUEVO: builders de payload JSON
│   ├── tasks/
│   │   ├── RegistrarUsuario.java                   ← NUEVO: POST /api/auth/register (201)
│   │   ├── IniciarSesionApi.java                   ← NUEVO: POST /api/auth/login (200 + token)
│   │   ├── IntentarRegistrarEmailDuplicado.java    ← NUEVO: POST /api/auth/register (409)
│   │   └── IntentarLoginConPasswordIncorrecto.java ← NUEVO: POST /api/auth/login (401)
│   ├── questions/
│   │   ├── ElCodigoDeRespuesta.java                ← NUEVO: status code del último response
│   │   ├── ElTokenJwt.java                         ← NUEVO: campo "token" del body JSON
│   │   └── ElMensajeDeRespuesta.java               ← NUEVO: campo "message" del body JSON
│   ├── hooks/
│   │   └── AutenticacionHooks.java                 ← NUEVO: @Before crea actor con UUID
│   ├── stepdefinitions/
│   │   ├── CrudEventosStepDefinitions.java         ← EXISTENTE — NO modificar
│   │   └── AutenticacionApiStepDefinitions.java    ← NUEVO: step defs del flujo de auth
│   └── runners/
│       └── CucumberTestRunner.java                 ← EXISTENTE — NO modificar
└── resources/
    ├── features/
    │   ├── crud_eventos.feature                    ← EXISTENTE — NO modificar
    │   └── autenticacion_api.feature               ← NUEVA: escenario de autenticación
    ├── serenity.conf                               ← EXISTENTE — NO modificar
    └── logback-test.xml                            ← EXISTENTE — NO modificar
```

**Structure Decision**: Extensión de proyecto existente (Option 1 — single project). Se añaden clases exclusivamente en paquetes vacíos ya previstos por la arquitectura del proyecto (`tasks/`, `questions/`, `hooks/`, `util/`). No se modifica ningún archivo existente.

---

## Implementation Phases

### Phase 1: Foundation — Util & Infrastructure (T1–T2)

**T1 — `util/Endpoints.java`**
- Constantes de URL para AuthService y EventsService
- `AUTH_BASE = "http://localhost:8003"`, `REGISTER`, `LOGIN`, `EVENTS_BASE`

**T2 — `util/AuthPayloadBuilder.java`**
- Método estático `registerPayload(firstName, lastName, email, password)` → `Map<String,String>` con 5 campos (`confirmPassword` = mismo valor que `password`)
- Método estático `loginPayload(email, password)` → `Map<String,String>` con 2 campos

---

### Phase 2: Tasks HTTP (T3–T6)

**T3 — `tasks/RegistrarUsuario.java`**
- `RegistrarUsuario.con(String nombre, String apellido, String email, String password)`
- Internamente: `SerenityRest.given().contentType(ContentType.JSON).body(AuthPayloadBuilder.registerPayload(...)).post(Endpoints.REGISTER)`
- No hace aserciones. Guarda el email en la memoria del actor: `actor.remember("email", email)`

**T4 — `tasks/IniciarSesionApi.java`**
- `IniciarSesionApi.con(String email, String password)`
- POST a `Endpoints.LOGIN` con `loginPayload`
- Extrae el token del response body y lo almacena: `actor.remember("token", token)`

**T5 — `tasks/IntentarRegistrarEmailDuplicado.java`**
- `IntentarRegistrarEmailDuplicado.con(String email, String password)`
- Mismo endpoint que T3, mismo email (recuperado con `actor.recall("email")`)
- No almacena nada — solo provoca el 409

**T6 — `tasks/IntentarLoginConPasswordIncorrecto.java`**
- `IntentarLoginConPasswordIncorrecto.conEmail(String email)`
- POST a `Endpoints.LOGIN` con email correcto + contraseña fija incorrecta (`"wrong-password"`)
- No almacena nada — solo provoca el 401

---

### Phase 3: Questions HTTP (T7–T9)

**T7 — `questions/ElCodigoDeRespuesta.java`**
- Implementa `Question<Integer>`
- `SerenityRest.lastResponse().statusCode()`

**T8 — `questions/ElTokenJwt.java`**
- Implementa `Question<String>`
- `SerenityRest.lastResponse().body().jsonPath().getString("token")`

**T9 — `questions/ElMensajeDeRespuesta.java`**
- Implementa `Question<String>`
- `SerenityRest.lastResponse().body().jsonPath().getString("message")`

---

### Phase 4: Hooks & Step Definitions (T10–T11)

**T10 — `hooks/AutenticacionHooks.java`**
- `@Before(order=0)` — crea el actor con habilidad `CallAnApi.at(Endpoints.AUTH_BASE)`
- Genera email único: `String email = "user-" + UUID.randomUUID() + "@test.com"`
- Almacena el email en el actor: `actor.remember("email", email)`
- `@After` — limpieza de Serenity si es necesaria

**T11 — `stepdefinitions/AutenticacionApiStepDefinitions.java`**
- Inyecta el actor desde el hook (vía `@SharedObjects` o campo de instancia)
- Implementa los 9 step methods del Gherkin que cubren los 4 pasos del flujo

---

### Phase 5: Gherkin Feature (T12)

**T12 — `resources/features/autenticacion_api.feature`**

```gherkin
Feature: Ciclo de autenticacion en el AuthService
  Como ingeniero de calidad
  Quiero validar el flujo completo de autenticacion
  Para garantizar que el ciclo de vida del servicio de autenticacion funciona correctamente

  Scenario: Ciclo completo de autenticacion — registro, login, duplicado y acceso denegado
    Given que el servicio de autenticacion esta disponible
    When un nuevo usuario se registra con datos validos
    Then el sistema confirma la creacion del usuario con codigo 201
    When el usuario inicia sesion con sus credenciales correctas
    Then el sistema devuelve un token de acceso valido con codigo 200
    When el mismo usuario intenta registrarse nuevamente con el mismo email
    Then el sistema rechaza el registro por conflicto con codigo 409
    When el usuario intenta iniciar sesion con una contrasena incorrecta
    Then el sistema deniega el acceso con codigo 401
```

---

## Cross-Reference: plan.md → tasks.md

Las 12 tareas de este plan (T1–T12, agrupadas en 5 fases de diseño) se expanden y renumeran en [tasks.md](tasks.md) como **T001–T022**, reorganizadas en 7 fases orientadas a user story para permitir implementación y verificación incremental. La correspondencia aproximada es:

| Plan | Tasks |
|------|-------|
| Phase 1 (T1–T2) | Phase 1: T001–T002 |
| Phase 2 (T3–T6) | Phase 3: T007–T009 (US1) + Phase 4: T010–T013 (US2) |
| Phase 3 (T7–T9) | Phase 2 Foundational: T003 + Phase 4: T010 |
| Phase 4 (T10–T11) | Phase 2 Foundational: T004–T006 |
| Phase 5 (T12) | Phase 5–6: T014–T019 (US3–US4) + Phase 7: T020–T023 |

---

## Complexity Tracking

No hay violaciones de Constitution. No se requiere justificación de complejidad añadida.

---

## Risks & Mitigations

| Riesgo | Probabilidad | Mitigación |
|--------|-------------|------------|
| AuthService no disponible en `localhost:8003` | Media | Fallo rápido en el Paso 1; mensaje de error descriptivo de ConnectionRefused |
| El campo `token` del login response cambia de nombre | Baja | Definido en constitución y contrato confirmado; documentado en `ElTokenJwt` |
| `[Compare]` del backend rechaza `password != confirmPassword` | Baja | `AuthPayloadBuilder.registerPayload` siempre iguala ambos campos |
| Runner no descubre `autenticacion_api.feature` | Muy baja | Runner ya escanea el directorio completo `src/test/resources/features` |
| `AutenticacionHooks` interfiere con `CrudEventosStepDefinitions` | Baja | El glue de Cucumber comparte hooks — marcar `@Before` con tag específico `@auth` si hay interferencia |
