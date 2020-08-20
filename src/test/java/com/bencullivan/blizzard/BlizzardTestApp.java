package com.bencullivan.blizzard;

public class BlizzardTestApp extends BlizzardApp {
    @Override
    public void registerRoutes() {

    }

    public static void main(String[] args) {
        // start the server
        new BlizzardTestApp().listen(5000);
    }
}
