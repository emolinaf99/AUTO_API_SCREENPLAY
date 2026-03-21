# Feature Specification: API Automation CRUD Flow with Screenplay

**Feature Branch**: `001-crud-screenplay`
**Created**: 2026-03-20
**Status**: Implemented
**Input**: Sistema real TicketRush — CrudService REST API en `http://localhost:8002`
**Contexto de la app**: `.specify/memory/app-context.md`

## Entorno de Ejecución

| Dato | Valor | Fuente |
|---|---|---|
| URL base del API | `http://localhost:8002` | Configuración automatizada en `serenity.conf` |
| Endpoint de eventos | `/api/Events` | Swagger del servicio activo |
| POST → código esperado | 201 Created | Implementación real del controlador |
| GET → código esperado | 200 OK | Contrato REST real |
| PUT → código esperado | 200 OK | Contrato REST real |
| DELETE → código esperado | 204 No Content | Implementación real del controlador |
| Autenticación requerida | Ninguna | API sin seguridad |

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ejecutar ciclo CRUD completo sobre un evento (Priority: P1)

Como automatizador de pruebas de API,
quiero ejecutar el ciclo completo POST → GET → PUT → DELETE sobre el recurso `/api/Events`,
para verificar que el CrudService cumple su contrato REST en los 4 verbos HTTP.

**Why this priority**: Es el único flujo de este proyecto. Cubre la totalidad del contrato
REST del CrudService para el recurso Event, que es el recurso central del sistema TicketRush.

**Independent Test**: Puede verificarse de forma aislada porque el escenario crea su propio
recurso en POST, lo usa en GET/PUT, y lo elimina en DELETE. No depende de datos previos en BD.

**Acceptance Scenarios**:

1. **Given** el servicio CRUD está disponible en `http://localhost:8002`,
   **When** el actor crea un nuevo evento vía POST `/api/Events`,
    **Then** el sistema responde con código 201 y retorna el evento con un ID generado,
   **When** el actor consulta el evento creado vía GET `/api/Events/{id}`,
   **Then** el sistema responde con código 200 y los datos del evento coinciden con los enviados,
   **When** el actor actualiza el evento vía PUT `/api/Events/{id}`,
   **Then** el sistema responde con código 200 y los datos actualizados son visibles,
   **When** el actor elimina el evento vía DELETE `/api/Events/{id}`,
    **Then** el sistema responde con código 204.

---

### Edge Cases — Fuera de Alcance

Los siguientes casos quedan **explícitamente fuera del alcance** de este proyecto:

- Validaciones de campos requeridos o formatos inválidos en el body del request.
- Manejo de errores 404 o 400 (recursos no encontrados o datos inválidos).
- Endpoints de tickets (`/api/tickets`).
- Producer Service (`http://localhost:8001`) y flujos de reserva/pago.
- Autenticación o tokens de seguridad.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El actor DEBE poder crear un evento con POST `/api/Events` y recibir 201.
- **FR-002**: El body de respuesta del POST DEBE contener el `id` del evento creado.
- **FR-003**: El actor DEBE poder consultar el evento con GET `/api/Events/{id}` y recibir 200.
- **FR-004**: Los datos del GET DEBEN coincidir con los enviados en el POST.
- **FR-005**: El actor DEBE poder actualizar el evento con PUT `/api/Events/{id}` y recibir 200.
- **FR-006**: El actor DEBE poder eliminar el evento con DELETE `/api/Events/{id}` y recibir 204.
- **FR-007**: El ID del POST DEBE propagarse automáticamente a GET, PUT y DELETE mediante un mecanismo de estado de Screenplay.
- **FR-008**: El escenario DEBE ser ejecutable de forma aislada sin datos previos en BD.
- **FR-009**: Los selectores/requests DEBEN usar Serenity Rest (`CallAnApi`, `Post.to()`, `Get.resource()`, etc.).
- **FR-010**: La URL base DEBE residir en `serenity.conf`, no en código Java.
- **FR-011**: Cada verbo HTTP DEBE ser una Task independiente con responsabilidad única (SRP).

### Non-Functional Requirements

- **NFR-001**: La ejecución DEBE generar reporte Serenity en `target/site/serenity/`.
- **NFR-002**: El código DEBE mantenerse sin comentarios y con nomenclatura semántica.
- **NFR-003**: La configuración del host base DEBE estar externalizada en `serenity.conf`.

### Key Entities

- **EventsApiMap** (UI/API Map): constantes con los endpoints `/api/Events` y `/api/Events/{id}`.
- **PostEvent** (Task): crea un evento vía POST y almacena el ID retornado.
- **GetEvent** (Task): consulta el evento por ID vía GET.
- **UpdateEvent** (Task): actualiza el evento vía PUT con nuevos datos.
- **DeleteEvent** (Task): elimina el evento vía DELETE.
- **TheLastCreatedEventId** (Question): extrae el `id` del último POST y lo deja disponible para el actor.
- **TheResponseStatusCode** (Question): verifica el código de estado HTTP de la última respuesta.
- **TheEventData** (Question): verifica campos del body de respuesta (nombre, descripción, etc.).
- **CrudSteps**: orquesta Tasks y Questions; no contiene lógica HTTP directa.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El escenario Gherkin CRUD se ejecuta sin errores con `./gradlew test aggregate`.
- **SC-002**: El reporte Serenity en `target/site/serenity/` muestra el escenario con estado PASS.
- **SC-003**: POST retorna 201 con ID en el body.
- **SC-004**: GET retorna 200 con datos coincidentes.
- **SC-005**: PUT retorna 200 con datos actualizados.
- **SC-006**: DELETE retorna 204.
- **SC-007**: El escenario es ejecutable de forma aislada produciendo PASS sin datos previos.
- **SC-008**: El código no contiene comentarios ni nomenclatura no semántica.
