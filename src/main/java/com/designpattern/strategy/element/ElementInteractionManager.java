package com.designpattern.strategy.element;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages multiple element interaction strategies and attempts them in sequence
 * until one succeeds or all fail.
 */
public class ElementInteractionManager {
    private static final Logger log = LogUtils.getLogger(ElementInteractionManager.class);
    private final List<ElementInteractionStrategy> strategies;
    
    public ElementInteractionManager() {
        strategies = new ArrayList<>();
        // Add strategies in order of preference
        strategies.add(new DefaultElementStrategy());
        strategies.add(new ActionsStrategy());
        strategies.add(new JavaScriptStrategy());
    }
    
    /**
     * Try to click an element using all available strategies
     */
    public boolean click(WebDriver driver, WebElement element, int timeout) {
        for (ElementInteractionStrategy strategy : strategies) {
            log.debug("Attempting click using strategy: {}", strategy.getStrategyName());
            if (strategy.click(driver, element, timeout)) {
                log.info("Successfully clicked element using strategy: {}", strategy.getStrategyName());
                return true;
            }
            log.debug("Click failed using strategy: {}", strategy.getStrategyName());
        }
        log.error("Failed to click element using all available strategies");
        return false;
    }
    
    /**
     * Try to type text using all available strategies
     */
    public boolean type(WebDriver driver, WebElement element, String text, int timeout) {
        for (ElementInteractionStrategy strategy : strategies) {
            log.debug("Attempting to type text using strategy: {}", strategy.getStrategyName());
            if (strategy.type(driver, element, text, timeout)) {
                log.info("Successfully typed text using strategy: {}", strategy.getStrategyName());
                return true;
            }
            log.debug("Type operation failed using strategy: {}", strategy.getStrategyName());
        }
        log.error("Failed to type text using all available strategies");
        return false;
    }
    
    /**
     * Try to clear element using all available strategies
     */
    public boolean clear(WebDriver driver, WebElement element, int timeout) {
        for (ElementInteractionStrategy strategy : strategies) {
            log.debug("Attempting to clear element using strategy: {}", strategy.getStrategyName());
            if (strategy.clear(driver, element, timeout)) {
                log.info("Successfully cleared element using strategy: {}", strategy.getStrategyName());
                return true;
            }
            log.debug("Clear operation failed using strategy: {}", strategy.getStrategyName());
        }
        log.error("Failed to clear element using all available strategies");
        return false;
    }
    
    /**
     * Check element visibility using all available strategies
     */
    public boolean isDisplayed(WebDriver driver, WebElement element, int timeout) {
        for (ElementInteractionStrategy strategy : strategies) {
            log.debug("Checking element visibility using strategy: {}", strategy.getStrategyName());
            if (strategy.isDisplayed(driver, element, timeout)) {
                log.debug("Element is visible according to strategy: {}", strategy.getStrategyName());
                return true;
            }
        }
        log.debug("Element is not visible according to any strategy");
        return false;
    }
    
    /**
     * Check if element is enabled using all available strategies
     */
    public boolean isEnabled(WebDriver driver, WebElement element, int timeout) {
        for (ElementInteractionStrategy strategy : strategies) {
            log.debug("Checking if element is enabled using strategy: {}", strategy.getStrategyName());
            if (strategy.isEnabled(driver, element, timeout)) {
                log.debug("Element is enabled according to strategy: {}", strategy.getStrategyName());
                return true;
            }
        }
        log.debug("Element is not enabled according to any strategy");
        return false;
    }
    
    /**
     * Add a custom strategy to the beginning of the strategy list
     */
    public void addStrategy(ElementInteractionStrategy strategy) {
        strategies.add(0, strategy);
        log.info("Added new interaction strategy: {}", strategy.getStrategyName());
    }
    
    /**
     * Remove a strategy by its class type
     */
    public void removeStrategy(Class<? extends ElementInteractionStrategy> strategyClass) {
        strategies.removeIf(s -> s.getClass().equals(strategyClass));
        log.info("Removed interaction strategy of type: {}", strategyClass.getSimpleName());
    }
    
    /**
     * Get the list of currently active strategies
     */
    public List<ElementInteractionStrategy> getStrategies() {
        return new ArrayList<>(strategies);
    }
}