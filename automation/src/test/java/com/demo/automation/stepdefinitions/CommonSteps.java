package com.demo.automation.stepdefinitions;

import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

@CucumberContextConfiguration
public class CommonSteps {

    protected WebDriver driver;

    protected WebDriver getDriver() {
        if (driver == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            driver = new ChromeDriver(options);
        }
        return driver;
    }

    // Helper methods (not step definitions)
    protected void navigateToLoginPage() {
        getDriver().get("http://localhost:3000/login");
        new WebDriverWait(getDriver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    }

    protected void loginAs(String email, String password) {
        getDriver().get("http://localhost:3000/login");
        new WebDriverWait(getDriver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        getDriver().findElement(By.id("email")).sendKeys(email);
        getDriver().findElement(By.id("password")).sendKeys(password);
        getDriver().findElement(By.xpath("//button[@type='submit']")).click();
        new WebDriverWait(getDriver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.urlContains("/"));
    }

    protected void navigateTo(String path) {
        getDriver().get("http://localhost:3000" + path);
    }

    protected void enterEmailAndPassword(String email, String password) {
        WebElement emailField = getDriver().findElement(By.id("email"));
        WebElement passwordField = getDriver().findElement(By.id("password"));
        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    protected void clickLoginButton() {
        getDriver().findElement(By.xpath("//button[@type='submit']")).click();
    }

    protected void waitForUrl(String fragment) {
        new WebDriverWait(getDriver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.urlContains(fragment));
    }

    protected void waitForElement(By locator) {
        new WebDriverWait(getDriver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Shared step definitions that are not owned by other step classes
    @Given("the application is running at {string}")
    public void theApplicationIsRunningAt(String url) {
        getDriver().get(url);
    }
}