package com.bencullivan.blizzard.http.exceptions;

/**
 * Thrown when a request has an illegal character in the request line.
 * @author Ben Cullivan (2020)
 */
public class IllegalCharException extends BadRequestException {
    public IllegalCharException() {
        super("Illegal character present in request line.");
    }
}
