package com.bencullivan.blizzard.eventloop;

import com.bencullivan.blizzard.util.BlizzardStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Handles accepting new connections on a port specified by the user
 * @author Ben Cullivan (2020)
 */
public class BlizzardListener implements Runnable {

    private final int PORT;
    private final ArrayBlockingQueue<SocketChannel> acceptedChannels;
    private ServerSocketChannel serverSocketChannel;

    /**
     * Constructs a new instance of BlizzardListener
     * @param port - the post on which to listen for new connections
     * @param store - the BlizzardStore where non-local data is stored
     */
    public BlizzardListener(int port, BlizzardStore store) {
        PORT = port;
        acceptedChannels = store.getAcceptedChannelQueue();
    }

    /**
     * Opens a non-blocking ServerSocketChannel on the specified port
     * @throws IOException if there is an exception when opening the channel
     */
    private void openChannel() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(true);
    }

    @Override
    public void run() {
        // open a non-blocking ServerSocketChannel on the specified port
        try {
            openChannel();
        } catch (IOException e) {
            System.out.println("Server stopped listening.");
            e.printStackTrace();
            return;
        }

        // begin the listening loop
        while (true) {
            try {
                // attempt to accept a new connection
                SocketChannel channel = serverSocketChannel.accept();
                if (channel == null) continue;
                // queue the connection
                acceptedChannels.put(channel);
            } catch (IOException e) {
                if (e instanceof ClosedChannelException) {
                    // if the channel was closed, attempt to open a new channel
                    try {
                        openChannel();
                    } catch (IOException e2) {
                        // if the attempt to open a new channel fails, stop execution
                        System.out.println("Server stopped listening.");
                        e2.printStackTrace();
                        return;
                    }
                } else {
                    System.out.println("Server stopped listening.");
                    e.printStackTrace();
                    return;
                }
            } catch (InterruptedException | SecurityException e) {
                System.out.println("Server stopped listening.");
                e.printStackTrace();
                return;
            }
        }
    }
}
