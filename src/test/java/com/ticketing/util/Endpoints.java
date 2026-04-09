package com.ticketing.util;

public class Endpoints {

    public static final String AUTH_BASE   = "http://localhost:8003";
    public static final String REGISTER    = AUTH_BASE + "/api/auth/register";
    public static final String LOGIN       = AUTH_BASE + "/api/auth/login";

    public static final String EVENTS_BASE = "http://localhost:8002";

    private Endpoints() {
    }
}
