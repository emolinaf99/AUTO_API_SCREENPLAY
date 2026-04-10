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

public class EntrarEnCola implements Task {

    private final long eventId;
    private final long ticketId;

    public EntrarEnCola(long eventId, long ticketId) {
        this.eventId = eventId;
        this.ticketId = ticketId;
    }

    public static EntrarEnCola paraTicket(long eventId, long ticketId) {
        return Tasks.instrumented(EntrarEnCola.class, eventId, ticketId);
    }

    @Override
    @Step("{0} entra a la cola del ticket #ticketId del evento #eventId")
    public <T extends Actor> void performAs(T actor) {
        String token = actor.recall("token");

        Map<String, Long> body = new HashMap<>();
        body.put("eventId", eventId);
        body.put("ticketId", ticketId);

        SerenityRest.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .post(Endpoints.QUEUE_ENTER);

        // Guardar posicion retornada para assertions posteriores
        int statusCode = SerenityRest.lastResponse().statusCode();
        if (statusCode == 200 || statusCode == 201) {
            int posicion = SerenityRest.lastResponse().body().jsonPath().getInt("position");
            actor.remember("posicionEnCola", posicion);
            actor.remember("ticketIdEnCola", ticketId);
        }
    }
}
