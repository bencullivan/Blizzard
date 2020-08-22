package com.bencullivan.blizzard.events;

import com.bencullivan.blizzard.http.*;
import com.bencullivan.blizzard.http.exceptions.BadRequest;
import com.bencullivan.blizzard.util.Processor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessMessageEventTest {

    private BlizzardMessage message = new BlizzardMessage(new BlizzardAttachment(),
            2048, 4);

    @AfterEach
    public void resetMessage() {
        message = new BlizzardMessage(new BlizzardAttachment(),
                2048, 4);
    }

    @Test
    public void testProcessMessage() throws InterruptedException {
        ArrayBlockingQueue<Event> eq = new ArrayBlockingQueue<>(4);
        ArrayBlockingQueue<BlizzardRequest> rq = new ArrayBlockingQueue<>(4);
        message.getCurrent().put(Requests.getB());
        eq.offer(new ProcessMessageEvent(message, rq));
        Thread t = new Thread(new Processor(eq));
        t.start();
        BlizzardRequest r = rq.take();
        t.interrupt();
        assertEquals("POST", r.getMethod());
        assertEquals("/pass.php", r.getUri());
        assertEquals("HTTP/1.1", r.getVersion());
        assertEquals("127.0.0.1", r.getHeader("host"));
        assertEquals("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:18.0) Gecko/20100101 Firefox/18.0",
                r.getHeader("user-agent"));
        assertEquals("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                r.getHeader("accept"));
        assertEquals("en-US,en;q=0.5", r.getHeader("accept-language"));
        assertEquals("gzip, deflate", r.getHeader("accept-encoding"));
        assertEquals("1", r.getHeader("dnt"));
        assertEquals("http://127.0.0.1/pass.php", r.getHeader("referer"));
        assertEquals("passx=87e8af376bc9d9bfec2c7c0193e6af70; PHPSESSID=l9hk7mfh0ppqecg8gialak6gt5",
                r.getHeader("cookie"));
        assertEquals("keep-alive", r.getHeader("connection"));
        assertEquals("application/x-www-form-urlencoded", r.getHeader("content-type"));
        assertEquals("29", r.getHeader("content-length"));
        assertEquals("username=zurfyx&pass=password", r.getBody());
    }

    @Test
    public void testProcessMessage2() throws InterruptedException {
        ArrayBlockingQueue<Event> eq = new ArrayBlockingQueue<>(4);
        ArrayBlockingQueue<BlizzardRequest> rq = new ArrayBlockingQueue<>(4);
        byte[][] arr = Requests.getB1();
        message.getCurrent().put(arr[0]);
        eq.offer(new ProcessMessageEvent(message, rq));
        message.getCurrent().put(arr[1]);
        eq.offer(new ProcessMessageEvent(message, rq));
        message.getCurrent().put(arr[2]);
        eq.offer(new ProcessMessageEvent(message, rq));
        message.getCurrent().put(arr[3]);
        eq.offer(new ProcessMessageEvent(message, rq));
        Thread t = new Thread(new Processor(eq));
        Thread tt = new Thread(new Processor(eq));
        t.start();
        tt.start();
        BlizzardRequest r = rq.take();
        t.interrupt();
        tt.interrupt();
        assertEquals("POST", r.getMethod());
        assertEquals("/pass.php", r.getUri());
        assertEquals("HTTP/1.1", r.getVersion());
        assertEquals("127.0.0.1", r.getHeader("host"));
        assertEquals("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:18.0) Gecko/20100101 Firefox/18.0",
                r.getHeader("user-agent"));
        assertEquals("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                r.getHeader("accept"));
        assertEquals("en-US,en;q=0.5", r.getHeader("accept-language"));
        assertEquals("gzip, deflate", r.getHeader("accept-encoding"));
        assertEquals("1", r.getHeader("dnt"));
        assertEquals("http://127.0.0.1/pass.php", r.getHeader("referer"));
        assertEquals("passx=87e8af376bc9d9bfec2c7c0193e6af70; PHPSESSID=l9hk7mfh0ppqecg8gialak6gt5",
                r.getHeader("cookie"));
        assertEquals("keep-alive", r.getHeader("connection"));
        assertEquals("application/x-www-form-urlencoded", r.getHeader("content-type"));
        assertEquals("29", r.getHeader("content-length"));
        assertEquals("username=zurfyx&pass=password", r.getBody());
    }

    @Test
    public void testBadMessage() throws InterruptedException {
        ArrayBlockingQueue<Event> eq = new ArrayBlockingQueue<>(10);
        ArrayBlockingQueue<BlizzardRequest> rq = new ArrayBlockingQueue<>(4);
        message.getCurrent().put(Requests.getBad());
        eq.add(new ProcessMessageEvent(message, rq));
        Thread t = new Thread(new Processor(eq));
        t.start();
        BlizzardRequest r = rq.take();
        t.interrupt();
        assertTrue(r.isBadRequest());
        assertEquals(BadRequest.REQUEST_LINE, r.getBadRequestType());
    }
}
