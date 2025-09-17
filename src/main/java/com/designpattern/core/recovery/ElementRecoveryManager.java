package com.designpattern.core.recovery;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebElement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles common element interaction failures with recovery strategies
 */
public class ElementRecoveryManager {
    private static final Logger log = LogUtils.getLogger(ElementRecoveryManager.class);
    private final WebDriver driver;
    private final Map<Class<? extends WebDriverException>, Function<WebElement, Boolean>> recoveryStrategies;
    
    public ElementRecoveryManager(WebDriver driver) {
        this.driver = driver;
        this.recoveryStrategies = new HashMap<>();
        initializeDefaultStrategies();
    }
    
    private void initializeDefaultStrategies() {
        // Handle stale element exceptions
        recoveryStrategies.put(StaleElementReferenceException.class, element -> {
            try {
                // Attempt to re-find the element using its locator
                if (element instanceof RemoteWebElement) {
                    String locator = ((RemoteWebElement) element).getId();
                    driver.findElement(By.id(locator));
                    return true;
                }
            } catch (Exception e) {
                log.debug("Failed to recover from stale element: {}", e.getMessage());
            }
            return false;
        });
        
        // Handle element intercepted exceptions
        recoveryStrategies.put(ElementClickInterceptedException.class, element -> {
            try {
                // Scroll element into view and try to remove overlays
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView(true); " +
                    "var overlays = document.querySelectorAll('.overlay, .modal, .dialog'); " +
                    "overlays.forEach(o => o.style.display = 'none');",
                    element
                );
                return true;
            } catch (Exception e) {
                log.debug("Failed to recover from intercepted element: {}", e.getMessage());
                return false;
            }
        });
        
        // Handle element not visible exceptions
        recoveryStrategies.put(ElementNotInteractableException.class, element -> {
            try {
                // Scroll element into view and wait briefly
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView(true);",
                    element
                );
                Thread.sleep(500); // Brief wait for scrolling
                return true;
            } catch (Exception e) {
                log.debug("Failed to recover from element not interactable: {}", e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Add a custom recovery strategy for a specific exception type
     */
    public void addRecoveryStrategy(
        Class<? extends WebDriverException> exceptionType,
        Function<WebElement, Boolean> strategy
    ) {
        recoveryStrategies.put(exceptionType, strategy);
    }
    
    /**
     * Attempt to recover from an element interaction failure
     */
    public boolean attemptRecovery(WebElement element, WebDriverException exception) {
        Function<WebElement, Boolean> strategy = recoveryStrategies.get(exception.getClass());
        if (strategy != null) {
            log.debug("Attempting recovery for exception: {}", exception.getClass().getSimpleName());
            return strategy.apply(element);
        }
        return false;
    }
    
    /**
     * Clear all recovery strategies
     */
    public void clearStrategies() {
        recoveryStrategies.clear();
    }
    
    /**
     * Check if a recovery strategy exists for an exception type
     */
    public boolean hasStrategyFor(Class<? extends WebDriverException> exceptionType) {
        return recoveryStrategies.containsKey(exceptionType);
    }
    
    /**
     * Get the total number of registered recovery strategies
     */
    public int getStrategyCount() {
        return recoveryStrategies.size();
    }
}