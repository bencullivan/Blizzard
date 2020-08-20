package com.bencullivan.blizzard.http.exceptions;

/**
 * Thrown when there is not http message.
 * @author Ben Cullivan (2020)
 */
public class NoMessageException extends BadRequestException {
    public NoMessageException() {
        super("No message.", BadRequest.NO_MESSAGE);
    }
}
