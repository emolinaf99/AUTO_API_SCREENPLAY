# Data Model: Automatización REST del AuthService

**Feature**: 001-screenplay-authservice-rest  
**Date**: 2026-04-08  
**Phase**: 1 — Design & Contracts

---

## Entidades del Dominio

### 1. RegisterUserRequest

Payload enviado al endpoint `POST /api/auth/register`.

| Campo | Tipo | Requerido | Validación | Origen en suite |
|-------|------|-----------|------------|-----------------|
| `firstName` | String | Sí | No vacío | Constante `"Test"` |
| `lastName` | String | Sí | No vacío | Constante `"User"` |
| `email` | String | Sí | Formato email válido; único en sistema | UUID generado en `@Before`: `"user-{uuid}@test.com"` |
| `password` | String | Sí | Cumple política del backend | Constante `"Password123!"` |
| `confirmPassword` | String | Sí | `[Compare("Password")]` — debe ser idéntico a `password` | Mismo valor que `password` |

**Estado del sistema tras éxito**: Usuario registrado — disponible para autenticación.  
**Estado del sistema tras fallo (409)**: Usuario ya existe — sistema rechaza sin modificar estado.

---

### 2. LoginUserRequest

Payload enviado al endpoint `POST /api/auth/login`.

| Campo | Tipo | Requerido | Validación | Origen en suite |
|-------|------|-----------|------------|-----------------|
| `email` | String | Sí | Email registrado | `actor.recall("email")` — generado en Paso 1 |
| `password` | String | Sí | Debe coincidir con la registrada | `"Password123!"` (Pasos 2) / `"wrong-password"` (Paso 4) |

---

### 3. LoginUserResponse *(respuesta del sistema)*

Cuerpo de la respuesta cuando `POST /api/auth/login` tiene éxito (200 OK).

| Campo | Tipo | Descripción | Acceso en suite |
|-------|------|-------------|-----------------|
| `token` | String | Token JWT de sesión. Tres segmentos separados por `.` | `ElTokenJwt.delUltimoLlamado()` → `actor.remember("token", token)` |
| `expiresIn` | int | Segundos de validez del token | No utilizado en la suite (no es parte de las aserciones requeridas) |

---

### 4. Actor (memoria entre pasos)

El Actor Screenplay actúa como contenedor de estado compartido dentro del escenario.

| Clave (`remember/recall`) | Tipo | Escrito en | Leído en | Descripción |
|---------------------------|------|------------|----------|-------------|
| `"email"` | String | `@Before` hook (generación UUID) | Paso 1, 2, 3, 4 | Email único de la ejecución |
| `"password"` | String | `@Before` hook | Paso 1, 2 | Contraseña válida (constante) |
| `"token"` | String | Paso 2 — `IniciarSesionApi.performAs(actor)` | Disponible para pasos posteriores (opcional) | Token JWT extraído del campo `token` del response |

---

## Transiciones de Estado del Recurso Usuario

```
[NO EXISTE]
     │
     │ Paso 1: POST /api/auth/register (email único, datos válidos)
     │ → 201 Created
     ▼
[REGISTRADO]
     │
     │ Paso 2: POST /api/auth/login (credenciales correctas)
     │ → 200 OK + { token, expiresIn }
     ▼
[AUTENTICADO] ← token JWT en memoria del actor
     │
     │ Paso 3: POST /api/auth/register (mismo email)   [intento de duplicado]
     │ → 409 Conflict (estado del usuario NO cambia)
     │
     │ Paso 4: POST /api/auth/login (contraseña incorrecta)   [acceso denegado]
     │ → 401 Unauthorized (estado del usuario NO cambia)
     ▼
[REGISTRADO] ← estado final inalterado
```

---

## Reglas de Validación

| Regla | Descripción | Impacto en suite |
|-------|-------------|-----------------|
| Email único | El AuthService rechaza el registro de un email ya existente con 409 | Paso 3 reutiliza el email del Paso 1 deliberadamente |
| `[Compare]` password | `confirmPassword` debe ser idéntico a `password` | `AuthPayloadBuilder.registerPayload` siempre iguala ambos |
| Credenciales válidas para login | El backend valida email + password contra la BD | Paso 4 usa contraseña deliberadamente diferente |
| Formato email | El backend valida formato de email antes de registrar | Email UUID generado siempre como `user-{uuid}@test.com` |

---

## Claves de Memoria del Actor (Constantes recomendadas)

```java
// En util/ActorMemoryKeys.java (opcional) o como constantes en Tasks/Hooks
public static final String EMAIL    = "email";
public static final String PASSWORD = "password";
public static final String TOKEN    = "token";
```
