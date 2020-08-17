package com.bencullivan.blizzard.http.exceptions;

import java.io.IOException;

/**
 * Subclass of IOException for when a bad http request is received.
 * @author Ben Cullivan (2020)
 */
public class BadRequestException extends IOException {
    /**
     * @param message The message passed along with this exception.
     */
    public BadRequestException(String message) {
        super(message);
    }
}
