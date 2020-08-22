package com.bencullivan.blizzard.eventloop;

import com.bencullivan.blizzard.events.Event;
import com.bencullivan.blizzard.util.BlizzardStore;
import com.bencullivan.blizzard.util.Processor;

import java.nio.channels.Selector;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Handles SocketChannel registration, reading, processing and writing.
 * @author Ben Cullivan (2020)
 */
public class BlizzardEventLoop {

    private final BlizzardAcceptor acceptor;
    private final BlizzardReader reader;
    private final BlizzardProcessor processor;
    private final BlizzardWriter writer;
    private final ArrayBlockingQueue<Event> eventQueue;  // the queue of events to be executed by the processor pool
    private final int PROCESSOR_COUNT; // the number of processor threads

    /**
     * @param readSelector The Selector that selects which SocketChannels are able to be read from.
     * @param writeSelector The Selector that selects which SocketChannels are able to be written to.
     * @param store The BlizzardStore that stores all of the queues.
     * @param processorCount The number of processor threads.
     * @param hbSize The size of the header buffer in a BlizzardMessage.
     */
    public BlizzardEventLoop(Selector readSelector, Selector writeSelector, BlizzardStore store,
                             int processorCount, int hbSize) {
        acceptor = new BlizzardAcceptor(readSelector, writeSelector, store, processorCount, hbSize);
        reader = new BlizzardReader(readSelector, store);
        processor = new BlizzardProcessor(store);
        writer = new BlizzardWriter(writeSelector);
        eventQueue = store.getEventQueue();
        PROCESSOR_COUNT = processorCount;
    }

    /**
     * Starts the desired number of processors on separate threads.
     */
    public void createProcessorPool() {
        for (int i = 0; i < PROCESSOR_COUNT; i++) {
            new Thread(new Processor(eventQueue)).start();
        }
    }

    /**
     * Starts the event loop.
     */
    public void start() {
        //noinspection InfiniteLoopStatement
        while (true) {
            acceptor.accept();
            reader.read();
            processor.process();
            writer.write();
        }
    }
}
