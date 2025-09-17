package com.designpattern.utils;

import com.designpattern.strategy.element.ElementInteractionManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.Random;

/**
 * Common utility methods for Selenium WebDriver operations
 */
public class WebElementUtils {
    private static final Logger log = LogUtils.getLogger(WebElementUtils.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final ElementInteractionManager interactionManager = new ElementInteractionManager();
    
    private WebElementUtils() {
        // Private constructor to prevent instantiation
    }


    
    /**
     * Safely clicks on an element with retries
     */
    public static void safeClick(WebDriver driver, WebElement element) {
        log.debug("Attempting to click element using all available strategies");
        if (!interactionManager.click(driver, element, (int) DEFAULT_TIMEOUT.getSeconds())) {
            throw new FrameworkException("Failed to click element using all available strategies");
        }
    }
    
    /**
     * Safely enters text into an element
     */
    public static void safeType(WebDriver driver, WebElement element, String text) {
        log.debug("Attempting to type text '{}' using all available strategies", text);
        if (!interactionManager.type(driver, element, text, (int) DEFAULT_TIMEOUT.getSeconds())) {
            throw new FrameworkException("Failed to type text using all available strategies");
        }
    }
    
    /**
     * Waits for element to be visible
     */
    public static WebElement waitForElementVisible(WebDriver driver, WebElement element) {
        log.debug("Waiting for element to be visible: {}", element);
        WebElement result = ExceptionUtils.waitSafely(
            ExpectedConditions.visibilityOf(element),
            DEFAULT_TIMEOUT,
            driver,
            "Element not visible"
        );
        log.debug("Element is now visible: {}", element);
        return result;
    }
    
    /**
     * Waits for element to be clickable
     */
    public static WebElement waitForElementClickable(WebDriver driver, WebElement element) {
        return ExceptionUtils.waitSafely(
            ExpectedConditions.elementToBeClickable(element),
            DEFAULT_TIMEOUT,
            driver,
            "Element not clickable"
        );
    }
    
    /**
     * Selects option from dropdown by visible text
     */
    public static void selectByVisibleText(WebDriver driver, WebElement element, String text) {
        ExceptionUtils.handleWebDriverOperation(d -> {
            waitForElementVisible(driver, element);
            new Select(element).selectByVisibleText(text);
            return true;
        }, driver, "Failed to select option: " + text);
    }
    
    /**
     * Hovers over an element
     */
    public static void hoverOverElement(WebDriver driver, WebElement element) {
        ExceptionUtils.handleWebDriverOperation(d -> {
            waitForElementVisible(driver, element);
            new Actions(driver).moveToElement(element).perform();
            return true;
        }, driver, "Failed to hover over element");
    }
    
    /**
     * Switches to a new window/tab
     */
    public static void switchToNewWindow(WebDriver driver) {
        String currentWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
    
    /**
     * Scrolls element into view
     */
    public static void scrollIntoView(WebDriver driver, WebElement element) {
        ExceptionUtils.handleWebDriverOperation(d -> {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView(true);", element);
            return true;
        }, driver, "Failed to scroll element into view");
    }
    
    /**
     * Gets random string for test data
     */
    public static String getRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    /**
     * Checks if element exists
     */
    public static boolean isElementPresent(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}