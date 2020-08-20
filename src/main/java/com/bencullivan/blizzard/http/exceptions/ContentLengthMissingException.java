package com.bencullivan.blizzard.http.exceptions;

/**
 * Thrown when a request with a body has no content-length field.
 * @author Ben Cullivan (2020)
 */
public class ContentLengthMissingException extends BadRequestException {
    public ContentLengthMissingException() {
        super("Content length is not specified but there is a body.", BadRequest.CONTENT_LENGTH_MISSING);
    }
}
