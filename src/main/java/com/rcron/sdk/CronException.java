package com.rcron.sdk;

/**
 * Custom exception for CronJobAPI operations
 */
public class CronException extends Exception {
    
    public CronException(String message) {
        super(message);
    }
    
    public CronException(String message, Throwable cause) {
        super(message, cause);
    }
} 