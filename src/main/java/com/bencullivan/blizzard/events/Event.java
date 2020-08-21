package com.bencullivan.blizzard.events;

/**
 * Defines that method that all Events must implement in order to be run on processor threads.
 * @author Ben Cullivan
 */
public interface Event {
    void execute();
}
