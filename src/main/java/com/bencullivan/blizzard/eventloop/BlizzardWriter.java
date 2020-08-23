package com.bencullivan.blizzard.eventloop;

import com.bencullivan.blizzard.http.BlizzardAttachment;
import com.bencullivan.blizzard.http.BlizzardOutgoingMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * Handles writing http responses to channels that are available to be written to.
 * @author Ben Cullivan (2020)
 */
public class BlizzardWriter {

    private final Selector selector;

    /**
     * @param selector The selector that selects which SocketChannels are ready to be written to.
     */
    public BlizzardWriter(Selector selector) {
        this.selector = selector;
    }

    /**
     * Writes bytes from http responses into their corresponding SocketChannels.
     */
    public void write() {
        try {
            if (selector.selectNow() == 0) return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // find out which registered channels can be written to
        Set<SelectionKey> keys = selector.selectedKeys();

        for (SelectionKey key: keys) {
            // get the outgoing message of this key
            BlizzardOutgoingMessage outMessage = ((BlizzardAttachment) key.attachment()).getOutMessage();
            ByteBuffer output = outMessage.getCurrent();
            if (output == null) continue;
            try {
                // write to the channel
                outMessage.updateRemaining(((SocketChannel) key.channel()).write(output));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
