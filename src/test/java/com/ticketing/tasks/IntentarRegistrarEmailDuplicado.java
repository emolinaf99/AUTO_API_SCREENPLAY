package com.ticketing.tasks;

import com.ticketing.util.AuthPayloadBuilder;
import com.ticketing.util.Endpoints;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.annotations.Step;

public class IntentarRegistrarEmailDuplicado implements Task {

    private final String email;
    private final String password;

    public IntentarRegistrarEmailDuplicado(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static IntentarRegistrarEmailDuplicado con(String email, String password) {
        return Tasks.instrumented(IntentarRegistrarEmailDuplicado.class, email, password);
    }

    @Override
    @Step("{0} intenta registrarse nuevamente con email duplicado #email")
    public <T extends Actor> void performAs(T actor) {
        SerenityRest.given()
                .contentType(ContentType.JSON)
                .body(AuthPayloadBuilder.registerPayload("Test", "User", email, password))
                .post(Endpoints.REGISTER);
    }
}
