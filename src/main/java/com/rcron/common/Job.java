package com.rcron.common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Job implements Comparable<Job>, Serializable {
    public String id;
    public String webhookUrl;
    public LocalDateTime nextTrigger;
    public int fallbackMinutes;

    public Job(String id, String webhookUrl, LocalDateTime nextTrigger, int fallbackMinutes) {
        this.id = id;
        this.webhookUrl = webhookUrl;
        this.nextTrigger = nextTrigger;
        this.fallbackMinutes = fallbackMinutes;
    }

    @Override
    public int compareTo(Job other) {
        return this.nextTrigger.compareTo(other.nextTrigger);
    }
}
