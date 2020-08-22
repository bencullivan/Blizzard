package com.bencullivan.blizzard.util;

import com.bencullivan.blizzard.events.RouteCallback;

import java.util.HashMap;

/**
 * A node in the trie structure that is used to store the routes and their callbacks.
 * @author Ben Cullivan (2020)
 */
public class PathNode {

    private final HashMap<String, PathNode> children;
    private RouteCallback callback;
    private boolean hasParams;
    private PathNode paramNode;
    private String paramName;

    public PathNode() {
        children = new HashMap<>();
        hasParams = false;
        paramName = "default";
    }

    /**
     * Adds a child node to this PathNode's map of children.
     * @param path The path section of the child node that will be added.
     */
    public void addChild(String path) {
        children.put(path, new PathNode());
    }

    /**
     * @param path The path section of the child node.
     * @return A child node with a path section corresponding to path. Or null if there is no child with this
     * path section.
     */
    public PathNode getChild(String path) {
        return hasParams ? paramNode : children.get(path);
    }

    /**
     * @param hasParams Whether the route that this PathNode is at the end of has url parameters.
     */
    public void setHasParams(boolean hasParams) {
        this.hasParams = hasParams;
    }

    /**
     * @return Whether this Node is at the end of a route that has parameters.
     */
    public boolean hasParams() {
        return hasParams;
    }

    /**
     * @param paramNode The node that corresponds to urls to this route that have parameters.
     */
    public void setParamNode(PathNode paramNode) {
        this.paramNode = paramNode;
    }

    /**
     * @param paramName The name of the parameter corresponding to this route. (If there is one.)
     */
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    /**
     * @return The name of the parameter to this route (if there is one).
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * @param callback The callback corresponding to the route that this node is at the end of.
     */
    public void setCallback(RouteCallback callback) {
        this.callback = callback;
    }

    /**
     * @return The callback corresponding to the route that this node is at the end of.
     */
    public RouteCallback getCallback() {
        return callback;
    }

    /**
     * @return The map containing the children of this node.
     */
    public HashMap<String, PathNode> getChildren() {
        return children;
    }

    /**
     * @return Whether this node has any child nodes.
     */
    public boolean hasChildren() {
        return !children.isEmpty() || paramNode != null;
    }
}
