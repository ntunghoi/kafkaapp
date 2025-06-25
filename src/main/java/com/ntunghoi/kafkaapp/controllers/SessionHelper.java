package com.ntunghoi.kafkaapp.controllers;

import jakarta.servlet.http.HttpSession;

public class SessionHelper {
    private final static String SESSION_KEY_USER_ID = "user_id";
    private final static String SESSION_KEY_PREFERRED_CURRENCY = "preferred_currency";

    private final HttpSession session;

    public SessionHelper(HttpSession session) {
        this.session = session;
    }

    public SessionHelper setUserId(int id) {
        session.setAttribute(SESSION_KEY_USER_ID, id);

        return this;
    }

    public SessionHelper setPreferredCurrency(String preferredCurrency) {
        session.setAttribute(SESSION_KEY_PREFERRED_CURRENCY, preferredCurrency);

        return this;
    }

    public int getUserId() {
        return Integer.parseInt(session.getAttribute(SESSION_KEY_USER_ID).toString());
    }


}
