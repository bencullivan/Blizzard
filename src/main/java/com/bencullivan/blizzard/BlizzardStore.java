package com.bencullivan.blizzard;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

class BlizzardStore {

    private final ArrayBlockingQueue<SocketChannel> acceptedChannelQueue;

    public BlizzardStore(int acceptedChannelQueueSize) {
        acceptedChannelQueue = new ArrayBlockingQueue<>(acceptedChannelQueueSize);
    }

    // GETTERS

    public ArrayBlockingQueue<SocketChannel> getAcceptedChannelQueue() {
        return acceptedChannelQueue;
    }

}
