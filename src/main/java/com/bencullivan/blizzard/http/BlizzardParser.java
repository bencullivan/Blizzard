package com.bencullivan.blizzard.http;

import java.util.ArrayList;

/**
 * Handles http request parsing.
 * @author Ben Cullivan
 */
class BlizzardParser {

    private final char[] CRLF;  // carriage return line feed

    BlizzardParser() {
        CRLF = new char[] {'\r', '\n'};
    }

    /**
     * Parses the header strings of an http request message.
     * @param startIndexes The index of the beginning of the current header String in reqStrings and the index
     *                     of the first char belonging to the current header, respectively.
     * @param reqStrings A list of the Strings that have been received through the input ByteBuffer of this parser's
     *                   BlizzardMessage.
     * @param request The BlizzardRequest associated with the BlizzardMessage that this BlizzardParser is operating on.
     * @param contentLength The length of the body of this http request.
     * @throws BadRequestException If the request is not in valid http format.
     */
    void parseHeader(int[] startIndexes, ArrayList<String> reqStrings, BlizzardRequest request,
                            int[] contentLength) throws BadRequestException {
        // if there are no request Strings, there is nothing to parse
        if (reqStrings.size() == 0) return;

        // the current index in the current String
        int i = 0;

        // parse the request line if it is not done being parsed
        if (!request.requestLineIsSet()) {
            startIndexes[0] = reqStrings.size()-1;
            startIndexes[1] = parseReqLine(reqStrings, request);
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
                splitHeader(start, i, startIndexes, reqStrings, request, contentLength);
                i += 2;
                startIndexes[0] = reqStrings.size()-1;
                startIndexes[1] = i;
                start = i;
            }

            // by default move forward two places
            else i += 2;
        }
    }

    /**
     * Splits the current header into its field and value.
     * @param start The start index of the header section that is contained in the current String.
     * @param i The end index of the header section that is contained in the current String.
     * @param startIndexes The index of the beginning of the current header String in reqStrings and the index
     *                     of the first char belonging to the current header, respectively.
     * @param reqStrings A list of the Strings that have been received through the input ByteBuffer of this parser's
     *                   BlizzardMessage.
     * @param request The BlizzardRequest associated with the BlizzardMessage that this BlizzardParser is operating on.
     * @param contentLength The length of the body of this http request.
     * @throws BadRequestException If the request is not in valid http format.
     */
    void splitHeader(int start, int i, int[] startIndexes, ArrayList<String> reqStrings, BlizzardRequest request,
                            int[] contentLength) throws BadRequestException {
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

        // ensure the start and end values are valid
        if (fStart >= vEnd || header.charAt(fStart) == ':' || header.charAt(vEnd-1) == ':')
            throw new BadRequestException(BadRequest.INVALID_HEADER);

        // search for the colon in the header
        int fEnd = header.indexOf(":");
        int vStart = fEnd+1;
        while (vStart < header.length() && Character.isWhitespace(header.charAt(vStart))) vStart++;

        // if there is another color in the header, this request is not in valid http format
        if (header.indexOf(":", vStart+1) != -1)
            throw new BadRequestException(BadRequest.INVALID_HEADER);

        // ensure that the start and end values are valid
        if (fEnd <= fStart || vEnd <= vStart) throw new BadRequestException(BadRequest.INVALID_HEADER);

        // add the header field and value to the header map
        String field = header.substring(fStart, fEnd).toLowerCase();
        String value = header.substring(vStart, vEnd);
        request.setHeader(field, value);

        // if this was the content length header, set the content length
        if (field.equals("content-length")) {
            try {
                contentLength[0] = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new BadRequestException(BadRequest.INVALID_HEADER);
            }
        }
    }

    /**
     * Parses the request line into its method, uri, and version.
     * @param reqStrings A list of the Strings that have been received through the input ByteBuffer of this parser's
     *                   BlizzardMessage.
     * @param request The BlizzardRequest associated with the BlizzardMessage that this BlizzardParser is operating on.
     * @return The current index in the current String.
     * @throws BadRequestException If this request is not in valid http format.
     */
    int parseReqLine(ArrayList<String> reqStrings, BlizzardRequest request) throws BadRequestException {
        // get the current String to parse
        String current = reqStrings.get(reqStrings.size()-1);

        // check for the edge case where the CRLF is split between this string and the previous String
        if (reqStrings.size() > 1 && current.charAt(0) == CRLF[1]) {
            // get the previous String
            String prev = reqStrings.get(reqStrings.size()-2);

            // if the previous string did not end with \r, then this request line is not in valid http format
            if (prev.charAt(prev.length()-1) != CRLF[0])
                throw new BadRequestException(BadRequest.ILLEGAL_CHAR);

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
            splitReqLine(toSplit.toString(), request);

            // the entire String has been parsed
            return 1;
        }
        // if the first char is not a \n and te final char of the previous string is a \r
        // the request line is not in valid http format
        else if (reqStrings.size() > 1 && reqStrings.get(reqStrings.size()-2)
                .charAt(reqStrings.get(reqStrings.size()-2).length()-1) == CRLF[0]) {
            throw new BadRequestException(BadRequest.ILLEGAL_CHAR);
        }
        // if the first char is a \n and this is the first string, the request line is not in valid http request format
        else if (reqStrings.size() == 1 && reqStrings.get(0).charAt(0) == CRLF[1])
            throw new BadRequestException(BadRequest.ILLEGAL_CHAR);

        // search for the CRLF
        int i = 0;
        while (i < current.length()-1) {
            // if the next char is the beginning of the CRLF, move forward one place
            if (current.charAt(i+1) == CRLF[0]) {
                // if the beginning of the CRLF is followed by a character other than \n
                // then the request is not in valid http format
                if (i+2 < current.length() && current.charAt(i+2) != CRLF[1])
                    throw new BadRequestException(BadRequest.ILLEGAL_CHAR);
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
                splitReqLine(toSplit.toString(), request);

                // the string up to i+2 has been parsed
                return i + 2;
            }

            // if the current character is a \n and it is not preceded by a \r, then the request is not
            // in valid http format
            else if (i > 0 && current.charAt(i) == CRLF[1] && current.charAt(i-1) != CRLF[0])
                throw new BadRequestException(BadRequest.INVALID_REQ_LINE_FORMAT);

            // by default move forward two places
            else i += 2;
        }

        // if the CRLF wasn't found, return the length of the current String
        return current.length();
    }

    /**
     * Splits the request line into its method, uri, and version.
     * @param line The request line.
     * @param request The BlizzardRequest associated with the BlizzardMessage that this BlizzardParser is operating on.
     * @throws BadRequestException
     */
    void splitReqLine(String line, BlizzardRequest request) throws BadRequestException {
        // strip any leading or trailing whitespace and then split the request line on whitespace
        String[] split = line.strip().split("\\s+");

        // make sure that there are three distinct parts of the request String
        // if there are, set the request line
        if (split.length  == 3 && split[2].length() > 5) request.setRequestLine(split);
        else {
            request.setBadRequest(true);
            throw new BadRequestException(BadRequest.INVALID_REQ_LINE_FORMAT);
        }
    }
}
