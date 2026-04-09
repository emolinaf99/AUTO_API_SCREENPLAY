# Quickstart: Automatización REST del AuthService

**Feature**: 001-screenplay-authservice-rest  
**Date**: 2026-04-08

---

## Prerequisitos

| Requisito | Verificación |
|-----------|-------------|
| Java 21 instalado | `java --version` → `21.x` |
| AuthService disponible | `curl -s http://localhost:8003/api/auth/register` (debe responder, aunque sea 400) |
| Gradle Wrapper presente | `./gradlew --version` desde la raíz del repo |

> **Nota**: El AuthService debe estar levantado en `localhost:8003` antes de ejecutar la suite. Esta suite no gestiona el arranque del servicio.

---

## Ejecutar la Suite Completa

```bash
./gradlew clean test aggregate
```

Esto:
1. Compila todas las clases de test
2. Ejecuta todos los Scenarios Cucumber (incluyendo `crud_eventos` y `autenticacion_api`)
3. Genera el reporte Serenity en `target/site/serenity/index.html`

---

## Ejecutar Solo la Feature de Autenticación

```bash
./gradlew clean test aggregate -Dcucumber.filter.tags="@auth"
```

> Requiere añadir la etiqueta `@auth` sobre el Scenario en `autenticacion_api.feature` (recomendado para aislamiento).

O bien, usando el filtro de nombre:

```bash
./gradlew clean test aggregate -Dcucumber.filter.tags="@autenticacion"
```

---

## Ver el Reporte Serenity

```bash
# Abrir en navegador (Linux)
xdg-open target/site/serenity/index.html

# O navegar manualmente a:
# target/site/serenity/index.html
```

El reporte mostrará:
- Estado (PASS/FAIL) del Scenario `Ciclo completo de autenticacion`
- Para cada paso: request enviado, response recibido, aserción ejecutada
- Evidencia de cada llamada HTTP con cuerpos JSON

---

## Estructura de Archivos que se Crean

```text
src/test/
├── java/com/ticketing/
│   ├── util/
│   │   ├── Endpoints.java
│   │   └── AuthPayloadBuilder.java
│   ├── tasks/
│   │   ├── RegistrarUsuario.java
│   │   ├── IniciarSesionApi.java
│   │   ├── IntentarRegistrarEmailDuplicado.java
│   │   └── IntentarLoginConPasswordIncorrecto.java
│   ├── questions/
│   │   ├── ElCodigoDeRespuesta.java
│   │   ├── ElTokenJwt.java
│   │   └── ElMensajeDeRespuesta.java
│   ├── hooks/
│   │   └── AutenticacionHooks.java
│   └── stepdefinitions/
│       └── AutenticacionApiStepDefinitions.java
└── resources/features/
    └── autenticacion_api.feature
```

---

## Flujo de Ejecución del Scenario

```
@Before hook (AutenticacionHooks)
  └── Crea Actor con CallAnApi
  └── Genera email UUID y lo almacena en actor memory
  └── Almacena password en actor memory

Given: que el servicio de autenticacion esta disponible
  └── (verificación implícita — el primer paso falla si el servicio no responde)

When: un nuevo usuario se registra con datos validos        [Paso 1]
  └── Task: RegistrarUsuario.con(...)
  └── POST http://localhost:8003/api/auth/register
Then: el sistema confirma la creacion del usuario con codigo 201
  └── Question: ElCodigoDeRespuesta → assertThat(actor).asksAboutThe(ElCodigoDeRespuesta) equalTo(201)

When: el usuario inicia sesion con sus credenciales correctas  [Paso 2]
  └── Task: IniciarSesionApi.con(email, password)
  └── POST http://localhost:8003/api/auth/login
  └── actor.remember("token", response.body.token)
Then: el sistema devuelve un token de acceso valido con codigo 200
  └── Question: ElCodigoDeRespuesta → equalTo(200)
  └── Question: ElTokenJwt → not(emptyOrNullString())

When: el mismo usuario intenta registrarse nuevamente con el mismo email  [Paso 3]
  └── Task: IntentarRegistrarEmailDuplicado.con(actor.recall("email"), password)
  └── POST http://localhost:8003/api/auth/register
Then: el sistema rechaza el registro por conflicto con codigo 409
  └── Question: ElCodigoDeRespuesta → equalTo(409)

When: el usuario intenta iniciar sesion con una contrasena incorrecta  [Paso 4]
  └── Task: IntentarLoginConPasswordIncorrecto.conEmail(actor.recall("email"))
  └── POST http://localhost:8003/api/auth/login  (con "wrong-password")
Then: el sistema deniega el acceso con codigo 401
  └── Question: ElCodigoDeRespuesta → equalTo(401)
```

---

## Diagnóstico de Fallos Comunes

| Fallo | Causa probable | Solución |
|-------|---------------|----------|
| `ConnectionRefusedException` en Paso 1 | AuthService no está corriendo | Levantar AuthService en `localhost:8003` |
| `Expected: 201 but was: 400` en Paso 1 | `confirmPassword ≠ password` o campos faltantes | Verificar `AuthPayloadBuilder.registerPayload` |
| `Expected: 200 but was: 401` en Paso 2 | Password incorrecta o usuario no creado en Paso 1 | Verificar que Paso 1 pasó; verificar valor de password |
| `Expected: 409 but was: 201` en Paso 3 | Email no fue el mismo del Paso 1 | Verificar `actor.recall("email")` en `IntentarRegistrarEmailDuplicado` |
| `Expected: 401 but was: 200` en Paso 4 | Password incorrecta coincide casualmente | Cambiar `"wrong-password"` a un valor claramente diferente |
| `NullPointerException` en `actor.recall("token")` | Token no fue almacenado en Paso 2 | Verificar `actor.remember("token", ...)` en `IniciarSesionApi` |
