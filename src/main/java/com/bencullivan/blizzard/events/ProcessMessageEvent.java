package com.bencullivan.blizzard.events;

import com.bencullivan.blizzard.http.BlizzardMessage;
import com.bencullivan.blizzard.http.BlizzardRequest;
import com.bencullivan.blizzard.http.exceptions.BadRequestException;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Processes a BlizzardMessage by parsing the bytes that were read into it into a BlizzardRequest.
 * This class's execute() method is always run on a processor thread.
 * @author Ben Cullivan (2020)
 */
public class ProcessMessageEvent implements Event {

    private final BlizzardMessage message;
    private final ArrayBlockingQueue<BlizzardRequest> requestQueue;

    /**
     * @param message The message to be processed.
     * @param requestQueue The queue of requests that are ready to be processed.
     */
    public ProcessMessageEvent(BlizzardMessage message, ArrayBlockingQueue<BlizzardRequest> requestQueue) {
        this.message = message;
        this.requestQueue = requestQueue;
    }

    @Override
    public void execute() {
        long id = Thread.currentThread().getId();
        try {
            synchronized (message) {
                // add the current processor thread to the queue of threads waiting to process this message
                message.addThread(id);
                // wait until it is this thread's turn
                while (message.getCurrentThreadId() != id) {
                    message.wait();
                }
            }
            // process the message and add the request to the request queue if this thread is done processing
            try {
                if (message.isDoneProcessing()) {
                    // get the request
                    BlizzardRequest good = message.getRequest();
                    // reset the message's data
                    message.restoreInitialValues();
                    requestQueue.put(good);
                }
                message.removeThread();
            } catch (BadRequestException e) {
                BlizzardRequest bad = message.getRequest();
                message.restoreInitialValues();
                message.removeThread();
                bad.clear();
                bad.setBadRequest(true);
                bad.setBadRequestType(e.getType());
                requestQueue.put(bad);
            }
        } catch (InterruptedException e) {
            System.out.println("Message processing was interrupted.\n" +
                    "If this is a test, this is good. If not, there is a problem.");
        }
    }
}
