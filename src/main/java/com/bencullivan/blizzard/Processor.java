package com.bencullivan.blizzard;

import com.bencullivan.blizzard.events.Event;

import java.util.concurrent.ArrayBlockingQueue;

public class Processor implements Runnable {

    private final ArrayBlockingQueue<Event> eventQueue;

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
