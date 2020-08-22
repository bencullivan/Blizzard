package com.bencullivan.blizzard.http;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlizzardRequestTest {

    private BlizzardRequest request = new BlizzardRequest(new BlizzardAttachment());

    @AfterEach
    public void resetRequest() {
        request = new BlizzardRequest(new BlizzardAttachment());
    }

    @Test
    public void processQueryTest() {
        request.appendToBody("query1=value1&query2=value2&query3=value3");
        request.processBody();
        assertEquals("value1", request.getQueries().get("query1"));
        assertEquals("value2", request.getQueries().get("query2"));
        assertEquals("value3", request.getQueries().get("query3"));
    }

    @Test
    public void processJSONTest() {
        request.appendToBody("{\"key\": \"value\", \"key2\": [\"a\", \"b\"]}");
        request.processBody();
        JSONObject comp = new JSONObject();
        comp.put("key", "value");
        JSONArray arr = new JSONArray();
        arr.put("a");
        arr.put("b");
        comp.put("key2", arr);
        assertEquals(comp.get("key"), ((JSONObject)request.getBodyJSON()).get("key"));
        assertEquals(((JSONArray)comp.get("key2")).get(0),
                ((JSONArray)((JSONObject)request.getBodyJSON()).get("key2")).get(0));
        assertEquals(((JSONArray)comp.get("key2")).get(1),
                ((JSONArray)((JSONObject)request.getBodyJSON()).get("key2")).get(1));
    }
}
