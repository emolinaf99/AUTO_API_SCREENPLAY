package com.ticketify.stepdefinitions;

import com.ticketify.questions.ElCodigoDeRespuesta;
import com.ticketify.questions.ElTokenJwt;
import com.ticketify.tasks.IniciarSesionApi;
import com.ticketify.tasks.IntentarLoginConPasswordIncorrecto;
import com.ticketify.tasks.IntentarRegistrarEmailDuplicado;
import com.ticketify.tasks.RegistrarUsuario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.actors.OnStage;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

public class AutenticacionApiStepDefinitions {

    @Given("que el servicio de autenticacion esta disponible")
    public void queElServicioDeAutenticacionEstaDisponible() {
    }

    @When("un nuevo usuario se registra con datos validos")
    public void unNuevoUsuarioSeRegistraConDatosValidos() {
        var actor = OnStage.theActorInTheSpotlight();
        String email    = actor.recall("email");
        String password = actor.recall("password");
        actor.attemptsTo(RegistrarUsuario.con("Test", "User", email, password));
    }

    @Then("el sistema confirma la creacion del usuario con codigo 201")
    public void elSistemaConfirmaLaCreacionDelUsuarioConCodigo201() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(201)));
    }

    @When("el usuario inicia sesion con sus credenciales correctas")
    public void elUsuarioIniciaSesionConSusCredencialesCorrectas() {
        var actor = OnStage.theActorInTheSpotlight();
        String email    = actor.recall("email");
        String password = actor.recall("password");
        actor.attemptsTo(IniciarSesionApi.con(email, password));
    }

    @Then("el sistema devuelve un token de acceso valido con codigo 200")
    public void elSistemaDevuelveUnTokenDeAccesoValidoConCodigo200() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(
                seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(200)),
                seeThat(ElTokenJwt.delUltimoLlamado(), not(emptyOrNullString()))
        );
    }

    @When("el mismo usuario intenta registrarse nuevamente con el mismo email")
    public void elMismoUsuarioIntentaRegistrarseNuevamenteConElMismoEmail() {
        var actor = OnStage.theActorInTheSpotlight();
        String email    = actor.recall("email");
        String password = actor.recall("password");
        actor.attemptsTo(IntentarRegistrarEmailDuplicado.con(email, password));
    }

    @Then("el sistema rechaza el registro por conflicto con codigo 409")
    public void elSistemaRechazaElRegistroPorConflictoConCodigo409() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(409)));
    }

    @When("el usuario intenta iniciar sesion con una contrasena incorrecta")
    public void elUsuarioIntentaIniciarSesionConUnaContrasenaIncorrecta() {
        var actor = OnStage.theActorInTheSpotlight();
        String email = actor.recall("email");
        actor.attemptsTo(IntentarLoginConPasswordIncorrecto.conEmail(email));
    }

    @Then("el sistema deniega el acceso con codigo 401")
    public void elSistemaDeniegaElAccesoConCodigo401() {
        var actor = OnStage.theActorInTheSpotlight();
        actor.should(seeThat(ElCodigoDeRespuesta.delUltimoLlamado(), equalTo(401)));
    }
}
