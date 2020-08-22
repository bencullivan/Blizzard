package com.bencullivan.blizzard.http;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Stores the write Selector, BlizzardMessage and BlizzardOutgoingMessage corresponding to a given SocketChannel.
 * @author Ben Cullivan (2020)
 */
public class BlizzardAttachment {

    private SocketChannel channel;
    private Selector writeSelector;
    private BlizzardMessage message;
    private BlizzardOutgoingMessage outMessage;

    public SocketChannel getChannel() {
        return channel;
    }

    public Selector getWriteSelector() {
        return writeSelector;
    }

    public BlizzardMessage getMessage() {
        return message;
    }

    public BlizzardOutgoingMessage getOutMessage() {
        return outMessage;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public void setWriteSelector(Selector writeSelector) {
        this.writeSelector = writeSelector;
    }

    public void setMessage(BlizzardMessage message) {
        this.message = message;
    }

    public void setOutMessage(BlizzardOutgoingMessage outMessage) {
        this.outMessage = outMessage;
    }
}
