package com.rcron;

import com.rcron.storage.JobStorage;
import com.rcron.storage.SQLite.SQLiteJobStorage;
import com.rcron.engine.CronServer;

public class Main {
    public static void main(String[] args) {
        try {
            int port = 9090;
            String dbFile = "cron_jobs.db";
            JobStorage storage = new SQLiteJobStorage(dbFile);
            CronServer server = new CronServer(port, storage);
            System.out.println("[Main] Starting cron server on port " + port + " with database: " + dbFile);
            server.start();
        } catch (Exception e) {
            System.err.println("[Main] Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}