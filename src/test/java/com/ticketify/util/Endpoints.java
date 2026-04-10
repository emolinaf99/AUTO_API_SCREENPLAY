package com.ticketify.util;

public class Endpoints {

    public static final String AUTH_BASE   = "http://localhost:8003";
    public static final String REGISTER    = AUTH_BASE + "/api/auth/register";
    public static final String LOGIN       = AUTH_BASE + "/api/auth/login";

    public static final String EVENTS_BASE = "http://localhost:8002";

    public static final String FAIR_QUEUE_BASE    = "http://localhost:8004";
    public static final String QUEUE_ENTER        = FAIR_QUEUE_BASE + "/api/queue/enter";
    public static final String QUEUE_POSITION     = FAIR_QUEUE_BASE + "/api/queue/{ticketId}/position";
    public static final String QUEUE_LEAVE        = FAIR_QUEUE_BASE + "/api/queue/{ticketId}/leave";

    private Endpoints() {
    }
}
