package com.designpattern.tests;

import com.designpattern.core.DriverManager;
import com.designpattern.data.UserData;
import com.designpattern.pages.LoginPage;
import com.designpattern.reporting.ExtentReportObserver;
import com.designpattern.reporting.TestEventManager;
import com.designpattern.utils.ConfigurationManager;
import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.*;

@Listeners(com.designpattern.utils.TestListener.class)
public class LoginTest {
    private static final Logger log = LogUtils.getLogger(LoginTest.class);
    private TestEventManager eventManager;
    private DriverManager driverManager;
    private ConfigurationManager config;

    @BeforeSuite
    public void setupSuite() {
        config = ConfigurationManager.getInstance();
        eventManager = TestEventManager.getInstance();
        eventManager.addObserver(new ExtentReportObserver());
        log.info("Test suite setup completed");
    }

    @BeforeMethod
    public void setup() {
        driverManager = DriverManager.getInstance();
        driverManager.getDriver(); // This will create a new driver instance
        driverManager.navigateToBaseUrl();
        log.info("Test setup completed");
    }

    @Test(retryAnalyzer = com.designpattern.utils.RetryAnalyzer.class)
    public void testSuccessfulLogin() {
        String testName = "Successful Login Test";
        LogUtils.setTestName(testName);
        log.info("Starting test: {}", testName);
        
        try {
            // Create test data using Builder pattern
            UserData userData = UserData.builder()
                    .username(config.getProperty("test.user.name"))
                    .password(config.getProperty("test.user.password"))
                    .build();

            // Initialize page and perform login
            LoginPage loginPage = new LoginPage(driverManager.getDriver());
            Assert.assertTrue(loginPage.isPageLoaded(), "Login page is not loaded");
            
            loginPage.login(userData.getUsername(), userData.getPassword());
            log.info("Login successful with username: {}", userData.getUsername());
            
            eventManager.notifyTestSuccess(testName);
        } catch (Exception e) {
            log.error("Test failed", e);
            eventManager.notifyTestFailure(testName, e);
            throw e;
        }
    }

    @Test(dataProvider = "invalidLoginData")
    public void testFailedLogin(String username, String password, String expectedError) {
        String testName = "Failed Login Test";
        LogUtils.setTestName(testName);
        log.info("Starting test with username: {} and expected error: {}", username, expectedError);
        
        try {
            LoginPage loginPage = new LoginPage(driverManager.getDriver());
            loginPage.login(username, password);
            
            String actualError = loginPage.getErrorMessage();
            Assert.assertEquals(actualError, expectedError, "Error message mismatch");
            
            log.info("Verified error message for invalid login");
        } catch (Exception e) {
            log.error("Test failed", e);
            throw e;
        }
    }

    @DataProvider(name = "invalidLoginData")
    public Object[][] getInvalidLoginData() {
        return new Object[][] {
            {"invalid", "wrong123", "Invalid credentials"},
            {"", "", "Username and password are required"},
            {"testuser", "wrongpass", "Invalid password"}
        };
    }

    @AfterMethod
    public void tearDown() {
        log.info("Tearing down test");
        driverManager.quitDriver();
        LogUtils.clearTestName();
    }
}
}