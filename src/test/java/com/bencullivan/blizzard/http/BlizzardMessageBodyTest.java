package com.bencullivan.blizzard.http;

import com.bencullivan.blizzard.http.exceptions.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;


public class BlizzardMessageBodyTest {

    private BlizzardMessage message = new BlizzardMessage(2048, new BlizzardRequest());

    @AfterEach
    public void resetMessage() {
        message = new BlizzardMessage(2048, new BlizzardRequest());
    }

    @Test
    public void testHeaderIntoBody1() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" COOKIE: yum yum tasty cookies \r\n Content-length: 23\r\nOther-header:yay " +
                "\r\n\r\nThis is the body, bitch");
        message.setReqStrings(reqStrings);
        message.setStartIndexes(new int[] {0,0});
        assertTrue(testHeaderIntoBody(23, true));
        assertEquals("yum yum tasty cookies", message.getRequest().getHeader("cookie"));
        assertEquals("23", message.getRequest().getHeader("content-length"));
        assertEquals("yay", message.getRequest().getHeader("other-header"));
        assertEquals("This is the body, bitch", message.getRequest().getBody());
    }

    @Test
    public void testHeaderIntoBody2() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" header: this header is irrelevant\r\n\r");
        reqStrings.add("\nThis is the body.");
        message.setReqStrings(reqStrings);
        message.setContentLength(17);
        message.setStartIndexes(new int[] {0,0});
        assertTrue(testHeaderIntoBody(17, true));
        assertEquals("This is the body.", message.getRequest().getBody());
    }

    @Test
    public void testHeaderIntoBody3() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" header: this header is irrelevant\r\n");
        reqStrings.add("\r\nThis is the body.");
        message.setReqStrings(reqStrings);
        message.setContentLength(17);
        message.setStartIndexes(new int[] {0,0});
        assertTrue(testHeaderIntoBody(17, true));
        assertEquals("This is the body.", message.getRequest().getBody());
    }

    @Test
    public void testHeaderIntoBody4() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" header: this header is irrelevant\r");
        reqStrings.add("\n\r\nThis is the body. yooo");
        message.setReqStrings(reqStrings);
        message.setContentLength(22);
        message.setStartIndexes(new int[] {0,0});
        assertTrue(testHeaderIntoBody(22, true));
        assertEquals("This is the body. yooo", message.getRequest().getBody());
        assertEquals("this header is irrelevant", message.getRequest().getHeader("header"));
    }

    @Test
    public void testHeaderIntoBody5() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" header: this header is irrelevant\r");
        reqStrings.add("\n");
        reqStrings.add("\r");
        reqStrings.add("\nThis is the body.");
        message.setReqStrings(reqStrings);
        message.setContentLength(17);
        message.setStartIndexes(new int[] {0,0});
        assertTrue(testHeaderIntoBody(17, true));
        assertEquals("This is the body.", message.getRequest().getBody());
    }

    @Test
    public void testHeaderIntoBody6() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" header: this header is irrelevant\r");
        reqStrings.add("\n");
        reqStrings.add("\r\nThis is the body.");
        message.setReqStrings(reqStrings);
        message.setContentLength(17);
        message.setStartIndexes(new int[] {0,0});
        assertTrue(testHeaderIntoBody(17, true));
        assertEquals("This is the body.", message.getRequest().getBody());
    }

    @Test
    public void testHeaderIntoBody7() throws BadRequestException {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" header: this header is irrelevant\r");
        reqStrings.add("\n");
        reqStrings.add("\r\n");
        message.setReqStrings(reqStrings);
        message.setStartIndexes(new int[] {0,0});
        assertTrue(testHeaderIntoBody(-42069, true));
        assertEquals("", message.getRequest().getBody());
    }

    public boolean testHeaderIntoBody(int clExpected, boolean rSet) throws BadRequestException {
        if (rSet) message.getRequest().setRequestLine(new String[] {"GET", "/", "1.1"});
        boolean val = message.parseHeader();
        assertEquals(clExpected, message.getContentLength());
        return val;
    }

    @Test
    public void testHeaderIntoBodyExcept1() {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add(" header: this header is irrelevant\r");
        reqStrings.add("\n\r\nThis is the body. yooo");
        message.setReqStrings(reqStrings);
        message.setStartIndexes(new int[] {0,0});
        testHeaderIntoBodyExcept("Content length is not specified but there is a body.");
    }

    @Test
    public void testHeaderIntoBodyExcept2() {
        ArrayList<String> reqStrings = new ArrayList<>();
        reqStrings.add("headerOne: this is a header \r\n headerTwo: this is another header \r\n\r\n a");
        message.setReqStrings(reqStrings);
        message.setStartIndexes(new int[] {0,0});
        testHeaderIntoBodyExcept("Content length is not specified but there is a body.");
    }

    public void testHeaderIntoBodyExcept(String msg) {
        message.getRequest().setRequestLine(new String[] {"GET", "/", "1.1"});
        BadRequestException thrown = assertThrows(
                BadRequestException.class,
                () -> message.parseHeader()
        );
        assertEquals(msg, thrown.getMessage());
    }
}
