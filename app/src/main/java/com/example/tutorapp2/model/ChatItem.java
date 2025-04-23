package com.example.tutorapp2.model;

public class ChatItem {
    private int userId;
    private String name;
    private String lastMessage;
    private String timestamp;

    public ChatItem(int userId, String name, String lastMessage, String timestamp) {
        this.userId = userId;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
