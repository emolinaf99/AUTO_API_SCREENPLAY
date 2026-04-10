package com.ticketify.tasks;

import com.ticketify.util.Endpoints;
import io.restassured.http.ContentType;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

import java.util.HashMap;
import java.util.Map;

public class IntentarEntrarSinToken implements Task {

    private final long eventId;
    private final long ticketId;

    public IntentarEntrarSinToken(long eventId, long ticketId) {
        this.eventId = eventId;
        this.ticketId = ticketId;
    }

    public static IntentarEntrarSinToken paraEventoYTicket(long eventId, long ticketId) {
        return Tasks.instrumented(IntentarEntrarSinToken.class, eventId, ticketId);
    }

    @Override
    @Step("{0} intenta entrar a la cola sin token JWT")
    public <T extends Actor> void performAs(T actor) {
        Map<String, Long> body = new HashMap<>();
        body.put("eventId", eventId);
        body.put("ticketId", ticketId);

        SerenityRest.given()
                .contentType(ContentType.JSON)
                .body(body)
                .post(Endpoints.QUEUE_ENTER);  // sin header Authorization
    }
}
