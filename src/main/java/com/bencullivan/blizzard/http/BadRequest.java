package com.bencullivan.blizzard.http;

/**
 * Variations of BadRequestException.
 * @author Ben Cullivan (2020)
 */
public enum BadRequest {
    ILLEGAL_CHAR,
    INVALID_REQ_LINE_FORMAT,
    INVALID_HEADER
}
