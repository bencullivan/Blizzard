package com.bencullivan.blizzard;

import com.bencullivan.blizzard.http.BlizzardMessage;
import com.bencullivan.blizzard.http.BlizzardRequest;
import com.bencullivan.blizzard.http.exceptions.BadRequestException;

import java.util.concurrent.ArrayBlockingQueue;

public class ProcessMessageEvent implements Event {
    private final BlizzardMessage message;
    private final ArrayBlockingQueue<BlizzardRequest> requestQueue;

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
            e.printStackTrace();
        }
    }
}
