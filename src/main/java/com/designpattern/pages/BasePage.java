package com.designpattern.pages;

import com.designpattern.utils.*;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base class for all Page Objects
 */
public abstract class BasePage {
    protected static final Logger log = LogUtils.getLogger(BasePage.class);
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final ConfigurationManager config;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.config = ConfigurationManager.getInstance();
        this.wait = new WebDriverWait(driver, 
            Duration.ofSeconds(config.getIntProperty("explicit.wait")));
        PageFactory.initElements(driver, this);
        log.debug("Initialized page object: {}", this.getClass().getSimpleName());
    }

    /**
     * Checks if the page is loaded
     * @return true if page is loaded, false otherwise
     */
    public abstract boolean isPageLoaded();

    /**
     * Safely clicks on an element
     * @param element Element to click
     */
    protected void click(WebElement element) {
        log.debug("Clicking element: {}", element);
        WebElementUtils.safeClick(driver, element);
    }

    /**
     * Safely types text into an element
     * @param element Element to type into
     * @param text Text to type
     */
    protected void type(WebElement element, String text) {
        log.debug("Typing text: '{}' into element: {}", text, element);
        WebElementUtils.safeType(driver, element, text);
    }

    /**
     * Takes a screenshot of the current page
     * @param name Name for the screenshot
     * @return Path to the screenshot
     */
    protected String takeScreenshot(String name) {
        return ScreenshotUtils.captureScreenshot(driver, name);
    }

    /**
     * Waits for element to be visible
     * @param element Element to wait for
     * @return WebElement once visible
     */
    protected WebElement waitForVisible(WebElement element) {
        return WebElementUtils.waitForElementVisible(driver, element);
    }
}