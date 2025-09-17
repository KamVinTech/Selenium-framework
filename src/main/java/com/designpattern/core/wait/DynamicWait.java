package com.designpattern.core.wait;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.function.Function;

/**
 * Provides dynamic waiting capabilities with adaptive polling and timeouts
 */
public class DynamicWait<T> {
    private static final Logger log = LogUtils.getLogger(DynamicWait.class);
    private final WebDriver driver;
    private Duration timeout = Duration.ofSeconds(10);
    private Duration minPollingInterval = Duration.ofMillis(250);
    private Duration maxPollingInterval = Duration.ofSeconds(2);
    private boolean progressivePolling = true;
    private int progressiveFactor = 2;
    private int retryAttempts = 3;
    
    public DynamicWait(WebDriver driver) {
        this.driver = driver;
    }
    
    public DynamicWait<T> withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }
    
    public DynamicWait<T> withPollingInterval(Duration min, Duration max) {
        this.minPollingInterval = min;
        this.maxPollingInterval = max;
        return this;
    }
    
    public DynamicWait<T> withProgressivePolling(boolean enable, int factor) {
        this.progressivePolling = enable;
        this.progressiveFactor = factor;
        return this;
    }
    
    public DynamicWait<T> withRetryAttempts(int attempts) {
        this.retryAttempts = attempts;
        return this;
    }
    
    public T until(Function<WebDriver, T> condition) {
        Exception lastException = null;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout.toMillis();
        Duration currentPolling = minPollingInterval;
        int attempts = 0;
        
        while (System.currentTimeMillis() < endTime && attempts < retryAttempts) {
            try {
                T result = condition.apply(driver);
                if (result != null && (!(result instanceof Boolean) || (Boolean) result)) {
                    return result;
                }
            } catch (Exception e) {
                lastException = e;
                log.debug("Attempt {} failed: {}", attempts + 1, e.getMessage());
            }
            
            try {
                Thread.sleep(currentPolling.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TimeoutException("Wait was interrupted", e);
            }
            
            if (progressivePolling) {
                currentPolling = Duration.ofMillis(
                    Math.min(
                        currentPolling.toMillis() * progressiveFactor,
                        maxPollingInterval.toMillis()
                    )
                );
            }
            
            attempts++;
        }
        
        String timeoutMessage = String.format(
            "Timeout waiting for condition after %d ms and %d attempts",
            System.currentTimeMillis() - startTime,
            attempts
        );
        
        throw new TimeoutException(timeoutMessage, lastException);
    }
    
    /**
     * Common wait conditions
     */
    public static class Conditions {
        public static ExpectedCondition<Boolean> elementDisplayed(WebElement element) {
            return driver -> {
                try {
                    return element.isDisplayed();
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            };
        }
        
        public static ExpectedCondition<Boolean> elementClickable(WebElement element) {
            return driver -> {
                try {
                    return element.isDisplayed() && element.isEnabled();
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            };
        }
        
        public static ExpectedCondition<Boolean> elementHasText(WebElement element, String text) {
            return driver -> {
                try {
                    String elementText = element.getText();
                    return elementText != null && elementText.equals(text);
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            };
        }
        
        public static ExpectedCondition<Boolean> elementContainsText(WebElement element, String text) {
            return driver -> {
                try {
                    String elementText = element.getText();
                    return elementText != null && elementText.contains(text);
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            };
        }
        
        public static ExpectedCondition<Boolean> elementHasValue(WebElement element, String value) {
            return driver -> {
                try {
                    String elementValue = element.getAttribute("value");
                    return elementValue != null && elementValue.equals(value);
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            };
        }
    }
}