package com.bencullivan.blizzard;

import java.nio.channels.Selector;

public class BlizzardEventLoop implements Runnable {

    private final BlizzardAcceptor acceptor;
    private final BlizzardReader reader;
    private final BlizzardProcessor processor;
    private final BlizzardWriter writer;

    public BlizzardEventLoop(Selector readerSelector) {
        acceptor = new BlizzardAcceptor();
        reader = new BlizzardReader(readerSelector);
        processor = new BlizzardProcessor();
        writer = new BlizzardWriter();
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            acceptor.accept();
            reader.read();
            processor.process();
            writer.write();
        }
    }
}
