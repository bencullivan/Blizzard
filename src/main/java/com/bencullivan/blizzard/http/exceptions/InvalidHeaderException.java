package com.bencullivan.blizzard.http.exceptions;

/**
 * Thrown when a request has an invalid header.
 * @author Ben Cullivan (2020)
 */
public class InvalidHeaderException extends BadRequestException {
    public InvalidHeaderException() {
        super("Invalid header.", BadRequest.INVALID_HEADER);
    }
}
