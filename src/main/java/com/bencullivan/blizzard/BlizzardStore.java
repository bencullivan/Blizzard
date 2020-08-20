package com.bencullivan.blizzard;

import com.bencullivan.blizzard.http.BlizzardRequest;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

class BlizzardStore {

    private final ArrayBlockingQueue<SocketChannel> acceptedChannelQueue;
    private final ArrayBlockingQueue<Event> eventQueue;
    private final ArrayBlockingQueue<BlizzardRequest> requestQueue;

    public BlizzardStore(int acceptedChannelQueueSize, int eventQueueSize, int requestQueueSize) {
        acceptedChannelQueue = new ArrayBlockingQueue<>(acceptedChannelQueueSize);
        eventQueue = new ArrayBlockingQueue<>(eventQueueSize);
        requestQueue = new ArrayBlockingQueue<>(requestQueueSize);
    }

    // GETTERS

    public synchronized ArrayBlockingQueue<SocketChannel> getAcceptedChannelQueue() {
        return acceptedChannelQueue;
    }

    public synchronized ArrayBlockingQueue<Event> getEventQueue() {
        return eventQueue;
    }

    public synchronized ArrayBlockingQueue<BlizzardRequest> getRequestQueue() {
        return requestQueue;
    }

}
