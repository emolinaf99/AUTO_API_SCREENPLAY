# Tasks: API Automation CRUD Flow with Screenplay

**Branch**: `001-crud-screenplay` | **Date**: 2026-03-20
**Plan**: `.specify/specs/001-crud-screenplay/plan.md`

---

## Phase 1 — Project Setup

### [X] T001 — Crear estructura de directorios del proyecto
- Crear `src/test/java/runners/`
- Crear `src/test/java/steps/`
- Crear `src/test/java/screenplay/api/`
- Crear `src/test/java/screenplay/tasks/`
- Crear `src/test/java/screenplay/questions/`
- Crear `src/test/resources/features/crud/`

### [X] T002 — Crear `build.gradle`
- Archivo: `build.gradle`
- Plugins: `java`, `net.serenity-bdd.serenity-gradle-plugin 4.2.34`
- Dependencies: `serenity-core`, `serenity-junit`, `serenity-cucumber`, `serenity-screenplay`, `serenity-screenplay-rest:4.2.34`, `serenity-rest-assured:4.2.34`, `junit:4.13.2`, `assertj-core:3.25.3`
- `gradle.startParameter.continueOnFailure = true`
- `test.finalizedBy(aggregate)`

### [X] T003 — Crear `serenity.conf`
- Archivo: `src/test/resources/serenity.conf`
- `serenity.project.name = "AUTO_API_SCREENPLAY"`
- `serenity.test.root = "runners"`
- `environments.default.api.crud.url = "http://localhost:8002"`

### [X] T004 — Crear archivo feature
- Archivo: `src/test/resources/features/crud/ciclo-crud-eventos.feature`
- `#language: es`
- 1 escenario con tag `@crud @critico`
- Secuencia: POST → GET → PUT → DELETE en pasos Gherkin explícitos
- Cada verbo tiene `Cuando` + `Entonces` para acción y validación

---

## Phase 2 — Foundation

### [X] T005 — Crear `CucumberTestRunner`
- Archivo: `src/test/java/runners/CucumberTestRunner.java`
- `@RunWith(CucumberWithSerenity.class)`
- `@CucumberOptions(features = "src/test/resources/features", glue = "steps", plugin = {"pretty"})`
- Sin lógica de negocio en el runner

---

## Phase 3 — Screenplay Infrastructure

### [X] T006 — Crear `EventsApiMap`
- Archivo: `src/test/java/screenplay/api/EventsApiMap.java`
- Constante `EVENTS_ENDPOINT = "/api/Events"`
- Constante `EVENT_BY_ID_ENDPOINT = "/api/Events/{id}"`
- Solo constantes de endpoints — sin lógica de negocio

### [X] T007 — Crear `PostEvent` (Task)
- Archivo: `src/test/java/screenplay/tasks/PostEvent.java`
- Implementa `Performable`
- Body de creación: `name`, `startsAt` con fecha futura
- `actor.attemptsTo(Post.to(EventsApiMap.EVENTS_ENDPOINT).with(requestSpec -> requestSpec.body(eventBody).contentType("application/json")))`
- Método estático factory: `PostEvent.withValidData()`

### [X] T008 — Crear `GetEvent` (Task)
- Archivo: `src/test/java/screenplay/tasks/GetEvent.java`
- Implementa `Performable`
- Recibe `eventId` como parámetro
- `actor.attemptsTo(Get.resource(EventsApiMap.EVENT_BY_ID_ENDPOINT).with(requestSpec -> requestSpec.pathParam("id", eventId)))`
- Método estático factory: `GetEvent.withId(String eventId)`

### [X] T009 — Crear `UpdateEvent` (Task)
- Archivo: `src/test/java/screenplay/tasks/UpdateEvent.java`
- Implementa `Performable`
- Recibe `eventId` como parámetro
- Body de actualización con `name` modificado y `startsAt` actualizado
- `actor.attemptsTo(Put.to(EventsApiMap.EVENT_BY_ID_ENDPOINT).with(requestSpec -> requestSpec.pathParam("id", eventId).body(updatedBody).contentType("application/json")))`
- Método estático factory: `UpdateEvent.withId(String eventId)`

### [X] T010 — Crear `DeleteEvent` (Task)
- Archivo: `src/test/java/screenplay/tasks/DeleteEvent.java`
- Implementa `Performable`
- Recibe `eventId` como parámetro
- `actor.attemptsTo(Delete.from(EventsApiMap.EVENT_BY_ID_ENDPOINT).with(requestSpec -> requestSpec.pathParam("id", eventId)))`
- Método estático factory: `DeleteEvent.withId(String eventId)`

### [X] T011 — Crear `TheResponseStatusCode` (Question)
- Archivo: `src/test/java/screenplay/questions/TheResponseStatusCode.java`
- Implementa `Question<Integer>`
- Retorna `SerenityRest.lastResponse().statusCode()`
- Método estático factory: `TheResponseStatusCode.ofTheLastResponse()`

### [X] T012 — Crear `TheEventData` (Question)
- Archivo: `src/test/java/screenplay/questions/TheEventData.java`
- Implementa `Question<String>`
- Extrae campo del body: `SerenityRest.lastResponse().body().path(field)`
- Método estático factory: `TheEventData.field(String fieldName)`
- Crear además `src/test/java/screenplay/questions/TheLastCreatedEventId.java`
- Implementa `Question<String>`
- Extrae el `id` de `SerenityRest.lastResponse().body().path("id")`
- Método estático factory: `TheLastCreatedEventId.value()`

---

## Phase 4 — Steps & Glue

### [X] T013 — Crear `CrudSteps`
- Archivo: `src/test/java/steps/CrudSteps.java`
- `@Before`: `OnStage.setTheStage(new OnlineCast())`; actor con `CallAnApi.at(crudBaseUrl)` donde `crudBaseUrl` proviene de `EnvironmentVariables`
- Actor: `Actor automatizador = OnStage.theActorCalled("Automatizador")`
- Mapear todos los pasos Gherkin del feature con `@Dado`, `@Cuando`, `@Entonces`
- `CrudSteps` solo orquesta Tasks y Questions; no lee `SerenityRest` directamente
- POST step: `automatizador.attemptsTo(PostEvent.withValidData())`
- POST validation: `automatizador.should(seeThat(TheResponseStatusCode.ofTheLastResponse(), equalTo(201)))`
- POST validation: `automatizador.should(seeThat(TheLastCreatedEventId.value(), not(emptyOrNullString())))`
- POST state: `automatizador.remember("eventId", automatizador.asksFor(TheLastCreatedEventId.value()))`
- GET step: `automatizador.attemptsTo(GetEvent.withId(automatizador.recall("eventId")))`
- GET validation: `automatizador.should(seeThat(TheResponseStatusCode.ofTheLastResponse(), equalTo(200)))`
- GET validation: `automatizador.should(seeThat(TheEventData.field("name"), equalTo("nombre creado")))`
- GET validation: `automatizador.should(seeThat(TheEventData.field("availableTickets"), equalTo("0")))`
- PUT step: `automatizador.attemptsTo(UpdateEvent.withId(automatizador.recall("eventId")))`
- PUT validation: `automatizador.should(seeThat(TheResponseStatusCode.ofTheLastResponse(), equalTo(200)))`
- PUT validation: `automatizador.should(seeThat(TheEventData.field("name"), equalTo("nombre actualizado")))`
- DELETE step: `automatizador.attemptsTo(DeleteEvent.withId(automatizador.recall("eventId")))`
- DELETE validation: `automatizador.should(seeThat(TheResponseStatusCode.ofTheLastResponse(), equalTo(204)))`

---

## Phase 5 — Verification & Polish

### [X] T014 — Verificar disponibilidad del CrudService
- Precondición operativa: confirmar que `http://localhost:8002/api/events` responde antes de ejecutar tests
- Alternativa de verificación manual: `curl http://localhost:8002/api/events` retorna 200

Resultado: el servicio real quedó verificado en `http://localhost:8002` con contrato funcional para el flujo CRUD.

### [X] T015 — Ejecutar el escenario CRUD completo
- Comando: `./gradlew test aggregate`
- Verificar PASS en reporte Serenity
- Confirmar 4 operaciones CRUD con código HTTP correcto y 8 pasos Gherkin ejecutados correctamente

Resultado: ejecución PASS contra el backend real con reporte Serenity generado.

### [X] T016 — Verificar independencia del escenario
- Ejecutar el escenario dos veces consecutivas: `./gradlew test aggregate && ./gradlew test aggregate`
- Confirmar PASS en ambas ejecuciones (el escenario crea y elimina su propio recurso)

Resultado: PASS en ambas ejecuciones consecutivas.

### [X] T018 — Verificar reporte Serenity
- Abrir `target/site/serenity/index.html`
- Confirmar 1/1 escenarios PASS
- Confirmar que cada step muestra el request/response HTTP en el reporte

Resultado: reporte PASS con detalle HTTP de POST, GET, PUT y DELETE contra el backend real.

---

## Summary

| Phase | Tasks | Key Deliverable |
|---|---|---|
| Setup | T001–T004 | Estructura + feature file |
| Foundation | T005 | CucumberTestRunner |
| Screenplay | T006–T012 | API Map + 4 Tasks + 2 Questions |
| Steps | T013 | CrudSteps con propagación de ID |
| Verification | T014–T016, T018 | 1 escenario CRUD PASS + independencia confirmada |
