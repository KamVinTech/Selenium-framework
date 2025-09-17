package com.designpattern.utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.apache.logging.log4j.Logger;

/**
 * Retry analyzer for failed tests
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger log = LogUtils.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY_COUNT = 2;
    private int retryCount = 0;
    
    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess()) {
            if (retryCount < MAX_RETRY_COUNT) {
                retryCount++;
                log.info("Retrying test {} for the {} time", result.getName(), retryCount);
                return true;
            }
        }
        return false;
    }
}