package screenplay.tasks;

import io.restassured.http.ContentType;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.rest.interactions.Put;
import screenplay.api.EventsApiMap;

public class UpdateEvent implements Task {

    private final String eventId;
    private final Map<String, Object> updatedBody;

    public UpdateEvent(String eventId, Map<String, Object> updatedBody) {
        this.eventId = eventId;
        this.updatedBody = updatedBody;
    }

    public static Performable withId(String eventId) {
        return Tasks.instrumented(UpdateEvent.class, eventId, updatedEventBody());
    }

    @Override
    @Step("{0} actualiza el evento con id #eventId")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            Put.to(EventsApiMap.EVENT_BY_ID_ENDPOINT)
                .with(request -> request
                    .pathParam("id", eventId)
                    .contentType(ContentType.JSON)
                    .body(updatedBody))
        );
    }

    private static Map<String, Object> updatedEventBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "nombre actualizado");
        body.put("startsAt", OffsetDateTime.now().plusDays(20).truncatedTo(ChronoUnit.SECONDS).toString());
        return body;
    }
}
