package com.demo.automation.stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class ValidationSteps {

    private final CommonSteps common;

    public ValidationSteps(CommonSteps common) {
        this.common = common;
    }

    @When("I fill in the form with invalid email {string}")
    public void iFillInTheFormWithInvalidEmail(String email) {
        common.getDriver().findElement(org.openqa.selenium.By.id("employeeId")).sendKeys("EMP-001");
        common.getDriver().findElement(org.openqa.selenium.By.id("fullName")).sendKeys("Test User");
        common.getDriver().findElement(org.openqa.selenium.By.id("email")).sendKeys(email);
        common.getDriver().findElement(org.openqa.selenium.By.id("password")).sendKeys("password123");
        common.getDriver().findElement(org.openqa.selenium.By.id("phone")).sendKeys("1234567890");
        common.getDriver().findElement(org.openqa.selenium.By.id("department")).sendKeys("Engineering");
        common.getDriver().findElement(org.openqa.selenium.By.id("designation")).sendKeys("Engineer");
        common.getDriver().findElement(org.openqa.selenium.By.id("joiningDate")).sendKeys("2024-01-15");
    }

    @When("I fill in the form with email {string}")
    public void iFillInTheFormWithEmail(String email) {
        common.getDriver().findElement(org.openqa.selenium.By.id("employeeId")).sendKeys("EMP-002");
        common.getDriver().findElement(org.openqa.selenium.By.id("fullName")).sendKeys("Test User 2");
        common.getDriver().findElement(org.openqa.selenium.By.id("email")).sendKeys(email);
        common.getDriver().findElement(org.openqa.selenium.By.id("password")).sendKeys("password123");
        common.getDriver().findElement(org.openqa.selenium.By.id("phone")).sendKeys("1234567890");
        common.getDriver().findElement(org.openqa.selenium.By.id("department")).sendKeys("Engineering");
        common.getDriver().findElement(org.openqa.selenium.By.id("designation")).sendKeys("Engineer");
        common.getDriver().findElement(org.openqa.selenium.By.id("joiningDate")).sendKeys("2024-01-15");
    }

    @When("I submit the form without filling required fields")
    public void iSubmitTheFormWithoutFillingRequiredFields() {
        common.clickLoginButton();
    }

    @When("I fill in the form with password {string}")
    public void iFillInTheFormWithPassword(String password) {
        common.getDriver().findElement(org.openqa.selenium.By.id("employeeId")).sendKeys("EMP-003");
        common.getDriver().findElement(org.openqa.selenium.By.id("fullName")).sendKeys("Test User 3");
        common.getDriver().findElement(org.openqa.selenium.By.id("email")).sendKeys("test3@test.com");
        common.getDriver().findElement(org.openqa.selenium.By.id("password")).sendKeys(password);
        common.getDriver().findElement(org.openqa.selenium.By.id("phone")).sendKeys("1234567890");
        common.getDriver().findElement(org.openqa.selenium.By.id("department")).sendKeys("Engineering");
        common.getDriver().findElement(org.openqa.selenium.By.id("designation")).sendKeys("Engineer");
        common.getDriver().findElement(org.openqa.selenium.By.id("joiningDate")).sendKeys("2024-01-15");
    }

    @When("I submit the form")
    public void iSubmitTheForm() {
        common.clickLoginButton();
    }

    @Then("I should see a validation error for email")
    public void iShouldSeeAValidationErrorForEmail() {
        common.waitForElement(org.openqa.selenium.By.xpath("//*[contains(text(),'email') and (contains(text(),'invalid') or contains(text(),'format'))]"));
    }

    @Then("I should see a validation error for duplicate email")
    public void iShouldSeeAValidationErrorForDuplicateEmail() {
        new org.openqa.selenium.support.ui.WebDriverWait(common.getDriver(), java.time.Duration.ofSeconds(10))
            .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(
                org.openqa.selenium.By.xpath("//*[contains(text(),'duplicate') or contains(text(),'already')]")));
    }

    @Then("I should see validation errors for all required fields")
    public void iShouldSeeValidationErrorsForAllRequiredFields() {
        new org.openqa.selenium.support.ui.WebDriverWait(common.getDriver(), java.time.Duration.ofSeconds(10))
            .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(
                org.openqa.selenium.By.xpath("//*[contains(text(),'required') or contains(text(),'Employee ID')]")));
    }

    @Then("I should see a validation error for password length")
    public void iShouldSeeAValidationErrorForPasswordLength() {
        new org.openqa.selenium.support.ui.WebDriverWait(common.getDriver(), java.time.Duration.ofSeconds(10))
            .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(
                org.openqa.selenium.By.xpath("//*[contains(text(),'password') and contains(text(),'length')]")));
    }
}