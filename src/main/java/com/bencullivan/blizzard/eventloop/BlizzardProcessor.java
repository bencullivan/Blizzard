package com.bencullivan.blizzard.eventloop;

import com.bencullivan.blizzard.events.Event;
import com.bencullivan.blizzard.events.ProcessRequestEvent;
import com.bencullivan.blizzard.http.BlizzardRequest;
import com.bencullivan.blizzard.util.BlizzardStore;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Hands BlizzardRequests to the processor pool for processing.
 * @author Ben Cullivan
 */
public class BlizzardProcessor {

    private final BlizzardStore store;
    private final ArrayBlockingQueue<Event> eventQueue;
    private final ArrayBlockingQueue<BlizzardRequest> requestQueue;

    /**
     * @param store The BlizzardStore that stores the queues.
     */
    public BlizzardProcessor(BlizzardStore store) {
        this.store = store;
        eventQueue = store.getEventQueue();
        requestQueue = store.getRequestQueue();
    }

    /**
     * Sends a maximum of eight requests for processing.
     */
    public void process() {
        int it = 8;
        BlizzardRequest request = requestQueue.poll();
        while (request != null) {
            // attempt to insert the event into the event queue
            Event requestEvent = new ProcessRequestEvent(request, store);
            if (!eventQueue.offer(requestEvent)) {
                // if the event can't be inserted immediately start a thread that will block until it is
                // possible to do so
                new Thread(() -> {
                    try {
                        eventQueue.put(requestEvent);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                return;
            }
            if (it-- <= 0) return;
            request = requestQueue.poll();
        }
    }
}
