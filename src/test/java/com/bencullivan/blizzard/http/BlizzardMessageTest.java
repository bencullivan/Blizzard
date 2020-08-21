package com.bencullivan.blizzard.http;

import com.bencullivan.blizzard.http.exceptions.*;
import org.junit.jupiter.api.*;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class BlizzardMessageTest {

    private BlizzardMessage message = new BlizzardMessage(2048, 20);

    @AfterEach
    public void resetMessage() {
        message = new BlizzardMessage(2048, 20);
    }

    @Test
    public void messageTest1() throws BadRequestException {
        byte[][] a = Requests.getA();
        message.getCurrent().put(a[0]);
        assertFalse(message.isDoneProcessing());
        message.getCurrent().put(a[1]);
        assertFalse(message.isDoneProcessing());
        message.getCurrent().put(a[2]);
        assertTrue(message.isDoneProcessing());
        BlizzardRequest r = message.getRequest();
        assertEquals("GET", r.getMethod());
        assertEquals("/posts/first", r.getUri());
        assertEquals("HTTP/1.1", r.getVersion());
        assertEquals("www.test101.com", r.getHeader("host"));
        assertEquals("en-us", r.getHeader("accept-language"));
        assertEquals("Mozilla/4.0", r.getHeader("user-agent"));
        assertEquals("10", r.getHeader("content-length"));
        assertEquals("What's up?", r.getBody());
    }

    @Test
    public void messageTest2() throws BadRequestException {
        byte[][] b = Requests.getB1();
        message.getCurrent().put(b[0]);
        assertFalse(message.isDoneProcessing());
        message.getCurrent().put(b[1]);
        assertFalse(message.isDoneProcessing());
        message.getCurrent().put(b[2]);
        assertFalse(message.isDoneProcessing());
        message.getCurrent().put(b[3]);
        assertTrue(message.isDoneProcessing());
        BlizzardRequest r = message.getRequest();
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
    public void messageTest3() throws BadRequestException {
        message.getCurrent().put(Requests.getB2()[0]);
        assertTrue(message.isDoneProcessing());
        BlizzardRequest r = message.getRequest();
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
    public void messageTest4() throws BadRequestException {
        byte[][] b = Requests.getB3();
        message.getCurrent().put(b[0]);
        assertFalse(message.isDoneProcessing());
        message.getCurrent().put(b[1]);
        assertTrue(message.isDoneProcessing());
        BlizzardRequest r = message.getRequest();
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
    public void messageHeaderTooLargeTest() throws BadRequestException {
        byte[] large = Requests.getHeaderTooLarge();
        int i = 0;
        while (i < 8000) {
            message.getCurrent().put(Arrays.copyOfRange(large, i, i + 2000));
            message.isDoneProcessing();
            i += 2000;
        }
        message.getCurrent().put(Arrays.copyOfRange(large, i, large.length));
        assertThrows(
                HeadersTooLargeException.class,
                () -> message.isDoneProcessing()
        );
    }
}
