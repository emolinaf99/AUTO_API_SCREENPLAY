package com.ticketify.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class ElCodigoDeRespuesta implements Question<Integer> {

    public static ElCodigoDeRespuesta delUltimoLlamado() {
        return new ElCodigoDeRespuesta();
    }

    @Override
    public Integer answeredBy(Actor actor) {
        return SerenityRest.lastResponse().statusCode();
    }
}
