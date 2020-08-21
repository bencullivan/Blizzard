package com.bencullivan.blizzard.http.exceptions;

/**
 * The types of bad http requests.
 * @author Ben Cullivan (2020)
 */
public enum BadRequest {
    CONTENT_LENGTH_MISSING,
    HEADERS_TOO_LARGE,
    ILLEGAL_CHAR,
    INVALID_HEADER,
    NO_MESSAGE,
    REQUEST_LINE
}
