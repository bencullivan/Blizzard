package com.bencullivan.blizzard.http.exceptions;

import java.io.IOException;

/**
 * Subclass of IOException for when a bad http request is received.
 * @author Ben Cullivan (2020)
 */
public class BadRequestException extends IOException {
    private final BadRequest type;
    /**
     * @param message The message passed along with this exception.
     * @param type The type of bad request.
     */
    public BadRequestException(String message, BadRequest type) {
        super(message);
        this.type = type;
    }
    public BadRequest getType() {
        return type;
    }
}
