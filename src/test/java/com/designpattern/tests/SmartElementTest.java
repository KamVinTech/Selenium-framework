package com.designpattern.tests;

import com.designpattern.core.element.SmartElement;

import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SmartElementTest {
    private WebDriver driver;
    
    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.get("https://www.example.com");
    }
    
    @Test
    public void testSmartElementInteraction() {
        // Create a SmartElement with all advanced features
        SmartElement element = new SmartElement(
            driver,
            driver.findElement(By.tagName("h1")),
            By.tagName("h1")
        );
        
        // Test element interaction with performance monitoring
        String text = element.getText();
        Assert.assertEquals(text, "Example Domain");
        
        // Get performance metrics
        Map<String, Object> metrics = element.getPerformanceMetrics();
        Assert.assertNotNull(metrics);
        Assert.assertTrue(metrics.containsKey("getText_duration"));
        
        // Test state validation
        Assert.assertTrue(element.getStateValidator().isDisplayed());
        
        // Add custom recovery strategy
        element.addRecoveryStrategy(
            org.openqa.selenium.StaleElementReferenceException.class,
            e -> {
                driver.navigate().refresh();
                return true;
            }
        );
        
        // Test state monitoring
        final boolean[] stateChanged = {false};
        element.waitForState("visible", e -> stateChanged[0] = true);
        
        // Test dynamic configuration
        element.getConfig().set("retry.max.attempts", 5);
        Assert.assertEquals(element.getConfig().get("retry.max.attempts"), 5);
    }
    
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}