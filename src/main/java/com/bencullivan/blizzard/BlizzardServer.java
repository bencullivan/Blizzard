package com.bencullivan.blizzard;

import com.bencullivan.blizzard.eventloop.BlizzardEventLoop;
import com.bencullivan.blizzard.eventloop.BlizzardListener;
import com.bencullivan.blizzard.events.RouteCallback;
import com.bencullivan.blizzard.util.BlizzardStore;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * This is where all the magic happens.
 * Registers routes, creates the event loop and processor pool, and listens on the desired port.
 * @author Ben Cullivan.
 */
public class BlizzardServer {

    private final int DEFAULT_PORT;
    private final int PROCESSOR_COUNT;
    private final int HB_SIZE;
    private final BlizzardStore store;

    public BlizzardServer() {
        this(20, 2000, 2000, 2000, 2048);
    }

    /**
     * @param processorCount The number of processors in the processor pool.
     * @param acceptedChannelQueueSize The size of the queue that will hold newly accepted SocketChannels.
     * @param eventQueueSize The size of the queue that will hold events that need to be processed.
     * @param requestQueueSize The size of the queue that will hold requests that need to be processed.
     * @param hbSize The size of the header buffer of a BlizzardMessage.
     */
    public BlizzardServer(int processorCount, int acceptedChannelQueueSize, int eventQueueSize, int requestQueueSize,
                          int hbSize) {
        DEFAULT_PORT = 5000;
        PROCESSOR_COUNT = processorCount;
        store = new BlizzardStore(acceptedChannelQueueSize, eventQueueSize, requestQueueSize);
        HB_SIZE = hbSize;
    }

    /**
     * Registers a POST route.
     * @param path The route path.
     * @param callback The callback that will be executed when an HTTP request hits this route.
     */
    public void post(String path, RouteCallback callback) {
        store.insertPostRoute(path.split("/"), callback);
    }

    /**
     * Registers a GET route.
     * @param path The route path.
     * @param callback The callback that will be executed when an HTTP request hits this route.
     */
    public void get(String path, RouteCallback callback) {
        store.insertGetRoute(path.split("/"), callback);
    }

    /**
     * Registers a PUT route.
     * @param path The route path.
     * @param callback The callback that will be executed when an HTTP request hits this route.
     */
    public void put(String path, RouteCallback callback) {
        store.insertPutRoute(path.split("/"), callback);
    }

    /**
     * Registers a PATCH route.
     * @param path The route path.
     * @param callback The callback that will be executed when an HTTP request hits this route.
     */
    public void patch(String path, RouteCallback callback) {
        store.insertPatchRoute(path.split("/"), callback);
    }

    /**
     * Registers a DELETE route.
     * @param path The route path.
     * @param callback The callback that will be executed when an HTTP request hits this route.
     */
    public void delete(String path, RouteCallback callback) {
        store.insertDeleteRoute(path.split("/"), callback);
    }

    /**
     * Starts the event loop and listens on the specified port.
     * @param port The port to listen on.
     */
    public void listen(int port) {
        // create the listener that will listen for incoming socket connections on a separate thread
        BlizzardListener listener = new BlizzardListener(port, store);

        // create the reader selector that will select channels that can be read from
        // and the write selector that will select channels that can be written to
        Selector readerSelector;
        Selector writeSelector;
        try {
            readerSelector = Selector.open();
            writeSelector = Selector.open();
        } catch (IOException e) {
            System.out.println("There was an error listening on port " + port);
            return;
        }

        // create the event loop that will handle message reading, writing, and processing on the main thread
        BlizzardEventLoop eventLoop = new BlizzardEventLoop(readerSelector, writeSelector, store,
                PROCESSOR_COUNT, HB_SIZE);

        // create the processor pool
        eventLoop.createProcessorPool();

        // start the listener on a separate Thread
        new Thread(listener).start();

        // inform the user that the server is now open to connections
        System.out.println("Ready for connections on port " + port);

        // start the event loop on the main thread
        eventLoop.start();
    }

    /**
     * Starts the event loop and listens on the default port.
     */
    public void listen() {
        listen(DEFAULT_PORT);
    }
}
