package com.rcron.sdk;

import com.rcron.common.Job;
import com.google.gson.Gson;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CronJobAPI {
    private final CronClient client;
    private final Gson gson = new Gson();
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public CronJobAPI(String host, int port) {
        this.client = new CronClient(host, port);
    }

    public void createJob(Job job) throws CronException {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("action", "create");
            payload.put("id", job.id);
            payload.put("webhook", job.webhookUrl);
            payload.put("trigger", job.nextTrigger.format(formatter));
            payload.put("fallback", String.valueOf(job.fallbackMinutes));
            client.sendCommand(gson.toJson(payload));
        } catch (Exception e) {
            throw new CronException("Failed to create job: " + job.id, e);
        }
    }

	public void updateJob(Job job) throws CronException {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("action", "update");
            payload.put("id", job.id);
            payload.put("webhook", job.webhookUrl);
            payload.put("trigger", job.nextTrigger.format(formatter));
            payload.put("fallback", String.valueOf(job.fallbackMinutes));
            client.sendCommand(gson.toJson(payload));
        } catch (Exception e) {
            throw new CronException("Failed to update job: " + job.id, e);
        }
    }

    public void deleteJob(String jobId) throws CronException {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("action", "delete");
            payload.put("id", jobId);
            client.sendCommand(gson.toJson(payload));
        } catch (Exception e) {
            throw new CronException("Failed to delete job: " + jobId, e);
        }
    }

    public String listJobs() throws CronException {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("action", "list");
            return client.sendCommand(gson.toJson(payload));
        } catch (Exception e) {
            throw new CronException("Failed to list jobs", e);
        }
    }

	public String getJob(String jobId) throws CronException {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("action", "get");
            payload.put("id", jobId);
            return client.sendCommand(gson.toJson(payload));
        } catch (Exception e) {
            throw new CronException("Failed to get job: " + jobId, e);
        }
    }

    public String ping() throws CronException {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("action", "ping");
            return client.sendCommand(gson.toJson(payload));
        } catch (Exception e) {
            throw new CronException("Failed to ping server", e);
        }
    }
}