package com.ticketify.tasks;

import com.ticketify.util.Endpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

public class ConsultarPosicionEnCola implements Task {

    public static ConsultarPosicionEnCola actual() {
        return Tasks.instrumented(ConsultarPosicionEnCola.class);
    }

    @Override
    @Step("{0} consulta su posicion en la cola")
    public <T extends Actor> void performAs(T actor) {
        String token    = actor.recall("token");
        long   ticketId = actor.<Integer>recall("ticketIdEnCola").longValue();

        SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .pathParam("ticketId", ticketId)
                .get(Endpoints.QUEUE_POSITION);
    }
}
