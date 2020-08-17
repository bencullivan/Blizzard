package com.bencullivan.blizzard.http.exceptions;

/**
 * Thrown when the request line is formatted incorrectly.
 * @author Ben Cullivan (2020)
 */
public class RequestLineException extends BadRequestException {
    public RequestLineException() {
        super("Invalid request line format.");
    }
}
