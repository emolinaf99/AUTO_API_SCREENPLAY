# AUTO_API_SCREENPLAY Constitution

## I. Stack y Herramientas (NO NEGOCIABLE)

| Componente | Tecnología |
|------------|------------|
| Lenguaje | Java 21 |
| Framework de Automatización | Serenity BDD 4.2.0 |
| Runner de Pruebas | Cucumber 7.18.0 + JUnit 4.13.2 |
| Gestión de Dependencias | Gradle |
| Patrón API | Screenplay + Serenity Rest (RestAssured) |
| Librerías | serenity-screenplay 4.2.0 + serenity-rest-assured 4.2.0 |
| Reporte | Serenity single-page HTML |

Sin dependencias de Selenium ni WebDriver — este repositorio es exclusivamente de pruebas de API REST. No se abre ningún navegador.

---

## II. Patrón de Automatización (NO NEGOCIABLE)

**Screenplay + Serenity Rest:**

```
Actor → attemptTo(CallAnApi.Task) → SerenityRest → Respuesta HTTP
Actor → asksAboutThe(ResponseBodyQuestion) → aserción sobre status / body
```

**Reglas estrictas:**
- **Actor**: tiene habilidad `CallAnApi` (no `BrowseTheWeb`). No se comparte entre escenarios.
- **Task**: encapsula una llamada HTTP con su payload. Una Task = un endpoint + un escenario de respuesta esperada.
- **Question**: consulta el estado de la última respuesta HTTP (status code, campo del body, header). Solo lee, nunca modifica.
- **Token JWT**: se almacena en la memoria del Actor (`actor.remember("token", value)`) entre pasos del mismo escenario. No se usa variable estática ni campo de clase.
- Sin selectores CSS/XPath — todos los localizadores son JSON paths o campos de respuesta.

---

## III. Arquitectura de Carpetas (NO NEGOCIABLE)

```
src/test/
├── java/com/ticketing/
│   ├── tasks/               ← Tasks HTTP (una por endpoint + caso)
│   │   ├── RegistrarUsuario.java
│   │   ├── IniciarSesionApi.java
│   │   └── [ExistingApiTasks].java
│   ├── questions/           ← Questions sobre la respuesta HTTP
│   │   ├── ElCodigoDeRespuesta.java
│   │   ├── ElTokenJwt.java
│   │   └── ElMensajeDeRespuesta.java
│   ├── stepdefinitions/     ← Step Definitions por feature
│   │   ├── AutenticacionApiStepDefinitions.java
│   │   └── CrudEventosStepDefinitions.java
│   ├── runners/             ← CucumberTestRunner
│   │   └── CucumberTestRunner.java
│   ├── hooks/               ← @Before / @After (setup Actor)
│   └── util/                ← Builders de payload, constantes de endpoints
└── resources/
    ├── features/
    │   ├── crud_eventos.feature           ← existente, NO modificar
    │   └── autenticacion_api.feature      ← nueva — feature de auth API
    ├── serenity.conf
    └── logback-test.xml
```

---

## IV. Convenciones de Código

**Tasks HTTP:**
- Nombre: verbo de negocio → `RegistrarUsuario`, `IniciarSesionApi`, `IntentarRegistrarEmailDuplicado`
- Método de fábrica estático con los datos necesarios: `RegistrarUsuario.con(nombre, apellido, email, password)`
- Usan `SerenityRest.given().body(payload).post(endpoint)` internamente
- No hacen aserciones — solo ejecutan la llamada y almacenan el resultado si es necesario

**Questions HTTP:**
- `ElCodigoDeRespuesta` → retorna `Integer` del último response
- `ElTokenJwt` → extrae `token` del body JSON del último response
- `ElMensajeDeRespuesta` → extrae campo `message` del body JSON

**Endpoints — constantes en `util/Endpoints.java`:**
```java
static final String AUTH_BASE    = "http://localhost:8003";
static final String REGISTER     = AUTH_BASE + "/api/auth/register";
static final String LOGIN        = AUTH_BASE + "/api/auth/login";
static final String EVENTS_BASE  = "http://localhost:8002";
```

**Gherkin:**
- Escenarios en español, declarativos
- Describe el flujo de negocio, no los detalles HTTP (no mencionar "POST" en el Gherkin)
- Cada escenario independiente — precondiciones de estado (ej. usuario registrado) se crean en el mismo escenario o en `@Before`

---

## V. Servicios Bajo Prueba

| Servicio | URL base |
|----------|----------|
| AuthService | `http://localhost:8003` |
| EventsService (CRUD) | `http://localhost:8002` |

Configuración en `serenity.conf`:
```
serenity.project.name = "AUTO_API_SCREENPLAY"
```
Sin configuración de WebDriver (no hay browser).

---

## VI. Scope de Esta Feature — Autenticación API

**Dentro del alcance de este repositorio:**

El flujo de autenticación se implementa como **un escenario con 4 pasos** que simula el ciclo de vida completo del recurso sesión. Los 4 pasos usan `POST` (auth solo expone POST) — permitido explícitamente por la rúbrica cuando no existen los 4 verbos REST.

| Paso | CP | Endpoint | Resultado esperado |
|------|----|----------|--------------------|
| 1 — Crear usuario | CP-HU1-01 | `POST /api/auth/register` | 201 Created |
| 2 — Autenticar | CP-HU2-01 | `POST /api/auth/login` | 200 OK + token JWT |
| 3 — Conflicto unicidad | CP-HU1-08 | `POST /api/auth/register` (mismo email) | 409 Conflict |
| 4 — Acceso denegado | CP-HU2-06 | `POST /api/auth/login` (email no existe) | 401 Unauthorized |

**CPs de seguridad/límites también cubribles en este repo (opcionales):**

| CP | Descripción |
|----|-------------|
| CP-HU2-09 | Tercer intento fallido → 423 Locked (requiere 2 llamadas de precondición) |
| CP-HU2-11 | Email > 255 chars → 400 Bad Request |
| CP-HU2-12 | Inyección SQL en email → rechazado sin exponer DB |

**Fuera del alcance de este repositorio:**
- Pruebas UI con navegador → AUTO_FRONT_POM_FACTORY / AUTO_FRONT_SCREENPLAY
- CP-HU1-09 (bcrypt) → prueba unitaria del backend .NET, no verificable desde API externa
- CP-HU2-13/14 (JWT persistencia/expiración) → estado de cliente, no de API

---

## VII. Reglas de Calidad (NO NEGOCIABLE)

- **Sin Selenium**: ninguna clase del proyecto debe importar `org.openqa.selenium`. Verificar en code review.
- **Token via Actor memory**: el token JWT obtenido en el paso de login se pasa entre steps usando `actor.remember("token", token)` y `actor.recall("token")`. Prohibido usar variables estáticas.
- **SRP en Tasks**: una Task = una llamada HTTP con un propósito claro. Si un escenario requiere dos endpoints, usa dos Tasks distintas.
- **Independencia de escenarios**: cada escenario crea sus propios datos de prueba (email único por ejecución, ej. con `UUID`). Ningún escenario reutiliza datos del anterior.
- **Sin código comentado**: ausencia total de código comentado. Sin bloques `/* */` deshabilitados.
- **Nomenclatura semántica**: nombres que expresen intención de negocio, no detalles HTTP.

---

## VIII. Definition of Done

Una tarea está completa cuando:

- El escenario Gherkin compila y ejecuta sin errores contra los servicios levantados
- Cada Task encapsula exactamente una llamada HTTP
- El token JWT se propaga correctamente entre pasos via `actor.remember/recall`
- Los status codes están afirmados en Questions, no en Tasks
- El escenario es independiente (datos únicos por ejecución)
- El reporte Serenity HTML muestra los pasos HTTP con request/response
- Sin código comentado en ninguna clase
- `./gradlew clean test aggregate` pasa sin errores de compilación

---

## Governance

Esta constitución extiende el proyecto existente sin contradecirlo. El repositorio ya cubre el CRUD de eventos (`crud_eventos.feature`) — esa feature se conserva intacta. La feature de autenticación API se agrega como extensión independiente en `autenticacion_api.feature`.

**Version**: 1.0.0 | **Ratified**: 2026-04-08 | **Last Amended**: 2026-04-08
