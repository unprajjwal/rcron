package com.rcron.storage.SQLite;

import com.rcron.common.Job;
import com.rcron.storage.JobStorage;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SQLiteJobStorage implements JobStorage {
    private final Connection connection;
    private static final String NEXT_TRIGGER_INDEX_NAME = "idx_jobs_next_trigger";

    public SQLiteJobStorage(String dbFilePath) throws Exception {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        System.out.println("[SQLiteJobStorage] Connected to database: " + dbFilePath);
        initTable();
        createNextTriggerIndex();
    }

    private void initTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS jobs (" +
                "id TEXT PRIMARY KEY, " +
                "webhook TEXT NOT NULL, " +
                "nextTrigger TEXT NOT NULL, " +
                "fallbackMinutes INTEGER NOT NULL" +
                ");");
        }
    }

    private void createNextTriggerIndex() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS " + NEXT_TRIGGER_INDEX_NAME + 
                    " ON jobs (nextTrigger)");
        }
    }

    @Override
    public void saveJob(Job job) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "REPLACE INTO jobs (id, webhook, nextTrigger, fallbackMinutes) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, job.id);
            ps.setString(2, job.webhookUrl);
            ps.setString(3, job.nextTrigger.toString());
            ps.setInt(4, job.fallbackMinutes);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteJob(String id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM jobs WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Job> loadNextJobs(int limit, LocalDateTime after) throws SQLException {
        List<Job> jobs = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM jobs WHERE nextTrigger > ? ORDER BY nextTrigger ASC LIMIT ?")) {
            ps.setString(1, after.toString());
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Job job = new Job(
                        rs.getString("id"),
                        rs.getString("webhook"),
                        LocalDateTime.parse(rs.getString("nextTrigger")),
                        rs.getInt("fallbackMinutes")
                );
                jobs.add(job);
            }
        }
        return jobs;
    }

    @Override
    public Job getJob(String id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM jobs WHERE id = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Job(
                        rs.getString("id"),
                        rs.getString("webhook"),
                        LocalDateTime.parse(rs.getString("nextTrigger")),
                        rs.getInt("fallbackMinutes")
                );
            }
            return null;
        }
    }

	@Override
	public List<Job> listJobs() throws SQLException {
		List<Job> jobs = new ArrayList<>();
		try (Statement stmt = connection.createStatement()) {
			ResultSet rs = stmt.executeQuery("SELECT * FROM jobs");
			while (rs.next()) {
				Job job = new Job(
						rs.getString("id"),
						rs.getString("webhook"),
						LocalDateTime.parse(rs.getString("nextTrigger")),
						rs.getInt("fallbackMinutes")
				);
				jobs.add(job);
			}
		}
		return jobs;
	}
}
