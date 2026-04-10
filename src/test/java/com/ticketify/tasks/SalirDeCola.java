package com.ticketify.tasks;

import com.ticketify.util.Endpoints;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;

public class SalirDeCola implements Task {

    public static SalirDeCola delaColaActual() {
        return Tasks.instrumented(SalirDeCola.class);
    }

    @Override
    @Step("{0} sale de la cola")
    public <T extends Actor> void performAs(T actor) {
        String token    = actor.recall("token");
        long   ticketId = actor.<Integer>recall("ticketIdEnCola").longValue();

        SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .pathParam("ticketId", ticketId)
                .delete(Endpoints.QUEUE_LEAVE);
    }
}
