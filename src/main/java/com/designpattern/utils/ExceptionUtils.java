package com.designpattern.utils;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Function;

/**
 * Exception handling utility for WebDriver operations
 */
public class ExceptionUtils {
    private static final Logger log = LogUtils.getLogger(ExceptionUtils.class);
    
    private ExceptionUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Execute a boolean operation (like click) with retry logic
     */
    public static boolean handleBooleanOperation(Function<WebDriver, Boolean> operation, 
                                             WebDriver driver, 
                                             String errorMessage) {
        try {
            return operation.apply(driver);
        } catch (StaleElementReferenceException e) {
            log.warn("Stale element encountered. Retrying operation...");
            return operation.apply(driver);
        } catch (ElementClickInterceptedException e) {
            log.warn("Element click intercepted");
            throw new FrameworkException(errorMessage, e);
        } catch (TimeoutException e) {
            log.error("Timeout waiting for element");
            throw new FrameworkException(errorMessage + " - Timeout", e);
        } catch (WebDriverException e) {
            log.error("WebDriver operation failed", e);
            throw new FrameworkException(errorMessage, e);
        }
    }

    /**
     * Execute a WebDriver operation with proper exception handling
     */
    public static <T> T handleWebDriverOperation(Function<WebDriver, T> operation, 
                                             WebDriver driver, 
                                             String errorMessage) {
        try {
            return operation.apply(driver);
        } catch (StaleElementReferenceException e) {
            log.warn("Stale element encountered. Retrying operation...");
            return operation.apply(driver);
        } catch (ElementClickInterceptedException e) {
            log.warn("Element click intercepted");
            throw new FrameworkException(errorMessage, e);
        } catch (TimeoutException e) {
            log.error("Timeout waiting for element");
            throw new FrameworkException(errorMessage + " - Timeout", e);
        } catch (WebDriverException e) {
            log.error("WebDriver operation failed", e);
            throw new FrameworkException(errorMessage, e);
        }
    }

    /**
     * Handle JavaScript click operation
     */
    public static boolean handleClickByJavaScript(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            return true;
        } catch (WebDriverException e) {
            log.error("JavaScript click failed", e);
            throw new FrameworkException("Failed to click element using JavaScript", e);
        }
    }

    /**
     * Wait safely for a condition
     */
    public static <T> T waitSafely(ExpectedCondition<T> condition,
                                Duration timeout,
                                WebDriver driver,
                                String errorMessage) {
        try {
            return new WebDriverWait(driver, timeout).until(condition);
        } catch (TimeoutException e) {
            log.error("Timeout waiting for condition: {}", errorMessage);
            throw new FrameworkException(errorMessage, e);
        }
    }
}