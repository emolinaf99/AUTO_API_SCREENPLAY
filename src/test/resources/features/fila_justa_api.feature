@fila-justa
Feature: Ciclo de la Fila Justa en el FairQueueService
  Como ingeniero de calidad
  Quiero validar el flujo completo de la cola justa
  Para garantizar que un usuario puede entrar, consultar su posicion y salir de la fila de forma segura

  Scenario: Ciclo completo de fila justa - entrar, consultar posicion y salir
    Given que el servicio de fila justa esta disponible
    And el usuario se ha registrado e iniciado sesion correctamente
    And existe un evento con al menos un ticket disponible
    When el usuario entra a la cola del ticket disponible
    Then el sistema confirma la entrada con codigo 200 o 201
    And el sistema retorna una posicion mayor a cero
    When el usuario consulta su posicion en la cola
    Then el sistema retorna la posicion actual con codigo 200
    And el estado del usuario en la cola es "waiting" o "active"
    When el usuario sale de la cola
    Then el sistema confirma la salida con codigo 204

  Scenario: Idempotencia - entrar dos veces a la misma cola retorna la posicion existente
    Given que el servicio de fila justa esta disponible
    And el usuario se ha registrado e iniciado sesion correctamente
    And existe un evento con al menos un ticket disponible
    When el usuario entra a la cola del ticket disponible
    Then el sistema confirma la entrada con codigo 200 o 201
    When el mismo usuario intenta entrar nuevamente a la misma cola
    Then el sistema retorna la misma posicion sin duplicar con codigo 200

  Scenario: Acceso sin token JWT es rechazado
    Given que el servicio de fila justa esta disponible
    When un usuario sin autenticar intenta entrar a la cola del ticket 1 del evento 1
    Then el sistema rechaza la solicitud con codigo 401
