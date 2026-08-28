package com.demo.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Map;

public class EmployeePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public EmployeePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void openList(String baseUrl) {
        driver.get(baseUrl + "/employees");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Employees')]")));
    }

    public void fillForm(Map<String, String> data) {
        for (Map.Entry<String, String> e : data.entrySet()) {
            String field = e.getKey();
            String value = e.getValue();
            By loc = By.cssSelector("#" + field + ", [name='" + field + "']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(loc));
            WebElement el = driver.findElement(loc);
            el.clear();
            el.sendKeys(value);
        }
    }

    public void submit() {
        driver.findElement(By.xpath("//button[@type='submit']")).click();
    }

    public boolean containsText(String text) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'" + text + "')]")));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
