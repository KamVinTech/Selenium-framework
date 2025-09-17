package com.designpattern.pages;

import com.designpattern.utils.FrameworkException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Sample Login Page implementing Page Object Model with Page Factory
 */
public class LoginPage extends BasePage {
    
    @FindBy(id = "username")
    private WebElement usernameField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    @FindBy(id = "loginButton")
    private WebElement loginButton;
    
    @FindBy(id = "errorMessage")
    private WebElement errorMessage;
    
    public LoginPage(WebDriver driver) {
        super(driver);
    }
    
    /**
     * Performs login operation
     * @param username username to login with
     * @param password password to login with
     */
    public void login(String username, String password) {
        log.info("Attempting login with username: {}", username);
        try {
            type(usernameField, username);
            type(passwordField, password);
            click(loginButton);
            log.info("Login attempt completed");
        } catch (Exception e) {
            String screenshot = takeScreenshot("login_failure");
            log.error("Login failed. Screenshot: {}", screenshot, e);
            throw new FrameworkException("Failed to perform login", e);
        }
    }
    
    /**
     * Gets the error message if present
     * @return Error message text
     */
    public String getErrorMessage() {
        try {
            return waitForVisible(errorMessage).getText();
        } catch (Exception e) {
            log.warn("Error message element not found");
            return null;
        }
    }
    
    @Override
    public boolean isPageLoaded() {
        try {
            boolean isLoaded = waitForVisible(loginButton).isDisplayed();
            log.info("Login page loaded: {}", isLoaded);
            return isLoaded;
        } catch (Exception e) {
            log.error("Login page not loaded", e);
            return false;
        }
    }
}