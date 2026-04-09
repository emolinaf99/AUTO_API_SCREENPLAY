# Contract: POST /api/auth/login

**Service**: AuthService  
**Base URL**: `http://localhost:8003`  
**Full URL**: `http://localhost:8003/api/auth/login`  
**Method**: POST  
**Feature**: 001-screenplay-authservice-rest  
**Date**: 2026-04-08

---

## Request

### Headers

| Header | Value | Required |
|--------|-------|----------|
| `Content-Type` | `application/json` | Yes |

### Body (JSON)

```json
{
  "email": "string (email registrado)",
  "password": "string"
}
```

**Source**: `LoginUserRequest` del AuthService.

#### Ejemplo — Paso 2 (Login exitoso)

```json
{
  "email": "user-550e8400-e29b-41d4-a716-446655440000@test.com",
  "password": "Password123!"
}
```

#### Ejemplo — Paso 4 (Login con contraseña incorrecta)

```json
{
  "email": "user-550e8400-e29b-41d4-a716-446655440000@test.com",
  "password": "wrong-password"
}
```

---

## Responses

### 200 OK — Autenticación exitosa

**Condición**: Email registrado y contraseña correcta.

```
HTTP/1.1 200 OK
Content-Type: application/json
```

**Cuerpo** (`LoginUserResponse`):

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyLTU1MGU4NDAwQHRlc3QuY29tIn0.signature",
  "expiresIn": 3600
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `token` | String | JWT de sesión. Tres segmentos `.`-separados. **Nombre de campo confirmado por contrato `LoginUserResponse`.** |
| `expiresIn` | int | Segundos hasta expiración del token (habitualmente 3600) |

**Aserciones en suite**:
1. `ElCodigoDeRespuesta.delUltimoLlamado()` → `equalTo(200)`
2. `ElTokenJwt.delUltimoLlamado()` → `is(not(emptyOrNullString()))` (no vacío, cadena con tres segmentos)

**Efecto en memoria del actor**: El campo `token` es inmediatamente almacenado con `actor.remember("token", token)` dentro de `IniciarSesionApi.performAs(actor)`.

---

### 401 Unauthorized — Credenciales inválidas

**Condición**: Email no existe, contraseña incorrecta, o cuenta bloqueada.

```
HTTP/1.1 401 Unauthorized
Content-Type: application/json
```

**Cuerpo esperado** (referencial):

```json
{
  "message": "Credenciales inválidas"
}
```

El campo `token` **no está presente** en la respuesta 401.

**Aserción en suite**: `ElCodigoDeRespuesta.delUltimoLlamado()` → `equalTo(401)`

---

## Uso en la Suite

| Paso | Task | Resultado esperado |
|------|------|--------------------|
| Paso 2 | `IniciarSesionApi.con(email, password)` | 200 OK + token almacenado |
| Paso 4 | `IntentarLoginConPasswordIncorrecto.conEmail(email)` | 401 Unauthorized |

---

## Nota sobre el Token JWT

El token es opaco desde el punto de vista de la suite — la aserción solo verifica que **existe** y **no está vacío** tras el Paso 2. No se valida la firma ni el contenido del payload JWT (eso es responsabilidad de pruebas unitarias del backend).
