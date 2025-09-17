# Selenium Test Automation Framework

## Framework Summary

This test automation framework i### Framework Features

### 1. Smart Element Interaction Strategies

```java
// Strategy interface
public interface ElementInteractionStrategy {
    void click(WebElement element);
    void type(WebElement element, String text);
    void select(WebElement element);
}

// Default implementation
public class DefaultElementStrategy implements ElementInteractionStrategy {
    @Override
    public void click(WebElement element) {
        element.click();
    }
    // Other implementations...
}

// JavaScript implementation
public class JavaScriptStrategy implements ElementInteractionStrategy {
    @Override
    public void click(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
    // Other implementations...
}

// Usage example
public class SmartElement {
    private final WebElement element;
    private final ElementInteractionManager strategyManager;
    
    public void click() {
        strategyManager.executeWithFallback(strategy -> strategy.click(element));
    }
}
```

### 2. Dynamic Wait Mechanisms

```java
public class DynamicWait<T> {
    public DynamicWait<T> withTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }
    
    public DynamicWait<T> withProgressivePolling(boolean enable, int factor) {
        this.progressivePolling = enable;
        this.progressiveFactor = factor;
        return this;
    }
    
    public T until(Function<WebDriver, T> condition) {
        // Smart waiting implementation with progressive polling
    }
}

// Usage example
DynamicWait<Boolean> wait = new DynamicWait<>(driver)
    .withTimeout(Duration.ofSeconds(10))
    .withProgressivePolling(true, 2);
wait.until(d -> element.isDisplayed());
```

### 3. Element State Validation

```java
public class ElementStateValidator {
    private final WebElement element;
    private final DynamicWait<Boolean> wait;
    
    public boolean isDisplayed() {
        return wait.until(DynamicWait.Conditions.elementDisplayed(element));
    }
    
    public boolean isClickable() {
        return wait.until(DynamicWait.Conditions.elementClickable(element));
    }
    
    // Other validation methods...
}
```

### 4. Error Recovery Mechanisms

```java
public class ElementRecoveryManager {
    private final Map<Class<? extends WebDriverException>, Function<WebElement, Boolean>> recoveryStrategies;
    
    public boolean attemptRecovery(WebElement element, WebDriverException exception) {
        Function<WebElement, Boolean> strategy = recoveryStrategies.get(exception.getClass());
        if (strategy != null) {
            return strategy.apply(element);
        }
        return false;
    }
}
```

### 5. Core Design Patterns & Implementation Examplesements industry-standard design patterns and best practices to create a robust, maintainable, and scalable test automation solution.

### Key Components

1. **Design Patterns**
   - Strategy Pattern: Multiple element interaction strategies
   - Singleton Pattern: Thread-safe WebDriver management
   - Factory Pattern: Dynamic browser configuration
   - Page Object Model: Maintainable page element management
   - Builder Pattern: Flexible test data creation
   - Observer Pattern: Event-based reporting

2. **Core Features**
   - Smart element interactions with multiple strategies
   - Dynamic waiting mechanisms with adaptive polling
   - Comprehensive element state validation
   - Automatic error recovery mechanisms
   - Cross-browser testing (Chrome, Firefox, Edge)
   - Parallel test execution support
   - Screenshot capture on failure
   - Comprehensive logging with Log4j2
   - Configurable test environments
   - Retry mechanism for flaky tests

3. **Test Frameworks**
   - TestNG: For traditional test organization
   - Cucumber: For BDD-style testing
   - ExtentReports: For detailed reporting
   - Data-driven testing support

4. **Utilities**
   - Smart element interactions with retry logic
   - Automatic screenshot capture
   - Custom exception handling
   - Configuration management
   - Logging utilities
   - Wait helper methods

### Technologies Used
- Language: Java
- Build Tool: Maven
- Testing: TestNG, Cucumber
- Automation: Selenium WebDriver
- Reporting: ExtentReports
- Logging: Log4j2
- Annotations: Lombok
- Dependencies: WebDriverManager, Cucumber-PicoContainer

## Quick Start Guide

### 1. Writing Your First Test

```java
@Test
public class LoginTest {
    private static final Logger log = LogUtils.getLogger(LoginTest.class);
    
    @Test
    public void testSuccessfulLogin() {
        // 1. Create test data using Builder Pattern
        UserData userData = UserData.builder()
                .username("testuser")
                .password("password123")
                .build();

        // 2. Initialize page object with built-in driver management
        LoginPage loginPage = new LoginPage(DriverManager.getInstance().getDriver());

        // 3. Perform test actions with automatic logging and error handling
        loginPage.login(userData.getUsername(), userData.getPassword());
        
        // 4. Assertions with automatic screenshot on failure
        Assert.assertTrue(loginPage.isLoggedIn(), "Login was not successful");
    }
    
    @Test(dataProvider = "invalidLoginData")
    public void testFailedLogin(String username, String password, String expectedError) {
        LoginPage loginPage = new LoginPage(DriverManager.getInstance().getDriver());
        loginPage.login(username, password);
        Assert.assertEquals(loginPage.getErrorMessage(), expectedError);
    }
    
    @DataProvider(name = "invalidLoginData")
    public Object[][] getInvalidLoginData() {
        return new Object[][] {
            {"invalid", "wrong123", "Invalid credentials"},
            {"", "", "Username and password are required"}
        };
    }
}
```

## Framework Features

### 1. Core Design Patterns & Implementation Examples

#### Singleton Pattern (DriverManager)
```java
public class DriverManager {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static DriverManager instance = null;
    
    private DriverManager() {}
    
    public static synchronized DriverManager getInstance() {
        if (instance == null) {
            instance = new DriverManager();
        }
        return instance;
    }
    
    public WebDriver getDriver() {
        WebDriver localDriver = driver.get();
        if (localDriver == null) {
            localDriver = DriverFactory.createDriver();
            driver.set(localDriver);
        }
        return localDriver;
    }
}
```

#### Factory Pattern (DriverFactory) 
```java
public class DriverFactory {
    public static WebDriver createDriver() {
        String browser = ConfigManager.getProperty("browser");
        WebDriver driver;
        
        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions options = new ChromeOptions();
                if (Boolean.parseBoolean(ConfigManager.getProperty("headless"))) {
                    options.addArguments("--headless");
                }
                driver = new ChromeDriver(options);
                break;
            case "firefox":
                driver = new FirefoxDriver();
                break;
            default:
                throw new BrowserNotSupportedException(browser);
        }
        
        driver.manage().timeouts().implicitlyWait(
            Duration.ofSeconds(Integer.parseInt(
                ConfigManager.getProperty("implicit.wait"))));
        return driver;
    }
}
```

#### Page Object Model
```java
public class LoginPage extends BasePage {
    @FindBy(id = "username")
    private WebElement usernameInput;
    
    @FindBy(id = "password") 
    private WebElement passwordInput;
    
    @FindBy(css = ".login-btn")
    private WebElement loginButton;
    
    @FindBy(css = ".error-message")
    private WebElement errorMessage;
    
    public LoginPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }
    
    public void login(String username, String password) {
        log.info("Attempting login with username: {}", username);
        WebElementUtils.safeType(usernameInput, username);
        WebElementUtils.safeType(passwordInput, password);
        WebElementUtils.safeClick(loginButton);
    }
    
    public boolean isLoggedIn() {
        return waitForUrlContains("/dashboard");
    }
    
    public String getErrorMessage() {
        return waitForElementVisible(errorMessage).getText();
    }
}
```

#### Builder Pattern (Test Data)
```java
@Data
@Builder
public class UserData {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    
    // Example usage
    public static UserData createDefaultUser() {
        return UserData.builder()
            .username("default_user")
            .password("Pass123!")
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .build();
    }
}
```

### 2. Utility Components

#### WebDriver Management
```java
// In your test or step definition
public void setUp() {
    // Get thread-safe driver instance
    WebDriver driver = DriverManager.getInstance().getDriver();
}

// Configuration (config.properties)
browser=chrome
headless=false
implicit.wait=10
explicit.wait=20
```

#### Element Interaction Utilities
```java
public class WebElementUtils {
    // Smart click with retry and logging
    public static void safeClick(WebElement element) {
        try {
            waitForElementClickable(element);
            element.click();
        } catch (ElementClickInterceptedException e) {
            // Automatic retry with JavaScript click
            jsClick(element);
        } catch (StaleElementReferenceException e) {
            // Automatic retry with fresh element
            retryClick(element);
        }
    }
    
    // Smart type with clear and verify
    public static void safeType(WebElement element, String text) {
        waitForElementVisible(element);
        element.clear();
        element.sendKeys(text);
        // Verify text was entered correctly
        Assert.assertEquals(element.getAttribute("value"), text);
    }
}
```

#### Automatic Screenshot Capture
```java
public class ScreenshotUtils {
    public static String captureScreenshotOnFailure(
            WebDriver driver, String testName) {
        try {
            String timestamp = getTimestamp();
            String filename = String.format(
                "failure_%s_%s.png", testName, timestamp);
            
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            File destination = new File("test-output/screenshots/" + filename);
            
            FileUtils.copyFile(source, destination);
            return destination.getAbsolutePath();
        } catch (Exception e) {
            log.error("Failed to capture screenshot", e);
            return null;
        }
    }
}
```

### 3. Test Execution Features & Examples

#### TestNG Parallel Execution
```xml
<!-- testng.xml -->
<suite name="Parallel Test Suite" parallel="methods" thread-count="3">
    <test name="Login Tests">
        <classes>
            <class name="com.designpattern.tests.LoginTest"/>
            <class name="com.designpattern.tests.RegistrationTest"/>
        </classes>
    </test>
</suite>
```

#### Cucumber Feature Example
```gherkin
# login.feature
Feature: User Login
  
  Scenario Outline: Login with different credentials
    Given I am on the login page
    When I enter username "<username>" and password "<password>"
    Then I should see "<message>"
    
    Examples:
      | username | password | message |
      | valid_user | Pass123! | Welcome back! |
      | wrong_user | wrong123 | Invalid credentials |
```

#### Step Definition Example
```java
public class LoginSteps {
    private LoginPage loginPage;
    private WebDriver driver;
    
    @Before
    public void setUp() {
        driver = DriverManager.getInstance().getDriver();
        loginPage = new LoginPage(driver);
    }
    
    @Given("I am on the login page")
    public void navigateToLoginPage() {
        driver.get(ConfigManager.getProperty("app.url"));
    }
    
    @When("I enter username {string} and password {string}")
    public void enterCredentials(String username, String password) {
        loginPage.login(username, password);
    }
    
    @Then("I should see {string}")
    public void verifyMessage(String expectedMessage) {
        if (expectedMessage.equals("Welcome back!")) {
            Assert.assertTrue(loginPage.isLoggedIn());
        } else {
            Assert.assertEquals(loginPage.getErrorMessage(), expectedMessage);
        }
    }
}
```

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/
│           └── designpattern/
│               ├── core/           
│               │   ├── element/    # Enhanced WebElement components
│               │   │   ├── SmartElement.java
│               │   │   └── ElementStateValidator.java
│               │   ├── wait/       # Smart waiting mechanisms
│               │   │   └── DynamicWait.java
│               │   └── recovery/   # Error recovery strategies
│               │       └── ElementRecoveryManager.java
│               ├── strategy/       
│               │   └── element/    # Element interaction strategies
│               │       ├── ElementInteractionStrategy.java
│               │       ├── DefaultElementStrategy.java
│               │       ├── JavaScriptStrategy.java
│               │       ├── ActionsStrategy.java
│               │       └── ElementInteractionManager.java
│               ├── pages/          # Page Object classes
│               ├── data/           # Test data classes
│               ├── reporting/      # Reporting classes
│               └── utils/          # Utility classes
└── test/
    ├── java/
    │   └── com/
    │       └── designpattern/
    │           ├── tests/         # TestNG test classes
    │           ├── runners/       # Cucumber test runners
    │           └── stepdefinitions/ # Cucumber step definitions
    └── resources/
        ├── features/             # Cucumber feature files
        └── testng.xml           # TestNG configuration
```

## Dependencies

- Selenium WebDriver
- TestNG
- Cucumber
- ExtentReports
- WebDriverManager
- Lombok
- Cucumber-TestNG
- Cucumber-PicoContainer

## Getting Started

1. Clone the repository
2. Install dependencies using Maven:
```bash
mvn clean install
```
3. Configure browser settings in `src/main/resources/config.properties`
4. Run tests using TestNG:
```bash
mvn test
```

## Running Tests

### Running All Tests
Use Maven to run both TestNG and Cucumber tests:
```bash
mvn clean test
```

### Running Specific Tests
1. Run only TestNG tests:
```bash
mvn test -Dtest=*Test
```

2. Run only Cucumber features:
```bash
mvn test -Dtest=TestNGCucumberRunner
```

### Reports
- TestNG reports will be generated in `test-output` directory
- Cucumber HTML reports will be generated in `target/cucumber-reports`
- ExtentReports will be available in `test-output/extent-report.html`

## Best Practices

1. Always use Page Object Model for better maintenance:
```java
public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final Logger log = LogUtils.getLogger();
    
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 
            Duration.ofSeconds(ConfigManager.getExplicitWaitTimeout()));
    }
    
    protected WebElement waitForElementVisible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }
    
    protected WebElement waitForElementClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }
    
    protected boolean waitForUrlContains(String fraction) {
        return wait.until(ExpectedConditions.urlContains(fraction));
    }
}
```

2. Use Builder Pattern for complex test data creation:
```java
TestData data = TestData.builder()
    .firstName("John")
    .lastName("Doe")
    .email("john.doe@example.com")
    .age(30)
    .build();
```

3. Implement reusable step definitions:
```java
@Given("I am logged in as {string}")
public void loginAs(String userType) {
    UserData userData = TestDataFactory.getUserData(userType);
    LoginPage loginPage = new LoginPage(driver);
    loginPage.login(userData.getUsername(), userData.getPassword());
}
```

4. Use scenario outlines for data-driven tests:
```gherkin
Scenario Outline: Search with different criteria
  Given I am on the search page
  When I search for "<item>"
  Then I should see "<count>" results
  
  Examples:
    | item      | count |
    | laptop    | 10    |
    | phone     | 20    |
    | tablet    | 15    |
```

5. Maintain clean and organized test structure:
```java
@Test
public void testUserRegistration() {
    // Given - Setup
    RegistrationPage registrationPage = new RegistrationPage(driver);
    UserData userData = TestData.createRandomUser();
    
    // When - Action
    registrationPage.registerNewUser(userData);
    
    // Then - Verification
    Assert.assertTrue(registrationPage.isRegistrationSuccessful());
    Assert.assertEquals(registrationPage.getWelcomeMessage(), 
        "Welcome " + userData.getFirstName());
}
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request