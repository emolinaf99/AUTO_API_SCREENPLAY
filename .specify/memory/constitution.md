# AUTO_API_SCREENPLAY Constitution

## Core Principles

### I. Spec-First
Toda funcionalidad comienza con un spec aprobado en `.specify/specs/`.
No se escribe código de producción sin spec + plan aprobados.
El spec define los escenarios sobre el contrato REST real; no se inventan endpoints.

### II. Java + Serenity BDD + Serenity Rest
Stack único: Java 21, Serenity BDD 4.2.9, Serenity Rest (RestAssured integrado), JUnit 4, Cucumber.
`CucumberWithSerenity` es el runner obligatorio.
`serenity-gradle-plugin 4.2.9` genera el reporte agregado.

### III. Screenplay + Serenity Rest (NON-NEGOTIABLE)
Patrón obligatorio: Actors, Tasks, Questions con responsabilidad única (SRP).
Cada verbo HTTP es una Task independiente: `PostEvent`, `GetEvent`, `UpdateEvent`, `DeleteEvent`.
`CallAnApi` es la habilidad del Actor para llamadas REST.
`Post.to()`, `Get.resource()`, `Put.to()`, `Delete.from()` son las acciones estándar.
Questions validan código de estado y body de respuesta.
Sin `RestAssured` directo en Steps; toda lógica REST reside en Tasks/Questions.

### IV. Código Limpio
Sin comentarios en código, sin lógica de negocio en el runner.
URLs del backend en `serenity.conf`, no en Java.
Nomenclatura semántica en todos los artefactos.

### V. Flujo CRUD Completo
El escenario único DEBE ejercer los 4 verbos en secuencia: POST → GET → PUT → DELETE.
El ID generado por POST se propaga automáticamente entre Tasks vía respuesta anterior.
Cada paso verifica el código de respuesta HTTP esperado.

## Constraints Técnicos

- Target API: `http://localhost:8002` (CrudService — .NET)
- Endpoints: `/api/events` (POST, GET-list), `/api/events/{id}` (GET, PUT, DELETE)
- Códigos esperados: POST→201, GET→200, PUT→200, DELETE→204
- Sin autenticación: la API no requiere tokens
- `./gradlew test aggregate` es el comando de ejecución

## Quality Gates

- 1 escenario Gherkin CRUD completo con estado PASS en reporte Serenity
- Escenario ejecutable de forma aislada (sin datos previos en BD)
- Código sin comentarios ni variables no semánticas
- Reporte en `target/site/serenity/`

## Governance

Esta constitución prevalece sobre cualquier otra práctica.
Enmiendas requieren documentación, aprobación y plan de migración.
Toda PR/revisión debe verificar cumplimiento de los 5 principios.

**Version**: 1.0.0 | **Ratified**: 2026-03-20 | **Last Amended**: 2026-03-20
