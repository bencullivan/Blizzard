package com.bencullivan.blizzard;

import com.bencullivan.blizzard.http.BlizzardRequest;
import com.bencullivan.blizzard.http.BlizzardResponse;

public interface RouteCallback {
    public void onCall(BlizzardRequest req, BlizzardResponse res);
}
