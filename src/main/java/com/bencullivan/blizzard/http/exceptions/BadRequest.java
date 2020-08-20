package com.bencullivan.blizzard.http.exceptions;

public enum BadRequest {
    CONTENT_LENGTH_MISSING,
    HEADERS_TOO_LARGE,
    ILLEGAL_CHAR,
    INVALID_HEADER,
    NO_MESSAGE,
    REQUEST_LINE
}
