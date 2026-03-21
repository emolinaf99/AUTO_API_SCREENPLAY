package screenplay.tasks;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.rest.interactions.Get;
import screenplay.api.EventsApiMap;

public class GetEvent implements Task {

    private final String eventId;

    public GetEvent(String eventId) {
        this.eventId = eventId;
    }

    public static Performable withId(String eventId) {
        return Tasks.instrumented(GetEvent.class, eventId);
    }

    @Override
    @Step("{0} consulta el evento con id #eventId")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            Get.resource(EventsApiMap.EVENT_BY_ID_ENDPOINT)
                .with(request -> request.pathParam("id", eventId))
        );
    }
}
