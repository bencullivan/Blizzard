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

    /**
     * @return The SocketChannel that this BlizzardAttachment is attached to.
     */
    public SocketChannel getChannel() {
        return channel;
    }

    /**
     * @return The Selector that this BlizzardAttachment's SocketChannel should be registered with (for writing).
     */
    public Selector getWriteSelector() {
        return writeSelector;
    }

    /**
     * @return The BlizzardMessage that handles parsing input from this BlizzardAttachment's SocketChannel.
     */
    public BlizzardMessage getMessage() {
        return message;
    }

    /**
     * @return The BlizzardOutgoingMessage that handles providing bytes to be written to this BlizzardAttachment's
     * SocketChannel.
     */
    public BlizzardOutgoingMessage getOutMessage() {
        return outMessage;
    }

    /**
     * @param channel The SocketChannel that this BlizzardAttachment should be attached to.
     */
    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * @param writeSelector The Selector that this BlizzardAttachment's SocketChannel should be registered with
     *                      (for writing).
     */
    public void setWriteSelector(Selector writeSelector) {
        this.writeSelector = writeSelector;
    }

    /**
     * @param message The BlizzardMessage that will handle parsing input from this BlizzardAttachment's SocketChannel.
     */
    public void setMessage(BlizzardMessage message) {
        this.message = message;
    }

    /**
     * @param outMessage The BlizzardOutgoingMessage that handles providing bytes to be written to this
     *                   BlizzardAttachment's SocketChannel.
     */
    public void setOutMessage(BlizzardOutgoingMessage outMessage) {
        this.outMessage = outMessage;
    }
}
