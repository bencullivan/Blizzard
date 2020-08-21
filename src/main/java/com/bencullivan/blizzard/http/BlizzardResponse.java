package com.bencullivan.blizzard.http;

import com.bencullivan.blizzard.util.FileReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Holds the data associated with an http response.
 * @author Ben Cullivan (2020)
 */
public class BlizzardResponse {

    private final HashMap<Integer, String> reasonPhrases;  // http status code reasons
    private ByteBuffer message;  // this http response (in bytes)
    private StringBuffer response;  // the String version of this http response
    private StringBuffer body;  // the String version of the body of this http response
    private String version;  // the http version of this response
    private int statusCode;  // the status code of this response
    private String reasonPhrase;  // the reason for this response's status

    /**
     * @param reasonPhrases Map of the reasons corresponding to the various http status codes.
     */
    public BlizzardResponse(HashMap<Integer, String> reasonPhrases) {
        this.reasonPhrases = reasonPhrases;
        response = new StringBuffer();
        body = new StringBuffer();
        version =  "HTTP/1.1";
        statusCode = 404;
        reasonPhrase = reasonPhrases.get(404);
    }

    /**
     * Sets the http version of this response.
     * @param version The http version of this response.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Sets the status code of the response.
     * @param code The status code.
     */
    public void sendStatus(int code) {
        reasonPhrase = reasonPhrases.get(code);
        if (reasonPhrase == null) {
            statusCode = 500;
            reasonPhrase = reasonPhrases.get(500);
        } else {
            statusCode = code;
        }
    }

    /**
     * Adds the provided text to the response body.
     * @param text The text to send.
     * @return This BlizzardResponse
     */
    public BlizzardResponse sendText(String text) {
        body.append(text);
        return this;
    }

    /**
     * Appends the String version of the JSONObject to the body.
     * @param object The JSONObject to be sent.
     * @return This BlizzardResponse.
     */
    public BlizzardResponse sendJSON(JSONObject object) {
        body.append(object.toString());
        return this;
    }

    /**
     * Appends the String version of the JSONArray to the body.
     * @param array The JSONArray to be sent.
     * @return This BlizzardResponse.
     */
    public BlizzardResponse sendJSON(JSONArray array) {
        body.append(array.toString());
        return this;
    }

    /**
     * Sends the specified file to the user.
     * @param filePath The path of the file to be sent.
     * @return This BlizzardResponse.
     */
    public BlizzardResponse sendFile(String filePath) {
        try {
            FileReader reader = new FileReader(filePath);
            reader.readFile();
            body.append(reader.getFileString());
            reader.close();
        } catch (IOException e) {
            System.out.println("Are you sure there is a readable file with that path?");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Appends the header line to the response.
     */
    public void composeHeaders() {
        // response line
        response.append(version).append(' ').append(statusCode).append(' ').append(reasonPhrase).append("\r\n");

    }

    /**
     * Performs cleanup before this response is sent to the client.
     */
    public void finish() {
        composeHeaders();
        response.append("\r\n").append(body);
        message = ByteBuffer.wrap(response.toString().getBytes());
        // get rid of references to the Strings that are no longer used
        response = null;
        body = null;
        version = null;
        reasonPhrase = null;
    }

    /**
     * @return The ByteBuffer containing the http response to be sent.
     */
    public ByteBuffer getMessage() {
        return message;
    }
}
