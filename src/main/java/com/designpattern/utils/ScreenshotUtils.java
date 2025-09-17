package com.designpattern.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for capturing screenshots during test execution
 */
public class ScreenshotUtils {
    private static final Logger log = LogUtils.getLogger(ScreenshotUtils.class);
    private static final String SCREENSHOT_DIR = "test-output/screenshots/";
    
    static {
        createScreenshotDirectory();
    }
    
    private ScreenshotUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Captures screenshot and returns the file path
     * @param driver WebDriver instance
     * @param testName Name of the test
     * @return Path to the screenshot file
     */
    public static String captureScreenshot(WebDriver driver, String testName) {
        String screenshotPath = null;
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = String.format("%s_%s.png", testName, timestamp);
            String filePath = SCREENSHOT_DIR + fileName;
            
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            File destination = new File(filePath);
            FileUtils.copyFile(source, destination);
            
            screenshotPath = destination.getAbsolutePath();
            log.info("Screenshot captured: {}", screenshotPath);
            
        } catch (IOException e) {
            log.error("Failed to capture screenshot", e);
        }
        return screenshotPath;
    }
    
    /**
     * Creates screenshot directory if it doesn't exist
     */
    private static void createScreenshotDirectory() {
        File directory = new File(SCREENSHOT_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    /**
     * Captures screenshot on failure
     * @param driver WebDriver instance
     * @param testName Name of the test
     * @return Path to the screenshot file
     */
    public static String captureScreenshotOnFailure(WebDriver driver, String testName) {
        String screenshotPath = captureScreenshot(driver, "FAILURE_" + testName);
        log.info("Failure screenshot captured for test: {}", testName);
        return screenshotPath;
    }
}