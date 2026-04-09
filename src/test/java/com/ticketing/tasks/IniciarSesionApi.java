package com.ticketing.tasks;

import com.ticketing.util.AuthPayloadBuilder;
import com.ticketing.util.Endpoints;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.annotations.Step;

public class IniciarSesionApi implements Task {

    private final String email;
    private final String password;

    public IniciarSesionApi(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static IniciarSesionApi con(String email, String password) {
        return Tasks.instrumented(IniciarSesionApi.class, email, password);
    }

    @Override
    @Step("{0} inicia sesion con email #email")
    public <T extends Actor> void performAs(T actor) {
        SerenityRest.given()
                .contentType(ContentType.JSON)
                .body(AuthPayloadBuilder.loginPayload(email, password))
                .post(Endpoints.LOGIN);

        String token = SerenityRest.lastResponse().body().jsonPath().getString("token");
        actor.remember("token", token);
    }
}
