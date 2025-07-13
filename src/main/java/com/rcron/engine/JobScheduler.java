package com.rcron.engine;

import com.rcron.common.Job;
import com.rcron.storage.JobStorage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.LockSupport;

public class JobScheduler {
    private final PriorityQueue<Job> pq = new PriorityQueue<>();
    private final JobStorage store;
    private final int MAX_IN_MEMORY = 1000;
    private volatile boolean running = true;
    private Thread schedulerThread;

    public JobScheduler(JobStorage store) {
        this.store = store;
    }

    public void start() {
        schedulerThread = new Thread(this::scheduleLoop);
        schedulerThread.start();
    }

    private void scheduleLoop() {
        while (running) {
            try {
                refillIfNeeded();
                Job next = pq.peek();
                if (next == null) {
                    LockSupport.parkUntil(System.currentTimeMillis() + 5000);
                    continue;
                }
                long delay = Duration.between(LocalDateTime.now(), next.nextTrigger).toMillis();
                if (delay > 0) {
                    LockSupport.parkUntil(System.currentTimeMillis() + delay);
                    continue;
                }
                pq.poll();
                next.nextTrigger = LocalDateTime.now().plusMinutes(next.fallbackMinutes);
                store.saveJob(next);
				executeJob(next);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void submitJob(Job job) throws Exception {
        store.saveJob(job);
        if (pq.size() < MAX_IN_MEMORY || job.nextTrigger.isBefore(pq.peek().nextTrigger)) {
            pq.add(job);
        }
        LockSupport.unpark(schedulerThread);
    }

    public synchronized void deleteJob(String jobId) throws Exception {
        store.deleteJob(jobId);
        pq.removeIf(job -> job.id.equals(jobId));
    }

    public synchronized List<Job> listJobs() throws Exception {
        return store.listJobs();
    }

    public synchronized Job getJob(String jobId) throws Exception {
        return store.getJob(jobId);
    }

    private void refillIfNeeded() throws Exception {
        if (pq.size() < MAX_IN_MEMORY) {
            pq.addAll(store.loadNextJobs(MAX_IN_MEMORY - pq.size(), LocalDateTime.now()));
        }
    }

	private void executeJob(Job job) {
		Thread.startVirtualThread(() -> {
			System.out.println("Executing job: " + job.id);
			try {
				java.net.URL url = new java.net.URL(job.webhookUrl);
				java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				con.setDoOutput(true);
				con.setConnectTimeout(1000);
				con.setReadTimeout(1000);
				con.setRequestProperty("X-Job-ID", job.id);
				con.getOutputStream().write(("Triggered by cron engine: " + job.id).getBytes());
				int code = con.getResponseCode();
				System.out.println("[Webhook] Status: " + code);
			} catch (Exception e) {
				System.err.println("[Webhook] Failed for job " + job.id + ": " + e.getMessage());
			}
		});
	}

    public void shutdown() {
        running = false;
    }
}

