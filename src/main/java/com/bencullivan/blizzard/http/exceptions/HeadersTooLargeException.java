package com.bencullivan.blizzard.http.exceptions;

/**
 * Thrown when a request has headers that exceed the maximum header size of 8KB.
 * @author Ben Cullivan (2020)
 */
public class HeadersTooLargeException extends BadRequestException {
    public HeadersTooLargeException() {
        super("Headers have exceeded the max header size of 8KB", BadRequest.HEADERS_TOO_LARGE);
    }
}
