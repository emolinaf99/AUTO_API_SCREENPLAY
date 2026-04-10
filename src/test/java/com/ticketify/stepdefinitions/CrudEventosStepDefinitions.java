package com.ticketify.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CrudEventosStepDefinitions {

    @Given("la API de eventos del sistema de ticketing esta disponible")
    public void laAPIDeEventosDelSistemaDeTicketingEstaDisponible() {
    }

    @When("se registra un nuevo evento con el nombre {string} mediante una peticion POST")
    public void seRegistraUnNuevoEventoConElNombreMedianteUnaPeticionPost(String nombreEvento) {
    }

    @And("se consulta el evento registrado mediante una peticion GET con su identificador")
    public void seConsultaElEventoRegistradoMedianteUnaPeticionGet() {
    }

    @Then("la API retorna los datos del evento {string} correctamente")
    public void laApiRetornaLosDatosDelEventoCorrectamente(String nombreEvento) {
    }

    @When("se actualiza el nombre del evento a {string} mediante una peticion PUT")
    public void seActualizaElNombreDelEventoMedianteUnaPeticionPut(String nombreEventoActualizado) {
    }

    @Then("la API confirma que el evento fue actualizado con el nuevo nombre")
    public void laApiConfirmaQueElEventoFueActualizadoConElNuevoNombre() {
    }

    @When("se elimina el evento mediante una peticion DELETE con su identificador")
    public void seEliminaElEventoMedianteUnaPeticionDelete() {
    }

    @Then("la API confirma la eliminacion exitosa del evento")
    public void laApiConfirmaLaEliminacionExitosaDelEvento() {
    }
}
