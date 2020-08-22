package com.bencullivan.blizzard.util;

import com.bencullivan.blizzard.events.Event;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Runs on a worker thread, processing events one after another.
 * @author Ben Cullivan (2020)
 */
public class Processor implements Runnable {

    private final ArrayBlockingQueue<Event> eventQueue;

    /**
     * @param eventQueue The queue that the events are taken from.
     */
    public Processor(ArrayBlockingQueue<Event> eventQueue) {
        this.eventQueue = eventQueue;
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Event event = eventQueue.take();
                event.execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
