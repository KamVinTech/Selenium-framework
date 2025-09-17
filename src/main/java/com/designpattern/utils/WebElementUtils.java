package com.designpattern.utils;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Common utility methods for Selenium WebDriver operations
 */
public class WebElementUtils {
    private static final Logger log = LogUtils.getLogger(WebElementUtils.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    
    private WebElementUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Safely clicks on an element with retries
     */
    public static void safeClick(WebDriver driver, WebElement element) {
        ExceptionUtils.handleWebDriverOperation(d -> {
            waitForElementClickable(driver, element);
            element.click();
            return true;
        }, driver, "Failed to click element");
    }
    
    /**
     * Safely enters text into an element
     */
    public static void safeType(WebDriver driver, WebElement element, String text) {
        ExceptionUtils.handleWebDriverOperation(d -> {
            waitForElementVisible(driver, element);
            element.clear();
            element.sendKeys(text);
            return true;
        }, driver, "Failed to type text: " + text);
    }
    
    /**
     * Waits for element to be visible
     */
    public static WebElement waitForElementVisible(WebDriver driver, WebElement element) {
        return ExceptionUtils.waitSafely(
            ExpectedConditions.visibilityOf(element),
            DEFAULT_TIMEOUT,
            driver,
            "Element not visible"
        );
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