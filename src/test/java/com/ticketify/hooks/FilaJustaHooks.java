package com.ticketify.hooks;

import com.ticketify.util.AuthPayloadBuilder;
import com.ticketify.util.Endpoints;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;

import java.util.UUID;

public class FilaJustaHooks {

    private static final String PASSWORD         = "Password123!";
    private static final int    TIMEOUT_MS       = 5000;

    /**
     * Evento y ticket fijos del ambiente de pruebas.
     * Deben existir en la BD antes de correr los tests
     * (se pueden crear via Swagger del crud-service en :8002).
     */
    public static final long TEST_EVENT_ID  = 1L;
    public static final long TEST_TICKET_ID = 1L;

    @Before(value = "@fila-justa", order = 0)
    public void prepararEscenarioDeFilaJusta() {
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", TIMEOUT_MS)
                        .setParam("http.socket.timeout",     TIMEOUT_MS));

        OnStage.setTheStage(Cast.whereEveryoneCan(CallAnApi.at(Endpoints.FAIR_QUEUE_BASE)));

        String email    = "fila-" + UUID.randomUUID() + "@test.com";
        String password = PASSWORD;

        Actor actor = OnStage.theActorCalled("Usuario Fila");
        actor.remember("email",    email);
        actor.remember("password", password);
        actor.remember("eventId",  TEST_EVENT_ID);
        actor.remember("ticketId", TEST_TICKET_ID);

        // Registrar usuario
        SerenityRest.given()
                .contentType(ContentType.JSON)
                .body(AuthPayloadBuilder.registerPayload("Fila", "Tester", email, password))
                .post(Endpoints.REGISTER);

        // Login para obtener token
        SerenityRest.given()
                .contentType(ContentType.JSON)
                .body(AuthPayloadBuilder.loginPayload(email, password))
                .post(Endpoints.LOGIN);

        String token = SerenityRest.lastResponse().body().jsonPath().getString("token");
        actor.remember("token", token);
    }

    @After(value = "@fila-justa", order = 0)
    public void limpiarEscenario() {
        OnStage.drawTheCurtain();
    }
}
