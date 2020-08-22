package com.bencullivan.blizzard.http;

import java.nio.charset.StandardCharsets;

public class Requests {
    private static final String a = "GET /posts/first HTTP/1.1 \r\n" +
            "Host:www.test101.com      \r\n " +
            "Accept-Language: en-us    \r\n" +
            "User-agent:Mozilla/4.0    \r\n" +
            "Content-length: 10        \r\n" +
            "\r\n" +
            "What's up?";
    static byte[][] getA() {
        return new byte[][] {
                a.substring(0, a.length()-30).getBytes(StandardCharsets.UTF_8),
                a.substring(a.length()-30, a.length()-20).getBytes(StandardCharsets.UTF_8),
                a.substring(a.length()-20).getBytes(StandardCharsets.UTF_8)
        };
    }

    private static final String b = "POST /pass.php HTTP/1.1\r\n" +
            "Host: 127.0.0.1\r\n" +
            "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:18.0) Gecko/20100101 Firefox/18.0\r\n" +
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
            "Accept-Language: en-US,en;q=0.5\r\n" +
            "Accept-Encoding: gzip, deflate\r\n" +
            "DNT: 1\r\n" +
            "Referer: http://127.0.0.1/pass.php\r\n" +
            "Cookie: passx=87e8af376bc9d9bfec2c7c0193e6af70; PHPSESSID=l9hk7mfh0ppqecg8gialak6gt5\r\n" +
            "Connection: keep-alive\r\n" +
            "Content-Type: application/x-www-form-urlencoded\r\n" +
            "Content-Length: 29\r\n" +
            "\r\n" +
            "username=zurfyx&pass=password";
    public static byte[] getB() {
        return b.getBytes(StandardCharsets.UTF_8);
    }
    public static byte[][] getB1() {
        return new byte[][] {
                b.substring(0, 20).getBytes(StandardCharsets.UTF_8),
                new byte[0],
                b.substring(20, b.length()-20).getBytes(StandardCharsets.UTF_8),
                b.substring(b.length()-20).getBytes(StandardCharsets.UTF_8)
        };
    }
    static byte[][] getB2() {
        return new byte[][]{
                b.getBytes(StandardCharsets.UTF_8)
        };
    }
    static byte[][] getB3() {
        return new byte[][] {
                b.substring(0, 42).getBytes(StandardCharsets.UTF_8),
                b.substring(42).getBytes(StandardCharsets.UTF_8)
        };
    }

    static byte[] getHeaderTooLarge() {
        return ("GET / HTTP/1.1\r\n header:" + "f".repeat(8400)).getBytes(StandardCharsets.UTF_8);
    }

    private static final String bad = "POST  HTTP/1.1\r\n" +
            "Host: 127.0.0.1\r\n" +
            "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:18.0) Gecko/20100101 Firefox/18.0\r\n" +
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
            "Accept-Language: en-US,en;q=0.5\r\n" +
            "Accept-Encoding: gzip, deflate\r\n" +
            "DNT: 1\r\n" +
            "Referer: http://127.0.0.1/pass.php\r\n" +
            "Cookie: passx=87e8af376bc9d9bfec2c7c0193e6af70; PHPSESSID=l9hk7mfh0ppqecg8gialak6gt5\r\n" +
            "Connection: keep-alive\r\n" +
            "Content-Type: application/x-www-form-urlencoded\r\n" +
            "Content-Length: 29\r\n" +
            "\r\n" +
            "username=zurfyx&pass=password";
    public static byte[] getBad() {
        return bad.getBytes(StandardCharsets.UTF_8);
    }
}
