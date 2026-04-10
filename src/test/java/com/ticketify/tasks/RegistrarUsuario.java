package com.ticketify.tasks;

import com.ticketify.util.AuthPayloadBuilder;
import com.ticketify.util.Endpoints;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.annotations.Step;

public class RegistrarUsuario implements Task {

    private final String nombre;
    private final String apellido;
    private final String email;
    private final String password;

    public RegistrarUsuario(String nombre, String apellido, String email, String password) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
    }

    public static RegistrarUsuario con(String nombre, String apellido, String email, String password) {
        return Tasks.instrumented(RegistrarUsuario.class, nombre, apellido, email, password);
    }

    @Override
    @Step("{0} registra un nuevo usuario con email #email")
    public <T extends Actor> void performAs(T actor) {
        SerenityRest.given()
                .contentType(ContentType.JSON)
                .body(AuthPayloadBuilder.registerPayload(nombre, apellido, email, password))
                .post(Endpoints.REGISTER);

        actor.remember("email", email);
    }
}
