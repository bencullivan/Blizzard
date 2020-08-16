package com.bencullivan.blizzard.http;

import java.io.IOException;

/**
 * Subclass of IOException for when a bad http request is received.
 * @author Ben Cullivan (2020)
 */
public class BadRequestException extends IOException {
    /**
     * @param message The message passed along with this exception.
     */
    public BadRequestException(BadRequest message) {
        super(switch(message) {
            case ILLEGAL_CHAR -> "Illegal character present in request line.";
            case INVALID_REQ_LINE_FORMAT -> "Incorrect request line format.";
            case INVALID_HEADER -> "Invalid header.";
        });
    }
}
