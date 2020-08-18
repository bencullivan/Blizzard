package com.bencullivan.blizzard.http;

public class BlizzardAttachment {

    private BlizzardMessage message;
    private BlizzardRequest request;

    public BlizzardAttachment(BlizzardMessage message, BlizzardRequest request) {
        this.message = message;
        this.request = request;
    }

    public BlizzardMessage getMessage() {
        return message;
    }

    public BlizzardRequest getRequest() {
        return request;
    }

    public void setMessage(BlizzardMessage message) {
        this.message = message;
    }

    public void setRequest(BlizzardRequest request) {
        this.request = request;
    }
}
