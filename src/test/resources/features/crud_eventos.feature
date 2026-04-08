Feature: Ciclo completo CRUD sobre eventos a traves de la API REST
  Como ingeniero de calidad
  Quiero validar la integridad de los servicios REST de eventos
  Para garantizar que el ciclo de vida completo de un evento funciona correctamente

  Scenario Outline: Validar el ciclo de vida completo de un evento mediante los verbos REST
    Given la API de eventos del sistema de ticketing esta disponible
    When se registra un nuevo evento con el nombre "<nombre_evento>" mediante una peticion POST
    And se consulta el evento registrado mediante una peticion GET con su identificador
    Then la API retorna los datos del evento "<nombre_evento>" correctamente
    When se actualiza el nombre del evento a "<nombre_evento_actualizado>" mediante una peticion PUT
    Then la API confirma que el evento fue actualizado con el nuevo nombre
    When se elimina el evento mediante una peticion DELETE con su identificador
    Then la API confirma la eliminacion exitosa del evento

    Examples:
      | nombre_evento         | nombre_evento_actualizado         |
      | Concierto Ticketing   | Concierto Ticketing Actualizado   |
