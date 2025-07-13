package com.rcron.engine;

import com.google.gson.Gson;
import com.rcron.common.Job;
import com.rcron.storage.JobStorage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CronServer {
	private final int port;
	private final JobScheduler scheduler;
	private final Gson gson;

	public CronServer(int port, JobStorage storage) {
		this.port = port;
		this.scheduler = new JobScheduler(storage);
		this.gson = new Gson();
	}

	public void start() throws Exception {
		scheduler.start();
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("[CronServer] Listening on port " + port);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				new Thread(() -> handleClient(clientSocket)).start();
			}
		}
	}

	/**
	 * Handle a client request received from the CronClient
	 * @param clientSocket
	 */
	private void handleClient(Socket clientSocket) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

			String input = in.readLine();
			Map<String, String> command = gson.fromJson(input, HashMap.class);

			String action = command.get("action");
			
			switch (action) {
				case "create":
					handleCreateJob(command, out);
					break;
				case "update":
					handleUpdateJob(command, out);
					break;
				case "delete":
					handleDeleteJob(command, out);
					break;
				case "list":
					handleListJobs(out);
					break;
				case "get":
					handleGetJob(command, out);
					break;
				case "ping":
					handlePing(out);
					break;
				default:
					throw new Exception("Unknown command: " + action);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleCreateJob(Map<String, String> command, PrintWriter out) throws Exception {
		Job job = new Job(
				command.get("id"),
				command.get("webhook"),
				LocalDateTime.parse(command.get("trigger")),
				Integer.parseInt(command.get("fallback")));
		scheduler.submitJob(job);
	}

	private void handleUpdateJob(Map<String, String> command, PrintWriter out) throws Exception {
		Job job = new Job(
				command.get("id"),
				command.get("webhook"),
				LocalDateTime.parse(command.get("trigger")),
				Integer.parseInt(command.get("fallback")));
		scheduler.submitJob(job);
	}

	private void handleDeleteJob(Map<String, String> command, PrintWriter out) throws Exception {
		String jobId = command.get("id");
		scheduler.deleteJob(jobId);
	}

	private void handleListJobs(PrintWriter out) throws Exception {
		List<Job> jobs = scheduler.listJobs();
		out.println(gson.toJson(jobs));
	}

	private void handleGetJob(Map<String, String> command, PrintWriter out) throws Exception {
		String jobId = command.get("id");
		Job job = scheduler.getJob(jobId);
		out.println(gson.toJson(job));
	}

	private void handlePing(PrintWriter out) {
		out.println("{\"status\": \"pong\"}");
	}
}
