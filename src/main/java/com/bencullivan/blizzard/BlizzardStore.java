package com.bencullivan.blizzard;

import com.bencullivan.blizzard.events.Event;
import com.bencullivan.blizzard.events.RouteCallback;
import com.bencullivan.blizzard.http.BlizzardRequest;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Stores data that must be accessible to all threads.
 * @author Ben Cullivan
 */
public class BlizzardStore {

    private final ArrayBlockingQueue<SocketChannel> acceptedChannelQueue;
    private final ArrayBlockingQueue<Event> eventQueue;
    private final ArrayBlockingQueue<BlizzardRequest> requestQueue;
    private final HashMap<Integer, String> reasonPhrases;
    private final PathNode postRoot;  // the root of the trie that will hold POST routes and their callbacks
    private final PathNode getRoot;  // the root of the trie that will hold GET routes and their callbacks
    private final PathNode putRoot;  // the root of the trie that will hold PUT routes and their callbacks
    private final PathNode patchRoot;  // the root of the trie that will hold PATCH routes and their callbacks
    private final PathNode deleteRoot;  // the root of the trie that will hold DELETE routes and their callbacks

    /**
     * @param acceptedChannelQueueSize The size of the queue that will hold newly accepted SocketChannels.
     * @param eventQueueSize The size of the queue that will hold Events waiting to be processed.
     * @param requestQueueSize The size of the queue that will hold BlizzardRequests waiting to be processed. (This
     *                         will also be used as the size of the response queue.)
     */
    public BlizzardStore(int acceptedChannelQueueSize, int eventQueueSize, int requestQueueSize) {
        acceptedChannelQueue = new ArrayBlockingQueue<>(acceptedChannelQueueSize);
        eventQueue = new ArrayBlockingQueue<>(eventQueueSize);
        requestQueue = new ArrayBlockingQueue<>(requestQueueSize);
        reasonPhrases = new HashMap<>();
        initReasonPhrases();
        postRoot = new PathNode();
        getRoot = new PathNode();
        putRoot = new PathNode();
        patchRoot = new PathNode();
        deleteRoot = new PathNode();
    }

    /**
     * Populates the map of http status codes to their corresponding reasons.
     */
    private void initReasonPhrases() {
        // 100s
        reasonPhrases.put(100, "Continue");
        reasonPhrases.put(101, "Switching Protocols");
        // 200s
        reasonPhrases.put(200, "OK");
        reasonPhrases.put(201, "Created");
        reasonPhrases.put(202, "Accepted");
        reasonPhrases.put(203, "Non-Authoritative Information");
        reasonPhrases.put(204, "No Content");
        reasonPhrases.put(205, "Reset Content");
        reasonPhrases.put(206, "Partial Content");
        // 300s
        reasonPhrases.put(300, "Multiple Choice");
        reasonPhrases.put(301, "Moved Permanently");
        reasonPhrases.put(302, "Found");
        reasonPhrases.put(303, "See Other");
        reasonPhrases.put(304, "Not Modified");
        reasonPhrases.put(305, "Use Proxy");
        reasonPhrases.put(307, "Temporary Redirect");
        // 400s
        reasonPhrases.put(400, "Bad Request");
        reasonPhrases.put(401, "Unauthorized");
        reasonPhrases.put(402, "Payment Required");
        reasonPhrases.put(403, "Forbidden");
        reasonPhrases.put(404, "Not Found");
        reasonPhrases.put(405, "Method Not Allowed");
        reasonPhrases.put(406, "Not Acceptable");
        reasonPhrases.put(407, "Proxy Authentication Required");
        reasonPhrases.put(408, "Request Time-out");
        reasonPhrases.put(409, "Conflict");
        reasonPhrases.put(410, "Gone");
        reasonPhrases.put(411, "Length Required");
        reasonPhrases.put(412, "Precondition Failed");
        reasonPhrases.put(413, "Payload Too Large");
        reasonPhrases.put(414, "Request-URI Too Large");
        reasonPhrases.put(415, "Unsupported Media Type");
        reasonPhrases.put(416, "Requested range not satisfiable");
        reasonPhrases.put(417, "Expectation failed");
        // 500s
        reasonPhrases.put(500, "Internal Server Error");
        reasonPhrases.put(501, "Not Implemented");
        reasonPhrases.put(502, "Bad Gateway");
        reasonPhrases.put(503, "Service Unavailable");
        reasonPhrases.put(504, "Gateway Time-out");
        reasonPhrases.put(505, "HTTP Version not supported");
    }

    // INSERTION

    /**
     * Registers a POST route with the server.
     * @param path The url path.
     * @param callback The function to be executed when a request hits this route.
     */
    public void insertPostRoute(String[] path, RouteCallback callback) {
        if (path.length == 0) {
            postRoot.setCallback(callback);
            return;
        }
        insertRoute(path, callback, 1);
    }

    /**
     * Registers a GET route with the server.
     * @param path The url path.
     * @param callback The function to be executed when a request hits this route.
     */
    public void insertGetRoute(String[] path, RouteCallback callback) {
        if (path.length == 0) {
            getRoot.setCallback(callback);
            return;
        }
        insertRoute(path, callback, 2);
    }

    /**
     * Registers a PUT route with the server.
     * @param path The url path.
     * @param callback The function to be executed when a request hits this route.
     */
    public void insertPutRoute(String[] path, RouteCallback callback) {
        if (path.length == 0) {
            putRoot.setCallback(callback);
            return;
        }
        insertRoute(path, callback, 3);
    }

    /**
     * Registers a PATCH route with the server.
     * @param path The url path.
     * @param callback The function to be executed when a request hits this route.
     */
    public void insertPatchRoute(String[] path, RouteCallback callback) {
        if (path.length == 0) {
            patchRoot.setCallback(callback);
            return;
        }
        insertRoute(path, callback, 4);
    }

    /**
     * Registers a DELETE route with the server.
     * @param path The url path.
     * @param callback The function to be executed when a request hits this route.
     */
    public void insertDeleteRoute(String[] path, RouteCallback callback) {
        if (path.length == 0) {
            deleteRoot.setCallback(callback);
            return;
        }
        insertRoute(path, callback, 5);
    }

    /**
     * Inserts a route into the trie corresponding to its method.
     * @param path The url path.
     * @param callback The function to be executed when a request hits this route.
     * @param method The http method of this route.
     */
    private void insertRoute(String[] path, RouteCallback callback, int method) {
        // determine whether the route has parameters
        boolean hasParams = path[path.length-1].charAt(0) == ':';
        int end = hasParams ? path.length-1 : path.length;

        // get a reference to the appropriate root based on which method is being used
        PathNode curr = switch(method) {
            case 1 -> postRoot;
            case 2 -> getRoot;
            case 3 -> putRoot;
            case 4 -> patchRoot;
            default -> deleteRoot;
        };

        // insert this route into the trie
        for (int i = 0; i < end; i++) {
            if (curr.getChild(path[i]) == null) curr.addChild(path[i]);
            curr = curr.getChild(path[i]);
        }

        // add the param node if necessary
        if (hasParams) {
            curr.setHasParams(true);
            PathNode paramNode = new PathNode();
            paramNode.setHasParams(true);
            curr.setParamNode(paramNode);
            curr = curr.getChild("");
            if (path[path.length-1].length() > 1) curr.setParamName(path[path.length-1].substring(1));
        }

        // set the callback
        curr.setCallback(callback);
    }

    // GETTERS

    public ArrayBlockingQueue<SocketChannel> getAcceptedChannelQueue() {
        return acceptedChannelQueue;
    }

    public ArrayBlockingQueue<Event> getEventQueue() {
        return eventQueue;
    }

    public ArrayBlockingQueue<BlizzardRequest> getRequestQueue() {
        return requestQueue;
    }

    public HashMap<Integer, String> getReasonPhrases() {
        return reasonPhrases;
    }

    public PathNode getPostRoot() {
        return postRoot;
    }

    public PathNode getGetRoot() {
        return getRoot;
    }

    public PathNode getPutRoot() {
        return putRoot;
    }

    public PathNode getPatchRoot() {
        return patchRoot;
    }

    public PathNode getDeleteRoot() {
        return deleteRoot;
    }
}
