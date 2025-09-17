package com.designpattern.core;

import com.designpattern.utils.ConfigurationManager;
import com.designpattern.utils.FrameworkException;
import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

/**
 * Factory pattern implementation for creating different types of WebDrivers
 */
public class DriverFactory {
    private static final Logger log = LogUtils.getLogger(DriverFactory.class);
    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    
    /**
     * Creates a WebDriver instance based on configuration
     * @return WebDriver instance
     */
    public static WebDriver createDriver() {
        String browserType = config.getProperty("browser", "chrome");
        boolean isHeadless = config.getBooleanProperty("headless");
        int implicitWait = config.getIntProperty("implicit.wait");
        
        log.info("Creating WebDriver instance for browser: {}", browserType);
        WebDriver driver;
        
        try {
            switch (browserType.toLowerCase()) {
                case "chrome":
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions chromeOptions = new ChromeOptions();
                    if (isHeadless) chromeOptions.addArguments("--headless");
                    driver = new ChromeDriver(chromeOptions);
                    break;
                    
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (isHeadless) firefoxOptions.addArguments("--headless");
                    driver = new FirefoxDriver(firefoxOptions);
                    break;
                    
                case "edge":
                    WebDriverManager.edgedriver().setup();
                    EdgeOptions edgeOptions = new EdgeOptions();
                    if (isHeadless) edgeOptions.addArguments("--headless");
                    driver = new EdgeDriver(edgeOptions);
                    break;
                    
                default:
                    throw new FrameworkException("Unsupported browser type: " + browserType);
            }
            
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
            log.info("WebDriver created successfully");
            return driver;
            
        } catch (Exception e) {
            log.error("Failed to create WebDriver instance", e);
            throw new FrameworkException("Failed to create WebDriver", e);
        }
    }
}