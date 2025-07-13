# Cron Engine - Technical Documentation

## Overview

This project is a standalone Java-based cron job engine designed to be run like a Redis server. Jobs are scheduled using an SDK instead of a REST API. When a job is due, the engine triggers a webhook. Persistence is supported through sqlite storage layers.

## Features

- Submit jobs with trigger times and webhooks
- Built-in persistence (SQLite)
- Local or Docker deployment as a daemon
- Lightweight SDK-based control
- Virtual thread job execution

## Architecture

```
cron-engine/
├── engine/         # Core engine that runs as a background service
├── sdk/            # Java SDK for interacting with the daemon
└── common/         # Shared model
```

## SDK Module (cron-engine-sdk)

### CronClient

Handles TCP connection and communication.

```java
CronClient client = new CronClient("localhost", 9090);
client.sendCommand("PING");
```

### CronJobAPI

Wrapper around CronClient to interact using typed Job data.

```java
CronJobAPI api = new CronJobAPI("localhost", 9090);
Job job = new Job("job-id", "https://my-server/hook", LocalDateTime.now().plusSeconds(30), 60);
api.createJob(job);
```

### Methods

- `createJob(Job)`
- `updateJob(Job)`
- `deleteJob(String jobId)`
- `listJobs()`
- `getJob(String jobId)`
- `ping()`

## Daemon Module (cron-engine-daemon)

### CronServer

Main daemon accepting and processing commands.

- Accepts connections over TCP
- Supports actions: create, update, delete, list, get, ping
- Executes webhooks using HttpURLConnection

### JobScheduler

Manages job execution queue and triggers.

- Uses in-memory priority queue for next 1000 jobs
- Saves and loads jobs using JobStorage
- Executes jobs on virtual threads

### Main

Starts the cron server and SQLite-based job storage.

```java
JobStorage storage = new SQLiteJobStorage("cron_jobs.db");
CronServer server = new CronServer(9090, storage);
server.start();
```

## Storage Options

All storage backends implement the `JobStorage` interface.

### SQLite

- File-based SQL storage
- Supports querying, updating, durability
- No external server needed

## How Job Triggering Works

1. Jobs are persisted using JobStorage
2. Priority queue in memory holds next 1000 jobs
3. Main loop sleeps until next trigger time
4. New jobs can preempt the queue
5. At trigger time, webhook is called via HttpURLConnection
6. Job may be rescheduled or removed

## Usage Example

```java
CronJobAPI api = new CronJobAPI("localhost", 9090);
Job job = new Job("job1", "https://example.com/hook", LocalDateTime.now().plusSeconds(10), 30);
String result = api.createJob(job);
System.out.println(result);
```

## Improvements

1. Persist sqlite data
2. Integrations tests
3. CLI support
4. Load test
5. SDK distribution
6. Deployment through docker
