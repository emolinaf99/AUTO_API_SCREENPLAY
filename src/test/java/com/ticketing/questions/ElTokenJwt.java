package com.ticketing.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class ElTokenJwt implements Question<String> {

    public static ElTokenJwt delUltimoLlamado() {
        return new ElTokenJwt();
    }

    @Override
    public String answeredBy(Actor actor) {
        return SerenityRest.lastResponse().body().jsonPath().getString("token");
    }
}
