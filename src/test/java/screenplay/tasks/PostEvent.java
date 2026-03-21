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
import net.serenitybdd.screenplay.rest.interactions.Post;
import screenplay.api.EventsApiMap;

public class PostEvent implements Task {

    private final Map<String, Object> eventBody;

    public PostEvent(Map<String, Object> eventBody) {
        this.eventBody = eventBody;
    }

    public static Performable withValidData() {
        return Tasks.instrumented(PostEvent.class, validEventBody());
    }

    @Override
    @Step("{0} crea un nuevo evento")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            Post.to(EventsApiMap.EVENTS_ENDPOINT)
                .with(request -> request
                    .contentType(ContentType.JSON)
                    .body(eventBody))
        );
    }

    private static Map<String, Object> validEventBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "nombre creado");
        body.put("startsAt", OffsetDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS).toString());
        return body;
    }
}
