package com.ticketify.stepdefinitions;

import com.ticketify.hooks.FilaJustaHooks;
import com.ticketify.questions.ElCodigoDeRespuesta;
import com.ticketify.questions.ElEstadoEnCola;
import com.ticketify.questions.LaPosicionEnCola;
import com.ticketify.tasks.ConsultarPosicionEnCola;
import com.ticketify.tasks.EntrarEnCola;
import com.ticketify.tasks.IntentarEntrarSinToken;
import com.ticketify.tasks.SalirDeCola;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.actors.OnStage;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.*;

public class FilaJustaStepDefinitions {

    @Given("que el servicio de fila justa esta disponible")
    public void queElServicioDeFilaJustaEstaDisponible() {
        // El hook @fila-justa ya levantó el actor y configuró RestAssured
    }

    @And("el usuario se ha registrado e iniciado sesion correctamente")
    public void elUsuarioSeHaRegistradoEIniciadoSesionCorrectamente() {
        var actor = OnStage.theActorInTheSpotlight();
        String token = actor.recall("token");
        // Si el hook asignó token, el login fue exitoso
        org.assertj.core.api.Assertions.assertThat(token)
                .as("El token JWT no debe ser nulo ni vacío tras el login")
                .isNotBlank();
    }

    @And("existe un evento con al menos un ticket disponible")
    public void existeUnEventoConAlMenosUnTicketDisponible() {
        // Los IDs de evento y ticket provienen del hook (TEST_EVENT_ID / TEST_TICKET_ID)
        // Se asume que el ambiente de pruebas tiene al menos 1 evento y 1 ticket creados
    }

    @When("el usuario entra a la cola del ticket disponible")
    public void elUsuarioEntraALaColaDelTicketDisponible() {
        var actor = OnStage.theActorInTheSpotlight();
        long eventId  = actor.<Long>recall("eventId");
        long ticketId = actor.<Long>recall("ticketId");
        actor.attemptsTo(EntrarEnCola.paraTicket(eventId, ticketId));
    }

    @Then("el sistema confirma la entrada con codigo 200 o 201")
    public void elSistemaConfirmaLaEntradaConCodigo200O201() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), anyOf(equalTo(200), equalTo(201))));
    }

    @And("el sistema retorna una posicion mayor a cero")
    public void elSistemaRetornaUnaPosicionMayorACero() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(seeThat(LaPosicionEnCola.delUltimoLlamado(), greaterThan(0)));
    }

    @When("el usuario consulta su posicion en la cola")
    public void elUsuarioConsultaSuPosicionEnLaCola() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.attemptsTo(ConsultarPosicionEnCola.actual());
    }

    @Then("el sistema retorna la posicion actual con codigo 200")
    public void elSistemaRetornaLaPosicionActualConCodigo200() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(200)));
    }

    @And("el estado del usuario en la cola es {string} o {string}")
    public void elEstadoDelUsuarioEnLaColaEs(String estado1, String estado2) {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(seeThat(ElEstadoEnCola.delUltimoLlamado(), anyOf(equalTo(estado1), equalTo(estado2))));
    }

    @When("el usuario sale de la cola")
    public void elUsuarioSaleDeLaCola() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.attemptsTo(SalirDeCola.delaColaActual());
    }

    @Then("el sistema confirma la salida con codigo 204")
    public void elSistemaConfirmaLaSalidaConCodigo204() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(204)));
    }

    // ── Idempotencia ────────────────────────────────────────────────────────────

    @When("el mismo usuario intenta entrar nuevamente a la misma cola")
    public void elMismoUsuarioIntentaEntrarNuevamenteALaMismaCola() {
        var actor = OnStage.theActorInTheSpotlight();
        long eventId  = actor.<Long>recall("eventId");
        long ticketId = actor.<Long>recall("ticketId");
        // Guardar posicion original antes de reintentar
        Integer posicionOriginal = actor.recall("posicionEnCola");
        actor.remember("posicionOriginal", posicionOriginal);
        actor.attemptsTo(EntrarEnCola.paraTicket(eventId, ticketId));
    }

    @Then("el sistema retorna la misma posicion sin duplicar con codigo 200")
    public void elSistemaRetornaLaMismaPosicionSinDuplicarConCodigo200() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(200)));

        int posicionOriginal = actor.<Integer>recall("posicionOriginal");
        actor.should(seeThat(LaPosicionEnCola.delUltimoLlamado(), equalTo(posicionOriginal)));
    }

    // ── Sin token ───────────────────────────────────────────────────────────────

    @When("un usuario sin autenticar intenta entrar a la cola del ticket {int} del evento {int}")
    public void unUsuarioSinAutenticarIntentaEntrarALaColaDelTicket(int ticketId, int eventId) {
        var actor = OnStage.theActorInTheSpotlight();
        actor.attemptsTo(IntentarEntrarSinToken.paraEventoYTicket(eventId, ticketId));
    }

    @Then("el sistema rechaza la solicitud con codigo 401")
    public void elSistemaRechazaLaSolicitudConCodigo401() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(401)));
    }
}
