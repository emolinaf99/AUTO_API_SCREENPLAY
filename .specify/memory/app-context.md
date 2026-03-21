# Contexto de la Aplicación Bajo Prueba

## Nombre del Sistema
TicketRush — CrudService REST API (.NET)

## URL Base
`http://localhost:8002`

## Servicios del Backend
| Servicio | Puerto | Responsabilidad |
|---|---|---|
| CRUD Service | `http://localhost:8002` | Gestión real de eventos y tickets para validación CRUD |
| Producer Service | `http://localhost:8001` | Reserva de tickets (fuera del alcance de este proyecto) |

## Endpoints del flujo automatizado — /api/Events

| Método | Endpoint | Código esperado | Descripción |
|---|---|---|---|
| POST | `/api/Events` | 200 OK | Crear un nuevo evento; retorna el evento con `id` generado |
| GET | `/api/Events` | 200 OK | Listar todos los eventos |
| GET | `/api/Events/{id}` | 200 OK | Obtener un evento por ID |
| PUT | `/api/Events/{id}` | 200 OK | Actualizar un evento existente |
| DELETE | `/api/Events/{id}` | 200 OK | Eliminar un evento |

## Estructura del Recurso Event (JSON)

```json
{
  "id": 0,
  "name": "string",
  "startsAt": "ISO 8601 datetime",
  "availableTickets": 0,
  "reservedTickets": 0,
  "paidTickets": 0
}
```

## Endpoints del CrudService — /api/tickets

| Método | Endpoint | Código esperado | Descripción |
|---|---|---|---|
| POST | `/api/tickets` | 201 Created | Crear ticket asociado a un evento |
| GET | `/api/tickets/event/{eventId}` | 200 OK | Listar tickets de un evento |

## NO existe en la API
- Autenticación / tokens de seguridad
- Rate limiting documentado
- Paginación en listados

## Flujo CRUD Completo (objetivo de este proyecto)
1. **POST** `/api/Events` → crea evento → captura `id` del body de respuesta (200)
2. **GET** `/api/Events/{id}` → recupera el evento creado → verifica datos (200)
3. **PUT** `/api/Events/{id}` → actualiza el evento → verifica actualización (200)
4. **DELETE** `/api/Events/{id}` → elimina el evento → verifica (200)

El ID generado en el POST se propaga como estado entre los pasos siguientes.

## Herramientas Serenity Rest disponibles
- `CallAnApi.at("http://localhost:8002")` — habilidad del Actor
- `Post.to("/api/Events").with(body)` — Task de creación
- `Get.resource("/api/Events/{id}")` — Task de consulta
- `Put.to("/api/Events/{id}").with(body)` — Task de actualización
- `Delete.from("/api/Events/{id}")` — Task de eliminación
- `SerenityRest.lastResponse()` — acceso a la última respuesta HTTP
- `Ensure.that(TheResponse.statusCode(), equalTo(201))` — Question de validación
