package com.demo.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DashboardPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/");
    }

    public boolean isVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Dashboard')]")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
