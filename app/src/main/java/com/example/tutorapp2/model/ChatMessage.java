package com.example.tutorapp2.model;

public class ChatMessage {
    private int senderId;
    private int receiverId;
    private String content;
    private String timestamp;

    public ChatMessage(int senderId, int receiverId, String content, String timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
