package com.rcron.storage;

import com.rcron.common.Job;

import java.time.LocalDateTime;
import java.util.List;

public interface JobStorage {
    void saveJob(Job job) throws Exception;
    void deleteJob(String id) throws Exception;
    List<Job> loadNextJobs(int limit, LocalDateTime after) throws Exception;
    Job getJob(String id) throws Exception;
	List<Job> listJobs() throws Exception;
}
