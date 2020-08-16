package com.bencullivan.blizzard.http;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the functionality of the BlizzardParser class.
 * @author Ben Cullivan
 */
public class BlizzardParserTest {

    BlizzardParser parser = new BlizzardParser();

    @Test
    public void splitReqLineTestGoodInput1() throws BadRequestException {
        splitReqLineGoodInput("GET /posts/all HTTP/1.1", new String[] {"GET", "/posts/all", "1.1"});
    }

    @Test
    public void splitReqLineTestGoodInput2() throws BadRequestException {
        splitReqLineGoodInput("   POST   /posts/new/post                      HTTP/2",
                new String[] {"POST", "/posts/new/post", "2"});
    }

    public void splitReqLineGoodInput(String reqLine, String[] expected) throws BadRequestException {
        BlizzardRequest request = new BlizzardRequest();
        parser.splitReqLine(reqLine, request);
        String[] actual = {request.getMethod(), request.getUri(), request.getVersion()};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void splitReqLineTestExcept1() {
        splitReqLineBadInput("GET /posts  HTTP");
    }

    @Test
    public void splitReqLineTestExcept2() {
        splitReqLineBadInput("DELETE/posts HTTP/2");
    }

    public void splitReqLineBadInput(String reqLine) {
        BlizzardRequest request = new BlizzardRequest();
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> parser.splitReqLine(reqLine, request)
        );
        assertEquals("Incorrect request line format.", thrown.getMessage());
    }

    @Test
    public void parseReqLineTestGoodInput1() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("GET /posts/all HTTP/1.1  \r");
        reqStrings.add("\n");
        parseReqLineGoodInput(reqStrings, new String[] {"GET", "/posts/all", "1.1"}, 1);
    }

    @Test
    public void parseReqLineTestGoodInput2() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("POST /posts/");
        reqStrings.add("all    HTTP");
        reqStrings.add("/");
        reqStrings.add("2\r\n");
        parseReqLineGoodInput(reqStrings, new String[] {"POST", "/posts/all", "2"}, 3);
    }

    public void parseReqLineGoodInput(ArrayList<String> reqStrings, String[] expected, int i)
            throws BadRequestException {
        BlizzardRequest request = new BlizzardRequest();
        int index = parser.parseReqLine(reqStrings, request);
        String[] actual = {request.getMethod(), request.getUri(), request.getVersion()};
        assertArrayEquals(expected, actual);
        assertEquals(i, index);
    }

    @Test
    public void parseReqLineTestBadInput1() {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("\n");
        parseReqLineBadInput(reqStrings, "Illegal character present in request line.");
    }

    @Test
    public void parseReqLineTestBadInput2() {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("/posts/");
        reqStrings.add("all    HTTP");
        reqStrings.add("/");
        reqStrings.add("2\r\n");
        parseReqLineBadInput(reqStrings, "Incorrect request line format.");
    }

    public void parseReqLineBadInput(ArrayList<String> reqStrings, String message) {
        BlizzardRequest request = new BlizzardRequest();
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> parser.parseReqLine(reqStrings, request)
        );
        assertEquals(message, thrown.getMessage());
    }

    @Test
    public void splitHeaderTestGoodInput1() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("\r\n Content-leng");
        reqStrings.add("th: ");
        reqStrings.add(" 1568 \r\n Other-header: yay \r\n");
        BlizzardRequest request = splitHeaderGoodInput(0, 6, new int[] {0, 2}, reqStrings, new int[] {1568});
        assertEquals("1568", request.getHeader("content-length"));
    }

    @Test
    public void splitHeaderTestGoodInput2() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("\r\n COOKIE: yum yum tasty cookies \r\n Other-header: yay \r\n");
        BlizzardRequest request = splitHeaderGoodInput(2, 33, new int[] {0, 2}, reqStrings, new int[] {-42069});
        assertEquals("yum yum tasty cookies", request.getHeader("cookie"));
    }

    public BlizzardRequest splitHeaderGoodInput(int start, int i, int[] startIndexes, ArrayList<String> reqStrings,
                                                int[] clExpected) throws BadRequestException {
        BlizzardRequest request = new BlizzardRequest();
        int[] contentLength = {-42069};
        parser.splitHeader(start, i, startIndexes, reqStrings, request, contentLength);
        assertArrayEquals(clExpected, contentLength);
        return request;
    }

    @Test
    public void splitHeaderTestBadInput1() {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("\r\n COOKIE: yum yum ta:sty cookies \r\n Other-header: yay \r\n");
        splitHeaderBadInput(2, 34, new int[] {0, 2}, reqStrings, "Invalid header.");
    }

    public void splitHeaderBadInput(int start, int i, int[] startIndexes, ArrayList<String> reqStrings,
                                    String message) {
        BlizzardRequest request = new BlizzardRequest();
        int[] contentLength = {-42069};
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> parser.splitHeader(start, i, startIndexes, reqStrings, request, contentLength)
        );
        assertEquals("Invalid header.", thrown.getMessage());
    }

    @Test
    public void parseHeaderTestGoodInput1() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" COOKIE: yum yum tasty cookies \r\n Other-header:yay \r\n   ");
        BlizzardRequest request =
                parseHeaderGoodInput(new int[] {0, 0}, reqStrings, new int[] {0, 53}, new int[] {-42069}, true);
        assertEquals("yay", request.getHeader("other-header"));
        assertEquals("yum yum tasty cookies", request.getHeader("cookie"));
    }

    @Test
    public void parseHeaderTestGoodInput2() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" GET / H");
        reqStrings.add("TTP/2  \r");
        reqStrings.add("\n  First-header:  first val \r\n second-header:second val \r\n");
        BlizzardRequest request =
                parseHeaderGoodInput(new int[] {2, 1}, reqStrings, new int[] {2, 58}, new int[] {-42069}, false);
        assertArrayEquals(new String[] {"GET", "/", "2"},
                new String[] {request.getMethod(), request.getUri(), request.getVersion()});
        assertEquals("first val", request.getHeader("first-header"));
        assertEquals("second val", request.getHeader("second-header"));
    }

    public BlizzardRequest parseHeaderGoodInput(int[] startIndexes, ArrayList<String> reqStrings, int[] sExpected,
                                                int[] clExpected, boolean rSet) throws BadRequestException {
        BlizzardRequest request = new BlizzardRequest();
        if (rSet) request.setRequestLine(new String[] {"GET", "/", "1.1"});
        int[] contentLength = {-42069};
        parser.parseHeader(startIndexes, reqStrings, request, contentLength);
        assertArrayEquals(sExpected, startIndexes);
        assertArrayEquals(clExpected, contentLength);
        return request;
    }
}
