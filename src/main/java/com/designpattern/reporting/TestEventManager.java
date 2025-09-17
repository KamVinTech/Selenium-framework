package com.designpattern.reporting;

import java.util.ArrayList;
import java.util.List;

/**
 * Test event manager that notifies all registered observers
 */
public class TestEventManager {
    private static TestEventManager instance;
    private List<TestObserver> observers;

    private TestEventManager() {
        observers = new ArrayList<>();
    }

    public static TestEventManager getInstance() {
        if (instance == null) {
            synchronized (TestEventManager.class) {
                if (instance == null) {
                    instance = new TestEventManager();
                }
            }
        }
        return instance;
    }

    public void addObserver(TestObserver observer) {
        observers.add(observer);
    }

    public void notifyTestStart(String testName) {
        observers.forEach(observer -> observer.onTestStart(testName));
    }

    public void notifyTestSuccess(String testName) {
        observers.forEach(observer -> observer.onTestSuccess(testName));
    }

    public void notifyTestFailure(String testName, Throwable throwable) {
        observers.forEach(observer -> observer.onTestFailure(testName, throwable));
    }

    public void notifyTestSkipped(String testName) {
        observers.forEach(observer -> observer.onTestSkipped(testName));
    }
}