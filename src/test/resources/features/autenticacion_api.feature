Feature: Ciclo de autenticacion en el AuthService
  Como ingeniero de calidad
  Quiero validar el flujo completo de autenticacion
  Para garantizar que el ciclo de vida del servicio de autenticacion funciona correctamente

  Scenario: Ciclo completo de autenticacion - registro, login, duplicado y acceso denegado
    Given que el servicio de autenticacion esta disponible
    When un nuevo usuario se registra con datos validos
    Then el sistema confirma la creacion del usuario con codigo 201
    When el usuario inicia sesion con sus credenciales correctas
    Then el sistema devuelve un token de acceso valido con codigo 200
    When el mismo usuario intenta registrarse nuevamente con el mismo email
    Then el sistema rechaza el registro por conflicto con codigo 409
    When el usuario intenta iniciar sesion con una contrasena incorrecta
    Then el sistema deniega el acceso con codigo 401
