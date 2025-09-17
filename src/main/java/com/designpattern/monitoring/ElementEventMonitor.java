package com.designpattern.monitoring;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Monitors element state changes and network activity during element interactions
 */
public class ElementEventMonitor implements WebDriverListener {
    private static final Logger log = LogUtils.getLogger(ElementEventMonitor.class);
    private final Map<String, Consumer<WebElement>> stateChangeHandlers = new ConcurrentHashMap<>();
    private final Map<String, Consumer<Map<String, Object>>> networkActivityHandlers = new ConcurrentHashMap<>();
    private final WebDriver driver;
    
    public ElementEventMonitor(WebDriver driver) {
        this.driver = driver;
    }
    
    public void onElementStateChange(WebElement element, String state, Consumer<WebElement> handler) {
        String elementKey = generateElementKey(element);
        stateChangeHandlers.put(elementKey + "_" + state, handler);
        startStateMonitoring(element, state);
    }
    
    public void onNetworkActivity(WebElement element, Consumer<Map<String, Object>> handler) {
        String elementKey = generateElementKey(element);
        networkActivityHandlers.put(elementKey, handler);
        startNetworkMonitoring(element);
    }
    
    private void startStateMonitoring(WebElement element, String state) {
        Thread monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    boolean stateChanged = checkElementState(element, state);
                    if (stateChanged) {
                        String elementKey = generateElementKey(element);
                        Consumer<WebElement> handler = stateChangeHandlers.get(elementKey + "_" + state);
                        if (handler != null) {
                            handler.accept(element);
                        }
                    }
                    Thread.sleep(100); // Poll every 100ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error monitoring element state: {}", e.getMessage());
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
    
    private void startNetworkMonitoring(WebElement element) {
        // Use Performance API to monitor network requests
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                "const observer = new PerformanceObserver((list) => {" +
                "   const entries = list.getEntries();" +
                "   window.seleniumNetworkEntries = entries;" +
                "});" +
                "observer.observe({entryTypes: ['resource', 'navigation']});"
            );
        }
    }
    
    private boolean checkElementState(WebElement element, String state) {
        try {
            switch (state.toLowerCase()) {
                case "visible":
                    return element.isDisplayed();
                case "enabled":
                    return element.isEnabled();
                case "selected":
                    return element.isSelected();
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    private String generateElementKey(WebElement element) {
        // Generate a unique key for the element based on its attributes
        try {
            String id = element.getAttribute("id");
            String name = element.getAttribute("name");
            String className = element.getAttribute("class");
            String xpath = element.toString(); // Usually contains the element's locator
            
            return String.format("%s_%s_%s_%s",
                id != null ? id : "",
                name != null ? name : "",
                className != null ? className : "",
                xpath.hashCode()
            ).replaceAll("\\s+", "_");
        } catch (Exception e) {
            return String.valueOf(element.hashCode());
        }
    }
    
    public Map<String, Object> getNetworkActivity(WebElement element) {
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object entries = js.executeScript("return window.seleniumNetworkEntries;");
            if (entries != null) {
                return new HashMap<>((Map<String, Object>) entries);
            }
        }
        return new HashMap<>();
    }
    
    public void clearNetworkActivity() {
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.seleniumNetworkEntries = [];");
        }
    }
}