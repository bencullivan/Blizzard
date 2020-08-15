package com.bencullivan.blizzard.http;

public class BlizzardExtra {

    private final BlizzardMessage message;
    private BlizzardRequest request;

    public BlizzardExtra(BlizzardMessage message) {
        this.message = message;
    }

    public BlizzardMessage getMessage() {
        return message;
    }

    public BlizzardRequest getRequest() {
        return request;
    }

    public void setRequest(BlizzardRequest request) {
        this.request = request;
    }
}
