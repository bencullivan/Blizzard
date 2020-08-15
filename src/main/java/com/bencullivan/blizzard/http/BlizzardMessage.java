package com.bencullivan.blizzard.http;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Handles http message storage and processing.
 * @author Ben Cullivan (2020)
 */
public class BlizzardMessage {

    private final int UNKNOWN;  // representing an unknown number
    private final BlizzardRequest request; // the object containing this http request
    private final BlizzardParser parser;  // the object that will parse this http request

    private ArrayList<String> reqStrings;  // a list of the request Strings that have been received
    private int[] startIndexes;  // {index in reqStrings, index in the String}
    private int[] contentLength; // the length (in bytes) if the body of this message
    private int remainingByteCount; // the number of bytes remaining in the message
    private int readByteCount;  // the number of bytes that have been read

    private ByteBuffer headerBuffer;  // a buffer that will accept incoming header bytes
    private ByteBuffer bodyBuffer;  // a buffer that will accept incoming body bytes

    public BlizzardMessage() {
        UNKNOWN = -42069;
        request = new BlizzardRequest();
        parser = new BlizzardParser();
        reqStrings = new ArrayList<>();
        startIndexes = new int[2];
        contentLength = new int[] {UNKNOWN};
        remainingByteCount = UNKNOWN;
        headerBuffer = ByteBuffer.allocate(2048);
    }

    /**
     * Processes the bytes that were read into the ByteBuffer that was supplied to the SocketChannel.
     * @return Whether the message is done being read.
     * @throws BadRequestException If this request is not in valid http format.
     */
    public boolean processInput() throws BadRequestException {
        return contentLength[0] == UNKNOWN ? processHeader() : processBody();
    }

    /**
     * Provides access to the current ByteBuffer that is available to be read into.
     * @return The current ByteBuffer.
     */
    public ByteBuffer getCurrent() {
        return contentLength[0] == UNKNOWN ? headerBuffer : bodyBuffer;
    }

    /**
     * Processes input that was read into the headerBuffer.
     * @return Whether the message is done being read.
     * @throws BadRequestException If the header is not in valid http format.
     */
    private boolean processHeader() throws BadRequestException {
        // convert the header buffer to reading mode
        headerBuffer.flip();

        // if there are no readable bytes, do nothing
        if (headerBuffer.limit() - headerBuffer.position() == 0) return false;

        // decode the buffer into a String and add it to the ArrayList of Strings
        reqStrings.add(new String(headerBuffer.array(), StandardCharsets.UTF_8));

        // parse the newly added String
        parser.parseHeader(startIndexes, reqStrings, request, contentLength);

        return false;
    }

    /**
     * Processes input that was read into the bodyBuffer.
     * @return Whether the message is done being read.
     */
    private boolean processBody() {
        // determine whether the entire message has been received

        return false;
    }

    /**
     * Creates a new ByteBuffer that is destined to receive http header input.
     * @return A new header buffer that has been allocated 2KB.
     */
    private ByteBuffer newHeaderBuffer() {
        return ByteBuffer.allocate(2048);
    }
}
