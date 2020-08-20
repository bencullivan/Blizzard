package com.bencullivan.blizzard;

public class BlizzardApp implements App {

    protected BlizzardServer app;

    public BlizzardApp() {
        app = new BlizzardServer();
        useMiddleware();
        registerRoutes();
    }

    @Override
    public void useMiddleware() {};

    @Override
    public void registerRoutes() {}

    @Override
    public void listen(int port) {}

    @Override
    public void listen(int port, RouteCallback callback) {

    }
}
