package com.designpattern.strategy.element;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Interface defining different strategies for interacting with web elements.
 * Each implementation provides a different approach to handle element interactions,
 * allowing for fallback mechanisms when one approach fails.
 */
public interface ElementInteractionStrategy {
    /**
     * Click on the element using this strategy
     * @param driver WebDriver instance
     * @param element Element to click
     * @param timeout Maximum time to wait for the operation
     * @return true if click was successful, false if this strategy couldn't perform the click
     */
    boolean click(WebDriver driver, WebElement element, int timeout);
    
    /**
     * Type text into the element using this strategy
     * @param driver WebDriver instance
     * @param element Element to type into
     * @param text Text to enter
     * @param timeout Maximum time to wait for the operation
     * @return true if typing was successful, false if this strategy couldn't type
     */
    boolean type(WebDriver driver, WebElement element, String text, int timeout);
    
    /**
     * Clear element's content using this strategy
     * @param driver WebDriver instance
     * @param element Element to clear
     * @param timeout Maximum time to wait for the operation
     * @return true if clear was successful, false if this strategy couldn't clear
     */
    boolean clear(WebDriver driver, WebElement element, int timeout);
    
    /**
     * Check if element is displayed using this strategy
     * @param driver WebDriver instance
     * @param element Element to check
     * @param timeout Maximum time to wait for the operation
     * @return true if element is displayed, false if not or if strategy couldn't check
     */
    boolean isDisplayed(WebDriver driver, WebElement element, int timeout);
    
    /**
     * Get the name of this strategy for logging purposes
     * @return Strategy name
     */
    String getStrategyName();
}