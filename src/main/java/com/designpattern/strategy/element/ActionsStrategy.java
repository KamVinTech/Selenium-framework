package com.designpattern.strategy.element;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Strategy that uses Selenium's Actions API for element interactions.
 * Useful for:
 * - Complex mouse movements
 * - Hover interactions
 * - Cases where direct element interaction fails
 * - Elements that need precise mouse positioning
 */
public class ActionsStrategy implements ElementInteractionStrategy {
    private static final Logger log = LogUtils.getLogger(ActionsStrategy.class);

    @Override
    public boolean click(WebDriver driver, WebElement element, int timeout) {
        try {
            log.debug("Attempting to click element using Actions strategy");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            
            Actions actions = new Actions(driver);
            
            // First try moving to element and clicking
            try {
                actions.moveToElement(element)
                       .pause(Duration.ofMillis(200))  // Wait for any tooltips/overlays
                       .click()
                       .perform();
            } catch (Exception e) {
                // If that fails, try clicking with offset
                actions.moveToElement(element, 1, 1)  // Slight offset to avoid any borders
                       .pause(Duration.ofMillis(200))
                       .click()
                       .perform();
            }
            
            log.debug("Successfully clicked element using Actions strategy");
            return true;
        } catch (Exception e) {
            log.warn("Failed to click element using Actions strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean type(WebDriver driver, WebElement element, String text, int timeout) {
        try {
            log.debug("Attempting to type text '{}' using Actions strategy", text);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            
            Actions actions = new Actions(driver);
            
            // First clear the field
            clear(driver, element, timeout);
            
            // Move to element, click to focus, and send keys
            actions.moveToElement(element)
                   .click()
                   .sendKeys(text)
                   .perform();
            
            // Verify the text was entered
            String actualValue = element.getAttribute("value");
            if (!text.equals(actualValue)) {
                log.warn("Text verification failed. Expected: '{}', Actual: '{}'", text, actualValue);
                return false;
            }
            
            log.debug("Successfully typed text using Actions strategy");
            return true;
        } catch (Exception e) {
            log.warn("Failed to type text using Actions strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean clear(WebDriver driver, WebElement element, int timeout) {
        try {
            log.debug("Attempting to clear element using Actions strategy");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            
            Actions actions = new Actions(driver);
            
            // Move to element and triple click to select all text
            actions.moveToElement(element)
                   .click()
                   .keyDown(Keys.CONTROL)
                   .sendKeys("a")
                   .keyUp(Keys.CONTROL)
                   .sendKeys(Keys.DELETE)
                   .perform();
            
            // Verify the field is empty
            if (!element.getAttribute("value").isEmpty()) {
                log.warn("Element not cleared completely using Actions strategy");
                return false;
            }
            
            log.debug("Successfully cleared element using Actions strategy");
            return true;
        } catch (Exception e) {
            log.warn("Failed to clear element using Actions strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isDisplayed(WebDriver driver, WebElement element, int timeout) {
        try {
            log.debug("Checking element visibility using Actions strategy");
            
            // Try to move mouse to element - will throw exception if not interactive
            Actions actions = new Actions(driver);
            actions.moveToElement(element).perform();
            
            // If we got here, element is interactable
            Point location = element.getLocation();
            Dimension size = element.getSize();
            
            // Check if element has size and is in viewport
            return size.getHeight() > 0 && 
                   size.getWidth() > 0 &&
                   isElementInViewport(driver, location, size);
                   
        } catch (Exception e) {
            log.warn("Element visibility check failed using Actions strategy: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getStrategyName() {
        return "Actions API Strategy";
    }
    
    private boolean isElementInViewport(WebDriver driver, Point location, Dimension size) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Long innerHeight = (Long) js.executeScript("return window.innerHeight");
        Long innerWidth = (Long) js.executeScript("return window.innerWidth");
        
        return location.getX() >= 0 &&
               location.getY() >= 0 &&
               location.getX() + size.getWidth() <= innerWidth &&
               location.getY() + size.getHeight() <= innerHeight;
    }

    @Override
    public boolean isEnabled(WebDriver driver, WebElement element, int timeout) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isEnabled'");
    }
}