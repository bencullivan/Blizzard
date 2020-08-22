package com.bencullivan.blizzard.eventloop;

import com.bencullivan.blizzard.util.BlizzardStore;
import com.bencullivan.blizzard.events.Event;
import com.bencullivan.blizzard.events.ProcessMessageEvent;
import com.bencullivan.blizzard.http.BlizzardAttachment;
import com.bencullivan.blizzard.http.BlizzardRequest;
import com.bencullivan.blizzard.http.BlizzardMessage;
import com.bencullivan.blizzard.http.exceptions.BadRequest;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Reads data from SocketChannels into BlizzardMessages and then sends the BlizzardMessages to be processed.
 * @author Ben Cullivan
 */
public class BlizzardReader{

    private final Selector selector;
    private final ArrayBlockingQueue<Event> eventQueue;
    private final ArrayBlockingQueue<BlizzardRequest> requestQueue;

    /**
     * @param selector The selector that selects which SocketChannels can be read from.
     * @param store The BlizzardStore that stores all the queues.
     */
    public BlizzardReader(Selector selector, BlizzardStore store) {
        this.selector = selector;
        this.eventQueue = store.getEventQueue();
        this.requestQueue = store.getRequestQueue();
    }

    /**
     * Reads data from SocketChannels into the buffers of their corresponding BlizzardMessages.
     */
    public void read() {
        try {
            if (selector.selectNow() == 0) return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // find out which registered channels can be read from
        Set<SelectionKey> keys = selector.selectedKeys();

        for (SelectionKey key: keys) {
            // get the message of this key
            BlizzardMessage message = ((BlizzardAttachment) key.attachment()).getMessage();
            try {
                // read into the message of this key
                ((SocketChannel) key.channel()).read(message.getCurrent());
                // add a process message event to be executed by the thread pool
                eventQueue.offer(new ProcessMessageEvent(message, requestQueue));
            } catch (IOException e) {
                System.out.println("IOException caught in read()");
                // add a bad request to the request queue with the status of server error
                BlizzardRequest failed = new BlizzardRequest((BlizzardAttachment) key.attachment());
                failed.setBadRequest(true);
                failed.setBadRequestType(BadRequest.SERVER_ERROR);
                requestQueue.offer(failed);
            }
        }
    }
}
