package com.ticketify.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class LaPosicionEnCola implements Question<Integer> {

    public static LaPosicionEnCola delUltimoLlamado() {
        return new LaPosicionEnCola();
    }

    @Override
    public Integer answeredBy(Actor actor) {
        return SerenityRest.lastResponse().body().jsonPath().getInt("position");
    }
}
