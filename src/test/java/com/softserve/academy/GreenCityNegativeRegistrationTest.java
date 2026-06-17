package com.softserve.academy;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenCityNegativeRegistrationTest {
    private static WebDriver driver;

    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Check if we are running in CI (GitHub Actions)
        if (System.getenv("GITHUB_ACTIONS") != null) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
        }

        driver = WebDriverManager.chromedriver().capabilities(options).create();
        driver.manage().window().maximize();
        // At this stage, we are not using complex waits, so we just maximize the window
    }

    @BeforeEach
    void openRegistrationForm() {
        driver.navigate().to("https://www.greencity.cx.ua/#/greenCity");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement signUpBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".header_sign-up-btn > span")));
        signUpBtn.click();
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("email")));}

    // --- TESTS ---

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-email",
            "test.com",
            "test@",
            "@gmail.com"
    })
    @DisplayName("Invalid email format (without @) → email error")
    void shouldShowErrorForInvalidEmail(String email) throws InterruptedException {
        typeEmail(email);
        typeUsername("ValidUsername");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");
        assertEmailErrorVisible();
        assertSignUpButtonDisabled();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true})
    @DisplayName("All fields empty → required errors shown")
    void shouldShowErrorsForAllEmptyFields(boolean ignored) throws InterruptedException {
        driver.findElement(By.id("email")).click();
        driver.findElement(By.id("firstName")).click();
        driver.findElement(By.id("password")).click();
        driver.findElement(By.id("repeatPassword")).click();
        clickSignUp();
        assertEmailErrorVisible();
        assertUsernameErrorVisible();
        assertPasswordErrorVisible();
        assertConfirmPasswordErrorVisible();
        assertSignUpButtonDisabled();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
    })
    @DisplayName("Empty username → username required")
    void shouldShowErrorForEmptyUsername(String username) throws InterruptedException {
        typeEmail("valid.email@gmail.com");
        typeUsername(username);
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");
        clickSignUp();
        assertUsernameErrorVisible();
        assertSignUpButtonDisabled();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1",
            "12",
            "123",
            "1234567"
    })
    void shouldShowErrorForShortPassword(String password) {
        typeEmail("valid.email@gmail.com");
        typeUsername("ValidUsername");
        typePassword(password);
        typeConfirm(password);
        assertPasswordErrorVisible();
        assertSignUpButtonDisabled();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Pass 123",
            "Val idPa ss123!!",
            "Valid Pass123!",
            "Valid P ass 123!"
    })
    void shouldShowErrorForPasswordWithSpace(String password) {
        typeEmail("valid.email@gmail.com");
        typeUsername("ValidUsername");
        driver.findElement(By.id("password")).click();
        typePassword(password);
        typeConfirm(password);
        assertPasswordErrorVisible();
        assertSignUpButtonDisabled();
    }

    @ParameterizedTest
    @CsvSource({
            "ValidPass123!,DifferentPass123!",
            "Password123!,Password321!",
            "Qwerty123!,Qwerty321!"
    })
    @DisplayName("Confirm password mismatch → confirm error")
    void shouldShowErrorForPasswordMismatch(String password, String confirmPassword) throws InterruptedException {
        typeEmail("valid.email@gmail.com");
        typeUsername("ValidUsername");
        typePassword(password);
        typeConfirm(confirmPassword);
        driver.findElement(By.id("password")).sendKeys(Keys.TAB);
        assertConfirmPasswordErrorVisible();
        assertSignUpButtonDisabled();
    }

// --- HELPERS (Helper methods) ---
    // This is the first step towards structuring code before learning Page Object

    private void typeEmail(String value) {
        WebElement field = driver.findElement(By.id("email"));
        field.clear();
        field.sendKeys(value);
        field.sendKeys(Keys.TAB);
    }


    private void typeUsername(String value) {
        WebElement field = driver.findElement(By.id("firstName"));
        field.clear();
        field.sendKeys(value);
        field.sendKeys(Keys.TAB);
    }

    private void typePassword(String value) {
        WebElement field = driver.findElement(By.id("password"));
        field.clear();
        field.sendKeys(value);
        field.sendKeys(Keys.TAB);
    }

    private void typeConfirm(String value) {
        WebElement field = driver.findElement(By.id("repeatPassword"));
        field.clear();
        field.sendKeys(value);
        field.sendKeys(Keys.TAB);
    }

    private void clickSignUp() {
        driver.findElement(By.cssSelector("button[type='submit'].greenStyle")).click();
    }

    private void assertEmailErrorVisible() {
        WebElement error = driver.findElement(By.id("email-err-msg"));
        assertTrue(error.isDisplayed(), "Email error message should be visible");
        assertTrue(error.getText().toLowerCase().contains("check") || error.getText().toLowerCase().contains("required"));
    }

    private void assertUsernameErrorVisible() {
        WebElement error = driver.findElement(By.id("firstname-err-msg"));
        assertTrue(error.isDisplayed(), "Username error message should be visible");
    }

    private void assertSignUpButtonDisabled() {
        WebElement btn = driver.findElement(By.cssSelector("button[type='submit'].greenStyle"));
        String disabled = btn.getAttribute("disabled");
    }

    private void assertPasswordErrorVisible() {
        WebElement error = driver.findElement(By.cssSelector("p.password-not-valid"));
        assertTrue(error.isDisplayed(), "Password error message should be visible");
    }

    private void assertConfirmPasswordErrorVisible() {
        WebElement error = driver.findElement(By.id("confirm-err-msg"));
        assertTrue(error.isDisplayed(), "Confirm password error message should be visible");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}