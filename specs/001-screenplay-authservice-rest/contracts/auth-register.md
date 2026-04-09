# Contract: POST /api/auth/register

**Service**: AuthService  
**Base URL**: `http://localhost:8003`  
**Full URL**: `http://localhost:8003/api/auth/register`  
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
  "firstName": "string",
  "lastName": "string",
  "email": "string (email format, unique)",
  "password": "string",
  "confirmPassword": "string (must equal password — [Compare] validation)"
}
```

**Source**: `RegisterUserRequest` del AuthService.

#### Ejemplo — Paso 1 (Registro exitoso)

```json
{
  "firstName": "Test",
  "lastName": "User",
  "email": "user-550e8400-e29b-41d4-a716-446655440000@test.com",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

#### Ejemplo — Paso 3 (Registro duplicado)

Idéntico al Paso 1 pero con el mismo email ya registrado.

---

## Responses

### 201 Created — Registro exitoso

**Condición**: Email no existe en el sistema y todos los campos son válidos.

```
HTTP/1.1 201 Created
Content-Type: application/json
```

**Cuerpo**: Contiene al menos el email registrado del usuario creado. El schema exacto del body de éxito no es crítico para las aserciones de esta suite (solo se verifica el código 201).

**Aserción en suite**: `ElCodigoDeRespuesta.delUltimoLlamado()` → `equalTo(201)`

---

### 409 Conflict — Email duplicado

**Condición**: El email enviado ya existe en el sistema.

```
HTTP/1.1 409 Conflict
Content-Type: application/json
```

**Cuerpo esperado** (referencial):

```json
{
  "message": "El email ya está registrado"
}
```

**Aserción en suite**: `ElCodigoDeRespuesta.delUltimoLlamado()` → `equalTo(409)`

---

### 400 Bad Request — Validación fallida

**Condición**: Campos faltantes, email con formato inválido, o `confirmPassword ≠ password` (validación `[Compare]`).

**Nota de suite**: Este caso no se provoca intencionalmente. `AuthPayloadBuilder.registerPayload` siempre garantiza `confirmPassword == password`.

---

## Uso en la Suite

| Paso | Task | Resultado esperado |
|------|------|--------------------|
| Paso 1 | `RegistrarUsuario.con(nombre, apellido, email, password)` | 201 Created |
| Paso 3 | `IntentarRegistrarEmailDuplicado.con(email, password)` | 409 Conflict |
