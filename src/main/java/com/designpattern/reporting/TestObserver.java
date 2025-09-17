package com.designpattern.reporting;

/**
 * Observer interface for test events
 */
public interface TestObserver {
    void onTestStart(String testName);
    void onTestSuccess(String testName);
    void onTestFailure(String testName, Throwable throwable);
    void onTestSkipped(String testName);
}