package com.bencullivan.blizzard;

import com.bencullivan.blizzard.http.exceptions.BadRequestException;
import com.bencullivan.blizzard.http.BlizzardExtra;
import com.bencullivan.blizzard.http.BlizzardMessage;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

class BlizzardReader implements Runnable {

    private final Selector selector;

    public BlizzardReader(Selector selector) {
        this.selector = selector;
    }

    private void read() throws IOException {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        for (SelectionKey key : keys) {
            // get the HttpMessage of this key
            BlizzardMessage message = ((BlizzardExtra) key.attachment()).getMessage();

            // read into the HttpMessage of this key
            ((SocketChannel) key.channel()).read(message.getCurrent());

            try {
                // process the message
                message.processInput();
            } catch (BadRequestException e) {
                //TODO: handle exception (bad request)
            } catch (IllegalStateException | UnsupportedOperationException e) {
                //TODO: handle exception (server error)
            }

        }

    }

    @Override
    public void run() {
        while (true) {
            try {
                read();
            } catch (IOException e) {
                return;
            }
        }
    }
}
