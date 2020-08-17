package com.bencullivan.blizzard.http;

import com.bencullivan.blizzard.http.exceptions.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Handles http message storage and processing.
 * @author Ben Cullivan (2020)
 */
public class BlizzardMessage {

    private final int UNKNOWN;  // representing an unknown number
    private final char[] CRLF;  // carriage return line feed

    private BlizzardRequest request; // the object containing this http request
    private ArrayList<String> reqStrings;  // a list of the request Strings that have been received
    private int[] startIndexes;  // {index in reqStrings, index in the String}
    private int contentLength; // the length (in bytes) if the body of this message
    private int remainingByteCount; // the number of bytes remaining in the message
    private int readByteCount;  // the number of bytes that have been read
    private boolean bodyFound;  // whether the body of the message has been found

    private ByteBuffer headerBuffer;  // a buffer that will accept incoming header bytes
    private ByteBuffer bodyBuffer;  // a buffer that will accept incoming body bytes

    /**
     * @param headerBufferSize The desired size (in bytes) of the header buffer of this message. (*A smaller header
     *                         buffer will incur a lower memory overhead but may result in slower performance.)
     */
    public BlizzardMessage(int headerBufferSize) {
        UNKNOWN = -42069;
        CRLF = new char[] {'\r', '\n'};
        request = new BlizzardRequest();
        reqStrings = new ArrayList<>();
        startIndexes = new int[2];
        contentLength = remainingByteCount = UNKNOWN;
        headerBuffer = ByteBuffer.allocate(headerBufferSize);
    }

    /**
     * Processes the bytes that were read into the ByteBuffer that was supplied to the SocketChannel.
     * @return Whether the message is done being read.
     * @throws BadRequestException If this request is not in valid http format.
     */
    public boolean processInput() throws BadRequestException {
        return headerBuffer == null ? processBody() : processHeader();
    }

    /**
     * Provides access to the current ByteBuffer that is available to be read into.
     * @return The current ByteBuffer.
     */
    public ByteBuffer getCurrent() {
        return headerBuffer == null ? bodyBuffer : headerBuffer;
    }

    /**
     * Processes input that was read into the headerBuffer.
     * @return Whether the message is done being read.
     * @throws BadRequestException If the header is not in valid http format.
     */
    boolean processHeader() throws BadRequestException {
        // convert the header buffer to reading mode
        headerBuffer.flip();

        // if there are no readable bytes, do nothing
        if (headerBuffer.limit() - headerBuffer.position() == 0) return false;

        // add to the byte count
        readByteCount += (headerBuffer.limit() - headerBuffer.position());

        // decode the buffer into a String and add it to the ArrayList of Strings
        reqStrings.add(new String(headerBuffer.array(), StandardCharsets.UTF_8));

        // parse the newly added String
        parseHeader();

        // clear the buffer
        if (bodyFound) headerBuffer = null;
        else headerBuffer.clear();

        return false;
    }

    /**
     * Processes input that was read into the bodyBuffer.
     * @return Whether the message is done being read.
     */
    boolean processBody() {
        // convert the body buffer to reading mode

        return false;
    }

    // ---- PARSING ---

    /**
     * Parses the header strings of an http request message.
     * @throws BadRequestException If the request is not in valid http format.
     */
    boolean parseHeader() throws BadRequestException {
        // if there are no request Strings, there is nothing to parse
        if (reqStrings.size() == 0) throw new NoMessageException();

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
        if (startIndexes[0] == reqStrings.size()-1 && startIndexes[1] >= current.length()) return false;

        // loop over the current String, searching for CRLFs
        while (i < current.length()-1) {
            // if the next char is the beginning of a CRLF, move forward one place
            if (current.charAt(i+1) == CRLF[0]) i++;

            // if a CRLF has been found
            else if (current.charAt(i) == CRLF[0] && current.charAt(i+1) == CRLF[1]) {
                // if this is the second consecutive CRLF either the body is next or the message is over
                if ((i > 1 && current.charAt(i-2) == CRLF[0] && current.charAt(i-1) == CRLF[1]) ||
                        (i == 0 && reqStrings.size() > 1 && reqStrings.get(reqStrings.size()-2)
                        .charAt(reqStrings.get(reqStrings.size()-2).length()-2) == CRLF[0] && reqStrings
                        .get(reqStrings.size()-2).charAt(reqStrings.get(reqStrings.size()-2).length()-1) == CRLF[1])) {
                    // if the content length is not known, the buffer is done being read from
                    if (contentLength == UNKNOWN && i+2 >= current.length()) return true;

                    // if the content length header was not specified but there is a body, this is not a valid request
                    else if (contentLength == UNKNOWN) {
                        i += 2;
                        for (; i < current.length(); i++) {
                            if (!Character.isWhitespace(current.charAt(i)))
                                throw new ContentLengthMissingException();
                        }
                        return true;
                    }

                    // allocate the body buffer and parse the body
                    String sub = current.substring(i+2);
                    int numBytes = sub.getBytes().length;
                    headerBuffer = null;
                    bodyBuffer = (contentLength-numBytes > 0) ? ByteBuffer.allocate(contentLength-numBytes) :
                            ByteBuffer.allocate(0);
                    return parseBody(numBytes, sub);
                }
                // if this is not the second consecutive CRLF, split the header and add it to the map of headers
                else {
                    splitHeader(start, i);
                    i += 2;
                    startIndexes[0] = reqStrings.size()-1;
                    startIndexes[1] = i;
                    start = i;
                }
            }

            // by default move forward two places
            else i += 2;
        }
        return false;
    }

    /**
     * Splits the current header into its field and value.
     * @param start The start index of the header section that is contained in the current String.
     * @param i The end index of the header section that is contained in the current String.
     * @throws BadRequestException If the request is not in valid http format.
     */
    void splitHeader(int start, int i) throws BadRequestException {
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
            if (!Character.isSpaceChar(header.charAt(j))) {
                fStart = j;
                break;
            }
        }
        for (int j = header.length()-1; j >= 0; j--) {
            if (!Character.isSpaceChar(header.charAt(j))) {
                vEnd = j+1;
                break;
            }
        }

        // ensure that there is a header field
        if (fStart >= vEnd || header.charAt(fStart) == ':')
            throw new InvalidHeaderException();

        // search for the colon in the header
        int fEnd = header.indexOf(":");
        int vStart = fEnd+1;
        while (vStart < header.length() && Character.isWhitespace(header.charAt(vStart))) vStart++;

        // if there is another color in the header, this request is not in valid http format
        if (header.indexOf(":", vStart+1) != -1)
            throw new InvalidHeaderException();

        // ensure that there is a header field
        if (fEnd <= fStart) throw new InvalidHeaderException();

        // add the header field and value to the header map
        String field = header.substring(fStart, fEnd).toLowerCase();
        String value = vStart >= vEnd ? "" : header.substring(vStart, vEnd);
        request.setHeader(field, value);

        // if this was the content length header, set the content length
        if (field.equals("content-length")) {
            try {
                contentLength = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new InvalidHeaderException();
            }
        }
    }

    /**
     * Parses the request line into its method, uri, and version.
     * @return The current index in the current String.
     * @throws BadRequestException If this request is not in valid http format.
     */
    int parseReqLine() throws BadRequestException {
        // get the current String to parse
        String current = reqStrings.get(reqStrings.size()-1);

        // check for the edge case where the CRLF is split between this string and the previous String
        if (reqStrings.size() > 1 && current.charAt(0) == CRLF[1]) {
            // get the previous String
            String prev = reqStrings.get(reqStrings.size()-2);

            // if the previous string did not end with \r, then this request line is not in valid http format
            if (prev.charAt(prev.length()-1) != CRLF[0])
                throw new IllegalCharException();

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
            return 1;
        }
        // if the first char is not a \n and te final char of the previous string is a \r
        // the request line is not in valid http format
        else if (reqStrings.size() > 1 && reqStrings.get(reqStrings.size()-2)
                .charAt(reqStrings.get(reqStrings.size()-2).length()-1) == CRLF[0]) {
            throw new IllegalCharException();
        }
        // if the first char is a \n and this is the first string, the request line is not in valid http request format
        else if (reqStrings.size() == 1 && reqStrings.get(0).charAt(0) == CRLF[1])
            throw new IllegalCharException();

        // search for the CRLF
        int i = 0;
        while (i < current.length()-1) {
            // if the next char is the beginning of the CRLF, move forward one place
            if (current.charAt(i+1) == CRLF[0]) {
                // if the beginning of the CRLF is followed by a character other than \n
                // then the request is not in valid http format
                if (i+2 < current.length() && current.charAt(i+2) != CRLF[1])
                    throw new IllegalCharException();
                i++;
            }

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

            // if the current character is a \n and it is not preceded by a \r, then the request is not
            // in valid http format
            else if (i > 0 && current.charAt(i) == CRLF[1] && current.charAt(i-1) != CRLF[0])
                throw new RequestLineException();

                // by default move forward two places
            else i += 2;
        }

        // if the CRLF wasn't found, return the length of the current String
        return current.length();
    }

    /**
     * Splits the request line into its method, uri, and version.
     * @param line The request line.
     * @throws BadRequestException If this request is not in valid http format.
     */
    void splitReqLine(String line) throws BadRequestException {
        // strip any leading or trailing whitespace and then split the request line on whitespace
        String[] split = line.strip().split("\\s+");

        // make sure that there are three distinct parts of the request String
        // if there are, set the request line
        if (split.length  == 3 && split[2].length() > 5) request.setRequestLine(split);
        else {
            throw new RequestLineException();
        }
    }

    /**
     * Parses an http message body.
     * @param numBytes The number of bytes in this part of the body.
     * @param bodySub The substring that composes this part of the body.
     * @return Whether this message is done being processed.
     * @throws BadRequestException
     */
    boolean parseBody(int numBytes, String bodySub) throws BadRequestException {
        if (reqStrings.size() == 0) throw new NoMessageException();

        // append this part of the body to the request body
        request.appendToBody(bodySub);

        // decrement the remaining byte count by the number of body bytes that have been read
        // if all the body bytes have been read, the message is done being read
        return (remainingByteCount -= numBytes) <= 0;
    }

    // --- GETTERS ---

    public BlizzardRequest getRequest() {
        return request;
    }

    ArrayList<String> getReqStrings() {
        return reqStrings;
    }

    int[] getStartIndexes() {
        return startIndexes;
    }

    int getContentLength() {
        return contentLength;
    }

    int getRemainingByteCount() {
        return remainingByteCount;
    }

    int getReadByteCount() {
        return readByteCount;
    }

    // --- SETTERS ---

    void setRequest(BlizzardRequest request) {
        this.request = request;
    }

    void setReqStrings(ArrayList<String> reqStrings) {
        this.reqStrings = reqStrings;
    }

    void setStartIndexes(int[] startIndexes) {
        this.startIndexes = startIndexes;
    }
}
