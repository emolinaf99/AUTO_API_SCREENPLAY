package com.ticketing.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class ElMensajeDeRespuesta implements Question<String> {

    public static ElMensajeDeRespuesta delUltimoLlamado() {
        return new ElMensajeDeRespuesta();
    }

    @Override
    public String answeredBy(Actor actor) {
        return SerenityRest.lastResponse().body().jsonPath().getString("message");
    }
}
