package com.bencullivan.blizzard.http;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * handles writing http responses to their corresponding channels.
 * @author Ben Cullivan (2020)
 */
public class BlizzardOutgoingMessage {

    private final ArrayBlockingQueue<BlizzardResponse> responses;  // the queue of responses that are ready to be sent
    // through this outgoing message's channel
    private BlizzardResponse current;  // the response that is currently being sent
    private int remainingBytes;  // the number of bytes remaining in the current response

    public BlizzardOutgoingMessage() {
        responses = new ArrayBlockingQueue<>(50);
    }

    /**
     * @return Whether this outgoing message has any responses to send.
     */
    public boolean hasResponses() {
        return current != null || !responses.isEmpty();
    }

    /**
     * @return The queue of responses waiting to be sent through this outgoing message's channel.
     */
    public ArrayBlockingQueue<BlizzardResponse> getResponses() {
        return responses;
    }

    /**
     * @return The response that is currently being sent.
     */
    public ByteBuffer getCurrent() {
        if (responses.isEmpty()) return null;
        if (current == null) {
            current = responses.poll();
            remainingBytes = current.getMessage().array().length;
        }
        current.getMessage().flip();
        return current.getMessage();
    }

    /**
     * Decrements the amount of bytes that must be read from the current response's buffer. Updates the current response
     * if the previous one is done being sent.
     * @param bytesRead The number of bytes read from the current response's buffer.
     */
    public void updateRemaining(int bytesRead) {
        if ((remainingBytes -= bytesRead) <= 0) {
            current = responses.poll();
        }
    }
}
