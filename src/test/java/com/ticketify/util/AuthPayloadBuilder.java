package com.ticketify.util;

import java.util.HashMap;
import java.util.Map;

public class AuthPayloadBuilder {

    private AuthPayloadBuilder() {
    }

    /**
     * Builds the RegisterUserRequest payload.
     * 5 fields required by the backend [Compare] validation:
     * firstName, lastName, email, password, confirmPassword (= password).
     */
    public static Map<String, String> registerPayload(String firstName,
                                                      String lastName,
                                                      String email,
                                                      String password) {
        Map<String, String> payload = new HashMap<>();
        payload.put("firstName", firstName);
        payload.put("lastName", lastName);
        payload.put("email", email);
        payload.put("password", password);
        payload.put("confirmPassword", password);
        return payload;
    }

    /**
     * Builds the LoginUserRequest payload.
     * 2 fields: email and password.
     */
    public static Map<String, String> loginPayload(String email, String password) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        return payload;
    }
}
