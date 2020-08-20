package com.bencullivan.blizzard;

public interface App {
    void useMiddleware();
    void registerRoutes();
    void listen(int port);
    void listen(int port, RouteCallback callback);
}
