package com.bencullivan.blizzard;

import com.bencullivan.blizzard.http.BlizzardMessage;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

class BlizzardAcceptor {

    private final Selector selector;
    private final ArrayBlockingQueue<SocketChannel> acceptedChannels;
    private final int PROCESSOR_COUNT;
    private final int HB_SIZE;

    public BlizzardAcceptor(Selector selector, BlizzardStore store, int processorCount, int hbSize) {
        this.selector = selector;
        acceptedChannels = store.getAcceptedChannelQueue();
        PROCESSOR_COUNT = processorCount;
        HB_SIZE = hbSize;
    }

    public void accept() {
        // accept a max of eight connections in one iteration of the event loop
        int it = 8;
        SocketChannel channel = acceptedChannels.poll();
        while (channel != null) {
            try {
                channel.configureBlocking(false);
                SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
                key.attach(new BlizzardMessage(HB_SIZE, PROCESSOR_COUNT));
            } catch (IOException e) {
                System.out.println("Unable to configure nonblocking channel:");
                e.printStackTrace();
                return;
            }
            if (it-- <= 0) break;
            channel = acceptedChannels.poll();
        }
    }
}
