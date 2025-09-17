package com.designpattern.strategy.element;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Strategy that uses JavaScript to interact with elements.
 * Useful for cases where standard WebElement methods fail due to:
 * - Element being covered by other elements
 * - Stale element references
 * - Elements not being in the viewport
 * - Browser-specific rendering issues
 */
public class JavaScriptStrategy implements ElementInteractionStrategy {
    private static final Logger log = LogUtils.getLogger(JavaScriptStrategy.class);

    @Override
    public boolean click(WebDriver driver, WebElement element, int timeout) {
        try {
            log.debug("Attempting to click element using JavaScript strategy");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // First scroll the element into view
            js.executeScript("arguments[0].scrollIntoView({behavior: 'auto', block: 'center'});", element);
            
            // Wait for any animations to complete
            try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            
            // Try clicking using different JavaScript methods
            try {
                js.executeScript("arguments[0].click();", element);
            } catch (Exception e) {
                // If direct click fails, try dispatching a click event
                js.executeScript(
                    "var event = new MouseEvent('click', { bubbles: true, cancelable: true, view: window });" +
                    "arguments[0].dispatchEvent(event);", element);
            }
            
            log.debug("Successfully clicked element using JavaScript strategy");
            return true;
        } catch (Exception e) {
            log.warn("Failed to click element using JavaScript strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean type(WebDriver driver, WebElement element, String text, int timeout) {
        try {
            log.debug("Attempting to type text '{}' using JavaScript strategy", text);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // First make sure element is visible and scrolled into view
            js.executeScript("arguments[0].scrollIntoView({behavior: 'auto', block: 'center'});", element);
            
            // Clear the field using JavaScript
            clear(driver, element, timeout);
            
            // Set the value using JavaScript
            js.executeScript("arguments[0].value = arguments[1];", element, text);
            
            // Dispatch change event to trigger any listeners
            js.executeScript(
                "var event = new Event('change', { bubbles: true });" +
                "arguments[0].dispatchEvent(event);", element);
            
            // Verify the text was entered
            String actualValue = (String) js.executeScript("return arguments[0].value;", element);
            if (!text.equals(actualValue)) {
                log.warn("Text verification failed. Expected: '{}', Actual: '{}'", text, actualValue);
                return false;
            }
            
            log.debug("Successfully typed text using JavaScript strategy");
            return true;
        } catch (Exception e) {
            log.warn("Failed to type text using JavaScript strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean clear(WebDriver driver, WebElement element, int timeout) {
        try {
            log.debug("Attempting to clear element using JavaScript strategy");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Try multiple clear methods
            js.executeScript("arguments[0].value = '';", element);
            
            // Dispatch change event
            js.executeScript(
                "var event = new Event('change', { bubbles: true });" +
                "arguments[0].dispatchEvent(event);", element);
            
            // Verify the field is empty
            String value = (String) js.executeScript("return arguments[0].value;", element);
            if (!value.isEmpty()) {
                log.warn("Element not cleared completely using JavaScript");
                return false;
            }
            
            log.debug("Successfully cleared element using JavaScript strategy");
            return true;
        } catch (Exception e) {
            log.warn("Failed to clear element using JavaScript strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isDisplayed(WebDriver driver, WebElement element, int timeout) {
        try {
            log.debug("Checking element visibility using JavaScript strategy");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Check multiple visibility conditions using JavaScript
            Boolean isVisible = (Boolean) js.executeScript(
                "var elem = arguments[0];" +
                "return !!(elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length) && " +
                "window.getComputedStyle(elem).visibility !== 'hidden' && " +
                "window.getComputedStyle(elem).display !== 'none';", 
                element
            );
            
            return isVisible != null && isVisible;
        } catch (Exception e) {
            log.warn("Element visibility check failed using JavaScript strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getStrategyName() {
        return "JavaScript Strategy";
    }
}