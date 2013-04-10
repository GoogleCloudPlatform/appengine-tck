package com.google.appengine.tck.util;

/**
 * TODO: http://go/java-style#javadoc
 */
/** Indicates that an error occurred during the auth process. */
public class AuthClientException extends Exception {
    public AuthClientException() {
        super();
    }

    public AuthClientException(String message) {
        super(message);
    }

    public AuthClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthClientException(Throwable cause) {
        super(cause);
    }
}
