package com.designpattern.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * Logging utility class that provides centralized logging capabilities
 */
public class LogUtils {
    private static final String THREAD_CONTEXT_KEY = "testName";
    
    private LogUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets a logger instance for the specified class
     * @param clazz The class to get the logger for
     * @return Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz);
    }
    
    /**
     * Sets the current test name in the thread context
     * @param testName Name of the current test
     */
    public static void setTestName(String testName) {
        ThreadContext.put(THREAD_CONTEXT_KEY, testName);
    }
    
    /**
     * Clears the test name from thread context
     */
    public static void clearTestName() {
        ThreadContext.remove(THREAD_CONTEXT_KEY);
    }
    
    /**
     * Logs the start of a test
     * @param logger Logger instance
     * @param testName Test name
     */
    public static void logTestStart(Logger logger, String testName) {
        setTestName(testName);
        logger.info("========== Starting Test: {} ==========", testName);
    }
    
    /**
     * Logs the end of a test
     * @param logger Logger instance
     * @param testName Test name
     */
    public static void logTestEnd(Logger logger, String testName) {
        logger.info("========== Ending Test: {} ==========", testName);
        clearTestName();
    }
}