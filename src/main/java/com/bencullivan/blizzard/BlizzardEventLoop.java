package com.bencullivan.blizzard;

import java.nio.channels.Selector;
import java.util.concurrent.ArrayBlockingQueue;

class BlizzardEventLoop {

    private final BlizzardAcceptor acceptor;
    private final BlizzardReader reader;
    private final BlizzardProcessor processor;
    private final BlizzardWriter writer;
    private final ArrayBlockingQueue<Event> eventQueue;

    public BlizzardEventLoop(Selector readerSelector, BlizzardStore store, int processorCount, int hbSize) {
        acceptor = new BlizzardAcceptor(readerSelector, store, processorCount, hbSize);
        reader = new BlizzardReader(readerSelector, store);
        processor = new BlizzardProcessor();
        writer = new BlizzardWriter();
        eventQueue = store.getEventQueue();
        createProcessorPool(processorCount);
    }

    /**
     * Starts the desired number of processors on separate threads.
     * @param processorCount
     */
    public void createProcessorPool(int processorCount) {
        for (int i = 0; i < processorCount; i++) {
            new Thread(new Processor(eventQueue)).start();
        }
    }

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
