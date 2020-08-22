package com.bencullivan.blizzard.events;

/**
 * Defines that method that all Events must implement in order to be run on processor threads.
 * @author Ben Cullivan (2020)
 */
public interface Event {
    void execute();
}
