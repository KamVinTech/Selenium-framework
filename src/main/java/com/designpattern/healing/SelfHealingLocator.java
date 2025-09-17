package com.designpattern.healing;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Provides self-healing capabilities for element locators
 */
public class SelfHealingLocator implements ElementLocator {
    private static final Logger log = LogUtils.getLogger(SelfHealingLocator.class);
    private final WebDriver driver;
    private final String elementKey;
    private final Map<String, List<By>> alternativeLocators = new ConcurrentHashMap<>();
    private final Map<String, Integer> locatorSuccessRates = new ConcurrentHashMap<>();
    
    public SelfHealingLocator(WebDriver driver, String elementKey, By... initialLocators) {
        this.driver = driver;
        this.elementKey = elementKey;
        List<By> locators = new ArrayList<>(Arrays.asList(initialLocators));
        alternativeLocators.put(elementKey, locators);
    }
    
    @Override
    public WebElement findElement() {
        List<By> healingStrategies = getHealingStrategies();
        NoSuchElementException lastException = null;
        
        for (By locator : healingStrategies) {
            try {
                WebElement element = driver.findElement(locator);
                updateLocatorSuccess(locator);
                return element;
            } catch (NoSuchElementException e) {
                lastException = e;
                updateLocatorFailure(locator);
            }
        }
        
        // If all strategies fail, try to generate new locators
        List<By> newLocators = generateNewLocators();
        alternativeLocators.get(elementKey).addAll(newLocators);
        
        // Try the new locators
        for (By locator : newLocators) {
            try {
                WebElement element = driver.findElement(locator);
                updateLocatorSuccess(locator);
                return element;
            } catch (NoSuchElementException e) {
                lastException = e;
                updateLocatorFailure(locator);
            }
        }
        
        if (lastException != null) {
            throw lastException;
        }
        throw new NoSuchElementException("Element not found with any strategy: " + elementKey);
    }
    
    @Override
    public List<WebElement> findElements() {
        List<WebElement> elements = new ArrayList<>();
        List<By> healingStrategies = getHealingStrategies();
        
        for (By locator : healingStrategies) {
            try {
                List<WebElement> found = driver.findElements(locator);
                if (!found.isEmpty()) {
                    updateLocatorSuccess(locator);
                    elements.addAll(found);
                }
            } catch (Exception e) {
                updateLocatorFailure(locator);
            }
        }
        
        if (elements.isEmpty()) {
            List<By> newLocators = generateNewLocators();
            alternativeLocators.get(elementKey).addAll(newLocators);
            
            for (By locator : newLocators) {
                try {
                    List<WebElement> found = driver.findElements(locator);
                    if (!found.isEmpty()) {
                        updateLocatorSuccess(locator);
                        elements.addAll(found);
                    }
                } catch (Exception e) {
                    updateLocatorFailure(locator);
                }
            }
        }
        
        return elements;
    }
    
    private List<By> getHealingStrategies() {
        List<By> strategies = alternativeLocators.get(elementKey);
        
        // Sort strategies by success rate
        return strategies.stream()
            .sorted((a, b) -> {
                int rateA = locatorSuccessRates.getOrDefault(a.toString(), 0);
                int rateB = locatorSuccessRates.getOrDefault(b.toString(), 0);
                return Integer.compare(rateB, rateA);
            })
            .collect(Collectors.toList());
    }
    
    private void updateLocatorSuccess(By locator) {
        String key = locator.toString();
        locatorSuccessRates.merge(key, 1, Integer::sum);
    }
    
    private void updateLocatorFailure(By locator) {
        String key = locator.toString();
        locatorSuccessRates.merge(key, -1, Integer::sum);
    }
    
    private List<By> generateNewLocators() {
        List<By> newLocators = new ArrayList<>();
        
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Try to find similar elements based on text content
            try {
                List<WebElement> existingElements = findElements();
                if (!existingElements.isEmpty()) {
                    WebElement reference = existingElements.get(0);
                    String text = reference.getText();
                    if (text != null && !text.isEmpty()) {
                        newLocators.add(By.xpath("//*[contains(text(), '" + text + "')]"));
                    }
                    
                    // Generate CSS selector based on attributes
                    String cssSelector = generateCssSelector(reference);
                    if (cssSelector != null) {
                        newLocators.add(By.cssSelector(cssSelector));
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to generate new locators based on existing element: {}", e.getMessage());
            }
        }
        
        return newLocators;
    }
    
    private String generateCssSelector(WebElement element) {
        try {
            StringBuilder selector = new StringBuilder();
            
            String id = element.getAttribute("id");
            if (id != null && !id.isEmpty()) {
                return "#" + id;
            }
            
            String className = element.getAttribute("class");
            if (className != null && !className.isEmpty()) {
                selector.append(".");
                selector.append(className.replaceAll("\\s+", "."));
            }
            
            String name = element.getAttribute("name");
            if (name != null && !name.isEmpty()) {
                selector.append("[name='").append(name).append("']");
            }
            
            return selector.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    public void addAlternativeLocator(By locator) {
        alternativeLocators.get(elementKey).add(locator);
    }
    
    public void removeLocator(By locator) {
        alternativeLocators.get(elementKey).remove(locator);
        locatorSuccessRates.remove(locator.toString());
    }
    
    public Map<String, Integer> getLocatorSuccessRates() {
        return new HashMap<>(locatorSuccessRates);
    }
}