package com.bencullivan.blizzard;

import com.bencullivan.blizzard.http.exceptions.BadRequestException;
import com.bencullivan.blizzard.http.BlizzardAttachment;
import com.bencullivan.blizzard.http.BlizzardMessage;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

class BlizzardReader{

    private final Selector selector;

    public BlizzardReader(Selector selector) {
        this.selector = selector;
    }

    public void read() {
        try {
            selector.selectNow();
        } catch (IOException e) {
            return;
        }
        Set<SelectionKey> keys = selector.selectedKeys();
        for (SelectionKey key : keys) {
            // get the HttpMessage of this key
            BlizzardMessage message = ((BlizzardAttachment) key.attachment()).getMessage();

            try {
                // read into the HttpMessage of this key
                ((SocketChannel) key.channel()).read(message.getCurrent());
                // process the message
                if (message.isDoneProcessing()) {
                    // the message can be garbage collected
                    ((BlizzardAttachment) key.attachment()).setMessage(null);

                }
            } catch (BadRequestException e) {
                //TODO: handle exception (bad request)
            } catch (IOException e) {
                //TODO: handle io exception
            } catch (IllegalStateException | UnsupportedOperationException e) {
                //TODO: handle exception (server error)
            }

        }

    }
}
