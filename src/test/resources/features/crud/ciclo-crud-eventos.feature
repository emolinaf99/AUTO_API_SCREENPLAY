#language: es
Característica: Ciclo CRUD completo de eventos vía API
  Como automatizador de pruebas
  Quiero ejecutar el ciclo POST - GET - PUT - DELETE sobre el recurso eventos
  Para verificar el contrato REST del CrudService en los 4 verbos HTTP

  @crud @critico
  Escenario: El sistema procesa correctamente el ciclo CRUD completo de un evento
    Dado que el servicio CRUD está disponible
    Cuando el actor crea un nuevo evento vía POST
    Entonces el sistema responde con código 201 y retorna el ID del evento creado
    Cuando el actor consulta el evento creado vía GET
    Entonces el sistema responde con código 200 y los datos del evento son correctos
    Cuando el actor actualiza el evento vía PUT
    Entonces el sistema responde con código 200 con los datos actualizados
    Cuando el actor elimina el evento vía DELETE
    Entonces el sistema responde con código 204
