package com.bencullivan.blizzard.http;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * handles writing http responses to their corresponding channels.
 * @author Ben Cullivan (2020)
 */
public class BlizzardOutgoingMessage {

    private final BlizzardAttachment attachment;  // the object storing the channel and write selector
    private final ArrayBlockingQueue<BlizzardResponse> responses;  // the queue of responses that are ready to be sent
    // through this outgoing message's channel
    private BlizzardResponse current;  // the response that is currently being sent
    private int remainingBytes;  // the number of bytes remaining in the current response
    private boolean writeRegistered;

    /**
     * @param attachment The object that stores the socket channel and write selector.
     */
    public BlizzardOutgoingMessage(BlizzardAttachment attachment) {
        this.attachment = attachment;
        responses = new ArrayBlockingQueue<>(50);
        writeRegistered = false;
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
     * Adds a response to the queue of responses waiting to be written. Registers this channel with the write selector
     * if it is not already registered. This method is synchronized because it will always be called from the processor
     * pool.
     * @param response The response to be added to the queue.
     */
    public synchronized void addResponse(BlizzardResponse response) {
        try {
            responses.put(response);
            // register with the selector if non already registered
            if (!writeRegistered) {
                try {
                    attachment.getChannel().register(attachment.getWriteSelector(), SelectionKey.OP_WRITE, attachment);
                    writeRegistered = true;
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return The response that is currently being sent.
     */
    public ByteBuffer getCurrent() {
        if (current == null && responses.isEmpty()) {
            // if there are no responses waiting to be written, unregister this channel with the selector
            SelectionKey key = attachment.getChannel().keyFor(attachment.getWriteSelector());
            if (key != null) key.cancel();
            writeRegistered = false;
            return null;
        }
        else if (current == null) {
            current = responses.poll();
            remainingBytes = current.getMessage().array().length;
        }
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
            if (current == null) {
                // if there are no responses waiting to be written, unregister this channel with the selector
                SelectionKey key = attachment.getChannel().keyFor(attachment.getWriteSelector());
                if (key != null) key.cancel();
                writeRegistered = false;
            }
        }
    }
}
