package com.bencullivan.blizzard.http;

import com.bencullivan.blizzard.util.BlizzardStore;
import com.bencullivan.blizzard.util.FileReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class BlizzardResponseTest {

    private BlizzardResponse response = new BlizzardResponse(
            new BlizzardStore(1,1,1).getReasonPhrases());

    @AfterEach
    public void resetResponse() {
        response = new BlizzardResponse(
                new BlizzardStore(1,1,1).getReasonPhrases());
    }

    @Test
    public void resTextTest() {
        response.sendText("This is a test").sendStatus(200);
        response.finish();
        String expected = "HTTP/1.1 200 OK\r\n" +
                "Date:" + response.getTime() + "\r\n" +
                "Content-Type:text/plain; charset=UTF-8\r\n" +
                "Content-Length:14\r\n" +
                "\r\nThis is a test";
        assertArrayEquals(expected.getBytes(StandardCharsets.UTF_8), response.getMessage().array());
    }

    @Test
    public void resJSONObjectTest() {
        JSONObject obj = new JSONObject();
        obj.put("key1", "val1");
        obj.put("key2", 5);
        JSONArray arr = new JSONArray();
        arr.put(5);
        arr.put(false);
        arr.put("yay");
        obj.put("arr", arr);
        response.sendJSON(obj).sendStatus(200);
        response.finish();
        String expected = "HTTP/1.1 200 OK\r\n" +
                "Date:" + response.getTime() + "\r\n" +
                "Content-Type:application/json\r\n" +
                "Content-Length:" + (obj.toString().getBytes(StandardCharsets.UTF_8).length) + "\r\n\r\n" +
                obj.toString();
        assertArrayEquals(expected.getBytes(StandardCharsets.UTF_8), response.getMessage().array());
    }

    @Test
    public void resJSONArrayTest() {
        JSONArray arr = new JSONArray();
        arr.put(5);
        arr.put(false);
        arr.put("yay");
        response.sendJSON(arr).sendStatus(200);
        response.finish();
        String expected = "HTTP/1.1 200 OK\r\n" +
                "Date:" + response.getTime() + "\r\n" +
                "Content-Type:application/json\r\n" +
                "Content-Length:" + (arr.toString().getBytes(StandardCharsets.UTF_8).length) + "\r\n\r\n" +
                arr.toString();
        assertArrayEquals(expected.getBytes(StandardCharsets.UTF_8), response.getMessage().array());
    }

    @Test
    public void resFileTest() throws IOException {
        response.sendFile(System.getProperty("user.dir") +
                "/src/test/resources/html-res-test.html").sendStatus(200);
        response.finish();
        String fileString = new FileReader(System.getProperty("user.dir") +
                "/src/test/resources/html-res-test.html").readFile().getFileString();
        String expected = "HTTP/1.1 200 OK\r\n" +
                "Date:" + response.getTime() + "\r\n" +
                "Content-Type:text/html; charset=UTF-8\r\n" +
                "Content-Length:" + fileString.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "\r\n" + fileString;
        assertArrayEquals(expected.getBytes(StandardCharsets.UTF_8), response.getMessage().array());
    }
}
