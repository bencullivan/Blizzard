package com.bencullivan.blizzard;

import java.io.IOException;
import java.nio.channels.Selector;

public class BlizzardServer {

    private final int DEFAULT_PORT;
    private final int PROCESSOR_COUNT;
    private final int HB_SIZE;
    private final BlizzardStore STORE;

    public BlizzardServer() {
        this(12, 2000, 2000, 2000, 2048);
    }

    public BlizzardServer(int processorCount, int acceptedChannelQueueSize, int eventQueueSize, int requestQueueSize,
                          int hbSize) {
        DEFAULT_PORT = 5000;
        PROCESSOR_COUNT = processorCount;
        STORE = new BlizzardStore(acceptedChannelQueueSize, eventQueueSize, requestQueueSize);
        HB_SIZE = hbSize;
    }

    public void addMiddleware() {

    }

    public void post(String path, RouteCallback callback) {

    }

    public void get(String path, RouteCallback callback) {

    }

    public void put(String path, RouteCallback callback) {

    }

    public void patch(String path, RouteCallback callback) {

    }

    public void delete(String path, RouteCallback callback) {

    }

    public void listen() {
        listen(DEFAULT_PORT);
    }

    public void listen(int port) {
        // create the listener that will listen for incoming socket connections on a separate thread
        BlizzardListener listener = new BlizzardListener(port, STORE);

        // create the reader selector that will select channels that can be read from
        Selector readerSelector;
        try {
            readerSelector = Selector.open();
        } catch (IOException e) {
            System.out.println("There was an error listening on port " + port);
            return;
        }
        // create the event loop that will handle message reading, writing, and processing on the main thread
        BlizzardEventLoop eventLoop = new BlizzardEventLoop(readerSelector, STORE, PROCESSOR_COUNT, HB_SIZE);

        // start the listener on a separate Thread
        new Thread(listener).start();

        // inform the user that the server is now open to connections
        System.out.println("Ready for connections on port " + port);

        // start the event loop on the main thread
        eventLoop.start();
    }
}
