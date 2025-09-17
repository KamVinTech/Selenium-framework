package com.designpattern.monitoring;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitors and collects performance metrics for element interactions
 */
public class PerformanceMonitor {
    private static final Logger log = LogUtils.getLogger(PerformanceMonitor.class);
    private final WebDriver driver;
    private final Map<String, Map<String, Long>> elementTimings = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> performanceData = new ConcurrentHashMap<>();
    
    public PerformanceMonitor(WebDriver driver) {
        this.driver = driver;
        initializePerformanceMonitoring();
    }
    
    private void initializePerformanceMonitoring() {
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                "if (!window.seleniumPerformanceData) {" +
                "    window.seleniumPerformanceData = {};" +
                "}" +
                "const observer = new PerformanceObserver((list) => {" +
                "    for (const entry of list.getEntries()) {" +
                "        window.seleniumPerformanceData[entry.name] = entry.toJSON();" +
                "    }" +
                "});" +
                "observer.observe({ entryTypes: ['navigation', 'resource', 'paint', 'largest-contentful-paint'] });"
            );
        }
    }
    
    public void startTimingElement(WebElement element, String action) {
        String elementKey = generateElementKey(element);
        Map<String, Long> timings = elementTimings.computeIfAbsent(elementKey, k -> new HashMap<>());
        timings.put(action + "_start", System.currentTimeMillis());
    }
    
    public void endTimingElement(WebElement element, String action) {
        String elementKey = generateElementKey(element);
        Map<String, Long> timings = elementTimings.get(elementKey);
        if (timings != null && timings.containsKey(action + "_start")) {
            long startTime = timings.get(action + "_start");
            long duration = System.currentTimeMillis() - startTime;
            timings.put(action + "_duration", duration);
            
            log.debug("Element action '{}' took {} ms", action, duration);
            
            // Store in performance data
            Map<String, Object> actionData = new HashMap<>();
            actionData.put("duration", duration);
            actionData.put("timestamp", System.currentTimeMillis());
            performanceData.computeIfAbsent(elementKey, k -> new HashMap<>()).put(action, actionData);
        }
    }
    
    public Map<String, Object> getElementPerformance(WebElement element) {
        String elementKey = generateElementKey(element);
        return new HashMap<>(performanceData.getOrDefault(elementKey, new HashMap<>()));
    }
    
    public Map<String, Object> getPagePerformanceMetrics() {
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Get basic timing data
            Map<String, Object> timingData = new HashMap<>();
            Object timing = js.executeScript("return window.performance.timing.toJSON();");
            if (timing instanceof Map) {
                timingData.putAll((Map<String, Object>) timing);
            }
            
            // Get navigation timing
            Object navTiming = js.executeScript(
                "if (window.performance.getEntriesByType) {" +
                "    return window.performance.getEntriesByType('navigation')[0];" +
                "} else { return null; }"
            );
            if (navTiming instanceof Map) {
                timingData.put("navigationTiming", navTiming);
            }
            
            // Get resource timing
            Object resourceTiming = js.executeScript(
                "if (window.performance.getEntriesByType) {" +
                "    return window.performance.getEntriesByType('resource');" +
                "} else { return []; }"
            );
            if (resourceTiming instanceof Object[]) {
                timingData.put("resourceTiming", resourceTiming);
            }
            
            return timingData;
        }
        return new HashMap<>();
    }
    
    private String generateElementKey(WebElement element) {
        try {
            String id = element.getAttribute("id");
            String name = element.getAttribute("name");
            String className = element.getAttribute("class");
            String xpath = element.toString();
            
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
    
    public void clearPerformanceData() {
        elementTimings.clear();
        performanceData.clear();
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.seleniumPerformanceData = {};");
        }
    }
}