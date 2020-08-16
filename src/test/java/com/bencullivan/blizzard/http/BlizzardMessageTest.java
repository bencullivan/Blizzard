package com.bencullivan.blizzard.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BlizzardMessageTest {

    private BlizzardMessage message = new BlizzardMessage();

    @AfterEach
    public void resetMessage() {
        message = new BlizzardMessage();
    }

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
        message.splitReqLine(reqLine);
        String[] actual = {message.getRequest().getMethod(), message.getRequest().getUri(),
                message.getRequest().getVersion()};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void splitReqLineTestExcept1() {
        splitReqLineExcept("GET /posts  HTTP");
    }

    @Test
    public void splitReqLineTestExcept2() {
        splitReqLineExcept("DELETE/posts HTTP/2");
    }

    public void splitReqLineExcept(String reqLine) {
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> message.splitReqLine(reqLine)
        );
        assertEquals("Incorrect request line format.", thrown.getMessage());
    }

    @Test
    public void parseReqLineTestGoodInput1() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("GET /posts/all HTTP/1.1  \r");
        reqStrings.add("\n");
        message.setReqStrings(reqStrings);
        parseReqLineGoodInput(new String[] {"GET", "/posts/all", "1.1"}, 1);
    }

    @Test
    public void parseReqLineTestGoodInput2() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("POST /posts/");
        reqStrings.add("all    HTTP");
        reqStrings.add("/");
        reqStrings.add("2\r\n");
        message.setReqStrings(reqStrings);
        parseReqLineGoodInput(new String[] {"POST", "/posts/all", "2"}, 3);
    }

    public void parseReqLineGoodInput(String[] expected, int i)
            throws BadRequestException {
        int index = message.parseReqLine();
        String[] actual = {message.getRequest().getMethod(), message.getRequest().getUri(),
                message.getRequest().getVersion()};
        assertArrayEquals(expected, actual);
        assertEquals(i, index);
    }

    @Test
    public void parseReqLineTestExcept1() {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("\n");
        message.setReqStrings(reqStrings);
        parseReqLineExcept("Illegal character present in request line.");
    }

    @Test
    public void parseReqLineTestExcept2() {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("/posts/");
        reqStrings.add("all    HTTP");
        reqStrings.add("/");
        reqStrings.add("2\r\n");
        message.setReqStrings(reqStrings);
        parseReqLineExcept("Incorrect request line format.");
    }

    public void parseReqLineExcept(String msg) {
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> message.parseReqLine()
        );
        assertEquals(msg, thrown.getMessage());
    }

    @Test
    public void splitHeaderTestGoodInput1() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("\r\n Content-leng");
        reqStrings.add("th: ");
        reqStrings.add(" 1568 \r\n Other-header: yay \r\n");
        message.setReqStrings(reqStrings);
        message.setStartIndexes(new int[] {0, 2});
        splitHeaderGoodInput(0, 6, new int[] {1568});
        assertEquals("1568", message.getRequest().getHeader("content-length"));
    }

    @Test
    public void splitHeaderTestGoodInput2() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("\r\n COOKIE: yum yum tasty cookies \r\n Other-header: yay \r\n");
        message.setReqStrings(reqStrings);
        message.setStartIndexes(new int[] {0, 2});
        splitHeaderGoodInput(2, 33, new int[] {-42069});
        assertEquals("yum yum tasty cookies", message.getRequest().getHeader("cookie"));
    }

    public void splitHeaderGoodInput(int start, int i, int[] clExpected) throws BadRequestException {
        message.splitHeader(start, i);
        assertArrayEquals(clExpected, message.getContentLength());
    }

    @Test
    public void splitHeaderTestExcept1() {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("\r\n COOKIE: yum yum ta:sty cookies \r\n Other-header: yay \r\n");
        message.setReqStrings(reqStrings);
        message.setStartIndexes(new int[] {0, 2});
        splitHeaderExcept(2, 34, "Invalid header.");
    }

    public void splitHeaderExcept(int start, int i, String msg) {
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> message.splitHeader(start, i)
        );
        assertEquals(msg, thrown.getMessage());
    }

    @Test
    public void parseHeaderTestGoodInput1() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" COOKIE: yum yum tasty cookies \r\n Other-header:yay \r\n   ");
        message.setReqStrings(reqStrings);
        message.setStartIndexes(new int[] {0,0});
        parseHeaderGoodInput(new int[] {0, 53}, new int[] {-42069}, true);
        assertEquals("yay", message.getRequest().getHeader("other-header"));
        assertEquals("yum yum tasty cookies", message.getRequest().getHeader("cookie"));
    }

    @Test
    public void parseHeaderTestGoodInput2() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" GET / H");
        reqStrings.add("TTP/2  \r");
        reqStrings.add("\n  First-header:  first val \r\n second-header:second val \r\n");
        message.setReqStrings(reqStrings);
        message.setStartIndexes(new int[] {2, 1});
        parseHeaderGoodInput(new int[] {2, 58}, new int[] {-42069}, false);
        assertEquals("GET", message.getRequest().getMethod());
        assertEquals("/", message.getRequest().getUri());
        assertEquals("2", message.getRequest().getVersion());
        assertEquals("first val", message.getRequest().getHeader("first-header"));
        assertEquals("second val", message.getRequest().getHeader("second-header"));
    }

    public void parseHeaderGoodInput(int[] sExpected, int[] clExpected, boolean rSet)
            throws BadRequestException {
        if (rSet) message.getRequest().setRequestLine(new String[] {"GET", "/", "1.1"});
        message.parseHeader();
        assertArrayEquals(sExpected, message.getStartIndexes());
        assertArrayEquals(clExpected, message.getContentLength());
    }
}
