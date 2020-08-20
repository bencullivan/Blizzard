package com.bencullivan.blizzard;

import com.bencullivan.blizzard.http.BlizzardRequest;
import com.bencullivan.blizzard.http.BlizzardMessage;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

class BlizzardReader{

    private final Selector selector;
    private final ArrayBlockingQueue<Event> eventQueue;
    private final ArrayBlockingQueue<BlizzardRequest> requestQueue;

    public BlizzardReader(Selector selector, BlizzardStore store) {
        this.selector = selector;
        this.eventQueue = store.getEventQueue();
        this.requestQueue = store.getRequestQueue();
    }

    public void read() {
        try {
            selector.selectNow();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Set<SelectionKey> keys = selector.selectedKeys();

        for (SelectionKey key : keys) {
            // get the HttpMessage of this key
            BlizzardMessage message = (BlizzardMessage) key.attachment();

            try {
                // read into the HttpMessage of this key
                ((SocketChannel) key.channel()).read(message.getCurrent());

                // add a process message event to be executed by the thread pool
                eventQueue.add(new ProcessMessageEvent(message, requestQueue));
            } catch (IOException e) {
                //TODO: handle io exception
            } catch (IllegalStateException | UnsupportedOperationException e) {
                //TODO: handle exception (server error)
            }
        }
    }
}
