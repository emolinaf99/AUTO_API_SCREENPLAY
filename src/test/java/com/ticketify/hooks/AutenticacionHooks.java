package com.ticketify.hooks;

import com.ticketify.util.Endpoints;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

import java.util.UUID;

public class AutenticacionHooks {

    private static final String PASSWORD         = "Password123!";
    private static final int    CONNECTION_TIMEOUT_MS = 5000;

    @Before(order = 0)
    public void prepararEscenarioDeAutenticacion() {
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", CONNECTION_TIMEOUT_MS)
                        .setParam("http.socket.timeout", CONNECTION_TIMEOUT_MS));

        OnStage.setTheStage(Cast.whereEveryoneCan(CallAnApi.at(Endpoints.AUTH_BASE)));

        String email = "user-" + UUID.randomUUID() + "@test.com";

        Actor actor = OnStage.theActorCalled("Usuario Test");
        actor.remember("email", email);
        actor.remember("password", PASSWORD);
    }

    @After(order = 0)
    public void limpiarEscenario() {
        OnStage.drawTheCurtain();
    }
}
