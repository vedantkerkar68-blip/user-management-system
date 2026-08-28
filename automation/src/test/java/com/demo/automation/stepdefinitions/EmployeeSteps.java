package com.demo.automation.stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class EmployeeSteps {

    private final CommonSteps common;

    public EmployeeSteps(CommonSteps common) {
        this.common = common;
    }

    @When("I fill in the employee form with:")
    public void iFillInTheEmployeeFormWith(io.cucumber.datatable.DataTable dataTable) {
        var rows = dataTable.asMaps(String.class, String.class);
        for (var row : rows) {
            String field = row.get("field");
            String value = row.get("value");
            WebElement fieldElement = common.getDriver().findElement(By.cssSelector("#" + field + ", [name='" + field + "']"));
            fieldElement.clear();
            fieldElement.sendKeys(value);
        }
    }

    @Then("the employee should be created successfully")
    public void theEmployeeShouldBeCreatedSuccessfully() {
        common.waitForElement(By.xpath("//*[contains(text(),'created') or contains(text(),'success')]"));
    }

    @Then("I should see {string} in the employee list")
    public void iShouldSeeInTheEmployeeList(String employeeId) {
        new WebDriverWait(common.getDriver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'" + employeeId + "')]")));
    }
}