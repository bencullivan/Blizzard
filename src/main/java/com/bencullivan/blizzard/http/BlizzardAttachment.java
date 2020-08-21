package com.bencullivan.blizzard.http;

/**
 * Stores the BlizzardMessage and BlizzardOutgoingMessage corresponding to a given SocketChannel.
 * @author Ben Cullivan (2020)
 */
public class BlizzardAttachment {

    private BlizzardMessage message;
    private BlizzardOutgoingMessage outMessage;

    public BlizzardMessage getMessage() {
        return message;
    }

    public BlizzardOutgoingMessage getOutMessage() {
        return outMessage;
    }

    public void setMessage(BlizzardMessage message) {
        this.message = message;
    }

    public void setOutMessage(BlizzardOutgoingMessage outMessage) {
        this.outMessage = outMessage;
    }
}
