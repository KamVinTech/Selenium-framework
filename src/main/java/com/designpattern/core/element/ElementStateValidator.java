package com.designpattern.core.element;

import com.designpattern.core.wait.DynamicWait;
import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;

import java.time.Duration;

/**
 * Handles element state validation with retry mechanisms
 */
public class ElementStateValidator {
    private static final Logger log = LogUtils.getLogger(ElementStateValidator.class);
    private final WebElement element;
    private final DynamicWait<Boolean> wait;
    
    public ElementStateValidator(WebElement element, DynamicWait<Boolean> wait) {
        this.element = element;
        this.wait = wait;
    }
    
    public boolean isDisplayed() {
        try {
            return wait.until(DynamicWait.Conditions.elementDisplayed(element));
        } catch (Exception e) {
            log.debug("Element is not displayed: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isClickable() {
        try {
            return wait.until(DynamicWait.Conditions.elementClickable(element));
        } catch (Exception e) {
            log.debug("Element is not clickable: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean hasText(String expectedText) {
        try {
            return wait.until(DynamicWait.Conditions.elementHasText(element, expectedText));
        } catch (Exception e) {
            log.debug("Element does not have expected text: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean containsText(String text) {
        try {
            return wait.until(DynamicWait.Conditions.elementContainsText(element, text));
        } catch (Exception e) {
            log.debug("Element does not contain expected text: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean hasValue(String expectedValue) {
        try {
            return wait.until(DynamicWait.Conditions.elementHasValue(element, expectedValue));
        } catch (Exception e) {
            log.debug("Element does not have expected value: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Retries validation with custom polling configuration
     */
    public ElementStateValidator withCustomPolling(Duration minPolling, Duration maxPolling) {
        wait.withPollingInterval(minPolling, maxPolling);
        return this;
    }
    
    /**
     * Enables progressive polling with custom factor
     */
    public ElementStateValidator withProgressivePolling(int factor) {
        wait.withProgressivePolling(true, factor);
        return this;
    }
    
    /**
     * Sets custom timeout for validations
     */
    public ElementStateValidator withTimeout(Duration timeout) {
        wait.withTimeout(timeout);
        return this;
    }
    
    /**
     * Sets number of retry attempts
     */
    public ElementStateValidator withRetries(int attempts) {
        wait.withRetryAttempts(attempts);
        return this;
    }
}