package com.example.tutorapp2.model;

public class MatchItem {
    private int matchId;
    private int userId;
    private String name;
    private String role;
    private String date;

    public MatchItem(int matchId, int userId, String name, String role, String date) {
        this.matchId = matchId;
        this.userId = userId;
        this.name = name;
        this.role = role;
        this.date = date;
    }

    public int getMatchId() {
        return matchId;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getDate() {
        return date;
    }
}
