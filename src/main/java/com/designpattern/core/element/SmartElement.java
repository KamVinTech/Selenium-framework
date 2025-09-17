package com.designpattern.core.element;

import com.designpattern.config.DynamicConfig;
import com.designpattern.healing.SelfHealingLocator;
import com.designpattern.monitoring.ElementEventMonitor;
import com.designpattern.monitoring.PerformanceMonitor;
import com.designpattern.retry.ContextAwareRetry;
import com.designpattern.strategy.element.ElementInteractionManager;
import com.designpattern.utils.LogUtils;
import com.designpattern.utils.exceptions.FrameworkException;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.io.FileHandler;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Enhanced WebElement wrapper that provides robust interaction methods with
 * built-in waits, retries, and fallback mechanisms.
 */
public class SmartElement implements WebElement {
    private static final Logger log = LogUtils.getLogger(SmartElement.class);
    
    private final WebDriver driver;
    private final WebElement element;
    private final ElementInteractionManager strategyManager;
    private final ElementStateValidator stateValidator;
    private final SelfHealingLocator healingLocator;
    private final ContextAwareRetry retryMechanism;
    private final ElementEventMonitor eventMonitor;
    private final PerformanceMonitor performanceMonitor;
    private final DynamicConfig config;
    
    public SmartElement(WebDriver driver, WebElement element, By originalLocator) {
        this.driver = driver;
        this.element = element;
        this.strategyManager = new ElementInteractionManager();
        this.stateValidator = new ElementStateValidator(element, null); // TODO: Add DynamicWait
        this.healingLocator = new SelfHealingLocator(driver, "element_" + originalLocator.toString(), originalLocator);
        this.retryMechanism = new ContextAwareRetry(driver);
        this.eventMonitor = new ElementEventMonitor(driver);
        this.performanceMonitor = new PerformanceMonitor(driver);
        this.config = new DynamicConfig();
        
        // Set up monitoring
        setupElementMonitoring();
    }
    
    private void setupElementMonitoring() {
        // Monitor element state changes
        eventMonitor.onElementStateChange(element, "visible", e -> 
            log.debug("Element visibility changed: {}", e.isDisplayed()));
            
        // Monitor network activity
        eventMonitor.onNetworkActivity(element, activity -> 
            log.debug("Network activity detected during element interaction"));
    }
    
    @Override
    public void click() {
        performanceMonitor.startTimingElement(element, "click");
        try {
            boolean success = retryMechanism.executeWithRetry(() -> 
                strategyManager.click(driver, element, config.getDefaultTimeout()));
            if (!success) {
                throw new FrameworkException("Failed to click element after all strategies");
            }
        } catch (Exception e) {
            throw new FrameworkException("Failed to click element", e);
        } finally {
            performanceMonitor.endTimingElement(element, "click");
        }
    }
    
    @Override
    public void sendKeys(CharSequence... keysToSend) {
        performanceMonitor.startTimingElement(element, "type");
        try {
            boolean success = retryMechanism.executeWithRetry(() ->
                strategyManager.type(driver, element, String.join("", keysToSend), config.getDefaultTimeout()));
            if (!success) {
                throw new FrameworkException("Failed to type text after all strategies");
            }
        } catch (Exception e) {
            throw new FrameworkException("Failed to type into element", e);
        } finally {
            performanceMonitor.endTimingElement(element, "type");
        }
    }
    
    @Override
    public void clear() {
        performanceMonitor.startTimingElement(element, "clear");
        try {
            boolean success = retryMechanism.executeWithRetry(() ->
                strategyManager.clear(driver, element, config.getDefaultTimeout()));
            if (!success) {
                throw new FrameworkException("Failed to clear element after all strategies");
            }
        } catch (Exception e) {
            throw new FrameworkException("Failed to clear element", e);
        } finally {
            performanceMonitor.endTimingElement(element, "clear");
        }
    }
    
    @Override
    public String getTagName() {
        return executeWithRetry(() -> element.getTagName());
    }
    
    @Override
    public String getAttribute(String name) {
        return executeWithRetry(() -> element.getAttribute(name));
    }
    
    @Override
    public boolean isSelected() {
        return executeWithRetry(() -> element.isSelected());
    }
    
    @Override
    public boolean isEnabled() {
        try {
            boolean enabled = strategyManager.isEnabled(driver, element, config.getDefaultTimeout());
            if (!enabled) {
                log.debug("Element is not enabled");
            }
            return enabled;
        } catch (Exception e) {
            log.debug("Failed to check if element is enabled: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getText() {
        return executeWithRetry(() -> element.getText());
    }
    
    @Override
    public List<WebElement> findElements(By by) {
        return executeWithRetry(() -> element.findElements(by));
    }
    
    @Override
    public WebElement findElement(By by) {
        return executeWithRetry(() -> element.findElement(by));
    }
    
    @Override
    public boolean isDisplayed() {
        try {
            return retryMechanism.executeWithRetry(() ->
                strategyManager.isDisplayed(driver, element, config.getDefaultTimeout()));
        } catch (Exception e) {
            log.debug("Failed to check element visibility: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public Point getLocation() {
        return executeWithRetry(() -> element.getLocation());
    }
    
    @Override
    public Dimension getSize() {
        return executeWithRetry(() -> element.getSize());
    }
    
    @Override
    public Rectangle getRect() {
        return executeWithRetry(() -> element.getRect());
    }
    
    @Override
    public String getCssValue(String propertyName) {
        return executeWithRetry(() -> element.getCssValue(propertyName));
    }
    
    private <T> T executeWithRetry(Callable<T> action) {
        try {
            return retryMechanism.executeWithRetry(action);
        } catch (Exception e) {
            throw new FrameworkException("Failed to execute action", e);
        }
    }
    
    // Additional enhanced methods
    
    public ElementStateValidator getStateValidator() {
        return stateValidator;
    }
    
    public void waitForState(String state, Consumer<WebElement> callback) {
        eventMonitor.onElementStateChange(element, state, callback);
    }
    
    public Map<String, Object> getPerformanceMetrics() {
        return performanceMonitor.getElementPerformance(element);
    }
    
    public void addRecoveryStrategy(Class<? extends WebDriverException> exceptionType, 
                                  Function<WebElement, Boolean> strategy) {
        healingLocator.addAlternativeLocator(By.cssSelector(strategy.toString()));
    }
    
    public DynamicConfig getConfig() {
        return config;
    }
    
    @Override
    public void submit() {
        performanceMonitor.startTimingElement(element, "submit");
        try {
            retryMechanism.executeWithRetry(() -> {
                element.submit();
                return null;
            });
        } catch (Exception e) {
            throw new FrameworkException("Failed to submit form", e);
        } finally {
            performanceMonitor.endTimingElement(element, "submit");
        }
    }
    
    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        if (element instanceof TakesScreenshot) {
            return ((TakesScreenshot) element).getScreenshotAs(target);
        }
        throw new UnsupportedOperationException("Underlying element does not support taking screenshots");
    }
    
    /**
     * Takes a screenshot of this element and saves it to the specified file
     */
    public File captureElementScreenshot(String fileName) {
        try {
            File screenshot = getScreenshotAs(OutputType.FILE);
            File destination = new File("test-output/screenshots/" + fileName);
            FileHandler.copy(screenshot, destination);
            return destination;
        } catch (Exception e) {
            throw new FrameworkException("Failed to capture element screenshot", e);
        }
    }
}