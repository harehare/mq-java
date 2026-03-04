package com.github.harehare.mq;

/**
 * Exception thrown when an mq operation fails.
 */
public class MqException extends RuntimeException {

    /**
     * Creates a new MqException with the specified message.
     *
     * @param message the error message
     */
    public MqException(String message) {
        super(message);
    }
}
