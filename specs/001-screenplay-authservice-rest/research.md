# Research: Automatización REST del AuthService con Screenplay + Serenity Rest

**Feature**: 001-screenplay-authservice-rest  
**Date**: 2026-04-08  
**Phase**: 0 — Outline & Research

---

## 1. Estado del Build — Dependencias

**Pregunta**: ¿Hay dependencias Serenity/RestAssured que añadir al `build.gradle`?

**Hallazgo**: El `build.gradle` ya incluye todas las dependencias necesarias:

```gradle
testImplementation "net.serenity-bdd:serenity-core:4.2.0"
testImplementation "net.serenity-bdd:serenity-screenplay:4.2.0"
testImplementation "net.serenity-bdd:serenity-rest-assured:4.2.0"
testImplementation "io.cucumber:cucumber-java:7.18.0"
testImplementation "junit:junit:4.13.2"
```

**Decisión**: No añadir ninguna dependencia.  
**Rationale**: El proyecto fue configurado precisamente para este stack.  
**Alternativas consideradas**: serenity-rest-assured-jackson (innecesario; RestAssured incluye Jackson por defecto en serenity-rest-assured 4.x).

---

## 2. Configuración del Runner (CucumberTestRunner)

**Pregunta**: ¿El runner existente necesita modificarse para descubrir la nueva feature?

**Hallazgo**:

```java
@CucumberOptions(
    features = "src/test/resources/features",    // ← directorio, no archivo específico
    glue = "com.ticketing.stepdefinitions"       // ← paquete completo
)
```

El runner ya apunta a:
- El directorio `features/` completo → descubrirá `autenticacion_api.feature` automáticamente.
- El paquete `com.ticketing.stepdefinitions` → descubrirá `AutenticacionApiStepDefinitions.java` automáticamente.

**Decisión**: No modificar `CucumberTestRunner.java`.  
**Rationale**: Los nuevos archivos caen dentro del scope ya configurado.  
**Alternativas consideradas**: Añadir tag `@CucumberOptions(tags = "@auth")` para aislar la suite de auth — viable pero innecesario en esta iteración.

---

## 3. Configuración serenity.conf

**Pregunta**: ¿Necesita actualización `serenity.conf` para soporte REST?

**Hallazgo**:

```
serenity {
    project.name = "AUTO_API_SCREENPLAY"
    encoding = "UTF-8"
    ...
    take.screenshots = FOR_FAILURES
}
```

- Sin configuración de WebDriver → correcto, no hay browser.
- `serenity-rest-assured 4.x` no requiere configuración adicional en `serenity.conf` para funcionar con `SerenityRest.given()`.
- La URL base (`localhost:8003`) se gestiona en código (constante `Endpoints.AUTH_BASE`), no en configuración.

**Decisión**: No modificar `serenity.conf`.  
**Rationale**: La configuración actual es compatible y suficiente.  
**Alternativas consideradas**: `restassured.baseURI` en serenity.conf — innecesario si se usa `SerenityRest.given()...post(fullUrl)`.

---

## 4. Patrón SerenityRest + Screenplay — Best Practices

**Pregunta**: ¿Cómo se integra correctamente `SerenityRest` con el patrón Screenplay en Serenity 4.2.0?

**Hallazgo** (basado en documentación Serenity BDD 4.x + constitution del proyecto):

### Habilidad del Actor
```java
Actor actor = Actor.named("Usuario Test")
    .whoCan(CallAnApi.at("http://localhost:8003"));
```

### Patrón Task
```java
public class RegistrarUsuario implements Task {
    // campos finales del payload...
    
    public static RegistrarUsuario con(String nombre, String apellido, String email, String password) {
        return instrumented(RegistrarUsuario.class, nombre, apellido, email, password);
    }

    @Override
    @Step("{0} registra un nuevo usuario con email #email")
    public <T extends Actor> void performAs(T actor) {
        SerenityRest.given()
            .contentType(ContentType.JSON)
            .body(AuthPayloadBuilder.registerPayload(nombre, apellido, email, password))
            .post(Endpoints.REGISTER);
        actor.remember("email", email);
    }
}
```

### Patrón Question
```java
public class ElCodigoDeRespuesta implements Question<Integer> {
    public static ElCodigoDeRespuesta delUltimoLlamado() {
        return new ElCodigoDeRespuesta();
    }
    
    @Override
    public Integer answeredBy(Actor actor) {
        return SerenityRest.lastResponse().statusCode();
    }
}
```

### remember/recall entre pasos
```java
// En IniciarSesionApi.performAs:
String token = SerenityRest.lastResponse().body().jsonPath().getString("token");
actor.remember("token", token);

// En paso posterior:
String tokenRecuperado = actor.recall("token");
```

**Decisión**: Usar `instrumented()` de `Tasks.instrumented()` para todas las Tasks (habilita el reporte Serenity).  
**Rationale**: `instrumented()` registra el paso en el reporte HTML con los parámetros interpolados.  
**Alternativas consideradas**: implementación directa con `new` — funciona pero no genera evidencia en el reporte Serenity.

---

## 5. Generación de Email Único por Ejecución

**Pregunta**: ¿Cómo garantizar unicidad de email sin contaminar entre ejecuciones?

**Hallazgo**: `UUID.randomUUID()` de `java.util.UUID` — disponible sin dependencias adicionales en Java 21.

**Decisión**: `"user-" + UUID.randomUUID() + "@test.com"` generado en `@Before` hook y almacenado con `actor.remember("email", email)`.

**Rationale**: 
- Garantiza unicidad estadísticamente perfecta (2^122 posibilidades).
- Formato válido para la mayoría de servicios de email.
- Sin necesidad de registro en Teardown — cada ejecución crea un usuario nuevo.

**Alternativas consideradas**: `System.currentTimeMillis()` como sufijo — colisiona en ejecuciones paralelas. `Faker` library — dependencia innecesaria para este propósito.

---

## 6. Compartir Actor entre Hook y Step Definitions

**Pregunta**: ¿Cómo comparte el actor entre `AutenticacionHooks` y `AutenticacionApiStepDefinitions` en Cucumber 7 + Serenity?

**Hallazgo**: En Cucumber 7, las clases de glue (hooks y step definitions) son instanciadas por el mismo contenedor IoC. Con Serenity + Cucumber, el mecanismo recomendado es:

**Opción A — PicoContainer (más limpio)**: Inyectar el actor como dependencia compartida vía constructor:
```java
// Clase compartida
public class SharedActorContext {
    public Actor actor;
}

// En hooks
public AutenticacionHooks(SharedActorContext ctx) { this.ctx = ctx; }

// En steps
public AutenticacionApiStepDefinitions(SharedActorContext ctx) { this.ctx = ctx; }
```

**Opción B — Campo estático (más simple pero viola constitution)**: `private static Actor actor` — PROHIBIDO por la constitution (regla: "Prohibido usar variables estáticas").

**Opción C — OnStage/TheScreenplay**: `OnStage.setTheStage(new Cast())` y `OnStage.theActorCalled("Usuario")` — patrón nativo de Serenity.

**Decisión**: Usar `OnStage` / `Cast` + `OnStage.theActorCalled()` que es el mecanismo nativo de Serenity Screenplay:
```java
// @Before hook:
OnStage.setTheStage(Cast.whereEveryoneCan(CallAnApi.at(Endpoints.AUTH_BASE)));
Actor actor = OnStage.theActorCalled("Usuario Test");

// En steps:
Actor actor = OnStage.theActorInTheSpotlight();
```

**Rationale**: Patrón nativo Serenity, sin dependencias adicionales, sin variables estáticas, sin violaciones de constitution.

---

## 7. Contratos AuthService — Confirmación

**Pregunta**: ¿Los contratos de request/response son los correctos basados en el codebase?

**Hallazgo** (confirmado en Clarifications de la spec):

| Endpoint | Request | Response (éxito) | Response (error) |
|----------|---------|-----------------|-----------------|
| `POST /api/auth/register` | `RegisterUserRequest`: `{ firstName, lastName, email, password, confirmPassword }` | 201 Created | 409 Conflict |
| `POST /api/auth/login` | `LoginUserRequest`: `{ email, password }` | 200 OK + `LoginUserResponse`: `{ token, expiresIn }` | 401 Unauthorized |

**Decisión**: Usar exactamente estos contratos.  
**Rationale**: Definidos en el AuthService y confirmados por el usuario en la sesión de clarificación.

---

## Resumen de Decisiones

| # | Decisión | Rationale |
|---|----------|-----------|
| 1 | No añadir dependencias a `build.gradle` | Todas presentes |
| 2 | No modificar `CucumberTestRunner.java` | Auto-descubrimiento ya configurado |
| 3 | No modificar `serenity.conf` | Compatible sin cambios |
| 4 | Usar `Tasks.instrumented()` en todas las Tasks | Genera evidencia en reporte Serenity |
| 5 | UUID para email único en `@Before` hook | Sin colisiones, sin dependencias |
| 6 | Usar `OnStage` / `Cast` para compartir actor | Patrón nativo Serenity, sin variables estáticas |
| 7 | Contratos confirmados: 5 campos register, 2 campos login, `token` en response | Verificado con usuario |
