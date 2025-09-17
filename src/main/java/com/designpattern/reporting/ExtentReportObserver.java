package com.designpattern.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

/**
 * ExtentReport implementation of TestObserver
 */
public class ExtentReportObserver implements TestObserver {
    private ExtentReports extent;
    private ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public ExtentReportObserver() {
        extent = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/extent-report.html");
        extent.attachReporter(spark);
    }

    @Override
    public void onTestStart(String testName) {
        ExtentTest extentTest = extent.createTest(testName);
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(String testName) {
        test.get().log(Status.PASS, "Test passed");
        extent.flush();
    }

    @Override
    public void onTestFailure(String testName, Throwable throwable) {
        test.get().log(Status.FAIL, "Test failed");
        test.get().log(Status.FAIL, throwable);
        extent.flush();
    }

    @Override
    public void onTestSkipped(String testName) {
        test.get().log(Status.SKIP, "Test skipped");
        extent.flush();
    }
}