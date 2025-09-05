package com.kafnotif.model;

public class Event {
    private String id;
    private String type;
    private String payload;

    public Event() {}

    public Event(String id, String type, String payload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
