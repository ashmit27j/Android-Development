package com.c013.ashmit_mad_assignment2;

public class Task {
    private String description;
    private String priority;
    private long timestamp;

    private String firebaseKey;

    public Task() {
    }

    public Task(String description, String priority) {
        this.description = description;
        this.priority = priority;
        this.timestamp = System.currentTimeMillis();
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getPriorityValue() {
        switch (priority) {
            case "High":
                return 3;
            case "Medium":
                return 2;
            case "Low":
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "firebaseKey='" + firebaseKey + '\'' +
                ", description='" + description + '\'' +
                ", priority='" + priority + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}