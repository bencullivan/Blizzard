package com.bencullivan.blizzard.events;

import com.bencullivan.blizzard.BlizzardStore;
import com.bencullivan.blizzard.PathNode;
import com.bencullivan.blizzard.http.BlizzardRequest;
import com.bencullivan.blizzard.http.BlizzardResponse;

import java.util.HashMap;

/**
 * Processes a request by calling the user-defined callback corresponding to the route of the request.
 * Creates an http response and prepares it to be sent back to the client.
 * This class's execute method is always run on a processor thread.
 * @author Ben Cullivan (2020)
 */
public class ProcessRequestEvent implements Event {

    private final BlizzardRequest request;
    private final HashMap<Integer, String> reasonPhrases;
    private final PathNode postRoot;  // the root of the trie that stores the POST callbacks
    private final PathNode getRoot;  // the root of the trie that stores the GET callbacks
    private final PathNode putRoot;  // the root of the trie that stores the PUT callbacks
    private final PathNode patchRoot;  // the root of the trie that stores the PATCH callbacks
    private final PathNode deleteRoot;  // the root of the trie that stores the DELETE routes

    /**
     * @param request The http request.
     * @param store The BlizzardStore that stores the queues and route callbacks.
     */
    public ProcessRequestEvent(BlizzardRequest request, BlizzardStore store) {
        this.request = request;
        reasonPhrases = store.getReasonPhrases();
        postRoot = store.getPostRoot();
        getRoot = store.getGetRoot();
        putRoot = store.getPutRoot();
        patchRoot = store.getPatchRoot();
        deleteRoot = store.getDeleteRoot();
    }

    @Override
    public void execute() {
        BlizzardResponse response = new BlizzardResponse(reasonPhrases);
        // if the request is bad, update the response accordingly
        if (request.isBadRequest()) {
            response.sendStatus(switch (request.getBadRequestType()) {
                case CONTENT_LENGTH_MISSING -> 411;
                case HEADERS_TOO_LARGE -> 413;
                default -> 400;
            });
            response.finish();
            try {
                request.getAttachment().getOutMessage().getResponses().put(response);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        // set the http version
        response.setVersion(request.getVersion());
        // find the correct root
        PathNode root = switch(request.getMethod()) {
            case "POST" -> postRoot;
            case "GET" -> getRoot;
            case "PUT" -> putRoot;
            case "PATCH" -> patchRoot;
            default -> deleteRoot;
        };
        // find the route callback
        String[] path = request.getUri().split("/");
        for (int i = 0; i < path.length && root != null; i++) root = root.getChild(path[i]);
        if (root == null) {
            // this route does not exist
            response.sendStatus(404);
            response.finish();
            try {
                request.getAttachment().getOutMessage().getResponses().put(response);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        // process the route parameter and query string
        if (root.hasParams()) request.setParameter(path[path.length-1]);
        request.processQuery();
        // call the user-defined callback
        if (root.getCallback() != null) root.getCallback().call(request, response);
        // perform cleanup and convert the response to a bytebuffer
        response.finish();
        // the response is ready for writing
        try {
            request.getAttachment().getOutMessage().getResponses().put(response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
