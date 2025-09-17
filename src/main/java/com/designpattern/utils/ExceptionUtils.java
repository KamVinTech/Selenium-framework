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
     * Executes a WebDriver operation with proper exception handling
     * @param operation Operation to execute
     * @param errorMessage Error message if operation fails
     * @param <T> Return type of the operation
     * @return Result of the operation
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
            log.warn("Element click intercepted. Attempting JavaScript click...");
            if (operation instanceof ClickOperation) {
                return (T) handleClickByJavaScript(driver, ((ClickOperation) operation).getElement());
            }
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
     * Waits for a condition with proper exception handling
     * @param condition Condition to wait for
     * @param timeout Timeout duration
     * @param errorMessage Error message if condition is not met
     * @return Result of the wait operation
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
    
    private static Boolean handleClickByJavaScript(WebDriver driver, WebElement element) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", element);
            return true;
        } catch (Exception e) {
            log.error("Failed to click element using JavaScript", e);
            throw new FrameworkException("Failed to click element", e);
        }
    }
    
    /**
     * Interface for click operations
     */
    public interface ClickOperation extends Function<WebDriver, Boolean> {
        WebElement getElement();
    }
}