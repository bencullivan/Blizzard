package com.bencullivan.blizzard;

import com.bencullivan.blizzard.events.RouteCallback;

public interface App {
    void useMiddleware();
    void registerRoutes();
    void listen(int port);
    void listen(int port, RouteCallback callback);
}
