package com.ticketify.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class ElEstadoEnCola implements Question<String> {

    public static ElEstadoEnCola delUltimoLlamado() {
        return new ElEstadoEnCola();
    }

    @Override
    public String answeredBy(Actor actor) {
        return SerenityRest.lastResponse().body().jsonPath().getString("status");
    }
}
