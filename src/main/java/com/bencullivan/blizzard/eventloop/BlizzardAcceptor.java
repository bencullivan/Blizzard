package com.bencullivan.blizzard.eventloop;

import com.bencullivan.blizzard.BlizzardStore;
import com.bencullivan.blizzard.http.BlizzardAttachment;
import com.bencullivan.blizzard.http.BlizzardMessage;
import com.bencullivan.blizzard.http.BlizzardOutgoingMessage;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Registers newly accepted SocketChannels with the read Selector.
 * @author BenCullivan (2020)
 */
public class BlizzardAcceptor {

    private final Selector selector;  // the read Selector
    private final ArrayBlockingQueue<SocketChannel> acceptedChannels;  // the newly selected SocketChannels
    private final int PROCESSOR_COUNT;  // the number of processor threads
    private final int HB_SIZE;  // the size of the header buffer in a BlizzardMessage

    /**
     * @param selector The read Selector.
     * @param store The BlizzardStore containing the concurrent queues.
     * @param processorCount The number of processor threads.
     * @param hbSize The size of the header buffer in a BlizzardMessage.
     */
    public BlizzardAcceptor(Selector selector, BlizzardStore store, int processorCount, int hbSize) {
        this.selector = selector;
        acceptedChannels = store.getAcceptedChannelQueue();
        PROCESSOR_COUNT = processorCount;
        HB_SIZE = hbSize;
    }

    /**
     * Registers up to eight SocketChannels with the read Selector.
     */
    public void accept() {
        // accept a max of eight connections in one iteration of the event loop
        int it = 8;
        SocketChannel channel = acceptedChannels.poll();
        while (channel != null) {
            try {
                channel.configureBlocking(false);
                SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
                BlizzardAttachment attachment = new BlizzardAttachment();
                attachment.setMessage(new BlizzardMessage(attachment, HB_SIZE, PROCESSOR_COUNT));
                attachment.setOutMessage(new BlizzardOutgoingMessage());
                key.attach(attachment);
            } catch (IOException e) {
                System.out.println("Unable to configure nonblocking channel:");
                e.printStackTrace();
                return;
            }
            if (it-- <= 0) return;
            channel = acceptedChannels.poll();
        }
    }
}
