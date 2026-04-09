# Requirements — AUTO_API_SCREENPLAY
## Feature: Autenticación de Usuarios — Endpoints REST (HU1 + HU2)

**Patrón**: Screenplay + Serenity Rest (sin browser, sin Selenium)
**Servicio bajo prueba**: AuthService en `http://localhost:8003`
**Contexto**: Ver TEST_CASES.md en authService/ para la especificación completa.

---

## Restricción de Scope

Este repositorio cubre **exclusivamente** pruebas de API REST de autenticación.
- Pruebas UI con formulario de registro → AUTO_FRONT_POM_FACTORY
- Pruebas UI de login/logout → AUTO_FRONT_SCREENPLAY

---

## Endpoints del AuthService

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registro de nuevo usuario |
| POST | `/api/auth/login` | Autenticación, retorna JWT |
| POST | `/api/auth/logout` | Cierre de sesión (stateless v1) |

### POST /api/auth/register
```json
// Request body
{
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "password": "string",
  "confirmPassword": "string"
}

// 201 Created
{ "message": "Cuenta creada exitosamente", "redirect": "/login" }

// 400 Bad Request (validación de modelo)
{ "errors": { "campo": ["mensaje de error"] } }

// 409 Conflict (email duplicado)
{ "message": "El correo electrónico ya está registrado." }
```

### POST /api/auth/login
```json
// Request body
{
  "email": "string",
  "password": "string"
}

// 200 OK
{ "token": "eyJ...", "expiresIn": 3600 }

// 401 Unauthorized (credenciales inválidas o usuario no existe)
{ "message": "Credenciales inválidas" }

// 423 Locked (cuenta bloqueada tras 3 intentos)
{ "message": "Cuenta bloqueada temporalmente. Intente de nuevo en 15 minutos." }
```

---

## Flujo Principal de Autenticación — 4 Pasos

El flujo se implementa como **un único escenario** con 4 pasos que simulan el ciclo de vida completo del recurso sesión. Auth solo expone POST — los 4 pasos usan POST con variaciones de caso (permitido explícitamente por la rúbrica).

```gherkin
Feature: Ciclo completo de autenticación a través de la API REST
  Como ingeniero de calidad
  Quiero validar la integridad de los endpoints de autenticación del AuthService
  Para garantizar que el ciclo de vida completo de una sesión funciona correctamente

  Scenario: Validar el ciclo de vida de autenticación mediante endpoints REST
    Given el AuthService está disponible en el puerto 8003

    # Paso 1 — Crear usuario (CP-HU1-01)
    When se registra un nuevo usuario con correo "test.auto@sofka.com" mediante el endpoint de registro
    Then el servicio confirma la creación exitosa del usuario con código 201

    # Paso 2 — Autenticar y obtener token (CP-HU2-01)
    When el usuario se autentica con sus credenciales válidas mediante el endpoint de login
    Then el servicio retorna un token JWT válido con código 200

    # Paso 3 — Intentar registro con email duplicado (CP-HU1-08)
    When se intenta registrar nuevamente con el mismo correo "test.auto@sofka.com"
    Then el servicio rechaza la solicitud con código de conflicto 409

    # Paso 4 — Intentar login con credenciales incorrectas (CP-HU2-06)
    When se intenta autenticar con una contraseña incorrecta para el correo registrado
    Then el servicio deniega el acceso con código 401
```

---

## Casos de Prueba Detallados

### CP-HU1-01 via API — Registro exitoso
**Endpoint**: `POST /api/auth/register`

| Campo | Valor |
|-------|-------|
| firstName | "Test" |
| lastName | "Automatizacion" |
| email | `"test.auto." + UUID + "@sofka.com"` (único por ejecución) |
| password | "SofkaTech2026!" |
| confirmPassword | "SofkaTech2026!" |

**Resultado esperado**: HTTP 201, body contiene `message`

---

### CP-HU2-01 via API — Login exitoso
**Endpoint**: `POST /api/auth/login`

| Campo | Valor |
|-------|-------|
| email | mismo email del paso 1 |
| password | "SofkaTech2026!" |

**Resultado esperado**: HTTP 200, body contiene `token` (string no vacío) y `expiresIn`
**Acción post-login**: `actor.remember("token", response.token)` para uso en pasos posteriores

---

### CP-HU1-08 via API — Email duplicado
**Endpoint**: `POST /api/auth/register` (segundo intento con mismo email del paso 1)

**Resultado esperado**: HTTP 409, body contiene `message`

---

### CP-HU2-06 via API — Usuario no existe / credenciales incorrectas
**Endpoint**: `POST /api/auth/login`

| Campo | Valor |
|-------|-------|
| email | mismo email del paso 1 |
| password | "ContraseñaIncorrecta999!" |

**Resultado esperado**: HTTP 401, body contiene `message: "Credenciales inválidas"`

---

## Casos de Prueba Opcionales (Seguridad)

### CP-HU2-09 via API — Bloqueo al tercer intento
Requiere 3 llamadas consecutivas con password incorrecta al mismo email:
- Intento 1 → 401
- Intento 2 → 401
- Intento 3 → 423 Locked

### CP-HU2-11 via API — Email mayor a 255 caracteres
**Endpoint**: `POST /api/auth/login`
Email de 256+ chars → HTTP 400 Bad Request

### CP-HU2-12 via API — Inyección SQL
**Endpoint**: `POST /api/auth/login`
Email: `"' OR 1=1 --"` → HTTP 400 o 401, sin exposición de detalle de BD

---

## Tasks Screenplay a Implementar

| Task | Endpoint | Acción |
|------|----------|--------|
| `RegistrarUsuario` | POST /api/auth/register | Envía payload de registro, verifica 201 |
| `IniciarSesionApi` | POST /api/auth/login | Envía credenciales, guarda token en Actor memory |
| `IntentarRegistrarEmailDuplicado` | POST /api/auth/register | Reusa mismo email, espera 409 |
| `IntentarLoginConPasswordIncorrecta` | POST /api/auth/login | Envía password incorrecta, espera 401 |

## Questions Screenplay a Implementar

| Question | Retorna |
|----------|---------|
| `ElCodigoDeRespuesta` | `Integer` — status HTTP de la última respuesta |
| `ElTokenJwt` | `String` — campo `token` del body JSON |
| `ElMensajeDeRespuesta` | `String` — campo `message` del body JSON |

---

## Reglas de Negocio Validadas en API

| ID | Regla | Endpoint | Código |
|----|-------|----------|--------|
| RN1 | Email único en BD | POST /register | 409 |
| RN2 | Política de contraseña | POST /register | 400 |
| RN4 | Acceso solo a registrados | POST /login | 401 |
| RN5 | Bloqueo tras 3 intentos | POST /login | 423 |

---

## Notas de Implementación

- **Email único por ejecución**: usar `"test.auto." + UUID.randomUUID() + "@sofka.com"` para evitar colisiones entre ejecuciones.
- **Token entre pasos**: usar `actor.remember("token", tokenValue)` tras el login exitoso. Recuperar con `actor.recall("token")` en pasos posteriores que requieran autenticación.
- **Sin Selenium**: ninguna clase debe importar `org.openqa.selenium`. Este repo no abre browser.
- **Base URL en constante**: `Endpoints.AUTH_BASE = "http://localhost:8003"` en clase utilitaria.
- **Reporte**: Serenity Rest incluye automáticamente el request/response en el reporte HTML — no agregar logging manual redundante.
- **CRUD de eventos existente**: `crud_eventos.feature` se conserva intacta. La nueva feature de auth va en `autenticacion_api.feature` independiente.
