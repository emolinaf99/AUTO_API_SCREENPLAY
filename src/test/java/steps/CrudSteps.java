package steps;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import net.serenitybdd.model.environment.EnvironmentSpecificConfiguration;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.thucydides.model.environment.SystemEnvironmentVariables;
import net.thucydides.model.util.EnvironmentVariables;
import screenplay.questions.TheEventData;
import screenplay.questions.TheLastCreatedEventId;
import screenplay.questions.TheResponseStatusCode;
import screenplay.tasks.DeleteEvent;
import screenplay.tasks.GetEvent;
import screenplay.tasks.PostEvent;
import screenplay.tasks.UpdateEvent;

public class CrudSteps {

    private static final String ACTOR_NAME = "Automatizador";
    private static final String EVENT_ID_KEY = "eventId";
    private static final String CREATED_NAME = "nombre creado";
    private static final String UPDATED_NAME = "nombre actualizado";

    private final EnvironmentVariables environmentVariables = SystemEnvironmentVariables.createEnvironmentVariables();

    @Before
    public void prepareActor() {
        String crudBaseUrl = EnvironmentSpecificConfiguration.from(environmentVariables).getProperty("api.crud.url");
        OnStage.setTheStage(new OnlineCast());
        OnStage.theActorCalled(ACTOR_NAME).can(CallAnApi.at(crudBaseUrl));
    }

    @Dado("que el servicio CRUD está disponible")
    public void queElServicioCrudEstaDisponible() {
        actor();
    }

    @Cuando("el actor crea un nuevo evento vía POST")
    public void elActorCreaUnNuevoEventoViaPost() {
        actor().attemptsTo(PostEvent.withValidData());
    }

    @Entonces("el sistema responde con código 201 y retorna el ID del evento creado")
    public void elSistemaRespondeConCodigo201YRetornaElIdDelEventoCreado() {
        actor().should(seeThat(TheResponseStatusCode.ofTheLastResponse(), equalTo(201)));
        actor().should(seeThat(TheLastCreatedEventId.value(), not(isEmptyOrNullString())));
        actor().remember(EVENT_ID_KEY, actor().asksFor(TheLastCreatedEventId.value()));
    }

    @Cuando("el actor consulta el evento creado vía GET")
    public void elActorConsultaElEventoCreadoViaGet() {
        actor().attemptsTo(GetEvent.withId(actor().recall(EVENT_ID_KEY)));
    }

    @Entonces("el sistema responde con código 200 y los datos del evento son correctos")
    public void elSistemaRespondeConCodigo200YLosDatosDelEventoSonCorrectos() {
        actor().should(seeThat(TheResponseStatusCode.ofTheLastResponse(), equalTo(200)));
        actor().should(seeThat(TheEventData.field("name"), equalTo(CREATED_NAME)));
        actor().should(seeThat(TheEventData.field("availableTickets"), equalTo("0")));
        actor().should(seeThat(TheEventData.field("reservedTickets"), equalTo("0")));
        actor().should(seeThat(TheEventData.field("paidTickets"), equalTo("0")));
    }

    @Cuando("el actor actualiza el evento vía PUT")
    public void elActorActualizaElEventoViaPut() {
        actor().attemptsTo(UpdateEvent.withId(actor().recall(EVENT_ID_KEY)));
    }

    @Entonces("el sistema responde con código 200 con los datos actualizados")
    public void elSistemaRespondeConCodigo200ConLosDatosActualizados() {
        actor().should(seeThat(TheResponseStatusCode.ofTheLastResponse(), equalTo(200)));
        actor().should(seeThat(TheEventData.field("name"), equalTo(UPDATED_NAME)));
    }

    @Cuando("el actor elimina el evento vía DELETE")
    public void elActorEliminaElEventoViaDelete() {
        actor().attemptsTo(DeleteEvent.withId(actor().recall(EVENT_ID_KEY)));
    }

    @Entonces("el sistema responde con código 204")
    public void elSistemaRespondeConCodigo204() {
        actor().should(seeThat(TheResponseStatusCode.ofTheLastResponse(), equalTo(204)));
    }

    private Actor actor() {
        return OnStage.theActorInTheSpotlight();
    }
}