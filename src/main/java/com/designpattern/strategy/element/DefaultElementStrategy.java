package com.designpattern.strategy.element;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Default strategy using standard WebElement methods with additional
 * waits and verifications.
 */
public class DefaultElementStrategy implements ElementInteractionStrategy {
    private static final Logger log = LogUtils.getLogger(DefaultElementStrategy.class);

    @Override
    public boolean click(WebDriver driver, WebElement element, int timeout) {
        try {
            log.debug("Attempting to click element using default strategy");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            
            // Scroll element into view before clicking
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'auto', block: 'center'});", element);
            
            // Wait for any animations to complete
            try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            element.click();
            log.debug("Successfully clicked element using default strategy");
            return true;
        } catch (Exception e) {
            log.warn("Failed to click element using default strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean type(WebDriver driver, WebElement element, String text, int timeout) {
        try {
            log.debug("Attempting to type text '{}' using default strategy", text);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            
            // Clear using different methods
            clear(driver, element, timeout);
            
            element.sendKeys(text);
            
            // Verify the text was entered correctly
            String actualValue = element.getAttribute("value");
            if (!text.equals(actualValue)) {
                log.warn("Text verification failed. Expected: '{}', Actual: '{}'", text, actualValue);
                return false;
            }
            
            log.debug("Successfully typed text using default strategy");
            return true;
        } catch (Exception e) {
            log.warn("Failed to type text using default strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean clear(WebDriver driver, WebElement element, int timeout) {
        try {
            log.debug("Attempting to clear element using default strategy");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            
            // Try multiple clear methods
            element.clear();
            
            // If element still has text, try sending CTRL+A, DELETE
            if (!element.getAttribute("value").isEmpty()) {
                element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            }
            
            // Final verification
            if (!element.getAttribute("value").isEmpty()) {
                log.warn("Element not cleared completely");
                return false;
            }
            
            log.debug("Successfully cleared element using default strategy");
            return true;
        } catch (Exception e) {
            log.warn("Failed to clear element using default strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isDisplayed(WebDriver driver, WebElement element, int timeout) {
        try {
            log.debug("Checking element visibility using default strategy");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            return wait.until(ExpectedConditions.visibilityOf(element)) != null;
        } catch (Exception e) {
            log.warn("Element visibility check failed using default strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getStrategyName() {
        return "Default WebElement Strategy";
    }
}