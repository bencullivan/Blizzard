package com.bencullivan.blizzard.http;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Handles http message storage and processing.
 * @author Ben Cullivan (2020)
 */
public class BlizzardMessage {

    private final char[] CRLF = {'\r', '\n'};
    private final BlizzardRequest request;
    private ByteBuffer headerBuffer;
    private ByteBuffer bodyBuffer;
    private ArrayList<String> reqStrings;
    private int[] startIndexes;  // {index in reqStrings, index in the String}
    private int byteCount;
    private int hbCount;
    private int contentLength;
    private boolean lenIsKnown;
    private boolean endReached;
    private int hStart;
    private int hEnd;
    private int vStart;
    private int vEnd;

    /**
     * Creates a new instance of BlizzardMessage
     */
    public BlizzardMessage() {
        // create the BlizzardRequest object that will hold this parsed request
        request = new BlizzardRequest();

        // create the initial header buffer
        headerBuffer = newHeaderBuffer();

        // initialize the ArrayList to store the Strings
        reqStrings = new ArrayList<>();

        // initialize the start indexes to 0
        startIndexes = new int[] {0, 0};

        // there is only one header buffer to start
        hbCount = 1;

        // the length of the body is not known yet
        lenIsKnown = false;

        // the end of the message has not been reached yet
        endReached = false;
    }

    /**
     * Processes the bytes that were read into the ByteBuffer that was supplied to the SocketChannel.
     * @return Whether the message is done being read.
     * @throws BadRequestException If this request is not in valid http format.
     */
    public boolean processInput() throws BadRequestException {
        return lenIsKnown ? processBody() : processHeader();
    }

    /**
     * Provides access to the current ByteBuffer that is available to be read into.
     * @return The current ByteBuffer.
     */
    public ByteBuffer getCurrent() {
        return lenIsKnown ? bodyBuffer : headerBuffer;
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

        // decode the buffer into a String and add it to the array of Strings
        reqStrings.add(new String(headerBuffer.array(), StandardCharsets.UTF_8));

        // parse the newly added array
        parseHeader();

        return endReached;
    }

    /**
     * Processes input that was read into the bodyBuffer.
     * @return Whether the message is done being read.
     */
    private boolean processBody() {
        // determine whether the entire message has been received

        return endReached;
    }

    private void parseHeader() throws BadRequestException {
        // if there are no request Strings, there is nothing to parse
        if (reqStrings.size() == 0) return;

        // the current index in the current String
        int i = 0;

        // parse the request line if it is not done being parsed
        if (!request.requestLineIsSet()) {
            startIndexes[0] = reqStrings.size()-1;
            startIndexes[1] = parseReqLine();
            i = startIndexes[1];
        }

        // the starting point in the current String
        int start = i;

        // get the current String
        String current = reqStrings.get(reqStrings.size()-1);

        // if the start is at the end of the current String, nothing needs to be done
        if (startIndexes[0] == reqStrings.size()-1 && startIndexes[1] >= current.length()) return;

        // loop over the current String, searching for CRLFs
        while (i < current.length()-1) {
            // if the next char is the beginning of a CRLF, move forward one place
            if (current.charAt(i+1) == CRLF[0]) i++;

                // if a CRLF has been found, add this header field and its value to the request's map of headers
            else if (current.charAt(i) == CRLF[0] && current.charAt(i+1) == CRLF[1]) {
                splitHeader(start, i);
                i += 2;
            }

            // by default move forward two places
            else i += 2;
        }
    }

    /**
     * Splits the http header and adds it to the map of header fields and values.
     * @param start The start position in the current String.
     * @param i The end position in the current String.
     * @throws BadRequestException If the header is not in valid http format.
     */
    private void splitHeader(int start, int i) throws BadRequestException {
        // this will hold the entire header field and value
        StringBuilder header = new StringBuilder();

        // if part of this header is in a prior String
        if (startIndexes[0] < reqStrings.size()-1) {
            // the first beginning of the header
            String beginning = reqStrings.get(startIndexes[0]).substring(startIndexes[1]);
            header.append(beginning);

            // add the rest of the Strings between the beginning String and the current String to the header
            for (int j = startIndexes[0]+1; j < reqStrings.size()-1; j++) {
                header.append(reqStrings.get(j));
            }
        }

        // add the part of the current string up to i
        String beginning = reqStrings.get(reqStrings.size()-1).substring(start, i);
        header.append(beginning);

        // find the start of the field and end of the value in the header
        int fStart = -1;
        int vEnd = -1;
        for (int j = 0; j < header.length(); j++) {
            if (!Character.isWhitespace(header.charAt(j))) {
                fStart = j;
                break;
            }
        }
        for (int j = header.length()-1; j >= 0; j--) {
            if (!Character.isWhitespace(header.charAt(j))) {
                vEnd = j+1;
                break;
            }
        }

        // ensure the start and end values are valid
        if (fStart >= vEnd || header.charAt(fStart) == ':' || header.charAt(vEnd-1) == ':')
            throw new BadRequestException("Invalid header");

        // search for the colon in the header
        int fEnd = -1;
        int vStart = -1;
        for (int j = 0; j < header.length(); j++) {
            if (header.charAt(j) == ':') {
                fEnd = j;
                vStart = j+1;
                while (vStart < header.length() && Character.isWhitespace(header.charAt(vStart))) vStart++;
                break;
            }
        }

        // ensure that the start and end values are valid
        if (fEnd <= fStart || vEnd <= vStart) throw new BadRequestException("Invalid header");

        // add the header field and value to the header map
        String field = header.substring(fStart, fEnd).toLowerCase();
        String value = header.substring(vStart, vEnd);
        request.setHeader(field, value);

        // if this was the content length header, set the content length
        if (field.equals("content-length")) {
            try {
                contentLength = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new BadRequestException("Invalid header");
            }
        }
    }

    /**
     * Parses the request line into method, uri, and version.
     * @throws BadRequestException If the request line is not in valid http format.
     */
    private int parseReqLine() throws BadRequestException {
        // get the current String to parse
        String current = reqStrings.get(reqStrings.size()-1);

        // check for the edge case where the CRLF is split between this string and the previous String
        if (reqStrings.size() > 1 && current.charAt(0) == CRLF[1]) {
            // get the previous String
            String prev = reqStrings.get(reqStrings.size()-2);

            // if the previous string did not end with \r, then this request line is not in valid http format
            if (prev.charAt(prev.length()-1) != CRLF[0])
                throw new BadRequestException("Illegal character present in request line");

            // instantiate the StringBuilder that will hold the String that will be split
            StringBuilder toSplit = new StringBuilder();

            // trim the last character off of the previous String
            prev = prev.substring(0, prev.length()-1);

            // append all prior Strings to the StringBuilder
            for (int i = 0; i < reqStrings.size()-2; i++) {
                toSplit.append(reqStrings.get(i));
            }
            toSplit.append(prev);

            // split and set the request line
            splitReqLine(toSplit.toString());

            // the entire String has been parsed
            return current.length();
        }

        // search for the CRLF
        int i = 0;
        while (i < current.length()-1) {
            // if the next char is the beginning of the CRLF, move forward one place
            if (current.charAt(i+1) == CRLF[0]) i++;

                // if the CRLF has been found, compose and split the request line
            else if (current.charAt(i) == CRLF[0] && current.charAt(i+1) == CRLF[1]) {
                // instantiate the StringBuilder that will hold the String that will be split
                StringBuilder toSplit = new StringBuilder();

                // get the substring up to the CRLF of the current String
                String curr = current.substring(0, i);

                // append all prior Strings to the StringBuilder
                for (int j = 0; j < reqStrings.size()-1; j++) {
                    toSplit.append(reqStrings.get(j));
                }

                // append the substring up to the CRLF of the current String
                toSplit.append(curr);

                // split and set the request line
                splitReqLine(toSplit.toString());

                // the string up to i+2 has been parsed
                return i + 2;
            }

            // by default move forward two places
            else i += 2;
        }

        // if the CRLF wasn't found, return the length of the current String
        return current.length();
    }

    /**
     * Splits the request line into method, uri, and version.
     * @param line The un-split request line.
     * @throws BadRequestException If the request line is not in valid http format.
     */
    private void splitReqLine(String line) throws BadRequestException {
        // strip any leading or trailing whitespace and then split the request line on whitespace
        String[] split = line.strip().split("\\s+");

        // make sure that there are three distinct parts of the request String
        // if there are, set the request line
        if (split.length != 3) {
            request.setBadRequest(true);
            throw new BadRequestException("Incorrect request line format.");
        }
        else request.setRequestLine(split);
    }

    /**
     * Creates a new ByteBuffer that is destined to receive http header input.
     * @return A new header buffer that has been allocated 2KB.
     */
    private ByteBuffer newHeaderBuffer() {
        return ByteBuffer.allocate(2048);
    }
}
