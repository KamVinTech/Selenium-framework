package com.designpattern.stepdefinitions;

import com.designpattern.core.DriverManager;
import com.designpattern.data.UserData;
import com.designpattern.pages.LoginPage;
import com.designpattern.utils.ConfigurationManager;
import com.designpattern.utils.LogUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class LoginSteps {
    private static final Logger log = LogUtils.getLogger(LoginSteps.class);
    private WebDriver driver;
    private LoginPage loginPage;
    private final DriverManager driverManager;
    private final ConfigurationManager config;

    public LoginSteps() {
        this.driverManager = DriverManager.getInstance();
        this.config = ConfigurationManager.getInstance();
    }

    @Before
    public void setup(Scenario scenario) {
        log.info("Starting scenario: {}", scenario.getName());
        LogUtils.setTestName(scenario.getName());
        driver = driverManager.getDriver();
        loginPage = new LoginPage(driver);
    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        log.info("Navigating to login page");
        driverManager.navigateToBaseUrl();
        Assert.assertTrue(loginPage.isPageLoaded(), "Login page is not loaded");
    }

    @When("I enter username {string} and password {string}")
    public void iEnterUsernameAndPassword(String username, String password) {
        log.info("Attempting login with username: {}", username);
        UserData userData = UserData.builder()
                .username(username)
                .password(password)
                .build();
        loginPage.login(userData.getUsername(), userData.getPassword());
    }

    @And("I click the login button")
    public void iClickTheLoginButton() {
        log.info("Login button click is handled in the login method");
    }

    @Then("I should be logged in successfully")
    public void iShouldBeLoggedInSuccessfully() {
        log.info("Verifying successful login");
        // Add verification logic here
        // For example: Assert.assertTrue(homePage.isUserLoggedIn());
    }

    @Then("I should see the message {string}")
    public void iShouldSeeTheMessage(String expectedMessage) {
        log.info("Verifying error message: {}", expectedMessage);
        String actualMessage = loginPage.getErrorMessage();
        Assert.assertEquals(actualMessage, expectedMessage, 
            "Error message does not match expected message");
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            log.error("Scenario failed: {}", scenario.getName());
            // Take screenshot on failure
            String screenshotPath = loginPage.takeScreenshot("failure_" + 
                scenario.getName().replaceAll("\\s+", "_"));
            log.info("Failure screenshot saved: {}", screenshotPath);
        }
        
        log.info("Completing scenario: {}", scenario.getName());
        driverManager.quitDriver();
        LogUtils.clearTestName();
    }
}