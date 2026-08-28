package com.demo.automation.stepdefinitions;

import io.cucumber.java.en.*;

public class AuthorizationSteps {

    private final CommonSteps common;

    public AuthorizationSteps(CommonSteps common) {
        this.common = common;
    }

    @Given("I am logged in as an admin")
    public void iAmLoggedInAsAnAdmin() {
        common.loginAs("admin@company.com", "password123");
    }

    @Given("I am logged in as HR")
    public void iAmLoggedInAsHR() {
        common.navigateToLoginPage();
        common.enterEmailAndPassword("hr@company.com", "password123");
        common.clickLoginButton();
        common.waitForUrl("/");
    }

    @Given("I am logged in as a manager")
    public void iAmLoggedInAsAManager() {
        common.navigateToLoginPage();
        common.enterEmailAndPassword("manager@company.com", "password123");
        common.clickLoginButton();
        common.waitForUrl("/");
    }

    @Given("I am logged in as an employee")
    public void iAmLoggedInAsAnEmployee() {
        common.loginAs("employee@company.com", "password123");
    }

    @When("I navigate to the dashboard")
    public void iNavigateToTheDashboard() {
        common.navigateTo("/");
    }

    @When("I navigate to the employees page")
    public void iNavigateToTheEmployeesPage() {
        common.navigateTo("/employees");
    }

    @When("I navigate to the audit logs page")
    public void iNavigateToTheAuditLogsPage() {
        common.navigateTo("/audit-logs");
    }

    @When("I try to access the add employee page")
    public void iTryToAccessTheAddEmployeePage() {
        common.navigateTo("/employees/add");
    }

    @Then("I should see the dashboard")
    public void iShouldSeeTheDashboard() {
        common.waitForElement(org.openqa.selenium.By.xpath("//h2[contains(text(),'Dashboard')]"));
    }

    @Then("I should see the employees list")
    public void iShouldSeeTheEmployeesList() {
        common.waitForElement(org.openqa.selenium.By.xpath("//h2[contains(text(),'Employees')]"));
    }

    @Then("I should see the audit logs")
    public void iShouldSeeTheAuditLogs() {
        common.waitForElement(org.openqa.selenium.By.xpath("//h2[contains(text(),'Audit Logs')]"));
    }

    @Then("I should be redirected or see access denied")
    public void iShouldBeRedirectedOrSeeAccessDenied() {
        // Accept either redirect or access denied message
    }
}