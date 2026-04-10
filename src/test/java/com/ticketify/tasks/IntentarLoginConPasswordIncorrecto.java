package com.ticketify.tasks;

import com.ticketify.util.AuthPayloadBuilder;
import com.ticketify.util.Endpoints;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.annotations.Step;

public class IntentarLoginConPasswordIncorrecto implements Task {

    private static final String WRONG_PASSWORD = "wrong-password";

    private final String email;

    public IntentarLoginConPasswordIncorrecto(String email) {
        this.email = email;
    }

    public static IntentarLoginConPasswordIncorrecto conEmail(String email) {
        return Tasks.instrumented(IntentarLoginConPasswordIncorrecto.class, email);
    }

    @Override
    @Step("{0} intenta iniciar sesion con contrasena incorrecta para #email")
    public <T extends Actor> void performAs(T actor) {
        SerenityRest.given()
                .contentType(ContentType.JSON)
                .body(AuthPayloadBuilder.loginPayload(email, WRONG_PASSWORD))
                .post(Endpoints.LOGIN);
    }
}
