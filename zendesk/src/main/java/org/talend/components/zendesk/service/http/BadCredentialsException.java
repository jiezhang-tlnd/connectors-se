package org.talend.components.zendesk.service.http;

public class BadCredentialsException extends Exception {

    public BadCredentialsException(String message) {
        super(message);
    }
}