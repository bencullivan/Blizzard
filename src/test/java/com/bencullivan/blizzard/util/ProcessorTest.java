package com.bencullivan.blizzard.util;

import com.bencullivan.blizzard.events.Event;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessorTest {

    @Test
    public void testProcessEvent() throws InterruptedException {
        ArrayBlockingQueue<String> res = new ArrayBlockingQueue<>(10);
        ArrayBlockingQueue<Event> q = new ArrayBlockingQueue<>(10);
        q.offer(() -> res.offer("It worked"));
        new Thread(new Processor(q)).start();
        assertEquals("It worked", res.take());
    }

}
