package com.bencullivan.blizzard.eventloop;

import com.bencullivan.blizzard.util.BlizzardStore;
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

    private final Selector readSelector;  // the read Selector
    private final Selector writeSelector;  // the write Selector
    private final ArrayBlockingQueue<SocketChannel> acceptedChannels;  // the newly selected SocketChannels
    private final int PROCESSOR_COUNT;  // the number of processor threads
    private final int HB_SIZE;  // the size of the header buffer in a BlizzardMessage

    /**
     * @param readSelector The Selector that chooses which channels are ready to be read from.
     * @param writeSelector The Selector that chooses which channels are ready to be written to.
     * @param store The BlizzardStore containing the concurrent queues.
     * @param processorCount The number of processor threads.
     * @param hbSize The size of the header buffer in a BlizzardMessage.
     */
    public BlizzardAcceptor(Selector readSelector, Selector writeSelector, BlizzardStore store,
                            int processorCount, int hbSize) {
        this.readSelector = readSelector;
        this.writeSelector = writeSelector;
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
                BlizzardAttachment attachment = new BlizzardAttachment();
                attachment.setChannel(channel);
                attachment.setWriteSelector(writeSelector);
                attachment.setMessage(new BlizzardMessage(attachment, HB_SIZE, PROCESSOR_COUNT));
                attachment.setOutMessage(new BlizzardOutgoingMessage(attachment));
                channel.register(readSelector, SelectionKey.OP_READ, attachment);
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
