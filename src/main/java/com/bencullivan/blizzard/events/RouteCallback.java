package com.bencullivan.blizzard.events;

import com.bencullivan.blizzard.http.BlizzardRequest;
import com.bencullivan.blizzard.http.BlizzardResponse;

/**
 * Defines the required method that a route callback must implement.
 * @author Ben Cullivan (2020)
 */
public interface RouteCallback {
    void call(BlizzardRequest req, BlizzardResponse res);
}
