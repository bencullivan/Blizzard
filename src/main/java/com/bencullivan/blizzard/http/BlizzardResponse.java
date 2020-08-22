package com.bencullivan.blizzard.http;

import com.bencullivan.blizzard.util.FileReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

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
    private String contentType;  // the content-type of the response body
    private String time;  // the server time

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
        contentType = "";
        time = "";
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
        if (text == null || text.length() == 0) return this;
        body.append(text);
        contentType = "text/plain; charset=UTF-8";
        return this;
    }

    /**
     * Appends the String version of the JSONObject to the body.
     * @param object The JSONObject to be sent.
     * @return This BlizzardResponse.
     */
    public BlizzardResponse sendJSON(JSONObject object) {
        if (object == null) return this;
        body.append(object.toString());
        contentType = "application/json";
        return this;
    }

    /**
     * Appends the String version of the JSONArray to the body.
     * @param array The JSONArray to be sent.
     * @return This BlizzardResponse.
     */
    public BlizzardResponse sendJSON(JSONArray array) {
        if (array == null) return this;
        body.append(array.toString());
        contentType = "application/json";
        return this;
    }

    /**
     * Sends the specified file to the user.
     * @param filePath The path of the file to be sent.
     * @return This BlizzardResponse.
     */
    public BlizzardResponse sendFile(String filePath) {
        if (filePath == null || filePath.length() == 0) return this;
        try {
            FileReader reader = new FileReader(filePath);
            reader.readFile();
            body.append(reader.getFileString());
            reader.close();
            contentType = "text/html; charset=UTF-8";
        } catch (IOException e) {
            System.out.println("Are you sure there is a readable file with that path?");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * @return The current time in http response format
     */
    private void setTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("EDT"));
        time = dateFormat.format(Calendar.getInstance().getTime());
    }

    /**
     * Appends the header line to the response.
     */
    public void composeHeaderLine() {
        // response line
        response.append(version).append(' ').append(statusCode).append(' ').append(reasonPhrase).append("\r\n");

    }

    private void addHeader(String field, String value) {
        response.append(field).append(':').append(value).append("\r\n");
    }

    /**
     * Performs cleanup before this response is sent to the client.
     */
    public void finish() {
        setTime();
        composeHeaderLine();
        addHeader("Date", time);
        // add the content type and content length headers if necessary
        if (!contentType.equals("")) {
            addHeader("Content-Type", contentType);
            addHeader("Content-Length", String.valueOf(body.toString().getBytes(StandardCharsets.UTF_8).length));
        }
        response.append("\r\n").append(body);
        message = ByteBuffer.wrap(response.toString().getBytes(StandardCharsets.UTF_8));
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

    /**
     * @return The time (for testing purposes).
     */
    String getTime() {
        return time;
    }
}
