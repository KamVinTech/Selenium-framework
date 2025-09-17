package com.designpattern.utils;

import com.designpattern.core.DriverManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.*;

/**
 * TestNG listener for test execution events
 */
public class TestListener implements ITestListener, ISuiteListener {
    private static final Logger log = LogUtils.getLogger(TestListener.class);

    @Override
    public void onStart(ISuite suite) {
        log.info("Starting test suite: {}", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("Finished test suite: {}", suite.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        LogUtils.logTestStart(log, testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        log.info("Test passed: {}", testName);
        LogUtils.logTestEnd(log, testName);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        log.error("Test failed: {}", testName);
        
        // Capture screenshot on failure
        WebDriver driver = DriverManager.getInstance().getDriver();
        if (driver != null) {
            String screenshotPath = ScreenshotUtils.captureScreenshotOnFailure(driver, testName);
            log.info("Failure screenshot saved at: {}", screenshotPath);
        }
        
        // Log the exception if present
        if (result.getThrowable() != null) {
            log.error("Test failure details:", result.getThrowable());
        }
        
        LogUtils.logTestEnd(log, testName);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        log.warn("Test skipped: {}", testName);
        LogUtils.logTestEnd(log, testName);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        log.warn("Test failed within success percentage: {}", testName);
    }
}