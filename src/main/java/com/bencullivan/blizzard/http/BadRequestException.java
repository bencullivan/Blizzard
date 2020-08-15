package com.bencullivan.blizzard.http;

import java.io.IOException;

/**
 * Subclass of IOException for when a bad http request is received.
 * @author Ben Cullivan
 */
public class BadRequestException extends IOException {
    /**
     * Creates a new instance of BadRequestException.
     * @param message The message passed along with this exception.
     */
    public BadRequestException(String message) {
        super(message);
    }
}