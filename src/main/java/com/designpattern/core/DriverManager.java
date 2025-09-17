package com.designpattern.core;

import com.designpattern.utils.ConfigurationManager;
import com.designpattern.utils.FrameworkException;
import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * Singleton pattern implementation for WebDriver management
 */
public class DriverManager {
    private static final Logger log = LogUtils.getLogger(DriverManager.class);
    private static DriverManager instance = null;
    private static final ThreadLocal<WebDriver> webDriver = new ThreadLocal<>();
    private static final ThreadLocal<String> sessionId = new ThreadLocal<>();

    private DriverManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the singleton instance of DriverManager
     * @return DriverManager instance
     */
    public static DriverManager getInstance() {
        if (instance == null) {
            synchronized (DriverManager.class) {
                if (instance == null) {
                    instance = new DriverManager();
                }
            }
        }
        return instance;
    }

    /**
     * Gets the WebDriver instance for current thread
     * @return WebDriver instance
     */
    public WebDriver getDriver() {
        WebDriver driver = webDriver.get();
        if (driver == null) {
            log.info("Creating new WebDriver instance for thread: {}", Thread.currentThread().getId());
            driver = DriverFactory.createDriver();
            setDriver(driver);
        }
        return driver;
    }

    /**
     * Sets the WebDriver instance for current thread
     * @param driver WebDriver instance
     */
    public void setDriver(WebDriver driver) {
        if (driver == null) {
            throw new FrameworkException("Driver cannot be null");
        }
        webDriver.set(driver);
        sessionId.set(String.valueOf(Thread.currentThread().getId()));
        log.info("WebDriver set for thread: {}", sessionId.get());
    }

    /**
     * Quits the WebDriver instance and removes it from thread local
     */
    public void quitDriver() {
        log.info("Quitting WebDriver for thread: {}", sessionId.get());
        WebDriver driver = webDriver.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                log.error("Error while quitting WebDriver", e);
            } finally {
                webDriver.remove();
                sessionId.remove();
            }
        }
    }

    /**
     * Navigates to the base URL from configuration
     */
    public void navigateToBaseUrl() {
        String baseUrl = ConfigurationManager.getInstance().getProperty("base.url");
        log.info("Navigating to base URL: {}", baseUrl);
        getDriver().get(baseUrl);
    }
}