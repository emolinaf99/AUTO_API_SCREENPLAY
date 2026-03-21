# Implementation Plan: API Automation CRUD Flow with Screenplay

**Branch**: `001-crud-screenplay` | **Date**: 2026-03-20 | **Spec**: `.specify/specs/001-crud-screenplay/spec.md`
**App context**: `.specify/memory/app-context.md`

## Summary

Implementar un proyecto de automatización de API sobre el CrudService REST usando
Serenity BDD + Serenity Rest con patrón Screenplay (Actors/Tasks/Questions/SRP).
La automatización cubrirá exactamente 1 escenario Gherkin con flujo CRUD completo:

- **US1 (ciclo CRUD)**: POST → GET → PUT → DELETE sobre `/api/Events` en localhost:8002

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Serenity BDD 4.2.34, Serenity Rest, Serenity Screenplay Rest, JUnit 4.13.2, Cucumber
**Storage**: N/A
**Testing**: JUnit 4 + Cucumber (`CucumberWithSerenity`) + Serenity aggregate reports
**Target Platform**: CrudService real en `http://localhost:8002`
**Project Type**: Test automation — API REST
**Constraints**: 1 escenario CRUD en 1 `.feature`, `CallAnApi` obligatorio, sin RestAssured directo en Steps, sin código comentado
**Scale/Scope**: 1 API Map (`EventsApiMap`), 4 Tasks (PostEvent/GetEvent/UpdateEvent/DeleteEvent), 3 Questions, 1 Steps class, 1 Runner

## Constitution Check

- ✅ I. Spec-First: `spec.md` aprobado y alineado al contrato REST real antes de crear código
- ✅ II. Java + Serenity BDD + Serenity Rest: stack confirmado
- ✅ III. Screenplay + Serenity Rest: Tasks SRP por verbo HTTP, Questions para validaciones
- ✅ IV. Código Limpio: URLs en serenity.conf, sin lógica HTTP en Steps
- ✅ V. Flujo CRUD Completo: POST→GET→PUT→DELETE en secuencia con propagación de ID

## Project Structure

```text
src/
  test/
    java/
      runners/
        CucumberTestRunner.java
      steps/
        CrudSteps.java
      screenplay/
        api/
          EventsApiMap.java
        tasks/
          PostEvent.java
          GetEvent.java
          UpdateEvent.java
          DeleteEvent.java
        questions/
          TheLastCreatedEventId.java
          TheResponseStatusCode.java
          TheEventData.java
    resources/
      features/
        crud/
          ciclo-crud-eventos.feature
      serenity.conf

build.gradle
```

## build.gradle

```groovy
plugins {
    id 'java'
    id 'net.serenity-bdd.serenity-gradle-plugin' version '4.2.34'
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'net.serenity-bdd:serenity-core:4.2.34'
    testImplementation 'net.serenity-bdd:serenity-junit:4.2.34'
    testImplementation 'net.serenity-bdd:serenity-cucumber:4.2.34'
    testImplementation 'net.serenity-bdd:serenity-screenplay:4.2.34'
    testImplementation 'net.serenity-bdd:serenity-screenplay-rest:4.2.34'
    testImplementation 'net.serenity-bdd:serenity-rest-assured:4.2.34'
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.assertj:assertj-core:3.25.3'
}

test {
    testLogging.showStandardStreams = true
    systemProperties System.getProperties()
}

gradle.startParameter.continueOnFailure = true
test.finalizedBy(aggregate)
```

## serenity.conf

```hocon
serenity {
    project.name = "AUTO_API_SCREENPLAY"
    test.root = "runners"
}

environments {
    default {
        api {
            crud.url = "http://localhost:8002"
        }
    }
}
```

## Feature File

**Archivo**: `src/test/resources/features/crud/ciclo-crud-eventos.feature`

```gherkin
#language: es
Característica: Ciclo CRUD completo de eventos vía API
  Como automatizador de pruebas
  Quiero ejecutar el ciclo POST - GET - PUT - DELETE sobre el recurso eventos
  Para verificar el contrato REST del CrudService en los 4 verbos HTTP

  @crud @critico
  Escenario: El sistema procesa correctamente el ciclo CRUD completo de un evento
    Dado que el servicio CRUD está disponible
    Cuando el actor crea un nuevo evento vía POST
    Entonces el sistema responde con código 201 y retorna el ID del evento creado
    Cuando el actor consulta el evento creado vía GET
    Entonces el sistema responde con código 200 y los datos del evento son correctos
    Cuando el actor actualiza el evento vía PUT
    Entonces el sistema responde con código 200 con los datos actualizados
    Cuando el actor elimina el evento vía DELETE
    Entonces el sistema responde con código 204
```

## Screenplay Responsibilities

| Artefacto | Tipo | Responsabilidad única |
|---|---|---|
| `EventsApiMap` | API Map | Constantes de endpoints: `/api/Events`, `/api/Events/{id}` |
| `PostEvent` | Task | `CallAnApi` + `Post.to("/api/Events")` con body `name` y `startsAt` |
| `GetEvent` | Task | `CallAnApi` + `Get.resource("/api/Events/{id}")` con ID recuperado desde memoria del Actor |
| `UpdateEvent` | Task | `CallAnApi` + `Put.to("/api/Events/{id}")` con body actualizado e ID de memoria del Actor |
| `DeleteEvent` | Task | `CallAnApi` + `Delete.from("/api/Events/{id}")` con ID de memoria del Actor |
| `TheLastCreatedEventId` | Question | Extrae `SerenityRest.lastResponse().body().path("id")` |
| `TheResponseStatusCode` | Question | `SerenityRest.lastResponse().statusCode()` |
| `TheEventData` | Question | Extrae y verifica campos del body JSON de la última respuesta |
| `CrudSteps` | Steps | Orquesta Tasks y Questions; configura Actor y memoria sin lógica HTTP directa |

## State Management

El ID del evento creado en POST se obtiene mediante `TheLastCreatedEventId`
y se almacena en memoria del Actor para reutilizarlo en GET, PUT y DELETE.

```
POST → actor.attemptsTo(PostEvent.withValidData())
POST → actor.remember("eventId", actor.asksFor(TheLastCreatedEventId.value()))
GET  → GetEvent.withId(actor.recall("eventId"))
PUT  → UpdateEvent.withId(actor.recall("eventId"))
DEL  → DeleteEvent.withId(actor.recall("eventId"))
```

## Complexity Tracking

| Violación | Por qué se necesita | Alternativa rechazada |
|---|---|---|
| Memoria de Actor | El ID del POST debe propagarse a GET/PUT/DELETE sin romper el modelo Screenplay | Campo mutable en Steps, por violar la constitución |
