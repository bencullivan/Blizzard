package com.bencullivan.blizzard.http;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests the functionality of the BlizzardParser class.
 * @author Ben Cullivan
 */
public class BlizzardParserTest {

    BlizzardParser parser = new BlizzardParser();

    @Test
    public void splitReqLineTest1() throws BadRequestException {
        String reqLine = "GET /posts/all HTTP/1.1";
        BlizzardRequest request = new BlizzardRequest();
        String[] expected = {"GET", "/posts/all", "1.1"};
        parser.splitReqLine(reqLine, request);
        String[] actual = {request.getMethod(), request.getUri(), request.getVersion()};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void splitReqLineTest2() throws BadRequestException {
        String reqLine = "POST   /posts/new/post       HTTP/2";
        BlizzardRequest request = new BlizzardRequest();
        String[] expected = {"POST", "/posts/new/post", "2"};
        parser.splitReqLine(reqLine, request);
        String[] actual = {request.getMethod(), request.getUri(), request.getVersion()};
        assertArrayEquals(expected, actual);
    }


}
