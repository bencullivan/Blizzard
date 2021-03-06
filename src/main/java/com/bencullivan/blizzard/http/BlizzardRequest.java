package com.bencullivan.blizzard.http;

import com.bencullivan.blizzard.http.exceptions.BadRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Stores all the data associated with an http request.
 * @author Ben Cullivan (2020)
 */
public class BlizzardRequest {

    private BlizzardAttachment attachment;  // the object that contains the message and outgoing message that are
    // used for channel reading and writing
    private String DEFAULT_RETURN_VAL = "none";  // returned when a request line field is missing
    private HashMap<String, String> headers;  // contains all of the header fields mapped to their values
    private HashMap<String, String> queries;  // contains all of the url queries
    private String[] requestLine;  // contains the three parts of the request line
    private StringBuffer body;  // contains the body of the message
    private Object bodyJSON;
    private String param;  // the route parameters, if there are any
    private boolean badRequest;  // whether this http request is a bad request
    private BadRequest badRequestType; // the type of bad request that this is

    /**
     * @param attachment The object containing the BlizzardMessage, and the BlizzardOutgoingMessage corresponding to the
     *                   channel that this request came from.
     */
    public BlizzardRequest(BlizzardAttachment attachment) {
        this.attachment = attachment;
        requestLine = new String[0];
        headers = new HashMap<>();
        queries = new HashMap<>();
        body = new StringBuffer();
        badRequest = false;
        badRequestType = null;
    }

    /**
     * @return A reference to the BlizzardAttachment object corresponding to the channel that this request came from.
     */
    public BlizzardAttachment getAttachment() {
        return attachment;
    }

    /**
     * Sets the request line of this request.
     * @param requestLine The String array that contains the three parts of this request's request line.
     */
    void setRequestLine(String[] requestLine) {
        this.requestLine = requestLine;
    }

    /**
     * @return The http method of this request. e.g. "GET", "POST"
     */
    public String getMethod() {
        return requestLine.length == 0 ? DEFAULT_RETURN_VAL : requestLine[0];
    }

    /**
     * @return The URI of this request. e.g. "/posts/all"
     */
    public String getUri() {
        return requestLine.length < 2 ? DEFAULT_RETURN_VAL : requestLine[1];
    }

    /**
     * @return The http version of this request. e.g. "HTTP/1.1", "HTTP/2"
     */
    public String getVersion() {
        return requestLine.length < 3 ? DEFAULT_RETURN_VAL : requestLine[2];
    }

    /**
     * @return Whether the request line has been set.
     */
    boolean requestLineIsSet() {
        return requestLine.length == 3;
    }

    /**
     * Sets a header for this http request.
     * @param field The header field.
     * @param value The header value.
     */
    void setHeader(String field, String value) {
        headers.put(field, value);
    }

    /**
     * Gets the value corresponding to a header field of this request.
     * @param field The header field. (This must be in lowercase.)
     * @return The value of the header field or null if the field has not been set.
     */
    public String getHeader(String field) {
        return headers.get(field);
    }

    /**
     * Appends the given String to the body of this request.
     * @param partialBody The part of the body to append.
     */
    void appendToBody(String partialBody) {
        body.append(partialBody);
    }

    /**
     * @return The body of this request.
     */
    public String getBody() {
        return body.toString();
    }

    /**
     * Sets the url parameter for this request.
     * @param parameter The url parameter to be set.
     */
    public void setParameter(String parameter) {
        param = parameter;
    }

    /**
     * @return The url parameter of this request (if there is one).
     */
    public String getParameter() {
        return param;
    }

    /**
     * @return The map of url queries of this request.
     */
    public HashMap<String, String> getQueries() {
        return queries;
    }

    /**
     * @return The JSONObject of JSONArray of the body.
     */
    public Object getBodyJSON() {
        return bodyJSON;
    }

    /**
     * Sets whether this request is bad.
     * @param badRequest Whether this request is bad.
     */
    public void setBadRequest(boolean badRequest) {
        this.badRequest = badRequest;
    }

    /**
     * Sets the type of bad request that this request is.
     * @param badRequestType The type of bad request.
     */
    public void setBadRequestType(BadRequest badRequestType) {
        this.badRequestType = badRequestType;
    }

    /**
     * @return Whether this request is bad.
     */
    public boolean isBadRequest() {
        return badRequest;
    }

    /**
     * @return The type of BadRequest.
     */
    public BadRequest getBadRequestType() {
        return badRequestType;
    }

    /**
     * @param attachment The BlizzardAttachment to attach to this BlizzardRequest.
     */
    public void setAttachment(BlizzardAttachment attachment) {
        this.attachment = attachment;
    }

    /**
     * Frees this request's data for garbage collection.
     */
    public void clear() {
        DEFAULT_RETURN_VAL = null;
        headers = null;
        requestLine = null;
        body = null;
        bodyJSON = null;
        queries = null;
    }

    /**
     * Either processes queries if there are some or processes JSON if the body is JSON.
     */
    public void processBody() {
        int i = 0;
        while (i < body.length() && Character.isWhitespace(body.charAt(i))) i++;
        if (i == body.length()) return;
        if ((body.charAt(i) == '{' || body.charAt(i) == '[')) processJSON(i);
        else processQuery(i);
    }

    /**
     * Adds any url queries to the map of queries.
     */
    void processQuery(int i) {
        String[] queries = body.substring(i).split("&");
        if (queries.length == 1) return;
        for (String query: queries) {
            String[] parsed = query.split("=");
            if (parsed.length != 2) continue;
            this.queries.put(parsed[0], parsed[1]);
        }
        body = new StringBuffer();
    }

    /**
     * Parses a JSON body into an object that can be accessed by the user.
     */
    void processJSON(int i) {
        if (body.charAt(i) == '{') bodyJSON = new JSONObject(body.substring(i));
        else bodyJSON = new JSONArray(body.substring(i));
        body = new StringBuffer();
    }
}
