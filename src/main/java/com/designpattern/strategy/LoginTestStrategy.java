package com.designpattern.strategy;

import com.designpattern.pages.LoginPage;
import com.designpattern.data.UserData;
import org.openqa.selenium.WebDriver;

/**
 * Concrete implementation of TestStrategy for login tests
 */
public class LoginTestStrategy implements TestStrategy {
    private WebDriver driver;
    private UserData userData;

    public LoginTestStrategy(WebDriver driver, UserData userData) {
        this.driver = driver;
        this.userData = userData;
    }

    @Override
    public void executeTest() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(userData.getUsername(), userData.getPassword());
    }
}